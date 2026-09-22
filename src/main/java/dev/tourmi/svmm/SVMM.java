package dev.tourmi.svmm;

import com.mojang.logging.LogUtils;
import dev.tourmi.svmm.commands.Commands;
import dev.tourmi.svmm.config.ClientConfigs;
import dev.tourmi.svmm.server.ClientStatus;
import dev.tourmi.svmm.server.ItemTeleporter;
import dev.tourmi.svmm.server.Tunneler;
import dev.tourmi.svmm.server.VeinMiner;
import dev.tourmi.svmm.utils.ModUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(SVMM.MOD_ID)
public final class SVMM
{
    public static final String MOD_ID = "svmm";

    private static final Logger LOGGER = LogUtils.getLogger();

    private final VeinMiner veinMiner = new VeinMiner();

    /**
     * NeoForge hands the mod bus and the mod container to the constructor.
     * <p>
     * Everything this mod listens to is a <em>game</em> event, so all listeners go on
     * {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}; the mod bus is only used by
     * NeoForge itself for the mod-loading lifecycle, which this mod does not take part in.
     */
    public SVMM(IEventBus modEventBus, ModContainer modContainer)
    {
        ModUtils.RegisterMod(this, modContainer);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Server-side Vein Mining successfully started");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        Commands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onBlockBroken(BreakBlockEvent event) {
        if (veinMiner.onBlockBroken(event.getLevel(), event.getPlayer(), event.getPos(), event.getState())) {
            // The mod mined (or tunneled) the block itself, so the vanilla break must not happen.
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerMine(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getSide().isClient()) return;

        Tunneler.updateFaceMined(event.getEntity().getUUID(), event.getFace());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // Ensures a player's config is created as they join.
        ClientConfigs.getClientConfig((event.getEntity()));
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ClientStatus.getClientStatus(event.getEntity().getUUID()).reset();
    }

    @SubscribeEvent
    public void onItemSpawn(EntityJoinLevelEvent event) {
        ItemTeleporter.checkAndTeleport(event.getEntity());
    }
}
