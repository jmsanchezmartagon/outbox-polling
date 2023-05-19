package es.jambo.outbox.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


class OraclePollingConfigTest {

    @Test
    public void shouldCreateConfig() {
        var expectedConfig = OraclePollingConfig.configPollingDefintion();

        Assertions.assertThat(expectedConfig).isNotNull();
        Assertions.assertThat(expectedConfig.configKeys().keySet()).contains(PropertiesPollingConig.DATASOURCE_URL).contains(PropertiesPollingConig.OUTBOX_TABLE_LIST);
    }
}