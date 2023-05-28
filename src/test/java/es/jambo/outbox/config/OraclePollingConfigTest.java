package es.jambo.outbox.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


class OraclePollingConfigTest {

    @Test
    void shouldCreateConfig() {
        var expectedConfig = OraclePollingConfig.configPollingDefinition();

        Assertions.assertThat(expectedConfig).isNotNull();
        Assertions.assertThat(expectedConfig.configKeys().keySet()).contains(PropertiesPollingConig.DATASOURCE_URL).contains(PropertiesPollingConig.OUTBOX_TABLE_LIST);
    }
}