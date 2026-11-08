package ajt;

import ajt.aeron.agents.EngineConsumerAgent;
import ajt.aeron.infrastructure.AeronEndpoints;
import ajt.aeron.infrastructure.AeronFactory;
import ajt.aeron.infrastructure.AeronPayloadPublisher;
import ajt.app.EngineFactory;
import ajt.common.constants.AERON;
import ajt.config.AeronConfiguration;
import ajt.engine.Engine;
import io.aeron.Publication;
import org.agrona.CloseHelper;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.IdleStrategy;

public class Main {
    public static void main(String[] args) {

        // Create Trading App
        final Engine engine = EngineFactory.createMatchingEngine();
        // Setup Messaging Infrastructure
        final AeronFactory.AeronRuntime runtime = AeronFactory.startRuntime();
        final IdleStrategy agentIdleStrat = AeronConfiguration.idleStrategy();
        final IdleStrategy pubIdleStrat = AeronConfiguration.idleStrategy();
        // Trading App listening on incoming orders channel
        final EngineConsumerAgent consumer = new EngineConsumerAgent(runtime.client(), engine);
        final AgentRunner engineRunner = new AgentRunner(agentIdleStrat, Throwable::printStackTrace, null, consumer);

        Publication publication = null;
        try {
            // Publish from main to incoming orders channel
            publication = AeronEndpoints.addPublication(runtime.client(), AERON.STREAMS.INCOMING_ORDERS);
            AgentRunner.startOnThread(engineRunner);

            AeronPayloadPublisher orderPublisher = new AeronPayloadPublisher(publication, pubIdleStrat);
            orderPublisher.waitUntilConnected();
            orderPublisher.publishString("8=FIX.4.4|35=D|11=1|55=EUR/USD|54=1|38=150000|40=2|44=1.0853|");

            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // close Messaging infrastructure
            CloseHelper.quietClose(engineRunner);
            CloseHelper.quietClose(publication);
            CloseHelper.quietClose(runtime.client());
            CloseHelper.quietClose(runtime.driver());
        }
    }
}
