package dev.qther.ars_controle;

import dev.qther.ars_controle.cc.ArsControleCCCompat;
import dev.qther.ars_controle.config.ServerConfig;
import dev.qther.ars_controle.datagen.Setup;
import dev.qther.ars_controle.registry.ArsNouveauRegistry;
import dev.qther.ars_controle.registry.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
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
        bus.addListener(this::onRegisterCapabilities);

        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ArsNouveauRegistry.registerSounds();
    }

    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        if (FMLLoader.getLoadingModList().getMods().stream().anyMatch(m -> m.getModId().equals("computercraft"))) {
            ArsControleCCCompat.register(event);
        }

        for (var erasedCap : BlockCapability.getAll()) {
            try {
                var cap = (BlockCapability<Object, Object>) erasedCap;

                event.registerBlockEntity(cap, ModRegistry.SCRYERS_LINKAGE_TILE.get(), (linkage, context) -> {
                    var info = linkage.getTargetInfo();
                    if (info == null) {
                        return null;
                    }

                    var level = info.first();
                    var block = info.second();

                    return level.getCapability(cap, block, context);
                });
            } catch (ClassCastException e) {
                LOGGER.error("Could not register capability for linkage", e);
            }
        }
    }
}
