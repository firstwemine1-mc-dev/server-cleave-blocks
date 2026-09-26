package io.github.firstwemine1.cleave;

import com.mojang.logging.LogUtils;
import io.github.firstwemine1.cleave.commands.Commands;
import io.github.firstwemine1.cleave.config.ClientConfigs;
import io.github.firstwemine1.cleave.server.ClientStatus;
import io.github.firstwemine1.cleave.server.ItemTeleporter;
import io.github.firstwemine1.cleave.server.Tunneler;
import io.github.firstwemine1.cleave.server.VeinMiner;
import io.github.firstwemine1.cleave.utils.ModUtils;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(Cleave.MOD_ID)
public final class Cleave
{
    public static final String MOD_ID = "cleave";

    private static final Logger LOGGER = LogUtils.getLogger();

    private final VeinMiner veinMiner = new VeinMiner();

    public Cleave()
    {
        ModUtils.RegisterMod(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Cleave successfully started");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        Commands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onBlockBroken(BlockEvent.BreakEvent event) {
        // On Forge 47 an event is cancelled through setCanceled(), not by returning true.
        if (veinMiner.onBlockBroken(event)) {
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
    public void onItemSpawn(EntityEvent.EntityConstructing event) {
        ItemTeleporter.checkAndTeleport(event.getEntity());
    }
}
