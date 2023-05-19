package es.jambo.outbox;

import es.jambo.outbox.config.OraclePollingConfig;
import es.jambo.outbox.config.PropertiesPollingConig;
import es.jambo.outbox.reader.OutboxReader;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutboxPollingConnectorold extends SourceConnector {

    private OraclePollingConfig oraclePollingConfig;

    @Override
    public void start(Map<String, String> map) {
        oraclePollingConfig = new OraclePollingConfig(map);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return OutboxReader.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int i) {
        final var outbox = oraclePollingConfig.getList(PropertiesPollingConig.OUTBOX_TABLE_LIST);
        final var numConfig = Math.min(i, outbox.size());
        final var numOuxbox = Math.max(i, outbox.size()) / numConfig;
        List<Map<String, String>> taskConfigs = new ArrayList<>(numConfig);

        Map<String, String> singleConfig = null;
        for (int count = 0; count < numConfig; count++) {
            singleConfig = new HashMap<>();
            taskConfigs.add(singleConfig);

            singleConfig.put(PropertiesPollingConig.DATASOURCE_URL, oraclePollingConfig.getString(PropertiesPollingConig.DATASOURCE_URL));

            StringBuilder listOutBoxString = new StringBuilder();
            for (int x = count, max = Math.min(count + numOuxbox, outbox.size()); x < max; x++) {
                listOutBoxString.append(PropertiesPollingConig.OUTBOX_LIST_TOKEN).append(outbox.get(x));
            }
            singleConfig.put(PropertiesPollingConig.OUTBOX_TABLE_LIST, listOutBoxString.substring(1));

        }

        return taskConfigs;
    }

    @Override
    public void stop() {
    }

    @Override
    public ConfigDef config() {
        return oraclePollingConfig.configPollingDefintion();
    }

    @Override
    public String version() {
        return PropertiesPollingConig.VERSION;
    }
}
