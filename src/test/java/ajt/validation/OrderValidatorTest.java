package ajt.validation;

import ajt.common.enums.Side;
import ajt.order.Order;
import ajt.validation.impl.OrderValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderValidatorTest {

    Validator<Order> validator = new OrderValidator();

    @Test
    public void testValidate_True() {
        // given
        Order order = new Order(1L, 1, Side.BUY, 100_000, 1_000);

        // when
        var result = validator.validate(order);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testValidate_False_NullOrder() {
        // when
        var result = validator.validate(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testValidate_False_InvalidPrice() {
        // given
        Order order = new Order(1L, 1, Side.BUY, 0, 1_000);

        // when
        var result = validator.validate(order);

        // then
        assertThat(result).isFalse();
    }
}

