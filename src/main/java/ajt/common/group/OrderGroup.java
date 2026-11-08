package ajt.common.group;

import ajt.order.Order;
import ajt.order.OrderBook;

public record OrderGroup(Order order, OrderBook book) {
}
