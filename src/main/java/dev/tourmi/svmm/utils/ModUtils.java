package dev.tourmi.svmm.utils;

import dev.tourmi.svmm.config.spec.ConfigManager;
import dev.tourmi.svmm.config.spec.ConfigSpec;

/**
 * Extract mod-related functions, to keep the loader-specific plumbing in a single place.
 */
public final class ModUtils {
    private ModUtils() {}

    public static void RegisterConfig(ConfigSpec spec, String path) {
        ConfigManager.register(spec, path);
    }

    public static void RegisterPlayerConfig(ConfigSpec spec, String path) {
        ConfigManager.registerForWorld(spec, path);
    }
}
