package ajt.serialization;

import ajt.common.enums.Side;
import ajt.order.Order;
import ajt.serialization.impl.DeserializeOrder;
import ajt.serialization.impl.SerializeOrder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SerializeOrderTest {

    Serializer<Order> serializer = new SerializeOrder();
    Deserializer<Order> deserializer = new DeserializeOrder();

    @Test
    void testSerialization() {
        // given
        Order input = new Order(1L, 1, Side.SELL, 10853L, 1_500_000L);

        // when
        var result = serializer.serialize(input);
        String fix = StandardCharsets.UTF_8.decode(result).toString();

        // then
        assertThat(fix).isEqualTo("8=FIX.4.4|35=D|11=1|55=1|54=2|38=1500000|40=2|44=1.0853|");
    }

    @Test
    void testSerialization_BuySideAndPaddedPrice() {
        // given
        Order input = new Order(10L, 1, Side.BUY, 10850L, 500L);

        // when
        var result = serializer.serialize(input);
        String fix = StandardCharsets.UTF_8.decode(result).toString();

        // then
        assertThat(fix).contains("54=1|");
        assertThat(fix).contains("44=1.0850|");
    }

    @Test
    void testSerialization_RoundTrip() {
        // given
        Order input = new Order(7L, 1, Side.BUY, 25001L, 42L);

        // when
        var result = serializer.serialize(input);
        Order parsed = deserializer.deserialize(result);

        // then
        assertThat(parsed.orderId).isEqualTo(input.orderId);
        assertThat(parsed.instrumentId).isEqualTo(1);
        assertThat(parsed.side).isEqualTo(input.side);
        assertThat(parsed.price).isEqualTo(input.price);
        assertThat(parsed.leavesQty).isEqualTo(input.leavesQty);
    }
}

