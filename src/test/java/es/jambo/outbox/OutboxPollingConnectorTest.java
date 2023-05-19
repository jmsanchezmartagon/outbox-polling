package es.jambo.outbox;

import es.jambo.outbox.config.PropertiesPollingConig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class OutboxPollingConnectorTest {


    @Test
    void should_getOneConfig_when_outboxListIsOne() {
        //given
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConig.DATASOURCE_URL, "");
            put(PropertiesPollingConig.OUTBOX_TABLE_LIST, "one");
        }};
        final var polling = new OutboxPollingConnector();

        polling.start(configMap);

        // when
        var resultConfig = polling.taskConfigs(4);

        // then
        Assertions.assertThat(resultConfig).isNotEmpty().size().isEqualTo(1);
    }

    @Test
    void should_getOneConfig_when_maxTaksIsOneIsRequired() {
        //given
        final var outboxTables = String.format("1%s2", PropertiesPollingConig.OUTBOX_LIST_TOKEN);
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConig.DATASOURCE_URL, "");
            put(PropertiesPollingConig.OUTBOX_TABLE_LIST, outboxTables);
        }};
        final var polling = new OutboxPollingConnector();

        polling.start(configMap);

        // when
        var resultConfig = polling.taskConfigs(1);

        // then
        Assertions.assertThat(resultConfig).isNotEmpty().size().isEqualTo(1);
        Assertions.assertThat(resultConfig.get(0).get(PropertiesPollingConig.OUTBOX_TABLE_LIST)).isEqualTo(outboxTables);
    }


    @Test
    void should_TwoConfig_when_maxTaksIsTwo() {
        //given
        final var outboxTables = String.format("1%s2", PropertiesPollingConig.OUTBOX_LIST_TOKEN);
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConig.DATASOURCE_URL, "");
            put(PropertiesPollingConig.OUTBOX_TABLE_LIST, outboxTables);
        }};
        final var polling = new OutboxPollingConnector();

        polling.start(configMap);

        // when
        var resultConfig = polling.taskConfigs(2);

        // then
        Assertions.assertThat(resultConfig).isNotEmpty().size().isEqualTo(2);

    }

    @Test
    void should_getTowConfigWithTwoParams_when_maxTaksIsMinorThanConfig() {
        //given
        final var outboxTables = String.format("1%s2%s3%s4", PropertiesPollingConig.OUTBOX_LIST_TOKEN, PropertiesPollingConig.OUTBOX_LIST_TOKEN, PropertiesPollingConig.OUTBOX_LIST_TOKEN);
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConig.DATASOURCE_URL, "");
            put(PropertiesPollingConig.OUTBOX_TABLE_LIST, outboxTables);
        }};
        final var polling = new OutboxPollingConnector();

        polling.start(configMap);

        // when
        var resultConfig = polling.taskConfigs(2);

        // then
        Assertions.assertThat(resultConfig).isNotEmpty().size().isEqualTo(2);
        Assertions.assertThat(resultConfig.get(0).get(PropertiesPollingConig.OUTBOX_TABLE_LIST)).isEqualTo(String.format("1%s3", PropertiesPollingConig.OUTBOX_LIST_TOKEN));
        Assertions.assertThat(resultConfig.get(1).get(PropertiesPollingConig.OUTBOX_TABLE_LIST)).isEqualTo(String.format("2%s4", PropertiesPollingConig.OUTBOX_LIST_TOKEN));
    }

    @Test
    void should_getException_when_configIsEmpty() {
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConig.DATASOURCE_URL, "");
            put(PropertiesPollingConig.OUTBOX_TABLE_LIST, "");
        }};

        // when
        Throwable error = Assertions.catchThrowable(() -> {
            final var polling = new OutboxPollingConnector();
            polling.start(configMap);
            polling.taskConfigs(2);
        });

        Assertions.assertThat(error).isInstanceOf(IllegalArgumentException.class).message().isEqualTo(String.format("%s is empty.", PropertiesPollingConig.OUTBOX_TABLE_LIST));
    }

    @Test
    void should_getConfigDefinition_when_createConnector() {
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConig.DATASOURCE_URL, "");
            put(PropertiesPollingConig.OUTBOX_TABLE_LIST, "");
        }};

        final var config = new OutboxPollingConnector().config();

        Assertions.assertThat(config).isNotNull();
    }
}