package es.jambo.outbox.reader;

import es.jambo.outbox.config.PropertiesPollingConig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;

import java.util.*;

public class OutboxReader extends SourceTask {
    private OutboxQuery query;
    private String[] listOutbox;
    private Map<String, OffsetRecord> offsetPartition;

    @Override
    public void initialize(SourceTaskContext context) {
        this.context = context;
        offsetPartition = new HashMap<>();
    }

    @Override
    public String version() {
        return PropertiesPollingConig.VERSION;
    }

    @Override
    public void start(Map<String, String> map) {
        listOutbox = getListOutbox(map);
        query = createQueryExecutor(map);
        initializeOffsetPartition();
    }


    private String[] getListOutbox(Map<String, String> map) {
        String tables = map.get(PropertiesPollingConig.OUTBOX_TABLE_LIST);
        if (tables == null || tables.isBlank()) {
            throw new IllegalArgumentException();
        }
        return tables.split(PropertiesPollingConig.OUTBOX_LIST_TOKEN);
    }

    private OutboxQuery createQueryExecutor(Map<String, String> map) {
        return new OutboxQuery(map.get(PropertiesPollingConig.DATASOURCE_URL));
    }

    private void initializeOffsetPartition() {
        for (var tableName : listOutbox) {
            var offsetMap = this.context.offsetStorageReader().offset(Collections.singletonMap(RecordFields.PARTITION, tableName));
            if (offsetMap != null) {
                var value = offsetMap.get(RecordFields.OFFSET);
                if (value instanceof String serializeOffset) {
                    offsetPartition.put(tableName, OffsetRecord.deserialize(serializeOffset));
                }
            }
        }
    }


    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        List<SourceRecord> records = new LinkedList<>();
        QueryResult result = null;

        for (var tableName : listOutbox) {
            result = query.readRecords(tableName, offsetPartition.get(tableName));

            records.addAll(result.records());
            offsetPartition.put(tableName, result.offset());
        }
        return records;
    }

    @Override
    public void stop() {
        query.stop();
    }

}
