package ajt.app;

import ajt.engine.Engine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppFactoryTest {

    @Test
    void testCreateEngine_ConstructsEngine() {
        Engine engine = EngineFactory.createMatchingEngine();

        assertThat(engine).isNotNull();
    }
}
