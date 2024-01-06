package es.jambo.outbox.reader;

import es.jambo.outbox.config.OraclePollingConfig;
import es.jambo.outbox.config.PropertiesPollingConfig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
public class OutboxReader extends SourceTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxReader.class);
    private OutboxQuery query;
    private List<String> listOutbox;
    private Map<String, String> offsetPartition;
    private int intervalSize;
    private long nextInterval = System.currentTimeMillis();

    @Override
    public void initialize(SourceTaskContext context) {
        this.context = context;
        offsetPartition = new HashMap<>();
    }

    @Override
    public String version() {
        return PropertiesPollingConfig.VERSION;
    }

    @Override
    public void start(Map<String, String> map) {
        var oraclePollingConfig = new OraclePollingConfig(map);
        intervalSize = oraclePollingConfig.getInt(PropertiesPollingConfig.POOL_INTERVAL_MS);
        listOutbox = oraclePollingConfig.getList(PropertiesPollingConfig.OUTBOX_TABLE_LIST);
        query = new OutboxQuery(oraclePollingConfig.getString(PropertiesPollingConfig.DATASOURCE_URL));
        initializeOffsetPartition();
    }

    private void initializeOffsetPartition() {
        for (var tableName : listOutbox) {
            var offsetMap = this.context.offsetStorageReader().offset(Collections.singletonMap(RecordFields.PARTITION, tableName));
            if (offsetMap != null) {
                var value = offsetMap.get(RecordFields.OFFSET);
                if (value instanceof String offsetValue) {
                    offsetPartition.put(tableName, offsetValue);
                }
            }
        }
    }


    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        if (nextInterval < System.currentTimeMillis()) {
            LOGGER.info("Offsets {} ", offsetPartition);
            List<SourceRecord> records = new LinkedList<>();

            for (var tableName : listOutbox) {
                var result = query.readRecords(tableName, offsetPartition.get(tableName));

                records.addAll(result.records());
                offsetPartition.put(tableName, result.offset());
            }

            LOGGER.info("Pool {} ", records.size());
            nextInterval = System.currentTimeMillis() + intervalSize;

            return records;
        }
        return Collections.emptyList();
    }


    @Override
    public void stop() {
        query.stop();
    }

}
