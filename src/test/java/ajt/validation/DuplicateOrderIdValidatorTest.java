package ajt.validation;

import ajt.common.enums.Side;
import ajt.order.Order;
import ajt.validation.impl.CompositeValidator;
import ajt.validation.impl.DuplicateOrderIdValidator;
import ajt.validation.impl.RiskValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DuplicateOrderIdValidatorTest {

    private static final int INITIAL_CAPACITY = 10_000;

    private static Order order(long id) {
        return new Order(id, 1, Side.BUY, 10_000, 100);
    }

    @Test
    public void testValidate_True_WhenOrderIdIsNew() {
        // given
        var validator = new DuplicateOrderIdValidator(INITIAL_CAPACITY);

        // when
        var result = validator.validate(order(1));

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testValidate_False_WhenOrderIdAlreadySeen() {
        // given
        var validator = new DuplicateOrderIdValidator(INITIAL_CAPACITY);
        validator.validate(order(1));

        // when
        var result = validator.validate(order(1));

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testValidate_True_WhenOrderIdsAreDifferent() {
        // given
        var validator = new DuplicateOrderIdValidator(INITIAL_CAPACITY);

        // when
        var first = validator.validate(order(1));
        var second = validator.validate(order(2));

        // then
        assertThat(first).isTrue();
        assertThat(second).isTrue();
    }

    @Test
    public void testValidate_False_WhenSameIdHasDifferentSideQuantityAndPrice() {
        // given
        var validator = new DuplicateOrderIdValidator(INITIAL_CAPACITY);
        validator.validate(new Order(7, 1, Side.BUY, 10_000, 100));

        // when
        var result = validator.validate(new Order(7, 1, Side.SELL, 12_000, 250));

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testValidate_False_ForEveryRepeatedId_WhenMoreIdsThanInitialCapacity() {
        // given: twice as many IDs as the initial capacity, so the set has to grow
        var validator = new DuplicateOrderIdValidator(INITIAL_CAPACITY);
        int count = INITIAL_CAPACITY * 2;
        for (long id = 1; id <= count; id++) {
            assertThat(validator.validate(order(id))).isTrue();
        }

        // when / then: every ID is still remembered after resizing
        for (long id = 1; id <= count; id++) {
            assertThat(validator.validate(order(id)))
                    .as("repeat of order id %d", id)
                    .isFalse();
        }
    }

    @Test
    public void testValidate_False_ForRejectedOrderId_WhenDedupeRunsBeforeOtherValidators() {
        // Documents the "dedupe runs first" decision: an ID is used up as soon as it is received,
        // even if a later validator rejects the order.

        // given: dedupe first, then the risk check (quantity must be under 500,000)
        Validator<Order> validator = new CompositeValidator<>(List.of(
                new DuplicateOrderIdValidator(INITIAL_CAPACITY),
                new RiskValidator()
        ));
        var tooLarge = new Order(1, 1, Side.BUY, 10_000, 500_000);
        var corrected = new Order(1, 1, Side.BUY, 10_000, 100);

        // when
        var firstAttempt = validator.validate(tooLarge);
        var resubmission = validator.validate(corrected);

        // then
        assertThat(firstAttempt).isFalse();   // rejected by the risk check
        assertThat(resubmission).isFalse();   // same ID, rejected as a duplicate
    }
}

