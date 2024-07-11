package dev.qther.ars_controle.tile;

import com.hollingsworth.arsnouveau.common.block.tile.ModdedTile;
import dev.qther.ars_controle.mixin.LevelGetEntitiesAccessor;
import dev.qther.ars_controle.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.UUID;

public class WarpingSpellPrismTile extends ModdedTile {
    public static final UUID ZERO_UUID = new UUID(0, 0);

    public WarpingSpellPrismTile(BlockPos pos, BlockState state) {
        super(ModRegistry.WARPING_SPELL_PRISM_TILE.get(), pos, state);
    }

    public @Nullable HitResult getHitResult() {
        var blockPos = this.getBlock();
        if (blockPos != null) {
            var pos = blockPos.getCenter();
            return new BlockHitResult(pos, Direction.DOWN, blockPos, true);
        }

        var tag = this.getPersistentData();

        Entity entity = null;
        if (tag.hasUUID("entity")) {
            var uuid = tag.getUUID("entity");
            var entities = ((LevelGetEntitiesAccessor) level).getEntities();
            entity = entities.get(uuid);
        }

        if (entity == null || !entity.isAlive()) {
            return null;
        }

        return new EntityHitResult(entity, entity.getEyePosition());
    }

    public void setBlock(@Nullable ResourceKey<Level> level, @Nullable BlockPos block) {
        var tag = this.getPersistentData();
        if (level == null) {
            tag.remove("dimension");
        }
        if (block == null) {
            tag.remove("block");
        }
        if (level == null || block == null) {
            this.setChanged();
            return;
        }
        tag.putString("dimension", level.location().toString());
        tag.putLong("block", block.asLong());
        tag.remove("entity");
        this.setChanged();
    }

    public @Nullable ServerLevel getTargetLevel() {
        var tag = this.getPersistentData();
        if (this.getBlock() != null) {
            var s = tag.contains("dimension", 8) ? tag.getString("dimension") : null;
            if (s == null || level == null) {
                return null;
            }
            for (var l : level.getServer().getAllLevels()) {
                if (s.equals(l.dimension().location().toString())) {
                    return l;
                }
            }
        }

        var entity = this.getEntity();
        if (entity != null) {
            return (ServerLevel) entity.level();
        }

        return null;
    }

    public @Nullable BlockPos getBlock() {
        var tag = this.getPersistentData();
        return tag.contains("block", 99) ? BlockPos.of(tag.getLong("block")) : null;
    }

    public void setEntityUUID(UUID uuid) {
        var tag = this.getPersistentData();
        tag.putUUID("entity", uuid);
        tag.remove("block");
        this.setChanged();
    }

    public UUID getEntityUUID() {
        var tag = this.getPersistentData();
        return tag.contains("entity", 99) ? tag.getUUID("entity") : ZERO_UUID;
    }

    public @Nullable Entity getEntity() {
        var uuid = getEntityUUID();
        if (uuid != ZERO_UUID) {
            for (var l : level.getServer().getAllLevels()) {
                var entity = l.getEntities().get(uuid);
                if (entity != null) {
                    return entity;
                }
            }
        }
        return null;
    }

    public int getSourceRequired(HitResult hitResult) {
        double distSqr;
        if (hitResult instanceof BlockHitResult b) {
            distSqr = b.getBlockPos().getCenter().distanceToSqr(this.getBlockPos().getCenter());
        } else if (hitResult instanceof EntityHitResult e) {
            distSqr = e.getLocation().distanceToSqr(this.getBlockPos().getCenter());
        } else {
            distSqr = 0;
        }

        return distSqr > 4096 ? (int) (Math.sqrt(distSqr - 4096) * 2) : 0;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        var uuid = this.getEntityUUID();
        if (uuid != ZERO_UUID) {
            tag.putUUID("entity", uuid);
            return;
        }

        var pos = this.getBlock();
        if (pos != null) {
            tag.putLong("block", pos.asLong());
        }
    }
}
