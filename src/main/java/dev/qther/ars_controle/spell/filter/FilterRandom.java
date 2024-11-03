package dev.qther.ars_controle.spell.filter;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDampen;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.ModNames;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class FilterRandom extends AbstractFilter {
    public static final FilterRandom INSTANCE = new FilterRandom();

    static final double BASE_CHANCE = 0.5D;
    public double chance = BASE_CHANCE;

    private FilterRandom() {
        super(ArsControle.prefix(ModNames.GLYPH_FILTER_RANDOM), "FilterRandom");
    }

    @Override
    public boolean shouldResolveOnBlock(BlockHitResult target, Level level) {
        return this.shouldResolve();
    }

    @Override
    public boolean shouldResolveOnEntity(EntityHitResult target, Level level) {
        return this.shouldResolve();
    }

    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        this.chance = calculateChance(spellStats.getAmpMultiplier());
        super.onResolveEntity(rayTraceResult, world, shooter, spellStats, spellContext, resolver);
    }

    @Override
    public void onResolveBlock(BlockHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        this.chance = calculateChance(spellStats.getAmpMultiplier());
        super.onResolveBlock(rayTraceResult, world, shooter, spellStats, spellContext, resolver);
    }

    public static double calculateChance(double amps) {
        return switch (Double.compare(amps, 0.0)) {
            case 1 -> 1.0 - BASE_CHANCE / Math.pow(2, amps);
            case -1 -> BASE_CHANCE / Math.pow(2, -amps);
            default -> BASE_CHANCE;
        };
    }

    public boolean shouldResolve() {
        return ThreadLocalRandom.current().nextDouble() <= this.chance;
    }

    @NotNull
    @Override
    public Set<AbstractAugment> getCompatibleAugments() {
        return augmentSetOf(AugmentAmplify.INSTANCE, AugmentDampen.INSTANCE);
    }
}
