package ajt.validation;

import ajt.validation.impl.CompositeValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CompositeValidatorTest {

    @Test
    public void testValidate_True_WhenAllValidatorsPass() {
        // given
        Validator<Integer> positive = value -> value > 0;
        Validator<Integer> lessThanTen = value -> value < 10;
        Validator<Integer> validator = new CompositeValidator<>(List.of(positive, lessThanTen));

        // when
        var result = validator.validate(5);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testValidate_False_WhenAnyValidatorFails() {
        // given
        Validator<Integer> positive = value -> value > 0;
        Validator<Integer> lessThanTen = value -> value < 10;
        Validator<Integer> validator = new CompositeValidator<>(List.of(positive, lessThanTen));

        // when
        var result = validator.validate(15);

        // then
        assertThat(result).isFalse();
    }
}

