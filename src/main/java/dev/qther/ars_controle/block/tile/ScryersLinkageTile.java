package dev.qther.ars_controle.block.tile;

import com.hollingsworth.arsnouveau.api.item.IWandable;
import com.hollingsworth.arsnouveau.common.block.CraftingLecternBlock;
import com.hollingsworth.arsnouveau.common.block.tile.ModdedTile;
import com.hollingsworth.arsnouveau.common.block.tile.StorageLecternTile;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import dev.qther.ars_controle.util.Cached;
import dev.qther.ars_controle.block.ScryersLinkageBlock;
import dev.qther.ars_controle.registry.ModRegistry;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class ScryersLinkageTile extends ModdedTile implements IWandable, Container {
    public ScryersLinkageTile(BlockPos pos, BlockState state) {
        super(ModRegistry.SCRYERS_LINKAGE_TILE.get(), pos, state);
    }

    public @Nullable GlobalPos getTarget() {
        var info = this.getTargetInfo();
        if (info == null) {
            return null;
        }

        return new GlobalPos(info.first().dimension(), info.second());
    }

    public @Nullable Pair<Level, BlockPos> getTargetInfo() {
        if (level == null) {
            return null;
        }

        var targetLevel = this.getTargetLevel();
        var targetPos = this.getTargetBlock();
        if (targetLevel == null || targetPos == null) {
            return null;
        }

        var block = targetLevel.getBlockState(targetPos).getBlock();
        if (block instanceof ScryersLinkageBlock || block instanceof CraftingLecternBlock) {
            this.removeBlock();
            return null;
        }

        return Pair.of(targetLevel, targetPos);
    }

    public boolean hasTarget() {
        if (level == null) {
            return false;
        }

        var tag = this.getPersistentData();
        return tag.contains("block", CompoundTag.TAG_LONG);
    }

    public boolean setBlock(@NotNull Level level, @NotNull BlockPos block) {
        var thisLevel = this.getLevel();
        if (thisLevel == null) {
            return false;
        }

        var target = level.getBlockState(block);
        if (target.getBlock() instanceof ScryersLinkageBlock || target.getBlock() instanceof CraftingLecternBlock) {
            return false;
        }

        var tag = this.getPersistentData();
        tag.putString("dimension", level.dimension().location().toString());
        tag.putLong("block", block.asLong());
        this.notifyChange();

        return true;
    }

    public void removeBlock() {
        var level = this.getLevel();
        if (level == null) {
            return;
        }

        var tag = this.getPersistentData();
        tag.remove("dimension");
        tag.remove("block");
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

        var tag = this.getPersistentData();
        var s = tag.contains("dimension", 8) ? tag.getString("dimension") : null;
        if (s == null) {
            return null;
        }

        if (s.equals(level.dimension().location().toString())) {
            return level;
        }

        if (level.isClientSide) {
            return null;
        }

        return Cached.getLevelByName(level.getServer().getAllLevels(), s);
    }

    private @Nullable BlockPos getTargetBlock() {
        if (level == null) {
            return null;
        }

        var tag = this.getPersistentData();
        return tag.contains("block", CompoundTag.TAG_LONG) ? BlockPos.of(tag.getLong("block")) : null;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);

        var pos = this.getTargetBlock();
        if (pos != null) {
            tag.putLong("block", pos.asLong());
        }

        var level = this.getTargetLevel();
        if (level != null) {
            tag.putString("dimension", level.dimension().location().toString());
        }
    }

    @Override
    public void onFinishedConnectionLast(@Nullable GlobalPos storedPos, @Nullable Direction face, @Nullable LivingEntity storedEntity, Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        if (storedPos != null) {
            var server = sp.getServer();
            var level = Cached.getLevelByName(server.getAllLevels(), storedPos.dimension().location().toString());

            if (level == null) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_dimension"));
                return;
            }

            if (this.setBlock(level, storedPos.pos())) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.block", storedPos.pos().toShortString(), level.dimension().location().toString()));
            } else {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_target"));
            }
        }
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

    public static final TicketType<ChunkPos> TICKET_TYPE = TicketType.create("scryers_linkage", Comparator.comparingLong(ChunkPos::toLong), 1);

    @SuppressWarnings("SameParameterValue")
    private <T> @Nullable T getTargetAs(Class<T> clazz) {
        var info = this.getTargetInfo();
        if (info == null) {
            return null;
        }

        var level = info.first();
        var pos = info.second();

        var loadPos = new ChunkPos(pos);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().addRegionTicket(TICKET_TYPE, loadPos, 1, loadPos, true);
        }
        var be = level.getBlockEntity(pos);
        if (be == null || be instanceof ScryersLinkageTile || be instanceof StorageLecternTile) {
            return null;
        }

        if (clazz.isAssignableFrom(be.getClass())) {
            return clazz.cast(be);
        }

        return null;
    }

    @Override
    public int getContainerSize() {
        var container = this.getTargetAs(Container.class);
        return container == null ? 0 : container.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        var container = this.getTargetAs(Container.class);
        return container == null || container.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        var container = this.getTargetAs(Container.class);
        return container == null ? ItemStack.EMPTY : container.getItem(i);
    }

    @Override
    public @NotNull ItemStack removeItem(int i, int i1) {
        var container = this.getTargetAs(Container.class);
        return container == null ? ItemStack.EMPTY : container.removeItem(i, i1);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int i) {
        var container = this.getTargetAs(Container.class);
        return container == null ? ItemStack.EMPTY : container.removeItemNoUpdate(i);
    }

    @Override
    public void setItem(int i, @NotNull ItemStack itemStack) {
        var container = this.getTargetAs(Container.class);
        if (container != null) {
            container.setItem(i, itemStack);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        var container = this.getTargetAs(Container.class);
        return container != null && container.stillValid(player);
    }

    @Override
    public void clearContent() {
        var container = this.getTargetAs(Container.class);
        if (container != null) {
            container.clearContent();
        }
    }
}
