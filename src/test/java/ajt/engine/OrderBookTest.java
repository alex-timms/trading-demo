package ajt.engine;

import ajt.common.enums.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OrderBookTest {

    final Order ask1 = new Order(1, Side.SELL, 10852L, 1_000_000);
    final Order ask2 = new Order(2, Side.SELL, 10855L, 2_000_000);
    final Order ask3 = new Order(3, Side.SELL, 10855L, 500_000);
    final Order bid1 = new Order(4, Side.BUY, 10848L, 1_000_000);

    EventSink sink = Mockito.mock(EventSink.class);

    OrderBook book = new OrderBook(sink);

    @BeforeEach
    void setUp() {
        book.submitLimit(ask1);
        book.submitLimit(ask2);
        book.submitLimit(ask3);
        book.submitLimit(bid1);
    }

    @Test
    public void testOrderBook_PartialFill_Buy() {
        // given
        Order bid = new Order(5, Side.BUY, 10853L, 1500000);

        // when
        book.submitLimit(bid);

        // then
        verify(sink, times(1)).accepted(bid);
        verify(sink, times(1)).fill(bid, ask1, 10852L, 1000000);
    }

    @Test
    public void testOrderBook_FullFill_Buy() {
        // given
        Order bid = new Order(5, Side.BUY, 10853L, 1000000);

        // when
        book.submitLimit(bid);

        // then
        verify(sink, times(1)).accepted(bid);
        verify(sink, times(1)).fill(bid, ask1, 10852L, 1000000);
    }

    @Test
    public void testOrderBook_NoFill_Buy() {
        // given
        Order bid = new Order(5, Side.BUY, 10851L, 1500000);

        // when
        book.submitLimit(bid);

        // then
        verify(sink, times(1)).accepted(bid);
        verify(sink, times(0)).fill(any(Order.class), any(Order.class), any(Long.class), any(Long.class));
    }

    @Test
    public void testOrderBook_FullFill_MultipleAsks_Buy() {
        // given
        Order bid = new Order(5, Side.BUY, 10855L, 1500000);

        // when
        book.submitLimit(bid);

        // then
        verify(sink, times(1)).accepted(bid);
        verify(sink, times(1)).fill(bid, ask1, 10852L, 1000000);
        verify(sink, times(1)).fill(bid, ask2, 10855L, 500000);
        verify(sink, times(0)).fill(eq(bid), eq(ask3), any(Long.class), any(Long.class));
    }

    @Test
    public void testOrderBook_PartialFill_Sell() {
        // given
        Order ask = new Order(5, Side.SELL, 10847L, 500_000);

        // when
        book.submitLimit(ask);

        // then
        verify(sink, times(1)).accepted(ask);
        verify(sink, times(1)).fill(ask, bid1, 10848L, 500_000);
    }

    @Test
    public void testOrderBook_NoFill_Sell() {
        // given
        Order ask = new Order(5, Side.SELL, 10850L, 500_000);

        // when
        book.submitLimit(ask);

        // then
        verify(sink, times(1)).accepted(ask);
        verify(sink, times(0)).fill(eq(ask), eq(bid1), any(Long.class), any(Long.class));
    }
}
