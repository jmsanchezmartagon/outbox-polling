package es.jambo.outbox;

import es.jambo.outbox.config.OraclePollingConfig;
import es.jambo.outbox.config.PropertiesPollingConfig;
import es.jambo.outbox.reader.OutboxReader;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.*;

public class OutboxPollingConnector extends SourceConnector {

    private OraclePollingConfig oraclePollingConfig;


    @Override
    public Class<? extends Task> taskClass() {
        return OutboxReader.class;
    }

    @Override
    public String version() {
        return PropertiesPollingConfig.VERSION;
    }

    @Override
    public void start(Map<String, String> map) {
        oraclePollingConfig = new OraclePollingConfig(map);
    }

    @Override
    public void stop() {
        oraclePollingConfig = null;
    }

    @Override
    public ConfigDef config() {
        return OraclePollingConfig.configPollingDefinition();
    }


    @Override
    public List<Map<String, String>> taskConfigs(int tasks) {
        final var globalOutboxConfig = oraclePollingConfig.getList(PropertiesPollingConfig.OUTBOX_TABLE_LIST);
        if (globalOutboxConfig.isEmpty())
            throw new IllegalArgumentException(String.format("%s is empty.", PropertiesPollingConfig.OUTBOX_TABLE_LIST));

        List<String>[] threadsTables = getTablesForTasks(tasks, globalOutboxConfig);

        return getMapsConfigForTask(threadsTables);
    }

    private List<String>[] getTablesForTasks(int tasks, List<String> globalOutboxConfig) {

        int taskConfigSize = Math.min(tasks, globalOutboxConfig.size());

        List<String>[] threadsTables = new List[taskConfigSize];
        var index = 0;
        for (String table : globalOutboxConfig) {
            if (threadsTables[index] == null) {
                threadsTables[index] = new ArrayList<>();
            }
            threadsTables[index].add(table);
            index = ++index % taskConfigSize;
        }
        return threadsTables;
    }


    private List<Map<String, String>> getMapsConfigForTask(List<String>[] threadsTables) {
        return Arrays.stream(threadsTables).map(outboxByThread -> {
            Map<String, String> config = new HashMap<>(oraclePollingConfig.originalsStrings());
            config.put(PropertiesPollingConfig.OUTBOX_TABLE_LIST, outboxByThread.stream()
                    .reduce((partialString, element) -> String.format("%s%s%s", partialString, PropertiesPollingConfig.OUTBOX_LIST_TOKEN, element)).orElse(""));
            return Collections.unmodifiableMap(config);
        }).toList();
    }

}
