package ajt.order;

import ajt.common.enums.Side;

public class Order {
    public final long orderId;
    public final int instrumentId;
    public final Side side;
    /**
     * Price as a scaled integer (fixed-point), not a decimal.
     * The stored value is the real price multiplied by 10,000, so the last four digits are the decimal places:
     * A long is used because decimal prices such as 0.1 cannot be stored exactly in a double, and the matcher
     * needs exact comparisons and exact price-level keys.
     */
    public final long price;
    private long leavesQty;

    public Order(long orderId, int instrumentId, Side side, long price, long qty) {
        this.orderId = orderId;
        this.instrumentId = instrumentId;
        this.side = side;
        this.price = price;
        this.leavesQty = qty;
    }

    public long leavesQty() { return leavesQty; }

    /** Reduces the remaining quantity by a fill. Intended for the matcher only. */
    public void fill(long qty) {
        if (qty <= 0 || qty > leavesQty) {
            throw new IllegalArgumentException("Invalid fill " + qty + " for leavesQty " + leavesQty);
        }
        leavesQty -= qty;
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
