package ajt.serialization;

import ajt.common.exceptions.IncompleteOrderDefinitionException;
import ajt.common.enums.Side;
import ajt.order.Order;
import ajt.serialization.impl.DeserializeOrder;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class DeserializeOrderTest {

    Deserializer<Order> deserializer = new DeserializeOrder();

    @Test
    void testDeserialization() {
        // given
        String input = "8=FIX.4.4|35=D|11=1|55=EUR/USD|54=1|38=1500000|40=2|44=1.0853|";
        Order output = new Order(1L, 1, Side.BUY, 10853L, 1_500_000L);

        // when
        var result = deserializer.deserialize(ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8)));

        // then
        assertThat(result.orderId).isEqualTo(output.orderId);
        assertThat(result.instrumentId).isEqualTo(output.instrumentId);
        assertThat(result.side).isEqualTo(output.side);
        assertThat(result.price).isEqualTo(output.price);
        assertThat(result.leavesQty).isEqualTo(output.leavesQty);
    }

    @Test
    void testDeserialization_MissingOrderIdTag_Throws() {
        // given
        String input = "8=FIX.4.4|35=D|55=EUR/USD|54=1|38=1500000|40=2|44=1.0853|";

        // when / then
        assertThatThrownBy(() -> deserializer.deserialize(ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(IncompleteOrderDefinitionException.class)
                .hasMessageContaining("Missing required tag: 11");
    }

    @Test
    void testDeserialization_MissingSideTag_Throws() {
        // given
        String input = "8=FIX.4.4|35=D|11=1|55=EUR/USD|38=1500000|40=2|44=1.0853|";

        // when / then
        assertThatThrownBy(() -> deserializer.deserialize(ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(IncompleteOrderDefinitionException.class)
                .hasMessageContaining("Missing required side tag: 54");
    }

    @Test
    void testDeserialization_InvalidSideValue_Throws() {
        // given
        String input = "8=FIX.4.4|35=D|11=1|55=EUR/USD|54=9|38=1500000|40=2|44=1.0853|";

        // when / then
        assertThatThrownBy(() -> deserializer.deserialize(ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported side value");
    }

    @Test
    void testDeserialization_InvalidPrice_Throws() {
        // given
        String input = "8=FIX.4.4|35=D|11=1|55=EUR/USD|54=1|38=1500000|40=2|44=1.08x3|";

        // when / then
        assertThatThrownBy(() -> deserializer.deserialize(ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid numeric value for tag 44");
    }

}
