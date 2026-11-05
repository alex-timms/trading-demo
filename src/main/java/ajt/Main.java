package ajt;

import ajt.common.constants.AERON;
import ajt.config.AeronConfiguration;
import ajt.config.Configuration;
import ajt.engine.Engine;
import ajt.events.EventSink;
import ajt.matcher.Matcher;
import ajt.order.Order;
import ajt.serialization.Deserializer;
import ajt.validation.Validator;
import io.aeron.Aeron;
import io.aeron.Publication;
import org.agrona.CloseHelper;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;

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

        IdleStrategy idle = AeronConfiguration.idleStrategy();
        try (
                Aeron aeron = AeronConfiguration.aeronStart();
                Publication pub = AeronConfiguration.addPub(aeron, AERON.CHANNELS.INCOMING_ORDERS);

        ){
            var engineConsumer = new EngineConsumer(aeron, engine);
            AgentRunner engineConsumerRunner = new AgentRunner(
                    idle,
                    Throwable::printStackTrace,
                    null,
                    engineConsumer);
            AgentRunner.startOnThread(engineConsumerRunner);
            while (!pub.isConnected()) {
                idle.idle();
            }
            String input = "8=FIX.4.4|35=D|11=1|55=EUR/USD|54=1|38=150000|40=2|44=1.0853|";
            byte[] payload = input.getBytes(StandardCharsets.UTF_8);
            UnsafeBuffer directBuffer = new UnsafeBuffer(payload);

            long offerResult;
            do {
                offerResult = pub.offer(directBuffer, 0, directBuffer.capacity());
                if (offerResult < 0) {
                    idle.idle();
                }
            } while (offerResult < 0);

            CloseHelper.quietClose(engineConsumerRunner);

        }


    }
}
