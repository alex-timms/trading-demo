package ajt.aeron.infrastructure;

import org.agrona.CloseHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AeronRuntimeFactoryTest {

    @Test
    void testStartRuntime_ReturnsInitializedResources() {
        AeronFactory.AeronRuntime runtime = AeronFactory.startRuntime();

        try {
            assertThat(runtime.driver()).isNotNull();
            assertThat(runtime.client()).isNotNull();
            assertThat(runtime.idleStrategy()).isNotNull();
        } finally {
            CloseHelper.quietClose(runtime.client());
            CloseHelper.quietClose(runtime.driver());
        }
    }
}
