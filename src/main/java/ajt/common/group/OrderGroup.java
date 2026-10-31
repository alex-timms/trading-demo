package ajt.common.group;

import ajt.events.EventSink;
import ajt.order.Order;
import ajt.order.OrderBook2;

public record OrderGroup(Order order, OrderBook2 book, EventSink sink) {
}
