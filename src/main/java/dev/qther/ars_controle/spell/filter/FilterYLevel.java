package dev.qther.ars_controle.spell.filter;

import com.hollingsworth.arsnouveau.api.spell.AbstractFilter;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.ACNames;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class FilterYLevel extends AbstractFilter {
    public static final FilterYLevel ABOVE = new FilterYLevel(ACNames.GLYPH_FILTER_ABOVE, "Filter: Above", "Only resolves the spell above the caster.", 1);
    public static final FilterYLevel BELOW = new FilterYLevel(ACNames.GLYPH_FILTER_BELOW, "Filter: Below", "Only resolves the spell below the caster.", -1);
    public static final FilterYLevel LEVEL = new FilterYLevel(ACNames.GLYPH_FILTER_LEVEL, "Filter: Level", "Only resolves the spell at the caster's elevation.", 0);

    private final int comparison;

    protected int y;

    private final String bookDescription;

    private FilterYLevel(String tag, String name, String bookDescription, int comparison) {
        super(ArsControle.prefix(tag), name);
        this.comparison = comparison;
        this.bookDescription = bookDescription;
    }

    @Override
    public String getBookDescription() {
        return this.bookDescription;
    }

    @Override
    public Integer getTypeIndex() {
        return 15;
    }

    @Override
    public boolean shouldResolveOnBlock(BlockHitResult target, Level level) {
        return Integer.compare(target.getBlockPos().getY(), this.y) == comparison;
    }

    @Override
    public boolean shouldResolveOnEntity(EntityHitResult target, Level level) {
        return Integer.compare((int) target.getEntity().position().y, this.y) == comparison;
    }

    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        this.y = (int) (spellContext.getCaster().getPosition().y - 1);
        super.onResolveEntity(rayTraceResult, world, shooter, spellStats, spellContext, resolver);
    }

    @Override
    public void onResolveBlock(BlockHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        this.y = (int) (spellContext.getCaster().getPosition().y - 1);
        super.onResolveBlock(rayTraceResult, world, shooter, spellStats, spellContext, resolver);
    }
}
