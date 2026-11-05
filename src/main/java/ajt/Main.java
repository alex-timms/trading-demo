package ajt;

import ajt.aeron.agents.EngineConsumerAgent;
import ajt.aeron.infrastructure.AeronEndpoints;
import ajt.aeron.infrastructure.AeronRuntimeFactory;
import ajt.app.EngineFactory;
import ajt.common.constants.AERON;
import ajt.engine.Engine;
import io.aeron.Publication;
import org.agrona.CloseHelper;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.charset.StandardCharsets;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        IO.println(String.format("Hello and welcome!"));

        // Setup Messaging Infrastructure
        AeronRuntimeFactory.AeronRuntime runtime = AeronRuntimeFactory.startRuntime();
        IdleStrategy idle = runtime.idleStrategy();

        // Create Trading App
        Engine engine = EngineFactory.createMatchingEngine();

        // Trading App listening on incoming orders channel
        EngineConsumerAgent consumer = new EngineConsumerAgent(runtime.client(), engine);
        AgentRunner runner = new AgentRunner(
                idle,
                Throwable::printStackTrace,
                null,
                consumer
        );

        Publication publication = null;
        try {
            // Publish from main to incoming orders channel
            publication = AeronEndpoints.addPublication(runtime.client(), AERON.STREAMS.INCOMING_ORDERS);
            AgentRunner.startOnThread(runner);

            while (!publication.isConnected()) {
                idle.idle();
            }

            // send payload
            String input = "8=FIX.4.4|35=D|11=1|55=EUR/USD|54=1|38=150000|40=2|44=1.0853|";
            byte[] payload = input.getBytes(StandardCharsets.UTF_8);
            UnsafeBuffer directBuffer = new UnsafeBuffer(payload);

            long offerResult;
            do {
                offerResult = publication.offer(directBuffer, 0, directBuffer.capacity());
                if (offerResult < 0) {
                    idle.idle();
                }
            } while (offerResult < 0);

        } finally {
            // close Messaging infrastructure
            CloseHelper.quietClose(runner);
            CloseHelper.quietClose(publication);
            CloseHelper.quietClose(runtime.client());
            CloseHelper.quietClose(runtime.driver());
        }
    }
}
