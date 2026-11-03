package ajt.validation;


import ajt.common.enums.Side;
import ajt.order.Order;
import ajt.validation.impl.RiskValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RiskValidatorTest {

    Validator<Order> validator = new RiskValidator();

    @Test
    public void testValidate_True() {
        // given
        Order order = new Order(1L, 1, Side.SELL, 100_000, 1000);

        // when
        var result = validator.validate(order);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testValidate_False() {
        // given
        Order order = new Order(1L, 1, Side.SELL, 100_000, 1_000_000);

        // when
        var result = validator.validate(order);

        // then
        assertThat(result).isFalse();
    }
}
