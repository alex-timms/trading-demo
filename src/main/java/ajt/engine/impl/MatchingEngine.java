package ajt.engine.impl;

import ajt.common.group.OrderGroup;
import ajt.engine.Engine;
import ajt.events.EventSink;
import ajt.matcher.Matcher;
import ajt.order.Order;
import ajt.order.OrderBook2;
import ajt.order.books.FirstOrderBook;
import ajt.serialization.Deserializer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MatchingEngine implements Engine {

    private final Map<Integer, OrderBook2> books = new HashMap<>();

    Deserializer<Order> deserializer;
    Matcher matcher;
    EventSink eventSink;

    public MatchingEngine(Deserializer<Order> deserializer, Matcher matcher, EventSink eventSink, int[] ids) {
        this.deserializer = deserializer;
        this.matcher = matcher;
        this.eventSink = eventSink;
        for (int i : ids) {
            books.put(i, new FirstOrderBook());
        }
    }

    @Override
    public void onNewOrder(ByteBuffer cmd) {
        // 1. stateful pre-trade risk checks; reject and return if they fail
        // 2. look up the OrderBook for cmd.instrumentId
        // 3. matcher.match(order, book, sink)
        // 4. events go to the sink, which publishes them to the Aeron "events" stream
        OrderGroup og = Optional.of(cmd)
                .map(deserializer::deserialize)
                .map(order -> {
                    OrderBook2 book = resolvedOrderBook(order);
                    return new OrderGroup(order, book, eventSink);
                })
                .get();
        matcher.match(og.order(), og.book(), og.sink());

    }

    private OrderBook2 resolvedOrderBook(Order order) {
        return books.get(order.instrumentId);
    }
}
