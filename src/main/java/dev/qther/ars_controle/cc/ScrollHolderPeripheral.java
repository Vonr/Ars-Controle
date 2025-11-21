package dev.qther.ars_controle.cc;

import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.qther.ars_controle.block.tile.ScrollHolderTile;
import dev.qther.ars_controle.util.XYMap;
import dev.qther.ars_controle.util.XYZMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import javax.annotation.Nullable;
import java.util.Map;

public class ScrollHolderPeripheral implements IPeripheral {
    private final String type;
    private final ScrollHolderTile owner;

    public ScrollHolderPeripheral(String type, ScrollHolderTile owner) {
        this.type = type;
        this.owner = owner;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        if (other instanceof ScrollHolderPeripheral peri) {
            return this.owner == peri.owner;
        }

        return false;
    }

    @Nullable
    @LuaFunction(mainThread = true)
    public final Map<String, Object> target() {
        var map = new Object2ObjectArrayMap<String, Object>(4);
        var scroll = this.owner.getItem(0);
        var data = scroll.get(DataComponentRegistry.WARP_SCROLL);
        if (data == null || !data.isValid()) {
            return null;
        }

        map.put("stable", data.crossDim());
        map.put("dimension", data.dimension());
        map.put("pos", XYZMap.of(data.pos().get()));
        map.put("rotation", XYMap.of(data.rotation()));

        return map;
    }

    public Object getTarget() {
        return this.owner;
    }
}
