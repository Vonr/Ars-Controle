package dev.qther.ars_controle.spell.filter;

import com.hollingsworth.arsnouveau.api.spell.IFilter;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public interface IAdaptiveFilter {
    int operands();

    boolean shouldResolve(Level level, HitResult hit, SpellContext spellContext);
}
