package dev.qther.ars_controle.mixin.enhanced_spellbook_controls;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.client.gui.NoShadowTextField;
import com.hollingsworth.arsnouveau.client.gui.book.GuiSpellBook;
import com.hollingsworth.arsnouveau.client.gui.buttons.CraftingButton;
import com.hollingsworth.arsnouveau.client.gui.buttons.GlyphButton;
import com.hollingsworth.arsnouveau.common.lib.GlyphLib;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import dev.qther.ars_controle.config.ClientConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = GuiSpellBook.class, remap = false)
public abstract class GuiSpellBookMixin extends ScreenMixin implements ContainerEventHandler {
    @Shadow
    public NoShadowTextField searchBar;

    @Shadow
    public List<AbstractSpellPart> spell;

    @Shadow
    public Renderable hoveredWidget;

    @Shadow
    protected abstract void validate();

    @Shadow
    public abstract boolean charTyped(char pCodePoint, int pModifiers);

    @Shadow
    public List<CraftingButton> craftingCells;
    @Shadow public int spellWindowOffset;

    @Unique
    private static final Object2ObjectArrayMap<String, String> GLYPH_EMOJI_REPLACEMENTS;

    static {
        GLYPH_EMOJI_REPLACEMENTS = new Object2ObjectArrayMap<>();
        GLYPH_EMOJI_REPLACEMENTS.put(ArsNouveau.MODID + ":" + GlyphLib.EffectPhantomBlockID, "conjure_mageblock");
        GLYPH_EMOJI_REPLACEMENTS.put(ArsNouveau.MODID + ":" + GlyphLib.EffectLightID, "conjure_magelight");
        GLYPH_EMOJI_REPLACEMENTS.put(ArsNouveau.MODID + ":" + GlyphLib.EffectKnockbackID, "knockback");
        GLYPH_EMOJI_REPLACEMENTS.put(ArsNouveau.MODID + ":" + GlyphLib.AugmentFortuneID, "luck");
    }

    @Override
    protected void onIgnoredKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        var cfg = ClientConfig.CLIENT;

        if (keyCode == GLFW.GLFW_KEY_S && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            var clipboard = new StringBuilder();
            var mod = modifiers ^ GLFW.GLFW_MOD_CONTROL;
            switch (mod) {
                case GLFW.GLFW_MOD_SHIFT -> {
                    clipboard.append('[');
                    for (var p : this.spell) {
                        if (clipboard.length() > 1) {
                            clipboard.append(", ");
                        }
                        clipboard.append('"');
                        clipboard.append(p.getRegistryName().toString());
                        clipboard.append('"');
                    }
                    clipboard.append(']');
                }
                case GLFW.GLFW_MOD_ALT -> {
                    for (var p : this.spell) {
                        clipboard.append(':');
                        var emoji = GLYPH_EMOJI_REPLACEMENTS.computeIfAbsent(p.getRegistryName().toString(), (String k) -> k.replaceFirst("^.*:(glyph_)?", "").replaceFirst("_glyph$", ""));
                        clipboard.append(emoji);
                        clipboard.append(':');
                    }
                }
                default -> {
                    return;
                }
            }

            var mc = Minecraft.getInstance();
            mc.keyboardHandler.setClipboard(clipboard.toString());
            PortUtil.sendMessage(mc.player, Component.translatable("ars_controle.spellbook.copied_to_clipboard"));
            cir.setReturnValue(true);
            return;
        }

        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            var num = keyCode - GLFW.GLFW_KEY_0;
            num = this.spellWindowOffset + num == 0 ? 9 : num - 1;

            if (this.hoveredWidget instanceof GlyphButton b) {
                this.clearFocus();
                this.craftingCells.get(num).setAbstractSpellPart(b.abstractSpellPart);
                this.arsControle$reconstructSpell();
                cir.setReturnValue(true);
                return;
            }

            if (cfg.SWAP_GLYPHS_WITH_NUMBER_KEYS.get() && this.hoveredWidget instanceof CraftingButton fstButton) {
                this.clearFocus();
                var fst = fstButton.getAbstractSpellPart();
                var sndButton = this.craftingCells.get(num);
                var snd = sndButton.getAbstractSpellPart();
                if (fst == null && snd == null) {
                    cir.setReturnValue(true);
                    return;
                }

                var fstIdx = this.craftingCells.indexOf(fstButton);
                this.craftingCells.get(fstIdx).setAbstractSpellPart(snd);
                this.craftingCells.get(num).setAbstractSpellPart(fst);

                this.arsControle$reconstructSpell();
                cir.setReturnValue(true);
                return;
            }
        }

        if (cfg.AUTOFOCUS_SEARCH.get() && !this.searchBar.isFocused()) {
            this.clearFocus();
            this.setFocused(this.searchBar);
            if (cfg.CLEAR_SEARCH_ON_AUTOFOCUS.get()) {
                this.searchBar.setValue("");
                this.searchBar.onClear.apply("");
            }
        }
    }

    @Override
    protected void onEscape(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (ClientConfig.CLIENT.ESCAPE_TO_CLEAR_SEARCH.get() && this.searchBar.isFocused() && !this.searchBar.getValue().isEmpty()) {
            this.searchBar.setValue("");
            this.searchBar.onClear.apply("");
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void arsControle$reconstructSpell() {
        this.spell.clear();
        for (var c : this.craftingCells) {
            var p = c.getAbstractSpellPart();
            if (p == null) {
                continue;
            }
            this.spell.add(p);
        }
        this.validate();
    }
}
