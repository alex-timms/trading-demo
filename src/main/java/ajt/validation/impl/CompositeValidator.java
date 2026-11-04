package ajt.validation.impl;

import ajt.validation.Validator;

import java.util.List;

public class CompositeValidator<T> implements Validator<T> {

    private final List<Validator<T>> validators;

    public CompositeValidator(List<Validator<T>> validators) {
        this.validators = List.copyOf(validators);
    }

    @Override
    public boolean validate(T t) {
        for (Validator<T> validator : validators) {
            if (!validator.validate(t)) {
                return false;
            }
        }
        return true;
    }
}
