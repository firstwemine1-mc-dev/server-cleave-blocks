package io.github.firstwemine1.cleave.config;

import io.github.firstwemine1.cleave.utils.ModUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public final class ClientConfigs {
    private static final LevelResource SERVERCONFIG = new LevelResource("serverconfig");

    private final static HashMap<UUID, ClientConfig> clientConfigs = new HashMap<>();

    public static ClientConfig getClientConfig(Entity player) {
        if (!clientConfigs.containsKey(player.getUUID())) {
            loadOrCreateClientConfig(player);
        }

        return clientConfigs.get(player.getUUID());
    }

    public static @Nullable ClientConfig getClientConfig(UUID uuid) {
        return clientConfigs.getOrDefault(uuid, null);
    }

    private static void loadOrCreateClientConfig(Entity player) {
        UUID playerUUID = player.getUUID();
        ClientConfig cfg = new ClientConfig(playerUUID);
        String configPath = "cleave-player-configs/" + playerUUID + ".toml";
        ModUtils.RegisterConfig(cfg.SPEC, configPath);

        // The only public way to bring a newly registered config into memory. Mirrors what
        // NeoForge's own ServerLifecycleHooks#handleServerAboutToStart does: the config directory is
        // the source, the world's serverconfig folder is the per-world override.
        MinecraftServer server = player.level().getServer();
        Path worldOverrides = server.getWorldPath(SERVERCONFIG);
        ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.SERVER, FMLPaths.CONFIGDIR.get(), worldOverrides);

        clientConfigs.put(playerUUID, cfg);
    }
}
