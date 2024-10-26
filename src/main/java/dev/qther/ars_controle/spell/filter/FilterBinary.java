package dev.qther.ars_controle.spell.filter;

import com.hollingsworth.arsnouveau.api.spell.*;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.ModNames;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class FilterBinary extends AbstractFilter {
    public static final FilterBinary OR = new FilterBinary(ModNames.GLYPH_FILTER_OR, "FilterOr", (a, b) -> a.get() || b.get());
    public static final FilterBinary XOR = new FilterBinary(ModNames.GLYPH_FILTER_XOR, "FilterXor", (a, b) -> a.get() != b.get());
    public static final FilterBinary XNOR = new FilterBinary(ModNames.GLYPH_FILTER_XNOR, "FilterXnor", (a, b) -> a.get() == b.get());

    private final BiFunction<Supplier<Boolean>, Supplier<Boolean>, Boolean> op;
    public SpellResolver res;

    private FilterBinary(String id, String desc, BiFunction<Supplier<Boolean>, Supplier<Boolean>, Boolean> op) {
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
        if (level.isClientSide) {
            return false;
        }

        var filters = this.getFilters();
        if (filters == null) {
            return false;
        }

        this.res.spellContext.setCurrentIndex(this.res.spellContext.getCurrentIndex() + 2);
        return op.apply(() -> filters.first().shouldResolveOnBlock(target, level), () -> filters.second().shouldResolveOnBlock(target, level));
    }

    @Override
    public boolean shouldResolveOnEntity(EntityHitResult target, Level level) {
        var filters = this.getFilters();
        if (filters == null) {
            return false;
        }

        this.res.spellContext.setCurrentIndex(this.res.spellContext.getCurrentIndex() + 2);
        return op.apply(() -> filters.first().shouldResolveOnEntity(target, level), () -> filters.second().shouldResolveOnEntity(target, level));
    }

    @Nullable
    public Pair<IFilter, IFilter> getFilters() {
        var idx = this.res.spellContext.getCurrentIndex();
        var spell = this.res.spell;

        if (spell.size() < idx + 2) {
            return null;
        }

        var fst = spell.get(idx);
        if (!(fst instanceof IFilter afst)) {
            return null;
        }

        var snd = spell.get(idx + 1);
        if (!(snd instanceof IFilter asnd)) {
            return null;
        }

        if (fst instanceof FilterYLevel yfst) {
            yfst.y = (int) (this.res.spellContext.getCaster().getPosition().y - 1);
        }
        if (snd instanceof FilterYLevel ysnd) {
            ysnd.y = (int) (this.res.spellContext.getCaster().getPosition().y - 1);
        }

        return Pair.of(afst, asnd);
    }
}
