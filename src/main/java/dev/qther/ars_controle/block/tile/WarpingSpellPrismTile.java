package dev.qther.ars_controle.block.tile;

import com.hollingsworth.arsnouveau.api.item.IWandable;
import com.hollingsworth.arsnouveau.common.block.tile.ModdedTile;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import dev.qther.ars_controle.util.Cached;
import dev.qther.ars_controle.config.ServerConfig;
import dev.qther.ars_controle.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class WarpingSpellPrismTile extends ModdedTile implements IWandable {
    public static final UUID ZERO_UUID = new UUID(0, 0);

    public WarpingSpellPrismTile(BlockPos pos, BlockState state) {
        super(ModRegistry.WARPING_SPELL_PRISM_TILE.get(), pos, state);
    }

    public @Nullable HitResult getHitResult() {
        if (level == null || level.isClientSide) {
            return null;
        }

        var blockPos = this.getBlock();
        if (blockPos != null) {
            var pos = blockPos.getCenter();
            return new BlockHitResult(pos, Direction.DOWN, blockPos, true);
        }

        var tag = this.getPersistentData();

        Entity entity = null;
        if (tag.hasUUID("entity")) {
            var uuid = tag.getUUID("entity");
            entity = Cached.getEntityByUUID(level.getServer().getAllLevels(), uuid);
        }

        if (entity == null) {
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
        if (level == null || level.isClientSide) {
            return null;
        }

        var tag = this.getPersistentData();
        if (this.getBlock() != null) {
            var s = tag.contains("dimension", 8) ? tag.getString("dimension") : null;
            if (s == null || level == null) {
                return null;
            }
            return Cached.getLevelByName(level.getServer().getAllLevels(), s);
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
        if (uuid == null) {
            tag.remove("entity");
        } else {
            tag.putUUID("entity", uuid);
        }
        tag.remove("block");
        this.setChanged();
    }

    public UUID getEntityUUID() {
        var tag = this.getPersistentData();
        return tag.contains("entity") ? tag.getUUID("entity") : ZERO_UUID;
    }

    public @Nullable Entity getEntity() {
        var uuid = getEntityUUID();
        return uuid == ZERO_UUID ? null : Cached.getEntityByUUID(level.getServer().getAllLevels(), uuid);
    }

    public int getSourceRequired(HitResult hitResult) {
        if (hitResult == null) {
            return 0;
        }

        double distSqr = 0;
        var dimCost = 0;
        if (hitResult instanceof BlockHitResult b) {
            distSqr = b.getBlockPos().getCenter().distanceToSqr(this.getBlockPos().getCenter());
            if (this.getTargetLevel() != level) {
                dimCost = ServerConfig.SERVER.WARPING_SPELL_PRISM_COST_DIMENSION.get();
            }
        } else if (hitResult instanceof EntityHitResult e) {
            distSqr = e.getLocation().distanceToSqr(this.getBlockPos().getCenter());
            if (this.getTargetLevel() != level) {
                dimCost = ServerConfig.SERVER.WARPING_SPELL_PRISM_COST_DIMENSION.get();
            }
        }

        var costMinDistance = ServerConfig.SERVER.WARPING_SPELL_PRISM_COST_MIN_DISTANCE.get();
        var costMinDistanceSqr = costMinDistance * costMinDistance;
        var costPerBlock = ServerConfig.SERVER.WARPING_SPELL_PRISM_COST_PER_BLOCK.get();

        if (distSqr > costMinDistanceSqr) {
            int maxCost = ServerConfig.SERVER.WARPING_SPELL_PRISM_MAX_SOURCE_COST.get();
            if (maxCost < 0) {
                maxCost = Integer.MAX_VALUE;
            }
            return Math.max(0, (int) Math.min(maxCost, dimCost + Math.sqrt(distSqr - costMinDistanceSqr) * costPerBlock));
        }

        return Math.max(0, dimCost);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);

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

    @Override
    public void onFinishedConnectionLast(@Nullable GlobalPos storedPos, @Nullable Direction face, @Nullable LivingEntity storedEntity, Player player) {
        if (storedPos != null) {
            this.setBlock(storedPos.dimension(), storedPos.pos());
            this.setChanged();
            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.block", storedPos.pos().toShortString(), storedPos.dimension().location().toString()));
            return;
        }

        if (storedEntity != null) {
            this.setEntityUUID(storedEntity.getUUID());
            this.setChanged();
            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.entity", storedEntity.getDisplayName(), storedEntity.level().dimension().location().toString()));
        }
    }

    @Override
    public void onFinishedConnectionLast(@Nullable BlockPos storedPos, @Nullable Direction face, @Nullable LivingEntity storedEntity, Player player) {
        if (storedPos != null) {
            var dim = player.level().dimension();
            this.setBlock(dim, storedPos);
            this.setChanged();
            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.block", storedPos.toShortString(), dim.location().toString()));
            return;
        }

        if (storedEntity != null) {
            this.setEntityUUID(storedEntity.getUUID());
            this.setChanged();
            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.entity", storedEntity.getDisplayName(), storedEntity.level().dimension().location().toString()));
        }
    }
}
