package ajt.engine;

import ajt.common.enums.Side;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class OrderBook {
    // bids: highest price first;
    private final TreeMap<Long, ArrayDeque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    // asks: lowest price first;
    private final TreeMap<Long, ArrayDeque<Order>> asks = new TreeMap<>();
    private final Map<Long, Order> byId = new HashMap<>();
    private final EventSink sink;

    OrderBook(EventSink sink) { this.sink = sink; }

    void submitLimit(Order incoming) {
        sink.accepted(incoming);
        TreeMap<Long, ArrayDeque<Order>> opposite = incoming.side == Side.BUY ? asks : bids;

        while (incoming.leavesQty > 0 && !opposite.isEmpty()) {
            // firstEntry() is always the best level
            Map.Entry<Long, ArrayDeque<Order>> level = opposite.firstEntry();
            long levelPrice = level.getKey();
            if (!crosses(incoming, levelPrice)) break;

            ArrayDeque<Order> queue = level.getValue();
            Order resting = queue.peekFirst();
            long qty = Math.min(incoming.leavesQty, resting.leavesQty);

            incoming.leavesQty -= qty;
            resting.leavesQty -= qty;
            sink.fill(incoming, resting, levelPrice, qty);   // trade at the RESTING price

            if (resting.leavesQty == 0) {
                queue.pollFirst();
                byId.remove(resting.orderId);
                if (queue.isEmpty()) opposite.pollFirstEntry();
            }
        }
        if (incoming.leavesQty > 0) rest(incoming);          // leftover goes into the book
    }

    private boolean crosses(Order in, long restingPrice) {
        return in.side == Side.BUY ? in.price >= restingPrice : in.price <= restingPrice;
    }

    private void rest(Order o) {
        (o.side == Side.BUY ? bids : asks)
                .computeIfAbsent(o.price, p -> new ArrayDeque<>()).addLast(o);
        byId.put(o.orderId, o);
    }
}
