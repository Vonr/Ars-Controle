package dev.qther.ars_controle.mixin.enhanced_spellbook_controls;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Screen.class)
public abstract class ScreenMixin {
    @Shadow public abstract void clearFocus();

    @Inject(method = "keyPressed", at = @At(value = "RETURN", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(III)Z", ordinal = 2), cancellable = true)
    protected void onIgnoredKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;onClose()V"), cancellable = true)
    protected void onEscape(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    }
}
