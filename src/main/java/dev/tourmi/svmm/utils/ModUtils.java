package dev.tourmi.svmm.utils;

import dev.tourmi.svmm.SVMM;
import dev.tourmi.svmm.config.SVMMConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Extract mod-related functions, to keep the loader-specific plumbing in a single place.
 */
public final class ModUtils {
    private ModUtils() {}

    private static ModContainer container;

    /**
     * The mod's own {@link ModContainer}. It is kept around because the per-player configs are
     * registered lazily, as players log in, rather than during mod construction: NeoForge's
     * {@code ModContainer#registerConfig} has no mod-loading-state guard, so this still works at
     * runtime, which is what the Forge branch relied on through {@code ModLoadingContext.get()}.
     */
    public static ModContainer getContainer() {
        if (container == null) {
            throw new IllegalStateException("SVMM's ModContainer was requested before the mod was constructed");
        }
        return container;
    }

    public static void RegisterConfig(ModConfigSpec spec, String path) {
        getContainer().registerConfig(ModConfig.Type.SERVER, spec, path);
    }

    public static void RegisterMod(SVMM mod, ModContainer modContainer) {
        container = modContainer;
        RegisterConfig(SVMMConfig.SPEC, "svmm-config.toml");
        // Every event this mod listens to is a game event, and game events live on the NeoForge bus.
        // The mod bus handed to the @Mod constructor only carries the mod-loading lifecycle, which
        // this mod takes no part in; a listener put there would simply never fire.
        NeoForge.EVENT_BUS.register(mod);
    }
}
