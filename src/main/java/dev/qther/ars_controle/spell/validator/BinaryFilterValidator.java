package dev.qther.ars_controle.spell.validator;

import com.hollingsworth.arsnouveau.api.item.ISpellModifier;
import com.hollingsworth.arsnouveau.api.spell.AbstractFilter;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.api.spell.ISpellValidator;
import com.hollingsworth.arsnouveau.api.spell.SpellValidationError;
import com.hollingsworth.arsnouveau.common.spell.validation.BaseSpellValidationError;
import com.hollingsworth.arsnouveau.common.spell.validation.ScanningSpellValidator;
import dev.qther.ars_controle.spell.filter.IAdaptiveFilter;
import dev.qther.ars_controle.spell.filter.FilterBinary;

import javax.annotation.Nullable;
import java.util.List;

public class BinaryFilterValidator extends ScanningSpellValidator<BinaryFilterValidator.Context> {
    public static final ISpellValidator INSTANCE = new BinaryFilterValidator();

    @Override
    protected BinaryFilterValidator.Context initContext() {
        return new BinaryFilterValidator.Context();
    }

    @Override
    protected void digestSpellPart(BinaryFilterValidator.Context context, int position, AbstractSpellPart spellPart, List<SpellValidationError> validationErrors) {
        switch (context.state) {
            case LOOKING -> {
                if (spellPart instanceof FilterBinary filter) {
                    context.filter = filter;
                    context.state = State.FOUND_BINARY_FILTER;
                }
            }
            case FOUND_BINARY_FILTER -> {
                if (spellPart instanceof IAdaptiveFilter) {
                    validationErrors.add(new NoChainingAdaptiveFilters(position, context.filter));
                    return;
                }

                if (spellPart instanceof AbstractFilter) {
                    context.state = State.FOUND_FIRST;
                } else {
                    validationErrors.add(new InvalidBinaryFilter(position, context.filter));
                    context.state = State.LOOKING;
                }
            }
            case FOUND_FIRST -> {
                if (spellPart instanceof IAdaptiveFilter) {
                    validationErrors.add(new NoChainingAdaptiveFilters(position, context.filter));
                    return;
                }

                if (spellPart instanceof ISpellModifier) {
                    return;
                }

                if (!(spellPart instanceof AbstractFilter)) {
                    validationErrors.add(new InvalidBinaryFilter(position, context.filter));
                }
                context.state = State.LOOKING;
            }
        }
    }

    public static class Context {
        @Nullable FilterBinary filter;
        State state = State.LOOKING;
    }

    enum State {
        LOOKING,
        FOUND_BINARY_FILTER,
        FOUND_FIRST,
    }

    private static class InvalidBinaryFilter extends BaseSpellValidationError {
        public InvalidBinaryFilter(int position, AbstractSpellPart spellPart) {
            super(position, spellPart, "binary_filters.next_two_not_filters", spellPart);
        }
    }

    private static class NoChainingAdaptiveFilters extends BaseSpellValidationError {
        public NoChainingAdaptiveFilters(int position, AbstractSpellPart spellPart) {
            super(position, spellPart, "binary_filters.no_chaining", spellPart);
        }
    }
}
