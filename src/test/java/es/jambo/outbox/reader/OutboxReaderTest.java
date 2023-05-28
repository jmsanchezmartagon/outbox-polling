package es.jambo.outbox.reader;

import es.jambo.outbox.config.PropertiesPollingConig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class OutboxReaderTest {

    @Test
    void shouldGetStartTask() {
        var reader = new OutboxReader();
        reader.start(createConfigMap());
    }


    @Test
    void shouldGetConnection() {
        var reader = new OutboxReader();
        reader.start(createConfigMap());
    }


    private Map<String, String> createConfigMap() {
        return new HashMap<String, String>() {{
            put(PropertiesPollingConig.DATASOURCE_URL, "jdbc:oracle:thin:TEST_IRIS_CUSTOMER/TEST_IRIS_CUSTOMER@localhost:1521:XE");
            put(PropertiesPollingConig.OUTBOX_TABLE_LIST, "one");
        }};
    }
}