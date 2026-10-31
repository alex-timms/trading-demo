package ajt.order;

import ajt.common.enums.Side;
import ajt.common.exceptions.NoAskException;
import ajt.common.exceptions.NoBidException;
import ajt.common.ext.Orders;
import ajt.events.EventSink;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public interface OrderBook2 {
    Order peekBest(Side side);               // best resting order on that side, or null
    void add(Order o);                       // rest an order
    void remove(Order o);                    // remove a fully filled or cancelled order


}
