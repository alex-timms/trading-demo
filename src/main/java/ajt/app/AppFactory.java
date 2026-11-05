package ajt.app;

import ajt.EngineConsumer;
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
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.IdleStrategy;

public final class AppFactory {

    private AppFactory() {
    }

    public static TradingRuntime createTradingRuntime() {
        Deserializer<Order> deserializer = Configuration.orderDeserializerConfig();
        Validator<Order> validator = Configuration.compositeOrderRiskValidatorConfig();
        Matcher matcher = Configuration.matcherConfig();
        EventSink sink = Configuration.eventSinkConfig();
        int[] instrumentIds = new int[]{1};

        Engine engine = Configuration.matchingEngineConfig(
                deserializer,
                validator,
                matcher,
                sink,
                instrumentIds
        );

        IdleStrategy idle = AeronConfiguration.idleStrategy();
        Aeron aeron = AeronConfiguration.aeronStart();
        Publication incomingOrdersPublication = AeronConfiguration.addPub(aeron, AERON.STREAMS.INCOMING_ORDERS);

        EngineConsumer consumer = new EngineConsumer(aeron, engine);
        AgentRunner runner = new AgentRunner(
                idle,
                Throwable::printStackTrace,
                null,
                consumer
        );

        return new TradingRuntime(aeron, incomingOrdersPublication, runner, idle);
    }
}

