package ajt.validation.impl;

import ajt.order.Order;
import ajt.validation.Validator;


public class RiskValidator implements Validator<Order> {
    @Override
    public boolean validate(Order order) {
        return order.leavesQty < 500_000; // can only trade less than 500,000 units at a time
    }
}
