package dev.qther.ars_controle.cc;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.qther.ars_controle.Cached;
import dev.qther.ars_controle.block.tile.WarpingSpellPrismTile;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

public class WarpingSpellPrismPeripheral implements IPeripheral {
    private final String type;
    private final WarpingSpellPrismTile owner;

    public WarpingSpellPrismPeripheral(String type, WarpingSpellPrismTile owner) {
        this.type = type;
        this.owner = owner;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        if (other instanceof WarpingSpellPrismPeripheral peri) {
            return this.owner == peri.owner;
        }

        return false;
    }

    public final void setTarget(BlockPos pos) {
        this.owner.setBlock(this.owner.getLevel().dimension(), pos);
    }

    public final void setTarget(String level, BlockPos pos) throws LuaException {
        var l = Cached.getLevelByName(this.owner.getLevel().getServer().getAllLevels(), level);
        if (l == null) {
            throw new LuaException("Invalid level: " + level);
        }
        this.owner.setBlock(l.dimension(), pos);
    }

    @LuaFunction(mainThread = true)
    public final void setTargetWithLevel(String level, int x, int y, int z) throws LuaException {
        this.setTarget(level, new BlockPos(x, y, z));
    }

    @LuaFunction(mainThread = true)
    public final void setTarget(int x, int y, int z) {
        this.setTarget(new BlockPos(x, y, z));
    }

    @Nullable
    @LuaFunction(mainThread = true)
    public final Map<String, Object> target() {
        var hit = this.owner.getHitResult();
        if (hit == null) {
            return null;
        }
        var map = new Object2ObjectArrayMap<String, Object>(3);

        map.put("type", hit.getType().toString().toLowerCase(Locale.ENGLISH));
        var loc = hit.getLocation();
        map.put("location", ArsControleCCCompat.vecToMap(loc));

        if (hit instanceof BlockHitResult bhr) {
            var bp = bhr.getBlockPos();
            var inner = new Object2ObjectArrayMap<String, Object>(2);
            inner.put("pos", ArsControleCCCompat.blockPosToMap(bp));
            inner.put("level", this.owner.getTargetLevel().dimension().location().toString());
            map.put("block", inner);
        } else if (hit instanceof EntityHitResult ehr) {
            var e = ehr.getEntity();
            var inner = new Object2ObjectArrayMap<String, Object>(11);
            inner.put("type", BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString());
            inner.put("name", e.getName().getString());
            inner.put("pos", ArsControleCCCompat.vecToMap(e.getPosition(1.0f)));
            inner.put("eyePos", ArsControleCCCompat.vecToMap(e.getEyePosition()));
            inner.put("level", e.level().dimension().location().toString());
            inner.put("yaw", Mth.wrapDegrees(e.getYRot()));
            inner.put("pitch", Mth.wrapDegrees(e.getXRot()));
            if (e instanceof LivingEntity le) {
                inner.put("health", le.getHealth());
                inner.put("absorption", le.getAbsorptionAmount());

                var effectList = le.getActiveEffects();
                var effectMap = new Object2IntArrayMap<String>(effectList.size());
                for (var effect : effectList) {
                    effectMap.put(effect.getDescriptionId(), effect.getAmplifier());
                }
                inner.put("effects", effectMap);
            }
            map.put("entity", inner);
        }

        return map;
    }

    @LuaFunction(mainThread = true)
    public final int sourceNeeded() {
        return this.owner.getSourceRequired(this.owner.getHitResult());
    }

    public Object getTarget() {
        return this.owner;
    }
}
