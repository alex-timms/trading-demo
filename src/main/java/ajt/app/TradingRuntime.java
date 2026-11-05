package ajt.app;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.driver.MediaDriver;
import org.agrona.CloseHelper;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.charset.StandardCharsets;

public final class TradingRuntime implements AutoCloseable {

    private final MediaDriver mediaDriver;
    private final Aeron aeron;
    private final Publication incomingOrdersPublication;
    private final AgentRunner engineConsumerRunner;
    private final IdleStrategy idle;

    public TradingRuntime(
            MediaDriver mediaDriver,
            Aeron aeron,
            Publication incomingOrdersPublication,
            AgentRunner engineConsumerRunner,
            IdleStrategy idle) {
        this.mediaDriver = mediaDriver;
        this.aeron = aeron;
        this.incomingOrdersPublication = incomingOrdersPublication;
        this.engineConsumerRunner = engineConsumerRunner;
        this.idle = idle;
    }

    public void start() {
        AgentRunner.startOnThread(engineConsumerRunner);
        while (!incomingOrdersPublication.isConnected()) {
            idle.idle();
        }
    }

    public void publishOrderFix(String fixOrder) {
        byte[] payload = fixOrder.getBytes(StandardCharsets.UTF_8);
        UnsafeBuffer directBuffer = new UnsafeBuffer(payload);

        long offerResult;
        do {
            offerResult = incomingOrdersPublication.offer(directBuffer, 0, directBuffer.capacity());
            if (offerResult < 0) {
                idle.idle();
            }
        } while (offerResult < 0);
    }

    @Override
    public void close() {
        CloseHelper.quietClose(engineConsumerRunner);
        CloseHelper.quietClose(incomingOrdersPublication);
        CloseHelper.quietClose(aeron);
        CloseHelper.quietClose(mediaDriver);
    }
}

