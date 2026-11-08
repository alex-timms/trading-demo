package ajt.matcher;

import ajt.order.Order;
import ajt.order.OrderBook;

public interface Matcher {

    void match(Order order, OrderBook book);
}
