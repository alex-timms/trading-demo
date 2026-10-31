package ajt.events.impl;

import ajt.events.EventSink;
import ajt.order.Order;

public class LogEvents implements EventSink {

    @Override
    public void accepted(Order order) {
        System.out.println("Order accepted");
    }

    @Override
    public void fill(Order aggressor, Order resting, long price, long qty) {
        System.out.println("Order fill");
    }
}
