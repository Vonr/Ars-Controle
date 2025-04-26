package dev.qther.ars_controle.block.tile;

import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.common.block.PortalBlock;
import com.hollingsworth.arsnouveau.common.block.tile.PortalTile;
import com.hollingsworth.arsnouveau.common.block.tile.SingleItemTile;
import com.hollingsworth.arsnouveau.common.datagen.BlockTagProvider;
import com.hollingsworth.arsnouveau.common.items.WarpScroll;
import com.hollingsworth.arsnouveau.setup.config.ServerConfig;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import dev.qther.ars_controle.block.ScrollHolderBlock;
import dev.qther.ars_controle.registry.ACRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ScrollHolderTile extends SingleItemTile {
    public ScrollHolderTile(BlockPos pos, BlockState state) {
        super(ACRegistry.Tiles.SCROLL_HOLDER.get(), pos, state);
    }

    public ItemHandler getItemHandler() {
        return new ItemHandler(this);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && this.stack.isEmpty() && stack.getItem() instanceof WarpScroll && stack.has(DataComponentRegistry.WARP_SCROLL);
    }

    private static final Direction[][] ORDERINGS = new Direction[][]{
            new Direction[]{Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH},
            new Direction[]{Direction.WEST, Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST},
            new Direction[]{Direction.NORTH, Direction.DOWN, Direction.SOUTH, Direction.UP, Direction.NORTH},
            new Direction[]{Direction.WEST, Direction.DOWN, Direction.EAST, Direction.UP, Direction.WEST},
    };

    public void update() {
        if (!(level instanceof ServerLevel) || !ServerConfig.ENABLE_WARP_PORTALS.get()) {
            return;
        }

        var state = this.getBlockState();
        var facing = state.getValue(ScrollHolderBlock.FACING);

        level.setBlock(this.getBlockPos(), state.setValue(ScrollHolderBlock.HAS_SCROLL, !this.stack.isEmpty()), 2);

        nextOrder:
        for (Direction[] order : ORDERINGS) {
            var cursor = this.getBlockPos().mutable();
            var vertices = new BlockPos[6];
            vertices[0] = this.getBlockPos();
            Direction lastDir = null;

            for (int i = 0; i < order.length; ++i) {
                var direction = order[i];
                direction = getRotatedDirection(facing, direction);
                var inside = order[i == order.length - 1 ? 1 : i + 1];
                inside = getRotatedDirection(facing, inside);

                var distance = 0;

                while (true) {
                    var nextState = level.getBlockState(cursor.setWithOffset(cursor, direction));
                    var insideState = level.getBlockState(cursor.setWithOffset(cursor, inside));
                    cursor.setWithOffset(cursor, inside.getOpposite());
                    if (insideState.getBlock() == ACRegistry.Blocks.SCROLL_HOLDER.get() || insideState.is(BlockTagProvider.DECORATIVE_AN)) {
                        break;
                    }

                    if (nextState.getBlock() == ACRegistry.Blocks.SCROLL_HOLDER.get()) {
                        cursor.setWithOffset(cursor, direction.getOpposite());
                        break;
                    }

                    if (!nextState.is(BlockTagProvider.DECORATIVE_AN)) {
                        var nextInsideState = level.getBlockState(cursor.setWithOffset(cursor, inside));
                        cursor.setWithOffset(cursor, inside.getOpposite());
                        if (nextInsideState.getBlock() == ACRegistry.Blocks.SCROLL_HOLDER.get() || nextInsideState.is(BlockTagProvider.DECORATIVE_AN)) {
                            break;
                        }
                        cursor.setWithOffset(cursor, direction.getOpposite());
                        break;
                    }

                    distance++;
                    if (distance > 23) {
                        continue nextOrder;
                    }

                    if (facing.getOpposite() == direction && cursor.get(direction.getAxis()) == this.getBlockPos().get(direction.getAxis())) {
                        break;
                    }
                }

                if (i == 1) {
                    if (cursor.distManhattan(this.getBlockPos()) > cursor.distManhattan(this.getBlockPos().offset(facing.getOpposite().getNormal()))) {
                        continue;
                    }
                }

                vertices[i + 1] = cursor.immutable();

                lastDir = direction;
            }

            if (lastDir == null) {
                continue;
            }

            if (cursor.setWithOffset(cursor, lastDir).equals(this.getBlockPos())) {
                for (int i = 1; i < 3; ++i) {
                    var distance = vertices[i].distManhattan(vertices[i + 1]);
                    if (distance < 2 || distance > 23) {
                        continue nextOrder;
                    }
                }

                var distance = vertices[1].distManhattan(vertices[4]);
                if (distance < 2 || distance > 23) {
                    continue;
                }

                var start = vertices[1];
                var end = vertices[3];
                start = new BlockPos(
                        start.getX() + Double.compare(end.getX(), start.getX()),
                        start.getY() + Double.compare(end.getY(), start.getY()),
                        start.getZ() + Double.compare(end.getZ(), start.getZ())
                );
                end = new BlockPos(
                        end.getX() + Double.compare(start.getX(), end.getX()),
                        end.getY() + Double.compare(start.getY(), end.getY()),
                        end.getZ() + Double.compare(start.getZ(), end.getZ())
                );

                var needsSource = false;
                if (!this.stack.isEmpty()) {
                    needsSource = true;
                    for (var pos : BlockPos.betweenClosed(start, end)) {
                        var insideState = level.getBlockState(pos);
                        var isPortal = insideState.getBlock() instanceof PortalBlock;
                        if (isPortal) {
                            needsSource = false;
                        }
                        if (!insideState.isAir() && !isPortal) {
                            continue nextOrder;
                        }
                    }
                }

                Direction.Axis axis;
                boolean horizontal = false;

                if (vertices[1].getY() == vertices[2].getY()) {
                    axis = Direction.Axis.Y;
                    horizontal = true;
                } else if (vertices[2].getX() != vertices[3].getX()) {
                    axis = Direction.Axis.X;
                } else {
                    axis = Direction.Axis.Z;
                }

                var data = this.stack.get(DataComponentRegistry.WARP_SCROLL);
                var displayName = this.stack.get(DataComponents.CUSTOM_NAME) != null ? this.stack.getHoverName().getString() : null;

                for (var pos : BlockPos.betweenClosed(start, end)) {
                    if (!this.stack.isEmpty() && level.getBlockState(pos).isAir()) {
                        if (needsSource) {
                            var taken = SourceUtil.takeSourceMultipleWithParticles(this.getBlockPos(), level, 10, 1000);
                            if (taken == null || taken.isEmpty()) {
                                continue nextOrder;
                            }
                        }

                        level.setBlock(pos, BlockRegistry.PORTAL_BLOCK.defaultBlockState().setValue(PortalBlock.AXIS, axis), 18);
                        if (level.getBlockEntity(pos) instanceof PortalTile tile) {
                            tile.setFromScroll(data);
                            tile.displayName = displayName;
                            tile.isHorizontal = horizontal;
                            tile.updateBlock();
                        }
                    } else if (this.stack.isEmpty() && level.getBlockState(pos).getBlock() instanceof PortalBlock) {
                        level.removeBlock(pos, false);
                    }
                }

                break;
            }
        }
    }

    private Direction getRotatedDirection(Direction facing, Direction direction) {
        return switch (facing) {
            case DOWN -> direction.getOpposite();
            case UP -> direction;
            case SOUTH -> direction.getClockWise(Direction.Axis.X);
            case NORTH -> direction.getCounterClockWise(Direction.Axis.X);
            case WEST -> direction.getClockWise(Direction.Axis.Z);
            case EAST -> direction.getCounterClockWise(Direction.Axis.Z);
        };
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.update();
    }

    public static class ItemHandler implements IItemHandler {
        ScrollHolderTile tile;

        public ItemHandler(ScrollHolderTile tile) {
            this.tile = tile;
        }

        @Override
        public int getSlots() {
            return this.tile.getContainerSize();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return this.tile.getItem(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!this.isItemValid(slot, stack)) {
                return stack;
            }

            if (simulate) {
                return stack.copyWithCount(stack.getCount() - 1);
            }

            this.tile.stack = stack.split(1);
            this.tile.setChanged();
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0 || this.tile.stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            if (simulate) {
                return this.tile.stack.copyWithCount(1);
            }

            var extracted = this.tile.stack.split(amount);
            this.tile.setChanged();
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 1 : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return this.tile.canPlaceItem(slot, stack);
        }
    }
}
