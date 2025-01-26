package dev.qther.ars_controle.mixin;

import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import com.hollingsworth.arsnouveau.api.ritual.RangeRitual;
import com.hollingsworth.arsnouveau.api.ritual.RitualEventQueue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.qther.ars_controle.item.PortableBrazierRelayItem;
import dev.qther.ars_controle.registry.AttachmentRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = RitualEventQueue.class, remap = false)
public class RitualEventQueueMixin {
    @Inject(method = "getRituals", at = @At("RETURN"))
    @SuppressWarnings("unchecked")
    private static <T extends RangeRitual> void withRelayedRituals(Level level, Class<T> type, CallbackInfoReturnable<List<T>> cir) {
        if (level instanceof ServerLevel serverLevel) {
            var out = cir.getReturnValue();

            for (var ritual : PortableBrazierRelayItem.getRelayedRituals().keySet()) {
                if (ritual.getClass().equals(type)) {
                    out.add((T) ritual);
                }
            }
        }
    }

    @Inject(method = "getRituals", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    private static <T extends RangeRitual> void excludeRelayed(Level level, Class<T> type, CallbackInfoReturnable<List<T>> cir, @Local AbstractRitual ritual) {
        if (ritual.tile.hasData(AttachmentRegistry.RELAY_UUID)) {
            cir.cancel();
        }
    }

    @Inject(method = "getRitual", at = @At("RETURN"), cancellable = true)
    @SuppressWarnings("unchecked")
    private static <T extends RangeRitual> void withRelayedRitual(Level level, Class<T> type, Predicate<T> isMatch, CallbackInfoReturnable<T> cir) {
        for (var ritual : PortableBrazierRelayItem.getRelayedRituals().keySet()) {
            if (ritual.getClass().equals(type) && isMatch.test((T) ritual)) {
                cir.setReturnValue((T) ritual);
            }
        }
    }

    @WrapOperation(method = "getRitual", at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"))
    private static boolean excludeRelayed2(Predicate instance, Object t, Operation<Boolean> original, @Local AbstractRitual ritual) {
        if (ritual.tile.hasData(AttachmentRegistry.RELAY_UUID)) {
            return false;
        }
        return original.call(instance, t);
    }
}
