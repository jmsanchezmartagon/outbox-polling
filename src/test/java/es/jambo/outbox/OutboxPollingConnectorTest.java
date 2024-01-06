package es.jambo.outbox;

import es.jambo.outbox.config.PropertiesPollingConfig;
import es.jambo.outbox.reader.OutboxReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
class OutboxPollingConnectorTest {

    @Test
    void should_getOneConfig_when_outboxListIsOne() {
        //given
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConfig.DATASOURCE_URL, "");
            put(PropertiesPollingConfig.OUTBOX_TABLE_LIST, "one");
            put(PropertiesPollingConfig.POOL_INTERVAL_MS, "500");
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
        final var outboxTables = String.format("1%s2", PropertiesPollingConfig.OUTBOX_LIST_TOKEN);
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConfig.DATASOURCE_URL, "");
            put(PropertiesPollingConfig.OUTBOX_TABLE_LIST, outboxTables);
            put(PropertiesPollingConfig.POOL_INTERVAL_MS, "500");
        }};
        final var polling = new OutboxPollingConnector();

        polling.start(configMap);

        // when
        var resultConfig = polling.taskConfigs(1);

        // then
        Assertions.assertThat(resultConfig).isNotEmpty().size().isEqualTo(1);
        Assertions.assertThat(resultConfig.get(0)).containsEntry(PropertiesPollingConfig.OUTBOX_TABLE_LIST, outboxTables);
    }


    @Test
    void should_TwoConfig_when_maxTaksIsTwo() {
        //given
        final var outboxTables = String.format("1%s2", PropertiesPollingConfig.OUTBOX_LIST_TOKEN);
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConfig.DATASOURCE_URL, "");
            put(PropertiesPollingConfig.OUTBOX_TABLE_LIST, outboxTables);
            put(PropertiesPollingConfig.POOL_INTERVAL_MS, "500");
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
        final var POOL_INTERVAL = "1500";
        final var outboxTables = String.format("1%s2%s3%s4", PropertiesPollingConfig.OUTBOX_LIST_TOKEN, PropertiesPollingConfig.OUTBOX_LIST_TOKEN, PropertiesPollingConfig.OUTBOX_LIST_TOKEN);
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConfig.DATASOURCE_URL, "");
            put(PropertiesPollingConfig.OUTBOX_TABLE_LIST, outboxTables);
            put(PropertiesPollingConfig.POOL_INTERVAL_MS, POOL_INTERVAL);
        }};
        final var polling = new OutboxPollingConnector();

        polling.start(configMap);

        // when
        var resultConfig = polling.taskConfigs(2);

        // then
        Assertions.assertThat(resultConfig).isNotEmpty().size().isEqualTo(2);
        Assertions.assertThat(resultConfig.get(0)).containsEntry(PropertiesPollingConfig.OUTBOX_TABLE_LIST, String.format("1%s3", PropertiesPollingConfig.OUTBOX_LIST_TOKEN));
        Assertions.assertThat(resultConfig.get(0)).containsEntry(PropertiesPollingConfig.POOL_INTERVAL_MS, POOL_INTERVAL);
        Assertions.assertThat(resultConfig.get(1)).containsEntry(PropertiesPollingConfig.OUTBOX_TABLE_LIST, String.format("2%s4", PropertiesPollingConfig.OUTBOX_LIST_TOKEN));
        Assertions.assertThat(resultConfig.get(1)).containsEntry(PropertiesPollingConfig.POOL_INTERVAL_MS, POOL_INTERVAL);
    }

    @Test
    void should_getException_when_configIsEmpty() {
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConfig.DATASOURCE_URL, "");
            put(PropertiesPollingConfig.OUTBOX_TABLE_LIST, "");
            put(PropertiesPollingConfig.POOL_INTERVAL_MS, "500");
        }};

        // when
        Throwable error = Assertions.catchThrowable(() -> {
            final var polling = new OutboxPollingConnector();
            polling.start(configMap);
            polling.taskConfigs(2);
        });

        Assertions.assertThat(error).isInstanceOf(IllegalArgumentException.class).message().isEqualTo(String.format("%s is empty.", PropertiesPollingConfig.OUTBOX_TABLE_LIST));
    }

    @Test
    void should_getConfigDefinition_when_createConnector() {
        final var configMap = new HashMap<String, String>() {{
            put(PropertiesPollingConfig.DATASOURCE_URL, "");
            put(PropertiesPollingConfig.OUTBOX_TABLE_LIST, "");
            put(PropertiesPollingConfig.POOL_INTERVAL_MS, "500");
        }};

        final var config = new OutboxPollingConnector().config();

        Assertions.assertThat(config).isNotNull();
    }

    @Test
    void should_beEqualsPropertiesPollingConfigVersion_when_getVersion() {
        final var version = new OutboxPollingConnector().version();
        Assertions.assertThat(version).isEqualTo(PropertiesPollingConfig.VERSION);
    }

    @Test
    void should_beOutReaderTask_when_getTaskClas() {
        final var classTask = new OutboxPollingConnector().taskClass();
        Assertions.assertThat(classTask).isEqualTo(OutboxReader.class);
    }
}