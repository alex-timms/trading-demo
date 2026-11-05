package ajt.aeron.agents;

import ajt.engine.Engine;
import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EngineConsumerAgentTest {

    @Test
    void testDoWork_ForwardsFragmentPayloadToEngine() throws Exception {
        Aeron aeron = mock(Aeron.class);
        Engine engine = mock(Engine.class);
        Subscription subscription = mock(Subscription.class);

        EngineConsumerAgent agent = new EngineConsumerAgent(aeron, engine);

        Field subField = EngineConsumerAgent.class.getDeclaredField("sub");
        subField.setAccessible(true);
        subField.set(agent, subscription);

        byte[] payload = "8=FIX.4.4|35=D|11=1|54=1|38=10|44=1.0000|".getBytes(StandardCharsets.UTF_8);

        when(subscription.poll(any(FragmentHandler.class), eq(100)))
                .thenAnswer(invocation -> {
                    FragmentHandler handler = invocation.getArgument(0);
                    UnsafeBuffer buffer = new UnsafeBuffer(payload);
                    handler.onFragment(buffer, 0, payload.length, null);
                    return 1;
                });

        int fragments = agent.doWork();

        assertThat(fragments).isEqualTo(1);

        org.mockito.ArgumentCaptor<ByteBuffer> captor = org.mockito.ArgumentCaptor.forClass(ByteBuffer.class);
        verify(engine).onNewOrder(captor.capture());

        ByteBuffer forwarded = captor.getValue();
        byte[] actual = new byte[forwarded.remaining()];
        forwarded.get(actual);
        assertThat(actual).isEqualTo(payload);
    }
}
