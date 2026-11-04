package ajt.validation.impl;

import ajt.order.Order;
import ajt.validation.Validator;

public class OrderValidator implements Validator<Order> {

    @Override
    public boolean validate(Order order) {
        return order != null
                && order.orderId > 0
                && order.instrumentId > 0
                && order.side != null
                && order.price > 0
                && order.leavesQty > 0;
    }
}

