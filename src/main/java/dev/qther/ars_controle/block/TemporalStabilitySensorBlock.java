package dev.qther.ars_controle.block;

import com.hollingsworth.arsnouveau.common.block.ModBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TemporalStabilitySensorBlock extends ModBlock {
    public TemporalStabilitySensorBlock() {
        super();
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            return (int) Math.round(serverLevel.getServer().getAverageTickTimeNanos() * 15.0 / 50.0 / TimeUtil.NANOSECONDS_PER_MILLISECOND);
        }

        return 0;
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.tick(state, level, pos, random);
        level.updateNeighbourForOutputSignal(pos, this);
        level.scheduleTick(pos, this, 10, TickPriority.EXTREMELY_HIGH);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.scheduleTick(pos, this, 10, TickPriority.EXTREMELY_HIGH);
    }
}
