package ajt.matcher;

import ajt.common.enums.Side;
import ajt.events.EventSink;
import ajt.matcher.impl.PriceTimeMatcher;
import ajt.order.Order;
import ajt.order.books.OrderBookImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PriceTimeMatcherTest {

    Matcher matcher;

    EventSink sink = Mockito.mock(EventSink.class);

    ajt.order.OrderBook book = new OrderBookImpl();

    final Order ask1 = new Order(1, 1, Side.SELL, 10852L, 1_000_000);
    final Order ask2 = new Order(2, 1, Side.SELL, 10855L, 2_000_000);
    final Order ask3 = new Order(3, 1, Side.SELL, 10855L, 500_000);
    final Order bid1 = new Order(4, 1, Side.BUY, 10848L, 1_000_000);

    @BeforeEach
    public void setup() {
        matcher = new PriceTimeMatcher(sink);
        book.add(ask1);
        book.add(ask2);
        book.add(ask3);
        book.add(bid1);
    }

    @Test
    public void testMatch_FullBuyOrder() {
        // given
        Order bid = new Order(5, 1, Side.BUY, 10853L, 1500000);

        // when
        matcher.match(bid, book);

        // then
        verify(sink, times(1)).accept(bid);
        verify(sink, times(1)).fill(bid, ask1, 10852L, 1000000);
    }

    @Test
    public void testOrderBook_FullFill_Buy() {
        // given
        Order bid = new Order(5, 1, Side.BUY, 10853L, 1000000);

        // when
        matcher.match(bid, book);

        // then
        verify(sink, times(1)).accept(bid);
        verify(sink, times(1)).fill(bid, ask1, 10852L, 1000000);
    }

    @Test
    public void testOrderBook_NoFill_Buy() {
        // given
        Order bid = new Order(5, 1, Side.BUY, 10851L, 1500000);

        // when
        matcher.match(bid, book);

        // then
        verify(sink, times(1)).accept(bid);
        verify(sink, times(0)).fill(any(Order.class), any(Order.class), any(Long.class), any(Long.class));
    }

    @Test
    public void testOrderBook_FullFill_MultipleAsks_Buy() {
        // given
        Order bid = new Order(5, 1, Side.BUY, 10855L, 1500000);

        // when
        matcher.match(bid, book);

        // then
        verify(sink, times(1)).accept(bid);
        verify(sink, times(1)).fill(bid, ask1, 10852L, 1000000);
        verify(sink, times(1)).fill(bid, ask2, 10855L, 500000);
        verify(sink, times(0)).fill(eq(bid), eq(ask3), any(Long.class), any(Long.class));
    }

    @Test
    public void testOrderBook_PartialFill_Sell() {
        // given
        Order ask = new Order(5, 1, Side.SELL, 10847L, 500_000);

        // when
        matcher.match(ask, book);

        // then
        verify(sink, times(1)).accept(ask);
        verify(sink, times(1)).fill(ask, bid1, 10848L, 500_000);
    }

    @Test
    public void testOrderBook_NoFill_Sell() {
        // given
        Order ask = new Order(5, 1, Side.SELL, 10850L, 500_000);

        // when
        matcher.match(ask, book);

        // then
        verify(sink, times(1)).accept(ask);
        verify(sink, times(0)).fill(eq(ask), eq(bid1), any(Long.class), any(Long.class));
    }
}
