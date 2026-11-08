package ajt.matcher.impl;

import ajt.common.enums.Side;
import ajt.events.EventSink;
import ajt.matcher.Matcher;
import ajt.order.Order;
import ajt.order.OrderBook2;

public class PriceTimeMatcher implements Matcher {

    private final EventSink sink;

    public PriceTimeMatcher(EventSink sink){
        this.sink = sink;
    }

    @Override
    public void match(Order order, OrderBook2 book) {
        Order resting;
        Side opposite = order.side == Side.BUY ? Side.SELL : Side.BUY;
        sink.accept(order);
        while (
                order.leavesQty > 0
                && (resting = book.peekBest(opposite)) != null
                && priceOverlap(order, resting.price)
        ) {
            long qty = Math.min(order.leavesQty, resting.leavesQty); // orders to fill at resting price
            order.leavesQty -= qty;                                  // qty left to fill from order
            resting.leavesQty -= qty;                                // qty left from resting
            sink.fill(order, resting, resting.price, qty);           // trade at the resting price
            if (resting.leavesQty == 0) book.remove(resting);        // used full rest, remove from order book
        }
        if (order.leavesQty > 0) {          // Every suitable opposite used and order quantity remaining
            sink.rest(order);               // resting order is partially filled, add remaining from book
            book.add(order);                // an IOC order would be cancelled here instead
        }
    }

    private boolean priceOverlap(Order in, long restingPrice) {
        return in.side == Side.BUY ? in.price >= restingPrice : in.price <= restingPrice;
    }
}
