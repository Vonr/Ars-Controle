package dev.qther.ars_controle.mixin;

import com.hollingsworth.arsnouveau.api.spell.ISpellValidator;
import com.hollingsworth.arsnouveau.common.spell.validation.StandardSpellValidator;
import com.llamalad7.mixinextras.sugar.Local;
import dev.qther.ars_controle.spell.validator.BinaryFilterValidator;
import dev.qther.ars_controle.spell.validator.UnaryFilterValidator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = StandardSpellValidator.class, remap = false)
public class StandardSpellValidatorMixin {
    @Inject(method = "<init>(Z)V", at = @At(value = "RETURN"))
    private void injectValidators(boolean enforceCastTimeValidations, CallbackInfo ci, @Local List<ISpellValidator> validators) {
        validators.add(BinaryFilterValidator.INSTANCE);
        validators.add(UnaryFilterValidator.INSTANCE);
    }
}
