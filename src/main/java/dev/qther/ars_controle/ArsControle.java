package dev.qther.ars_controle;

import dev.qther.ars_controle.datagen.Setup;
import dev.qther.ars_controle.packets.PacketClearRemote;
import dev.qther.ars_controle.registry.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ArsControle.MODID)
public class ArsControle {
    public static final String MODID = "ars_controle";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public ArsControle(IEventBus bus, ModContainer container) {
        ModRegistry.registerRegistries(bus);
        ArsNouveauRegistry.registerGlyphs();
        bus.addListener(this::setup);
        bus.addListener(Setup::gatherData);
        bus.addListener(ModNetworking::register);
        bus.addListener(this::doClientStuff);

        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        NeoForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ArsNouveauRegistry.registerSounds();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    public void onClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        var stack = event.getItemStack();
        if (stack.getItem() == ModRegistry.REMOTE.get()) {
            ModNetworking.sendToServer(new PacketClearRemote());
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Ars Controle says hello!");
    }
}
