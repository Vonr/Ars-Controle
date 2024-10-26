package dev.qther.ars_controle.spell.validator;

import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.api.spell.IFilter;
import com.hollingsworth.arsnouveau.api.spell.ISpellValidator;
import com.hollingsworth.arsnouveau.api.spell.SpellValidationError;
import com.hollingsworth.arsnouveau.common.spell.validation.BaseSpellValidationError;
import com.hollingsworth.arsnouveau.common.spell.validation.ScanningSpellValidator;
import dev.qther.ars_controle.spell.filter.FilterBinary;

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
                if (spellPart instanceof FilterBinary) {
                    context.state = State.FOUND_BINARY_FILTER;
                }
            }
            case FOUND_BINARY_FILTER -> {
                if (spellPart instanceof FilterBinary) {
                    validationErrors.add(new NoChainingBinaryFilters(position, spellPart));
                    return;
                }

                if (spellPart instanceof IFilter) {
                    context.state = State.FOUND_FIRST;
                } else {
                    validationErrors.add(new InvalidBinaryFilter(position, spellPart));
                    context.state = State.LOOKING;
                }
            }
            case FOUND_FIRST -> {
                if (spellPart instanceof FilterBinary) {
                    validationErrors.add(new NoChainingBinaryFilters(position, spellPart));
                    return;
                }

                if (!(spellPart instanceof IFilter)) {
                    validationErrors.add(new InvalidBinaryFilter(position, spellPart));
                }
                context.state = State.LOOKING;
            }
        }
    }

    public static class Context {
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

    private static class NoChainingBinaryFilters extends BaseSpellValidationError {
        public NoChainingBinaryFilters(int position, AbstractSpellPart spellPart) {
            super(position, spellPart, "binary_filters.no_chaining", spellPart);
        }
    }
}
