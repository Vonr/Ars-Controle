package dev.qther.ars_controle;

import dev.qther.ars_controle.cc.ArsControleCCCompat;
import dev.qther.ars_controle.config.ClientConfig;
import dev.qther.ars_controle.config.ConfigHelper;
import dev.qther.ars_controle.config.ServerConfig;
import dev.qther.ars_controle.datagen.Setup;
import dev.qther.ars_controle.packets.PacketClearRemote;
import dev.qther.ars_controle.registry.ArsNouveauRegistry;
import dev.qther.ars_controle.registry.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        bus.addListener(this::onRegisterCapabilities);

        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        container.registerExtensionPoint(IConfigScreenFactory.class, new ConfigHelper.ConfigScreenFactory());

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

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Ars Controle says hello!");
    }

    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        if (FMLLoader.getLoadingModList().getMods().stream().anyMatch(m -> m.getModId().equals("computercraft"))) {
            ArsControleCCCompat.register(event);
        }
    }
}
