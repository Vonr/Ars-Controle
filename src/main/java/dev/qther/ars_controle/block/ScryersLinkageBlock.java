package dev.qther.ars_controle.block;

import com.hollingsworth.arsnouveau.api.util.ANEventBus;
import com.hollingsworth.arsnouveau.common.block.TickableModBlock;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.block.tile.ScryersLinkageTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ScryersLinkageBlock extends TickableModBlock implements EntityBlock, ContextualAnalogSignalling {
    public ScryersLinkageBlock() {
        super(
                defaultProperties()
                        .noOcclusion()
                        .isValidSpawn(Blocks::never)
                        .isRedstoneConductor(ScryersLinkageBlock::never)
                        .isSuffocating(ScryersLinkageBlock::never)
                        .isViewBlocking(ScryersLinkageBlock::never)
        );
    }

    public static Boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return false;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult _hr) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (ANEventBus.post(new BlockEvent.BreakEvent(level, pos, state, player))) {
            return InteractionResult.FAIL;
        }

        var te = level.getBlockEntity(pos);
        if (te == null) {
            ArsControle.LOGGER.warn("No tile entity in scryer's linkage.");
            return InteractionResult.FAIL;
        }

        if (!(te instanceof ScryersLinkageTile tile)) {
            ArsControle.LOGGER.warn("Wrong tile entity in scryer's linkage.");
            return InteractionResult.FAIL;
        }

        var b = tile.getTarget();
        if (b == null) {
            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.get.none"));
            return InteractionResult.SUCCESS;
        }

        PortUtil.sendMessage(player, Component.translatable("ars_controle.target.get.block", b.pos().toShortString(), b.dimension().location().toString()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new ScryersLinkageTile(pPos, pState);
    }

    @Override
    protected @NotNull VoxelShape getVisualShape(@NotNull BlockState p_309057_, @NotNull BlockGetter p_308936_, @NotNull BlockPos p_308956_, @NotNull CollisionContext p_309006_) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(@NotNull BlockState p_308911_, @NotNull BlockGetter p_308952_, @NotNull BlockPos p_308918_) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(@NotNull BlockState p_309084_, @NotNull BlockGetter p_309133_, @NotNull BlockPos p_309097_) {
        return true;
    }

    @Override
    protected int getSignal(@NotNull BlockState blockState, BlockGetter getter, @NotNull BlockPos pos, @NotNull Direction side) {
        var be = getter.getBlockEntity(pos);
        if (!(be instanceof ScryersLinkageTile tile)) {
            return 0;
        }

        var info = tile.getTargetInfo();
        if (info == null) {
            return 0;
        }

        var targetLevel = info.first();
        var targetPos = info.second();

        return targetLevel.getBlockState(targetPos).getSignal(targetLevel, targetPos, side);
    }

    @Override
    protected int getDirectSignal(@NotNull BlockState blockState, BlockGetter getter, @NotNull BlockPos pos, @NotNull Direction side) {
        var be = getter.getBlockEntity(pos);
        if (!(be instanceof ScryersLinkageTile tile)) {
            return 0;
        }

        var info = tile.getTargetInfo();
        if (info == null) {
            return 0;
        }

        var targetLevel = info.first();
        var targetPos = info.second();

        return targetLevel.getBlockState(targetPos).getDirectSignal(targetLevel, targetPos, side);
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public boolean hasAnalogOutputSignalGivenContext(LevelAccessor level, BlockPos pos, BlockState state, Direction side) {
        var be = level.getBlockEntity(pos);
        if (!(be instanceof ScryersLinkageTile tile)) {
            return false;
        }

        var info = tile.getTargetInfo();
        if (info == null) {
            return false;
        }

        var targetLevel = info.first();
        var targetPos = info.second();

        var targetState = targetLevel.getBlockState(targetPos);

        if (targetState.getBlock() instanceof ContextualAnalogSignalling cas) {
            return cas.hasAnalogOutputSignalGivenContext(targetLevel, targetPos, targetState, side);
        }

        return targetState.hasAnalogOutputSignal();
    }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        var be = level.getBlockEntity(pos);
        if (!(be instanceof ScryersLinkageTile tile)) {
            return 0;
        }

        var info = tile.getTargetInfo();
        if (info == null) {
            return 0;
        }

        var targetLevel = info.first();
        var targetPos = info.second();

        return targetLevel.getBlockState(targetPos).getAnalogOutputSignal(targetLevel, targetPos);
    }

    @Override
    public boolean canConnectRedstone(@NotNull BlockState ignoredState, BlockGetter getter, @NotNull BlockPos pos, @Nullable Direction direction) {
        var be = getter.getBlockEntity(pos);
        if (!(be instanceof ScryersLinkageTile tile)) {
            return false;
        }

        var info = tile.getTargetInfo();
        if (info == null) {
            return false;
        }

        var targetLevel = info.first();
        var targetPos = info.second();

        var state = targetLevel.getBlockState(targetPos);
        return state.getBlock().canConnectRedstone(state, targetLevel, targetPos, direction);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level ignoredLevel, @NotNull BlockState ignoredState, @NotNull BlockEntityType<T> ignoredType) {
        var be = new AtomicReference<>(new WeakReference<>((ScryersLinkageTile) null));
        var target = new AtomicInteger(-1);
        return (Level level, BlockPos pos, BlockState state, T type) -> {
            var tile = be.get().get();
            if (tile == null) {
                var maybeBe = level.getBlockEntity(pos);
                if (maybeBe instanceof ScryersLinkageTile tile2) {
                    be.set(new WeakReference<>(tile2));
                }
            }

            tile = be.get().get();
            if (tile != null) {
                if (!tile.hasTarget()) {
                    if (target.getAndSet(-1) != -1) {
                        tile.invalidateCapabilities();
                    }
                    return;
                }

                var info = tile.getTargetInfo();
                if (info == null) {
                    target.set(-1);
                    return;
                }
                var newBe = System.identityHashCode(info.first().getBlockEntity(info.second()));
                if (target.getAndSet(newBe) != newBe) {
                    tile.invalidateCapabilities();
                }
                level.updateNeighborsAt(pos, state.getBlock());
                level.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
        };
    }

    static final VoxelShape SHAPE;
    // Generated from blockbench
    static {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 0.0625, 0.0625, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.0625, 0.1875, 0.1875, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 0.125, 0.125, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.25, 0.25, 0.75, 0.75, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0, 0, 1, 0.0625, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0, 1, 0.125, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.125, 0, 1, 0.25, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0, 0.125, 1, 0.0625, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0, 0.9375, 1, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.875, 1, 0.125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0, 0.9375, 0.875, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.125, 0.9375, 1, 0.25, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0, 0.75, 1, 0.0625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.9375, 0.0625, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.875, 0.125, 0.125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.9375, 0.25, 0.0625, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.125, 0.9375, 0.0625, 0.25, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.75, 0.0625, 0.0625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0, 0.0625, 1, 0.061875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.875, 0, 0.125, 1, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.9375, 0, 0.25, 1, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.75, 0, 0.0625, 0.875, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0.125, 0.0625, 0.9375, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.9375, 0, 1, 1, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.875, 0, 1, 1, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.9375, 0, 0.875, 1, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.75, 0, 1, 0.875, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.9375, 0.125, 1, 1, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.9375, 0.9375, 1, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.875, 0.875, 1, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.9375, 0.9375, 0.875, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.75, 0.9375, 1, 0.875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.9375, 0.75, 1, 1, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0.9375, 0.0625, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.875, 0.875, 0.125, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.9375, 0.9375, 0.25, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.75, 0.9375, 0.0625, 0.875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0.75, 0.0625, 1, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.0625, 0.0625, 0.9375, 0.1875, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.0625, 0.8125, 0.9375, 0.1875, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.0625, 0.8125, 0.1875, 0.1875, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 0.0625, 0.9375, 0.9375, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.8125, 0.8125, 0.9375, 0.9375, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.8125, 0.0625, 0.1875, 0.9375, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.8125, 0.8125, 0.1875, 0.9375, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0.0625, 0.0625, 1, 0.063125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0.125, 0.0625, 1, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0.125, 0.0625, 1, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1, 0.125, 0.0625, 1, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1, 0.125, 0.0625, 1, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0, 0.25, 0.0625, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.125, 0, 0.0625, 0.25, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.125, 0.0625, 0.0625, 0.25), BooleanOp.OR);
        SHAPE = Shapes.join(shape, Shapes.box(0.75, 0, 0, 0.875, 0.0625, 0.0625), BooleanOp.OR);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState p_220053_1_, @NotNull BlockGetter p_220053_2_, @NotNull BlockPos p_220053_3_, @NotNull CollisionContext p_220053_4_) {
        return SHAPE;
    }
}
