package dev.qther.ars_controle.cc;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.ACNames;
import dev.qther.ars_controle.registry.ACRegistry;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ACPeripherals {
    public static void register(RegisterCapabilitiesEvent event) {
        ArsControle.LOGGER.info("Registering capabilities for CC compat");
        event.registerBlockEntity(PeripheralCapability.get(), ACRegistry.Tiles.WARPING_SPELL_PRISM.get(), (b, d) -> new WarpingSpellPrismPeripheral(ACNames.WARPING_SPELL_PRISM, b));
        event.registerBlockEntity(PeripheralCapability.get(), ACRegistry.Tiles.SCROLL_HOLDER.get(), (b, d) -> new ScrollHolderPeripheral(ACNames.SCROLL_HOLDER, b));
    }
}
