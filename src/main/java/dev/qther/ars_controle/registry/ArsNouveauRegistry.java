package dev.qther.ars_controle.registry;

import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import dev.qther.ars_controle.spell.effect.EffectPreciseDelay;
import dev.qther.ars_controle.spell.filter.FilterBinary;
import dev.qther.ars_controle.spell.filter.FilterRandom;
import dev.qther.ars_controle.spell.filter.FilterUnary;
import dev.qther.ars_controle.spell.filter.FilterYLevel;

import java.util.ArrayList;
import java.util.List;

public class ArsNouveauRegistry {

    public static List<AbstractSpellPart> registeredSpells = new ArrayList<>(); //this will come handy for datagen

    public static void registerGlyphs() {
        register(EffectPreciseDelay.INSTANCE);
        register(FilterYLevel.ABOVE);
        register(FilterYLevel.BELOW);
        register(FilterYLevel.LEVEL);
        register(FilterBinary.OR);
        register(FilterBinary.XOR);
        register(FilterBinary.XNOR);
        register(FilterUnary.NOT);
        register(FilterRandom.INSTANCE);
    }

    public static void registerSounds() {
//        ModRegistry.EXAMPLE_SPELL_SOUND = SpellSoundRegistry.registerSpellSound(new SpellSound(ModRegistry.EXAMPLE_FAMILY.get(), Component.literal("Example")));
    }

    public static void register(AbstractSpellPart spellPart) {
        GlyphRegistry.registerSpell(spellPart);
        registeredSpells.add(spellPart);
    }
}
