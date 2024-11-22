package dev.qther.ars_controle.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.qther.ars_controle.block.ContextualAnalogSignalling;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComparatorBlock.class)
public class ComparatorBlockMixin {
    @WrapOperation(method = "getInputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasAnalogOutputSignal()Z"))
    public boolean hasAnalogOutputSignalWithContext(BlockState instance, Operation<Boolean> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        if (instance.getBlock() instanceof ContextualAnalogSignalling cas) {
            var direction = state.getValue(ComparatorBlock.FACING);
            var blockpos = pos.relative(direction);
            return cas.hasAnalogOutputSignalGivenContext(level, blockpos, instance, direction);
        }

        return original.call(instance);
    }
}
