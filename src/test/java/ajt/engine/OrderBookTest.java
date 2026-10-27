package ajt.engine;

import ajt.common.enums.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OrderBookTest {

    final Order ask1 = new Order(1, Side.SELL, 10852L, 1000000);
    final Order ask2 = new Order(2, Side.SELL, 10855L, 2000000);
    final Order bids1 = new Order(3, Side.BUY, 10848L, 1000000);

    EventSink sink = Mockito.mock(EventSink.class);

    OrderBook book = new OrderBook(sink);

    @BeforeEach
    void setUp() {
        book.submitLimit(ask1);
        book.submitLimit(ask2);
        book.submitLimit(bids1);
    }

    @Test
    public void testOrderBook() {
        // given
        Order bid = new Order(4, Side.BUY, 10853L, 1500000);

        // when
        book.submitLimit(bid);

        // then
        verify(sink, times(1)).fill(bid, ask1, 10852L, 1000000);

    }
}
