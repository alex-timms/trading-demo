package ajt.order;

import ajt.common.enums.Side;

public class Order {
    public final long orderId;
    public final int instrumentId;
    public final Side side;
    public final long price;
    public long leavesQty;

    public Order(long orderId, int instrumentId, Side side, long price, long qty) {
        this.orderId = orderId;
        this.instrumentId = instrumentId;
        this.side = side;
        this.price = price;
        this.leavesQty = qty;
    }

    @Override
    public String toString() {
        return String.format(
                "Order: %d (%s) for instrument: %d at price: %d for quantity: %d",
                orderId,
                side,
                instrumentId,
                price,
                leavesQty
        );
    }
}
