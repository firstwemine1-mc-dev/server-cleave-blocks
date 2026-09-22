package dev.tourmi.svmm;

import com.mojang.logging.LogUtils;
import dev.tourmi.svmm.commands.Commands;
import dev.tourmi.svmm.config.ClientConfigs;
import dev.tourmi.svmm.config.SVMMConfig;
import dev.tourmi.svmm.config.spec.ConfigManager;
import dev.tourmi.svmm.server.ClientStatus;
import dev.tourmi.svmm.server.ItemTeleporter;
import dev.tourmi.svmm.server.Tunneler;
import dev.tourmi.svmm.server.VeinMiner;
import dev.tourmi.svmm.utils.ModUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import org.jetbrains.annotations.Nullable;

public final class SVMM implements ModInitializer
{
    public static final String MOD_ID = "svmm";

    private static final Logger LOGGER = LogUtils.getLogger();

    private final VeinMiner veinMiner = new VeinMiner();

    @Override
    public void onInitialize()
    {
        ModUtils.RegisterConfig(SVMMConfig.SPEC, "svmm-config.toml");

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ClientConfigs.clear();
            ConfigManager.onServerStopped();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.register(dispatcher));

        PlayerBlockBreakEvents.BEFORE.register(this::onBlockBroken);
        AttackBlockCallback.EVENT.register(this::onPlayerMine);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLogin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerLogout(handler.player));

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> ItemTeleporter.checkAndTeleport(entity));
    }

    private void onServerStarting(MinecraftServer server) {
        ConfigManager.onServerStarting(server);

        LOGGER.info("Server-side Vein Mining successfully started");
    }

    /**
     * @return false to cancel the vanilla block break, which is what happens whenever the mod
     *         handles the block itself.
     */
    private boolean onBlockBroken(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        return !veinMiner.onBlockBroken(level, player, pos, state);
    }

    private InteractionResult onPlayerMine(Player player, Level level, InteractionHand hand, BlockPos pos, Direction face) {
        if (level.isClientSide()) return InteractionResult.PASS;

        Tunneler.updateFaceMined(player.getUUID(), face);

        return InteractionResult.PASS;
    }

    private void onPlayerLogin(Player player) {
        // Ensures a player's config is created as they join.
        ClientConfigs.getClientConfig(player);
    }

    private void onPlayerLogout(Player player) {
        ClientStatus.getClientStatus(player.getUUID()).reset();
    }
}
