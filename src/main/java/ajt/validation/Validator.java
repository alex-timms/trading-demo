package ajt.validation;

import ajt.order.Order;

public interface Validator {

    boolean validate(Order order);
}
