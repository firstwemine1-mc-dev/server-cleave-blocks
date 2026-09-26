package io.github.firstwemine1.cleave.config.spec;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Minimal stand-in for Forge's ForgeConfigSpec, backed by NightConfig.
 *
 * Fabric has no built-in config system, so this reimplements the subset of the Forge API this mod
 * actually uses : a commented, sectioned TOML file with typed values that may be read, written and
 * saved at runtime. The on-disk format matches the Forge version's, so existing config files carry
 * over untouched.
 */
public final class ConfigSpec {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<ConfigValue<?>> values;
    private @Nullable CommentedFileConfig file;
    private int revision;

    private ConfigSpec(List<ConfigValue<?>> values) {
        this.values = List.copyOf(values);
        for (ConfigValue<?> value : this.values) {
            value.spec = this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Attaches the spec to a file, creating it with defaults and comments when it doesn't exist. */
    public void bind(Path path) {
        try {
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);

            CommentedFileConfig config = CommentedFileConfig.builder(path)
                    .preserveInsertionOrder()
                    .sync()
                    .build();
            config.load();

            this.file = config;
            this.revision++;

            for (ConfigValue<?> value : values) {
                value.correct();
            }

            config.save();
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not load config file {}, falling back to the default values", path, e);
            this.file = null;
            this.revision++;
        }
    }

    public void unbind() {
        if (file != null) {
            file.close();
            file = null;
        }
        revision++;
    }

    public boolean isBound() {
        return file != null;
    }

    public void save() {
        if (file != null) file.save();
    }

    public static final class Builder {
        private final List<String> stack = new ArrayList<>();
        private final List<ConfigValue<?>> values = new ArrayList<>();
        private @Nullable String comment;

        public Builder push(String key) {
            stack.add(key);
            return this;
        }

        public Builder pop() {
            stack.remove(stack.size() - 1);
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public BooleanValue define(String key, boolean defaultValue) {
            return define(key, () -> defaultValue);
        }

        public BooleanValue define(String key, Supplier<Boolean> defaultValue) {
            return add(new BooleanValue(path(key), defaultValue, takeComment()));
        }

        public IntValue defineInRange(String key, int defaultValue, int min, int max) {
            return add(new IntValue(path(key), () -> defaultValue, takeComment(), min, max));
        }

        public ListValue defineList(String key, List<? extends String> defaultValue) {
            List<String> copy = List.copyOf(defaultValue);
            return add(new ListValue(path(key), () -> copy, takeComment()));
        }

        public <T extends Enum<T>> EnumValue<T> defineEnum(String key, Class<T> clazz, T defaultValue) {
            return add(new EnumValue<>(path(key), () -> defaultValue, takeComment(), clazz));
        }

        public <T extends Enum<T>> EnumValue<T> defineEnum(String key, Class<T> clazz, Supplier<T> defaultValue) {
            return add(new EnumValue<>(path(key), defaultValue, takeComment(), clazz));
        }

        public ConfigSpec build() {
            if (!stack.isEmpty()) {
                throw new IllegalStateException("Unbalanced push/pop while building a config spec : " + stack);
            }
            return new ConfigSpec(values);
        }

        private <T extends ConfigValue<?>> T add(T value) {
            values.add(value);
            return value;
        }

        private List<String> path(String key) {
            List<String> path = new ArrayList<>(stack);
            path.add(key);
            return List.copyOf(path);
        }

        private @Nullable String takeComment() {
            String taken = comment;
            comment = null;
            return taken;
        }
    }

    /** A single typed entry. Mirrors ForgeConfigSpec.ConfigValue. */
    public static class ConfigValue<T> implements Supplier<T> {
        private final List<String> path;
        private final Supplier<T> defaultValue;
        private final @Nullable String comment;

        private ConfigSpec spec;
        private @Nullable T cached;
        private int cachedRevision = -1;

        ConfigValue(List<String> path, Supplier<T> defaultValue, @Nullable String comment) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.comment = comment;
        }

        @Override
        public T get() {
            if (cached != null && cachedRevision == spec.revision) return cached;

            T value = read();
            cached = value;
            cachedRevision = spec.revision;
            return value;
        }

        public void set(T value) {
            if (spec.file != null) {
                spec.file.set(path, toRaw(value));
                spec.revision++;
            }
            cached = value;
            cachedRevision = spec.revision;
        }

        /** Writes the whole spec back to disk. Named for parity with the Forge API. */
        public void save() {
            spec.save();
        }

        public T getDefault() {
            return defaultValue.get();
        }

        private T read() {
            if (spec.file == null) return defaultValue.get();

            Object raw = spec.file.get(path);
            T value = raw == null ? null : fromRaw(raw);
            return value == null ? defaultValue.get() : value;
        }

        void correct() {
            if (spec.file == null) return;

            Object raw = spec.file.get(path);
            if (raw == null || fromRaw(raw) == null) {
                spec.file.set(path, toRaw(defaultValue.get()));
            }
            if (comment != null) {
                spec.file.setComment(path, " " + comment.replace("\n", "\n "));
            }
            spec.revision++;
        }

        /** @return the converted value, or null when the stored value is unusable, in which case the default is used. */
        @SuppressWarnings("unchecked")
        protected @Nullable T fromRaw(Object raw) {
            return (T) raw;
        }

        protected Object toRaw(T value) {
            return value;
        }
    }

    public static final class BooleanValue extends ConfigValue<Boolean> {
        BooleanValue(List<String> path, Supplier<Boolean> defaultValue, @Nullable String comment) {
            super(path, defaultValue, comment);
        }

        @Override
        protected @Nullable Boolean fromRaw(Object raw) {
            return raw instanceof Boolean bool ? bool : null;
        }
    }

    public static final class IntValue extends ConfigValue<Integer> {
        private final int min;
        private final int max;

        IntValue(List<String> path, Supplier<Integer> defaultValue, @Nullable String comment, int min, int max) {
            super(path, defaultValue, comment);
            this.min = min;
            this.max = max;
        }

        @Override
        protected @Nullable Integer fromRaw(Object raw) {
            if (!(raw instanceof Number number)) return null;

            int value = number.intValue();
            return value >= min && value <= max ? value : null;
        }
    }

    public static final class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
        private final Class<T> clazz;

        EnumValue(List<String> path, Supplier<T> defaultValue, @Nullable String comment, Class<T> clazz) {
            super(path, defaultValue, comment);
            this.clazz = clazz;
        }

        @Override
        protected @Nullable T fromRaw(Object raw) {
            if (clazz.isInstance(raw)) return clazz.cast(raw);
            if (!(raw instanceof String name)) return null;

            for (T value : clazz.getEnumConstants()) {
                if (value.name().equalsIgnoreCase(name)) return value;
            }
            return null;
        }

        @Override
        protected Object toRaw(T value) {
            return value.name();
        }
    }

    public static final class ListValue extends ConfigValue<List<? extends String>> {
        ListValue(List<String> path, Supplier<List<? extends String>> defaultValue, @Nullable String comment) {
            super(path, defaultValue, comment);
        }

        @Override
        protected @Nullable List<? extends String> fromRaw(Object raw) {
            if (!(raw instanceof List<?> list)) return null;

            List<String> values = new ArrayList<>(list.size());
            for (Object element : list) {
                if (!(element instanceof String string)) return null;
                values.add(string);
            }
            return List.copyOf(values);
        }

        @Override
        protected Object toRaw(List<? extends String> value) {
            return new ArrayList<String>(value);
        }
    }
}
