package dev.qther.ars_controle.block.tile;

import com.hollingsworth.arsnouveau.api.item.IWandable;
import com.hollingsworth.arsnouveau.client.particle.ColorPos;
import com.hollingsworth.arsnouveau.common.block.tile.ModdedTile;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import dev.qther.ars_controle.config.ACServerConfig;
import dev.qther.ars_controle.registry.ACRegistry;
import dev.qther.ars_controle.util.Cached;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import java.util.List;
import java.util.UUID;

public class WarpingSpellPrismTile extends ModdedTile implements IWandable, IDimensionalHighlighter {
    public WarpingSpellPrismTile(BlockPos pos, BlockState state) {
        super(ACRegistry.Tiles.WARPING_SPELL_PRISM.get(), pos, state);
    }

    public @Nullable HitResult getHitResult() {
        if (level == null || level.isClientSide) {
            return null;
        }

        var blockPos = this.getExistingData(ACRegistry.Attachments.GLOBAL_POS_TARGET).orElse(null);
        if (blockPos != null) {
            var pos = blockPos.pos().getCenter();
            return new BlockHitResult(pos, Direction.DOWN, blockPos.pos(), true);
        }

        UUID uuid = this.getExistingData(ACRegistry.Attachments.ENTITY_TARGET).orElse(null);
        Entity entity = null;
        if (uuid != null) {
            entity = Cached.getEntityByUUID(uuid);
        }

        if (entity == null) {
            return null;
        }

        return new EntityHitResult(entity, entity.getEyePosition());
    }

    public void setBlock(@Nullable ResourceKey<Level> level, @Nullable BlockPos block) {
        if (level == null || block == null) {
            this.removeData(ACRegistry.Attachments.GLOBAL_POS_TARGET);
        } else {
            this.setData(ACRegistry.Attachments.GLOBAL_POS_TARGET, new GlobalPos(level, block));
            this.removeData(ACRegistry.Attachments.ENTITY_TARGET);
        }

        this.setChanged();
    }

    public @Nullable ServerLevel getTargetLevel() {
        if (level == null || level.isClientSide) {
            return null;
        }

        var pos = this.getExistingData(ACRegistry.Attachments.GLOBAL_POS_TARGET).orElse(null);
        if (pos != null) {
            return Cached.getLevelByKey(pos.dimension());
        }

        var entity = this.getEntity();
        if (entity != null) {
            return (ServerLevel) entity.level();
        }

        return null;
    }

    public void setEntityUUID(@Nullable UUID uuid) {
        if (uuid == null) {
            this.removeData(ACRegistry.Attachments.ENTITY_TARGET);
        } else {
            this.setData(ACRegistry.Attachments.ENTITY_TARGET, uuid);
            this.removeData(ACRegistry.Attachments.GLOBAL_POS_TARGET);
        }

        this.setChanged();
    }

    public @Nullable UUID getEntityUUID() {
        return this.getExistingData(ACRegistry.Attachments.ENTITY_TARGET).orElse(null);
    }

    public @Nullable Entity getEntity() {
        var uuid = getEntityUUID();
        return uuid == null ? null : Cached.getEntityByUUID(uuid);
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
                dimCost = ACServerConfig.SERVER.WARPING_SPELL_PRISM_COST_DIMENSION.get();
            }
        } else if (hitResult instanceof EntityHitResult e) {
            distSqr = e.getLocation().distanceToSqr(this.getBlockPos().getCenter());
            if (this.getTargetLevel() != level) {
                dimCost = ACServerConfig.SERVER.WARPING_SPELL_PRISM_COST_DIMENSION.get();
            }
        }

        var costMinDistance = ACServerConfig.SERVER.WARPING_SPELL_PRISM_COST_MIN_DISTANCE.get();
        var costMinDistanceSqr = costMinDistance * costMinDistance;
        var costPerBlock = ACServerConfig.SERVER.WARPING_SPELL_PRISM_COST_PER_BLOCK.get();

        if (distSqr > costMinDistanceSqr) {
            int maxCost = ACServerConfig.SERVER.WARPING_SPELL_PRISM_MAX_SOURCE_COST.get();
            if (maxCost < 0) {
                maxCost = Integer.MAX_VALUE;
            }
            return Math.max(0, (int) Math.min(maxCost, dimCost + Math.sqrt(distSqr - costMinDistanceSqr) * costPerBlock));
        }

        return Math.max(0, dimCost);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        var data = this.getPersistentData();

        if (data.contains("block", CompoundTag.TAG_LONG) && data.contains("dimension", CompoundTag.TAG_STRING)) {
            var block = data.getLong("block");
            var dimension = data.getString("dimension");
            this.setData(ACRegistry.Attachments.GLOBAL_POS_TARGET, new GlobalPos(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)), BlockPos.of(block)));
        } else if (data.contains("entity", CompoundTag.TAG_INT_ARRAY)) {
            var entity = data.getUUID("entity");
            this.setData(ACRegistry.Attachments.ENTITY_TARGET, entity);
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        var data = this.getPersistentData();
        data.remove("entity");
        data.remove("block");
        data.remove("dimension");

        super.saveAdditional(tag, registries);
    }

    @Override
    public Result onLastConnection(@Nullable GlobalPos storedPos, @Nullable Direction face, @Nullable LivingEntity storedEntity, Player player) {
        if (storedPos != null) {
            this.setBlock(storedPos.dimension(), storedPos.pos());
            this.setChanged();
            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.block", storedPos.pos().toShortString(), storedPos.dimension().location().toString()));
            return Result.SUCCESS;
        }

        if (storedEntity != null) {
            if (storedEntity instanceof Player && !player.isCreative() && !ACServerConfig.SERVER.WARPING_SPELL_PRISM_ALLOW_LINKING_OTHER_PLAYERS.get() && !storedEntity.getUUID().equals(player.getUUID())) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.fail.other_player"));
                return Result.FAIL;
            }
            this.setEntityUUID(storedEntity.getUUID());
            this.setChanged();
            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.entity", storedEntity.getDisplayName(), storedEntity.level().dimension().location().toString()));
            return Result.SUCCESS;
        }

        return Result.FAIL;
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
            if (storedEntity instanceof Player && !player.isCreative() && !ACServerConfig.SERVER.WARPING_SPELL_PRISM_ALLOW_LINKING_OTHER_PLAYERS.get() && !storedEntity.getUUID().equals(player.getUUID())) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.fail.other_player"));
                return;
            }
            this.setEntityUUID(storedEntity.getUUID());
            this.setChanged();
            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.entity", storedEntity.getDisplayName(), storedEntity.level().dimension().location().toString()));
        }
    }

    @Override
    public List<ColorPos> getWandHighlight(List<ColorPos> list) {
        var target = this.getHitResult();
        return target == null ? List.of() : List.of(new ColorPos(target.getLocation()));
    }

    @Override
    public List<ColorPos> getWandHighlight(Level level, List<ColorPos> list) {
        var dim = this.getTargetLevel();
        if (dim == level) {
            var target = this.getHitResult();
            if (target != null) {
                list.add(new ColorPos(target.getLocation()));
            }
        }
        return list;
    }
}
