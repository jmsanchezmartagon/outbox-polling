package es.jambo.outbox;

import es.jambo.outbox.config.OraclePollingConfig;
import es.jambo.outbox.config.PropertiesPollingConig;
import es.jambo.outbox.reader.OutboxReader;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutboxPollingConnector extends SourceConnector {

    private OraclePollingConfig oraclePollingConfig;


    @Override
    public Class<? extends Task> taskClass() {
        return OutboxReader.class;
    }

    @Override
    public String version() {
        return PropertiesPollingConig.VERSION;
    }

    @Override
    public void start(Map<String, String> map) {
        oraclePollingConfig = new OraclePollingConfig(map);
    }

    @Override
    public void stop() {
    }

    @Override
    public ConfigDef config() {
        return oraclePollingConfig.configPollingDefintion();
    }


    @Override
    public List<Map<String, String>> taskConfigs(int tasks) {
        final var globalOutboxConfig = oraclePollingConfig.getList(PropertiesPollingConig.OUTBOX_TABLE_LIST);
        if (globalOutboxConfig.isEmpty())
            throw new IllegalArgumentException(String.format("%s is empty.", PropertiesPollingConig.OUTBOX_TABLE_LIST));

        List<String>[] threadsTables = getTablesForTaks(tasks, globalOutboxConfig);

        return getMapsConfigForTask(threadsTables);
    }

    private List<String>[] getTablesForTaks(int tasks, List<String> globalOutboxConfig) {

        int taskConfigSize = getTaskConfigSize(tasks, globalOutboxConfig);

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


    private int getTaskConfigSize(int tasks, List<String> globalOutboxConfig) {
        int globalConfigSize = globalOutboxConfig.size();
        if (tasks > globalConfigSize)
            return globalConfigSize;
        return tasks;
    }


    private List<Map<String, String>> getMapsConfigForTask(List<String>[] threadsTables) {
        return Arrays.stream(threadsTables).map(outboxByThread -> {
            Map<String, String> config = new HashMap<>();
            config.put(PropertiesPollingConig.DATASOURCE_URL, oraclePollingConfig.getString(PropertiesPollingConig.DATASOURCE_URL));
            config.put(PropertiesPollingConig.OUTBOX_TABLE_LIST, outboxByThread.stream()
                    .reduce((partialString, element) -> String.format("%s%s%s", partialString, PropertiesPollingConig.OUTBOX_LIST_TOKEN, element)).orElse(""));
            return Collections.unmodifiableMap(config);
        }).toList();
    }

}
