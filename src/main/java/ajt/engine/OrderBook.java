package ajt.engine;

import ajt.common.enums.Side;
import ajt.common.exceptions.NoAskException;
import ajt.common.exceptions.NoBidException;
import ajt.common.ext.Orders;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class OrderBook {
    // bids: highest price first;
    private final TreeMap<Long, Orders> bids = new TreeMap<>(Comparator.reverseOrder());
    // asks: lowest price first;
    private final TreeMap<Long, Orders> asks = new TreeMap<>();
    private final Map<Long, Order> byId = new HashMap<>();
    private final EventSink sink;

    OrderBook(EventSink sink) { this.sink = sink; }

    void submitLimit(Order incoming) {
        sink.accepted(incoming);
        TreeMap<Long, Orders> opposite = incoming.side == Side.BUY ? asks : bids;

        while (incoming.leavesQty > 0 && !opposite.isEmpty()) {
            // firstEntry() is always the best level
            Map.Entry<Long, Orders> level = opposite.firstEntry();
            long levelPrice = level.getKey();
            if (!priceOverlap(incoming, levelPrice)) break;

            Orders ordersQueue = level.getValue();
            Order restingOrder = ordersQueue.peekFirst();
            long qty = Math.min(incoming.leavesQty, restingOrder.leavesQty);

            incoming.leavesQty -= qty;
            restingOrder.leavesQty -= qty;
            sink.fill(incoming, restingOrder, levelPrice, qty);   // trade at the RESTING price

            if (restingOrder.leavesQty == 0) { // whole order used
                ordersQueue.pollFirst();
                byId.remove(restingOrder.orderId);
                if (ordersQueue.isEmpty()) opposite.pollFirstEntry(); // price no longer available
            }
        }
        if (incoming.leavesQty > 0) rest(incoming);          // leftover goes into the book
    }

    private boolean priceOverlap(Order in, long restingPrice) {
        return in.side == Side.BUY ? in.price >= restingPrice : in.price <= restingPrice;
    }

    private void rest(Order o) {
        (o.side == Side.BUY ? bids : asks)
                .computeIfAbsent(o.price, p -> new Orders()).addLast(o);
        byId.put(o.orderId, o);
    }

    public Long getSpread() throws Exception {
        long maxBid = Optional.of(bids)
                .map(TreeMap::firstEntry)
                .map(Map.Entry::getKey)
                .orElseThrow(NoBidException::new);
        long minAsk = Optional.of(asks)
                .map(TreeMap::firstEntry)
                .map(Map.Entry::getKey)
                .orElseThrow(NoAskException::new);
        return maxBid - minAsk;
    }
}
