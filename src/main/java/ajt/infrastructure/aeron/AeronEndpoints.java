package ajt.infrastructure.aeron;

import ajt.common.constants.AERON;
import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;

public final class AeronEndpoints {

    private AeronEndpoints() {
    }

    public static Publication addPublication(Aeron aeron, int streamId) {
        return aeron.addPublication(AERON.CHANNELS.IPC_CHANNEL, streamId);
    }

    public static Subscription addSubscription(Aeron aeron, int streamId) {
        return aeron.addSubscription(AERON.CHANNELS.IPC_CHANNEL, streamId);
    }
}

