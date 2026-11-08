package ajt.order;

import ajt.common.enums.Side;

public interface OrderBook {
    Order peekBest(Side side);               // best resting order on that side, or null
    void add(Order o);                       // rest an order
    void remove(Order o);                    // remove a fully filled or cancelled order


}
