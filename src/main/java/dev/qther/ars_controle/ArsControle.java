package dev.qther.ars_controle;

import com.hollingsworth.arsnouveau.setup.config.ANModConfig;
import dev.qther.ars_controle.packets.PacketClearRemote;
import dev.qther.ars_controle.registry.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ArsControle.MODID)
public class ArsControle {
    public static final String MODID = "ars_controle";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public ArsControle() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.registerRegistries(bus);
        ArsNouveauRegistry.registerGlyphs();
        bus.addListener(this::setup);
        bus.addListener(this::doClientStuff);

        ANModConfig serverConfig = new ANModConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, ModLoadingContext.get().getActiveContainer(), MODID + "-server");

        ModLoadingContext.get().getActiveContainer().addConfig(serverConfig);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(MODID, path);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ModNetworking.registerMessages();
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
