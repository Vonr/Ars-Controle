package dev.qther.ars_controle.cc;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.block.tile.WarpingSpellPrismTile;
import dev.qther.ars_controle.registry.ModRegistry;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ArsControleCCCompat {
    public static void register(RegisterCapabilitiesEvent event) {
        ArsControle.LOGGER.info("Registering capabilities for CC compat");
        event.registerBlockEntity(PeripheralCapability.get(), (BlockEntityType) ModRegistry.WARPING_SPELL_PRISM_TILE.get(), (b, d) -> new WarpingSpellPrismPeripheral("warping_spell_prism", (WarpingSpellPrismTile) b));
    }

    public static Object2IntArrayMap<String> blockPosToMap(BlockPos pos) {
        var map = new Object2IntArrayMap<String>();
        map.put("x", pos.getX());
        map.put("y", pos.getY());
        map.put("z", pos.getZ());
        return map;
    }

    public static Object2DoubleArrayMap<String> vecToMap(Vec3 vec) {
        var map = new Object2DoubleArrayMap<String>();
        map.put("x", vec.x);
        map.put("y", vec.y);
        map.put("z", vec.z);
        return map;
    }
}
