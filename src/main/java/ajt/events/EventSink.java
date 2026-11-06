package ajt.events;

import ajt.order.Order;

public interface EventSink {
    void accept(Order order);
    void rest(Order order);
    void fill(Order aggressor, Order resting, long price, long qty);
}
