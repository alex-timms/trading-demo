package ajt;

import ajt.config.Configuration;
import ajt.engine.Engine;
import ajt.events.EventSink;
import ajt.matcher.Matcher;
import ajt.order.Order;
import ajt.serialization.Deserializer;
import ajt.validation.Validator;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        IO.println(String.format("Hello and welcome!"));

        Deserializer<Order> orderDeserializer = Configuration.orderDeserializerConfig();
        Validator<Order> orderValidator = Configuration.compositeOrderRiskValidatorConfig();
        Matcher matcher = Configuration.matcherConfig();
        EventSink eventSink = Configuration.eventSinkConfig();
        int[] ids = new int[]{1};
        Engine engine = Configuration.matchingEngineConfig(
                orderDeserializer,
                orderValidator,
                matcher,
                eventSink,
                ids
        );

        String input = "8=FIX.4.4|35=D|11=1|55=EUR/USD|54=1|38=150000|40=2|44=1.0853|";
        ByteBuffer buf = ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8));
        engine.onNewOrder(buf);
    }
}
