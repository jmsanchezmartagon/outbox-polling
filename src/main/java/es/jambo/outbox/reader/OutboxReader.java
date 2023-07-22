package es.jambo.outbox.reader;

import es.jambo.outbox.CouldNotCloseConnectionException;
import es.jambo.outbox.CouldNotOpenConnectionException;
import es.jambo.outbox.config.PropertiesPollingConig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OutboxReader extends SourceTask {
    private Connection connection;
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
        connection = getConnection(map);
        initializeOffsetPartition();
    }


    private String[] getListOutbox(Map<String, String> map) {
        String tables = map.get(PropertiesPollingConig.OUTBOX_TABLE_LIST);
        if (tables == null || tables.isBlank()) {
            throw new IllegalArgumentException();
        }
        return tables.split(PropertiesPollingConig.OUTBOX_LIST_TOKEN);
    }

    private Connection getConnection(Map<String, String> map) {
        Connection conn = null;
        try {
            final var urlConnection = map.get(PropertiesPollingConig.DATASOURCE_URL);
            conn = DriverManager.getConnection(urlConnection);
            try (final var statement = conn.createStatement()) {
                statement.execute("call DBMS_APPLICATION_INFO.SET_CLIENT_INFO('Outbox')");
            }

        } catch (SQLException e) {
            throw new CouldNotOpenConnectionException(e.getMessage(), e);
        }
        return conn;
    }

    private void initializeOffsetPartition() {
        for (var tableName : listOutbox) {
            var offsetMap = this.context.offsetStorageReader().offset( Collections.singletonMap(RecordFields.PARTITION, tableName));
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
        final List<SourceRecord> list = new LinkedList<>();
        OffsetRecord offsetValue = null;
        OutboxQuery query  = new OutboxQuery(connection);

        for (var tableName : listOutbox) {
            offsetValue = query.addRecords(tableName,offsetPartition.get(tableName),list);
            offsetPartition.put(tableName,offsetValue);
        }
        return list;
    }

    @Override
    public void stop() {
        try {
            connection.close();
        } catch (SQLException sqlEx) {
            throw new CouldNotCloseConnectionException(sqlEx.getMessage(), sqlEx);
        }
    }

}
