package ajt.order.books;

import ajt.common.enums.Side;
import ajt.common.ext.Orders;
import ajt.order.Order;
import ajt.order.OrderBook;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;


// ToDo: Remove getters, refine method logic
public class FirstOrderBook implements OrderBook {

    public TreeMap<Long, Orders> getAsks() {
        return asks;
    }

    public TreeMap<Long, Orders> getBids() {
        return bids;
    }

    public Map<Long, Order> getById() {
        return byId;
    }

    // bids: highest price first;
    private final TreeMap<Long, Orders> bids = new TreeMap<>(Comparator.reverseOrder());
    // asks: lowest price first;
    private final TreeMap<Long, Orders> asks = new TreeMap<>();
    private final Map<Long, Order> byId = new HashMap<>();

    @Override
    public Order peekBest(Side side) {
        return Optional.ofNullable(side)
                .map(this::bookFor)
                .map(TreeMap::firstEntry)
                .map(Map.Entry::getValue)
                .filter(orders -> !orders.isEmpty())
                .map(Orders::getFirst)
                .orElse(null);
    }

    @Override
    public void add(Order o) {
        if (byId.containsKey(o.orderId)) {
            throw new IllegalArgumentException("Duplicate orderId: " + o.orderId);
        }
        byId.put(o.orderId, o);
         bookFor(o.side)
                .computeIfAbsent(o.price, p -> new Orders()).addLast(o);

    }

    @Override
    public void remove(Order o) {
        if (o == null) return;

        // Canonical order state comes from the index, not caller-provided object.
        Order existing = byId.get(o.orderId);
        if (existing == null) return;

        var resolvedBook = bookFor(existing.side);
        Orders priceOrders = resolvedBook.get(existing.price);
        if (priceOrders == null) {
            throw new IllegalStateException("Removed orderId: " + o.orderId + " but was not present in book");
        };
        priceOrders.remove(existing);
        if (priceOrders.isEmpty()) {
            resolvedBook.remove(existing.price);
        }
        byId.remove(o.orderId);
    }

    private TreeMap<Long, Orders> bookFor(Side side) {
        return side == Side.BUY ? bids : asks;
    }
}
