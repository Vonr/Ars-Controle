package dev.qther.ars_controle.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface ContextualAnalogSignalling {
    boolean hasAnalogOutputSignalGivenContext(LevelAccessor level, BlockPos pos, BlockState state, Direction side);
}
