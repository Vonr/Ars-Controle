package dev.qther.ars_controle.block.tile;

import com.hollingsworth.arsnouveau.api.item.IWandable;
import com.hollingsworth.arsnouveau.client.particle.ColorPos;
import com.hollingsworth.arsnouveau.common.block.tile.ModdedTile;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import dev.qther.ars_controle.datagen.ACBlockTagProvider;
import dev.qther.ars_controle.registry.ACRegistry;
import dev.qther.ars_controle.util.Cached;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScryersLinkageTile extends ModdedTile implements IWandable, IDimensionalHighlighter {
    public ScryersLinkageTile(BlockPos pos, BlockState state) {
        super(ACRegistry.Tiles.SCRYERS_LINKAGE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        var data = this.getPersistentData();

        if (data.contains("block", CompoundTag.TAG_LONG) && data.contains("dimension", CompoundTag.TAG_STRING)) {
            var block = data.getLong("block");
            var dimension = data.getString("dimension");
            this.setData(ACRegistry.Attachments.GLOBAL_POS_TARGET, new GlobalPos(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)), BlockPos.of(block)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        var data = this.getPersistentData();
        data.remove("block");
        data.remove("dimension");
        super.saveAdditional(tag, registries);
    }

    public @Nullable GlobalPos getTarget() {
        return this.getExistingData(ACRegistry.Attachments.GLOBAL_POS_TARGET).orElse(null);
    }

    public @Nullable Pair<Level, BlockPos> getTargetInfo() {
        var pos = this.getTarget();
        if (pos == null) {
            return null;
        }
        return Pair.of(Cached.getLevelByKey(pos.dimension()), pos.pos());
    }

    public boolean hasTarget() {
        return this.hasData(ACRegistry.Attachments.GLOBAL_POS_TARGET);
    }

    public boolean setBlock(@NotNull Level level, @NotNull BlockPos block) {
        var thisLevel = this.getLevel();
        if (thisLevel == null) {
            return false;
        }

        var target = level.getBlockState(block);
        if (BuiltInRegistries.BLOCK.wrapAsHolder(target.getBlock()).is(ACBlockTagProvider.SCRYERS_LINKAGE_BLACKLIST)) {
            return false;
        }

        this.setData(ACRegistry.Attachments.GLOBAL_POS_TARGET, new GlobalPos(level.dimension(), block));
        this.notifyChange();

        return true;
    }

    public void removeBlock() {
        var level = this.getLevel();
        if (level == null) {
            return;
        }

        this.removeData(ACRegistry.Attachments.GLOBAL_POS_TARGET);
        this.notifyChange();
    }

    private void notifyChange() {
        var level = this.getLevel();
        if (level == null) {
            return;
        }

        this.setChanged();
        this.invalidateCapabilities();
        var pos = this.getBlockPos();
        var state = this.getBlockState();
        var thisBlock = state.getBlock();
        level.updateNeighborsAt(pos, thisBlock);
        level.updateNeighbourForOutputSignal(pos, thisBlock);
        state.updateNeighbourShapes(level, pos, 3);
    }

    private @Nullable Level getTargetLevel() {
        if (level == null) {
            return null;
        }

        var target = this.getTarget();
        if (target == null) {
            return null;
        }

        if (level.dimension().equals(target.dimension())) {
            return level;
        }

        if (level.isClientSide) {
            return null;
        }

        return Cached.getLevelByKey(target.dimension());
    }

    private @Nullable BlockPos getTargetBlock() {
        if (level == null) {
            return null;
        }

        var target = this.getTarget();
        if (target == null) {
            return null;
        }

        return target.pos();
    }

    @Override
    public Result onLastConnection(@Nullable GlobalPos storedPos, @Nullable Direction face, @Nullable LivingEntity storedEntity, Player player) {
        if (!(player instanceof ServerPlayer)) {
            return Result.FAIL;
        }

        if (storedPos == null) {
            return Result.FAIL;
        }

        var level = Cached.getLevelByKey(storedPos.dimension());

        if (level == null) {
            PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_dimension"));
            return Result.FAIL;
        }

        if (!this.setBlock(level, storedPos.pos())) {
            PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_target"));
            return Result.FAIL;
        }

        PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.block", storedPos.pos().toShortString(), level.dimension().location().toString()));
        return Result.SUCCESS;
    }

    @Override
    public void onFinishedConnectionLast(@Nullable BlockPos storedPos, @Nullable Direction face, @Nullable LivingEntity storedEntity, Player player) {
        if (storedPos != null) {
            var level = player.level();
            if (this.setBlock(level, storedPos)) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.block", storedPos.toShortString(), level.dimension().location().toString()));
            } else {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_target"));
            }
        }
    }

    @Override
    public List<ColorPos> getWandHighlight(List<ColorPos> list) {
        var target = this.getTargetBlock();
        if (target != null) {
            list.add(ColorPos.centered(target));
        }
        return list;
    }

    @Override
    public List<ColorPos> getWandHighlight(Level level, List<ColorPos> list) {
        var target = this.getTargetInfo();
        if (target != null && target.first() == level) {
            list.add(ColorPos.centered(target.second()));
        }
        return list;
    }
}
