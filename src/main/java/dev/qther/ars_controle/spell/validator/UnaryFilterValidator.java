package dev.qther.ars_controle.spell.validator;

import com.hollingsworth.arsnouveau.api.spell.AbstractFilter;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.api.spell.ISpellValidator;
import com.hollingsworth.arsnouveau.api.spell.SpellValidationError;
import com.hollingsworth.arsnouveau.common.spell.validation.BaseSpellValidationError;
import com.hollingsworth.arsnouveau.common.spell.validation.ScanningSpellValidator;
import dev.qther.ars_controle.spell.filter.IAdaptiveFilter;
import dev.qther.ars_controle.spell.filter.FilterUnary;

import javax.annotation.Nullable;
import java.util.List;

public class UnaryFilterValidator extends ScanningSpellValidator<UnaryFilterValidator.Context> {
    public static final ISpellValidator INSTANCE = new UnaryFilterValidator();

    @Override
    protected UnaryFilterValidator.Context initContext() {
        return new UnaryFilterValidator.Context();
    }

    @Override
    protected void digestSpellPart(UnaryFilterValidator.Context context, int position, AbstractSpellPart spellPart, List<SpellValidationError> validationErrors) {
        switch (context.state) {
            case LOOKING -> {
                if (spellPart instanceof FilterUnary filter) {
                    context.filter = filter;
                    context.state = State.FOUND_UNARY_FILTER;
                }
            }
            case FOUND_UNARY_FILTER -> {
                if (spellPart instanceof IAdaptiveFilter) {
                    validationErrors.add(new NoChainingAdaptiveFilters(position, context.filter));
                    return;
                }

                if (!(spellPart instanceof AbstractFilter)) {
                    validationErrors.add(new InvalidUnaryFilter(position, context.filter));
                }
                context.state = State.LOOKING;
            }
        }
    }

    public static class Context {
        @Nullable FilterUnary filter;
        State state = State.LOOKING;
    }

    enum State {
        LOOKING,
        FOUND_UNARY_FILTER,
    }

    private static class InvalidUnaryFilter extends BaseSpellValidationError {
        public InvalidUnaryFilter(int position, AbstractSpellPart spellPart) {
            super(position, spellPart, "unary_filters.next_not_filter", spellPart);
        }
    }

    private static class NoChainingAdaptiveFilters extends BaseSpellValidationError {
        public NoChainingAdaptiveFilters(int position, AbstractSpellPart spellPart) {
            super(position, spellPart, "unary_filters.no_chaining", spellPart);
        }
    }
}
