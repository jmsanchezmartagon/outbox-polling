package es.jambo.outbox.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
class OraclePollingConfigTest {

    @Test
    void shouldCreateConfig() {
        var expectedConfig = OraclePollingConfig.configPollingDefinition();

        Assertions.assertThat(expectedConfig).isNotNull();
        Assertions.assertThat(expectedConfig.configKeys().keySet()).contains(PropertiesPollingConfig.DATASOURCE_URL)
                .contains(PropertiesPollingConfig.OUTBOX_TABLE_LIST)
                .contains(PropertiesPollingConfig.POOL_INTERVAL_MS);
    }
}