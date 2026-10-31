package ajt.matcher.impl;

import ajt.events.EventSink;
import ajt.matcher.Matcher;
import ajt.order.Order;
import ajt.order.OrderBook2;

public class PriceTimeMatcher implements Matcher {

    // ToDo: Add logic to fill order if possible
    @Override
    public void match(Order order, OrderBook2 book, EventSink sink) {

    }
}
