package io.github.firstwemine1.cleave.config.spec;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import org.jetbrains.annotations.Nullable;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads config specs from the world's serverconfig directory.
 *
 * Replaces Forge's ModConfig.Type.SERVER handling : Forge binds those configs to
 * &lt;world&gt;/serverconfig automatically, Fabric has no such concept, so the binding happens here
 * on server start instead. Specs registered before a server exists are bound as soon as one does,
 * which matters in single player, where the mod initializes long before a world is loaded.
 */
public final class ConfigManager {
    private static final String SERVER_CONFIG_DIR = "serverconfig";

    private record Entry(ConfigSpec spec, boolean perWorld) {}

    private static final Map<String, Entry> specs = new LinkedHashMap<>();
    private static @Nullable MinecraftServer server;

    private ConfigManager() {}

    /** Registers a spec that lives for the whole game session, rebound to every world that gets loaded. */
    public static void register(ConfigSpec spec, String relativePath) {
        register(spec, relativePath, false);
    }

    /** Registers a spec that only makes sense for the currently loaded world, such as a player's config. */
    public static void registerForWorld(ConfigSpec spec, String relativePath) {
        register(spec, relativePath, true);
    }

    public static void onServerStarting(MinecraftServer startingServer) {
        server = startingServer;

        specs.forEach((relativePath, entry) -> entry.spec().bind(configPath(startingServer, relativePath)));
    }

    public static void onServerStopped() {
        specs.values().forEach(entry -> entry.spec().unbind());
        specs.entrySet().removeIf(entry -> entry.getValue().perWorld());
        server = null;
    }

    private static void register(ConfigSpec spec, String relativePath, boolean perWorld) {
        specs.put(relativePath, new Entry(spec, perWorld));

        if (server != null) {
            spec.bind(configPath(server, relativePath));
        }
    }

    private static Path configPath(MinecraftServer server, String relativePath) {
        return server.getWorldPath(LevelResource.ROOT).resolve(SERVER_CONFIG_DIR).resolve(relativePath).normalize();
    }
}
