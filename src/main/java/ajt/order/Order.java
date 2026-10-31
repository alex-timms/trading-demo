package ajt.order;

import ajt.common.enums.Side;

public class Order {
    public final long orderId;
    public final Side side;
    public final long price;
    public long leavesQty;

    Order(long orderId, Side side, long price, long qty) {
        this.orderId = orderId; this.side = side; this.price = price; this.leavesQty = qty;
    }
}
