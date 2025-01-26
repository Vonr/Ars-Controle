package dev.qther.ars_controle;

import dev.qther.ars_controle.config.ClientConfig;
import dev.qther.ars_controle.config.ConfigScreenFactory;
import dev.qther.ars_controle.packets.serverbound.PacketClearRemote;
import dev.qther.ars_controle.registry.ModRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(value = ArsControle.MODID, dist = Dist.CLIENT)
public class ArsControleClient {
    public ArsControleClient(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, new ConfigScreenFactory());

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        var stack = event.getItemStack();
        if (stack.getItem() == ModRegistry.REMOTE.get()) {
            ModNetworking.sendToServer(new PacketClearRemote());
        }
    }
}
