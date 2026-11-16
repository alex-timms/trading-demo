package ajt.validation.impl;

import ajt.order.Order;
import ajt.validation.Validator;
import org.agrona.collections.LongHashSet;

public class DuplicateOrderIdValidator implements Validator<Order> {

    private final LongHashSet seenIds;

    public DuplicateOrderIdValidator(int expectedOrders) {
        this.seenIds = new LongHashSet(expectedOrders);   // pre-sized
    }

    @Override
    public boolean validate(Order order) {
        return seenIds.add(order.orderId);   // true if new, false if already seen
    }
}
