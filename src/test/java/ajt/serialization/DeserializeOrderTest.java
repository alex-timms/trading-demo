package ajt.serialization;

import ajt.common.enums.Side;
import ajt.order.Order;
import ajt.serialization.impl.DeserializeOrder;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DeserializeOrderTest {

    Deserializer<Order> deserializer = new DeserializeOrder();

    @Test
    void testDeserialization() {
        // given
        String input = "8=FIX.4.4|35=D|11=1|55=EUR/USD|54=1|38=1500000|40=2|44=1.0853|";
        Order output = new Order(1L, 1, Side.SELL, 10853L, 1_500_000L);

        // when
        var result = deserializer.deserialize(ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8)));

        // then
        assertThat(result.orderId).isEqualTo(output.orderId);
        assertThat(result.instrumentId).isEqualTo(output.instrumentId);
        assertThat(result.side).isEqualTo(output.side);
        assertThat(result.price).isEqualTo(output.price);
        assertThat(result.leavesQty).isEqualTo(output.leavesQty);
    }

}
