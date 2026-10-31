package ajt.order.books;

import ajt.common.enums.Side;
import ajt.common.ext.Orders;
import ajt.order.Order;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

public class FirstOrderBooksTest {

    FirstOrderBook firstOrderBook = new FirstOrderBook();

    @Test
    public void testOrderBooks_Add() {
        // given
        Order buyOrder = new Order(1L, 2, Side.BUY, 10_000, 1000);

        // when
        firstOrderBook.add(buyOrder);

        // then
        var postBids = firstOrderBook.getBids();
        var orderID = firstOrderBook.getById().get(1L);
        assertThat(orderID).isEqualTo(buyOrder);
        assertThat(postBids.size()).isEqualTo(1);
    }

    @Test
    public void testOrderBooks_AddMultiple() {
        // given
        long orderID1 = 1L;
        long orderID2 = 2L;
        long orderID3 = 3L;
        long bestPrice = 10_000L;
        long lowerPrice = 9_000L;
        Order buyOrder1 = new Order(orderID1, 2, Side.BUY, bestPrice, 1000);
        Order buyOrder2 = new Order(orderID2, 2, Side.BUY, lowerPrice, 1000);
        Order buyOrder3 = new Order(orderID3, 2, Side.BUY, bestPrice, 10000);


        // when
        firstOrderBook.add(buyOrder1);
        firstOrderBook.add(buyOrder2);
        firstOrderBook.add(buyOrder3);

        // then
        // assert order Ids added
        var idMap = firstOrderBook.getById();
        assertThat(idMap)
                .containsEntry(orderID1, buyOrder1)
                .containsEntry(orderID2, buyOrder2)
                .containsEntry(orderID3, buyOrder3);
        TreeMap<Long, Orders> postBids = firstOrderBook.getBids();
        assertThat(postBids.size()).isEqualTo(2);
        assertThat(postBids.firstEntry().getKey()).isEqualTo(bestPrice);
        assertThat(postBids.firstEntry().getValue().getFirst()).isEqualTo(buyOrder1);
        assertThat(postBids.firstEntry().getValue().getLast()).isEqualTo(buyOrder3);
        assertThat(postBids.lastEntry().getKey()).isEqualTo(lowerPrice);
        assertThat(postBids.lastEntry().getValue().getFirst()).isEqualTo(buyOrder2);
    }
}
