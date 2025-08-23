package dev.qther.ars_controle;

import dev.qther.ars_controle.cc.ACPeripherals;
import dev.qther.ars_controle.config.ACServerConfig;
import dev.qther.ars_controle.config.ACStartupConfig;
import dev.qther.ars_controle.datagen.ACSetup;
import dev.qther.ars_controle.item.PortableBrazierRelayItem;
import dev.qther.ars_controle.packets.ACNetworking;
import dev.qther.ars_controle.registry.ACRegistry;
import dev.qther.ars_controle.util.Cached;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ArsControle.MODID)
public class ArsControle {
    public static final String MODID = "ars_controle";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public ArsControle(IEventBus bus, ModContainer container) {
        ACRegistry.register(bus);

        bus.addListener(ACSetup::gatherData);
        bus.addListener(ACNetworking::register);
        bus.addListener(this::onRegisterCapabilities);
        bus.addListener(ACStartupConfig::onLoad);

        NeoForge.EVENT_BUS.addListener(ArsControle::onServerStopped);

        container.registerConfig(ModConfig.Type.STARTUP, ACStartupConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ACServerConfig.SPEC);
    }

    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ACRegistry.Tiles.SCROLL_HOLDER.get(), (tile, context) -> tile.getItemHandler());

        if (ModList.get().isLoaded("computercraft")) {
            ACPeripherals.register(event);
        }

        nextCap: for (var cap : BlockCapability.getAll()) {
            for (var c : ACStartupConfig.STARTUP.SCRYERS_LINKAGE_BLACKLISTED_CLASSES) {
                if (cap.typeClass().isAssignableFrom(c)) {
                    continue nextCap;
                }
            }

            try {
                var erased = (BlockCapability<Object, Object>) cap;

                event.registerBlockEntity(erased, ACRegistry.Tiles.SCRYERS_LINKAGE.get(), (linkage, context) -> {
                    var info = linkage.getTargetInfo();
                    if (info == null) {
                        return null;
                    }

                    var level = info.first();
                    var block = info.second();

                    return level.getCapability(erased, block, context);
                });
            } catch (ClassCastException e) {
                LOGGER.error("Could not register capability for linkage", e);
            }
        }
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        Cached.ENTITIES_BY_UUID.invalidateAll();
        PortableBrazierRelayItem.clearCache();
    }
}
