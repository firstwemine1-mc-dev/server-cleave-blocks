package io.github.firstwemine1.cleave.utils;

import io.github.firstwemine1.cleave.Cleave;
import io.github.firstwemine1.cleave.config.CleaveConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Extract mod-related functions, to isolate Forge event-bus/config API differences between versions.
 */
public final class ModUtils {
    public static void RegisterConfig(ForgeConfigSpec spec, String path) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, spec, path);
    }

    public static void RegisterMod(Cleave mod) {
        RegisterConfig(CleaveConfig.SPEC, "cleave-config.toml");
        MinecraftForge.EVENT_BUS.register(mod);
    }
}
