package ajt.engine;

import ajt.common.enums.Side;

public class Order {
    final long orderId;
    final Side side;
    final long price;
    long leavesQty;

    Order(long orderId, Side side, long price, long qty) {
        this.orderId = orderId; this.side = side; this.price = price; this.leavesQty = qty;
    }
}
