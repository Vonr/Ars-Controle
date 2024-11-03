package dev.qther.ars_controle.spell.filter;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.ModNames;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.booleans.Boolean2BooleanFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterUnary extends AbstractFilter implements IAdaptiveFilter {
    public static final FilterUnary NOT = new FilterUnary(ModNames.GLYPH_FILTER_NOT, "FilterNot", (a) -> !a);

    private final Boolean2BooleanFunction op;
    public SpellResolver res;

    private FilterUnary(String id, String desc, Boolean2BooleanFunction op) {
        super(ArsControle.prefix(id), desc);
        this.op = op;
    }

    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        this.res = resolver;
        super.onResolveEntity(rayTraceResult, world, shooter, spellStats, spellContext, resolver);
    }

    @Override
    public void onResolveBlock(BlockHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        this.res = resolver;
        super.onResolveBlock(rayTraceResult, world, shooter, spellStats, spellContext, resolver);
    }

    @Override
    public boolean shouldResolveOnBlock(BlockHitResult target, Level level) {
        if (res == null || level.isClientSide) {
            return false;
        }

        var f = this.getFilter();
        if (f == null) {
            return false;
        }
        var filter = f.second();

        var idx = this.res.spellContext.getCurrentIndex();
        this.res.spellContext.setCurrentIndex(idx + f.first());

        try {
            return op.get(filter.shouldResolveOnBlock(target, level));
        } catch (Exception e) {
            if (res.spellContext.getUnwrappedCaster() instanceof Player player) {
                PortUtil.sendMessageNoSpam(player, Component.translatable("ars_controle.glyph.error.generic.error_at_position", Component.translatable(this.getLocalizationKey()), idx));
            }
            ArsControle.LOGGER.error("Failed to resolve binary filter", e);
            return false;
        }
    }

    @Override
    public boolean shouldResolveOnEntity(EntityHitResult target, Level level) {
        if (res == null || level.isClientSide) {
            return false;
        }

        var f = this.getFilter();
        if (f == null) {
            return false;
        }
        var filter = f.second();

        var idx = this.res.spellContext.getCurrentIndex();
        this.res.spellContext.setCurrentIndex(idx + f.first());

        try {
            return op.get(filter.shouldResolveOnEntity(target, level));
        } catch (Exception e) {
            if (res.spellContext.getUnwrappedCaster() instanceof Player player) {
                PortUtil.sendMessageNoSpam(player, Component.translatable("ars_controle.glyph.error.generic.error_at_position", Component.translatable(this.getLocalizationKey()), idx));
            }
            ArsControle.LOGGER.error("Failed to resolve binary filter", e);
            return false;
        }
    }

    @Nullable
    public Pair<Integer, IFilter> getFilter() {
        var idx = this.res.spellContext.getCurrentIndex();
        var spell = this.res.spell;
        var caster = res.spellContext.getUnwrappedCaster();

        if (spell.size() < idx + 1) {
            return null;
        }

        var next = spell.get(idx);
        if (!(next instanceof IFilter afst)) {
            return null;
        }

        var firstAugments = spell.getAugments(idx, caster);
        var skip = firstAugments.size() + 1;

        if (next instanceof FilterYLevel ynext) {
            ynext.y = (int) (this.res.spellContext.getCaster().getPosition().y - 1);
        }

        if (next instanceof FilterRandom rnext) {
            var newCtx = res.spellContext.makeChildContext();
            var newRes = res.getNewResolver(newCtx);
            var stats = new SpellStats.Builder()
                    .setAugments(firstAugments)
                    .addItemsFromEntity(caster)
                    .build(rnext, newRes.hitResult, caster.level(), caster, newCtx);
            rnext.chance = FilterRandom.calculateChance(stats.getAmpMultiplier());
        }

        return Pair.of(skip, afst);
    }
}
