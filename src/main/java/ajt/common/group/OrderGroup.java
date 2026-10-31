package ajt.common.group;

import ajt.order.Order;
import ajt.order.OrderBook2;

public record OrderGroup(Order order, OrderBook2 book) {
}
