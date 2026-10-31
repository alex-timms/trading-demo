package ajt.order.books;

import ajt.common.enums.Side;
import ajt.common.ext.Orders;
import ajt.order.Order;
import ajt.order.OrderBook2;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static ajt.common.enums.Side.BUY;
// ToDo: Remove getters, refine method logic
public class FirstOrderBook implements OrderBook2 {

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
        var resolvedBook = side == BUY ? bids : asks;
        return resolvedBook.firstEntry().getValue().getFirst();
    }

    @Override
    public void add(Order o) {
         (o.side == Side.BUY ? bids : asks)
                .computeIfAbsent(o.price, p -> new Orders()).addLast(o);
        byId.put(o.orderId, o);
    }

    @Override
    public void remove(Order o) {
        Long id = o.orderId;
        Side side = o.side;
        Long price = o.price;
        var resolvedBook = side == BUY ? bids : asks;
        Orders priceOrders = resolvedBook.get(price);
        priceOrders.remove(o);
        byId.remove(id);
    }
}
