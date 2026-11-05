package ajt.app;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppFactoryTest {

    @Test
    void testCreateTradingRuntime_ConstructsAndCloses() {
        TradingRuntime runtime = AppFactory.createTradingRuntime();

        try {
            assertThat(runtime).isNotNull();
        } finally {
            runtime.close();
        }
    }
}

