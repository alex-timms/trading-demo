package ajt.matcher;

import ajt.events.EventSink;
import ajt.order.Order;
import ajt.order.OrderBook2;

public interface Matcher {

    void match(Order order, OrderBook2 book);
}
