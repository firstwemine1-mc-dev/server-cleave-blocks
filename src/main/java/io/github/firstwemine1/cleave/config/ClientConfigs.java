package io.github.firstwemine1.cleave.config;

import io.github.firstwemine1.cleave.utils.ModUtils;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.UUID;

public final class ClientConfigs {
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

    /**
     * Player configs belong to the world they were loaded from, so they are dropped when it unloads.
     */
    public static void clear() {
        clientConfigs.clear();
    }

    private static void loadOrCreateClientConfig(Entity player) {
        UUID playerUUID = player.getUUID();
        ClientConfig cfg = new ClientConfig(playerUUID);
        String configPath = "cleave-player-configs/" + playerUUID + ".toml";
        ModUtils.RegisterPlayerConfig(cfg.SPEC, configPath);
        clientConfigs.put(playerUUID, cfg);
    }
}
