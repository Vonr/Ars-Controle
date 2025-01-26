package dev.qther.ars_controle.mixin;

import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AbstractRitual.class, remap = false)
public interface AbstractRitualInvoker {
    @Invoker
    void invokeTick();
}
