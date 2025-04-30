package dev.qther.ars_controle.block;

import com.hollingsworth.arsnouveau.common.block.ModBlock;
import com.hollingsworth.arsnouveau.common.block.PortalBlock;
import com.hollingsworth.arsnouveau.common.util.VoxelShapeUtils;
import dev.qther.ars_controle.block.tile.ScrollHolderTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ScrollHolderBlock extends ModBlock implements EntityBlock {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty HAS_SCROLL = BooleanProperty.create("has_scroll");

    public ScrollHolderBlock() {
        super();
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(HAS_SCROLL, false));
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new ScrollHolderTile(pPos, pState);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    protected @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_SCROLL);
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        var newBlock = level.getBlockState(pos).getBlock();
        if (!(newBlock instanceof PortalBlock)) {
            if (level.getBlockEntity(pos) instanceof ScrollHolderTile tile) {
                tile.update();
            }
        }
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel)) {
            return ItemInteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof ScrollHolderTile tile) {
            var cap = tile.getItemHandler();
            var hasSpace = cap.getStackInSlot(0).isEmpty();
            if (cap.isItemValid(0, stack)) {
                cap.insertItem(0, stack, false);
                return ItemInteractionResult.SUCCESS;
            } else if (!hasSpace) {
                var extracted = cap.extractItem(0, 1, false);
                if (!extracted.isEmpty()) {
                    player.addItem(extracted);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel && !newState.is(state.getBlock()) && level.getBlockEntity(pos) instanceof ScrollHolderTile tile) {
            var stack = tile.getStack();
            if (!stack.isEmpty()) {
                var sp = pos.getCenter();
                level.addFreshEntity(new ItemEntity(level, sp.x, sp.y, sp.z, stack));
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return state.getValue(HAS_SCROLL) ? 15 : 0;
    }

    public static final VoxelShape UP = Stream.of(
            Shapes.box(0, 0.875, 0, 1, 1, 1),
            Shapes.box(0, 0, 0, 1, 0.125, 0.375),
            Shapes.box(0, 0, 0.375, 0.375, 0.125, 0.625),
            Shapes.box(0.375, 0.0625, 0.375, 0.625, 0.1875, 0.625),
            Shapes.box(0.625, 0, 0.375, 1, 0.125, 0.625),
            Shapes.box(0, 0, 0.625, 1, 0.125, 1),
            Shapes.box(0.375, 0.8125, 0.375, 0.625, 0.875, 0.625),
            Shapes.box(0, 0.125, 0, 0.375, 0.875, 0.375),
            Shapes.box(0.625, 0.125, 0, 1, 0.875, 0.375),
            Shapes.box(0, 0.125, 0.625, 0.375, 0.875, 1),
            Shapes.box(0.625, 0.125, 0.625, 1, 0.875, 1)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public static final VoxelShape DOWN = VoxelShapeUtils.rotate(UP, Direction.UP);
    public static final VoxelShape EAST = VoxelShapeUtils.rotate(UP, Direction.WEST);
    public static final VoxelShape WEST = VoxelShapeUtils.rotate(UP, Direction.EAST);
    public static final VoxelShape NORTH = VoxelShapeUtils.rotate(UP, Direction.SOUTH);
    public static final VoxelShape SOUTH = VoxelShapeUtils.rotate(UP, Direction.NORTH);

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        return switch (facing) {
            case UP -> UP;
            case DOWN -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }
}
