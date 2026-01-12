package dev.qther.ars_controle.spell.validator;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.validation.BaseSpellValidationError;
import com.hollingsworth.arsnouveau.common.spell.validation.ScanningSpellValidator;
import dev.qther.ars_controle.spell.filter.IAdaptiveFilter;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectIntMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ArrayListDeque;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AdaptiveFilterValidator extends ScanningSpellValidator<AdaptiveFilterValidator.Context> {
    public static final ISpellValidator INSTANCE = new AdaptiveFilterValidator();

    @Override
    protected AdaptiveFilterValidator.Context initContext() {
        return new AdaptiveFilterValidator.Context();
    }

    @Override
    protected void digestSpellPart(AdaptiveFilterValidator.Context context, int position, AbstractSpellPart spellPart, List<SpellValidationError> validationErrors) {
        if (spellPart instanceof IAdaptiveFilter adaptiveFilter) {
            var operands = adaptiveFilter.operands();
            if (context.expecting > 0) {
                context.expecting -= 1;
            }
            context.expecting += operands;
            context.adaptiveFilters.add(ObjectIntMutablePair.of(spellPart, operands));
            return;
        }

        if (context.expecting <= 0) {
            return;
        }

        if (spellPart instanceof IFilter) {
            context.expecting -= 1;

            if (!context.adaptiveFilters.isEmpty()) {
                var last = context.adaptiveFilters.getLast();
                var lastExpecting = last.secondInt() - 1;
                last.second(lastExpecting);
                if (lastExpecting <= 0) {
                    context.adaptiveFilters.removeLast();
                }
            }

            return;
        }

        var last = context.adaptiveFilters.getLast();
        validationErrors.add(new StillExpectingFiltersError(position, last.first(), last.secondInt()));
    }

    public static class Context {
        List<ObjectIntMutablePair<AbstractSpellPart>> adaptiveFilters = new ArrayList<>();
        int expecting = 0;
    }

    private static class StillExpectingFiltersError extends BaseSpellValidationError {
        private StillExpectingFiltersError(int position, AbstractSpellPart spellPart, int expecting) {
            super(position, spellPart, "adaptive_filters.still_expecting_filters", Component.translatable(spellPart.getLocalizationKey()), Component.literal(Integer.toString(expecting)));
        }
    }
}
