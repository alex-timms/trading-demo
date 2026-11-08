package ajt.events.impl;

import ajt.events.EventSink;
import ajt.order.Order;

public class LogEvents implements EventSink {

    @Override
    public void accept(Order order) {
        System.out.printf("Order accepted: %d", order.orderId);
    }

    @Override
    public void fill(Order aggressor, Order resting, long price, long qty) {
        System.out.printf(
                "Order fill: %d (%s) with resting order: %d (%s) at price: %d for quantity: %d%n",
                aggressor.orderId,
                aggressor.side,
                resting.orderId,
                resting.side,
                price,
                qty
        );
    }

    @Override
    public void rest(Order order) {
        System.out.println("Rest Order : " + order);
    }
}
