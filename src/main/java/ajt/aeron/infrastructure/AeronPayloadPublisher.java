package ajt.aeron.infrastructure;

import io.aeron.Publication;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class AeronPayloadPublisher {

    private final Publication publication;
    private final IdleStrategy idleStrategy;

    public AeronPayloadPublisher(Publication publication, IdleStrategy idleStrategy) {
        this.publication = Objects.requireNonNull(publication, "publication");
        this.idleStrategy = Objects.requireNonNull(idleStrategy, "idleStrategy");
    }

    public void waitUntilConnected() {
        while (!publication.isConnected()) {
            idleStrategy.idle();
        }
    }

    public void publishString(String input) {
        Objects.requireNonNull(input, "fixOrder");
        publishBytes(input.getBytes(StandardCharsets.UTF_8));
    }

    public void publishBytes(byte[] payload) {
        Objects.requireNonNull(payload, "fixOrder");
        UnsafeBuffer directBuffer = new UnsafeBuffer(payload);

        long offerResult;
        do {
            offerResult = publication.offer(directBuffer, 0, directBuffer.capacity());
            if (offerResult < 0) {
                idleStrategy.idle();
            }
        } while (offerResult < 0);
    }
}

