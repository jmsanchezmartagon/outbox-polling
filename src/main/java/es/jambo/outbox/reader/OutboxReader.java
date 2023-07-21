package es.jambo.outbox.reader;

import es.jambo.outbox.CouldNotCloseConnectionException;
import es.jambo.outbox.CouldNotOpenConnectionException;
import es.jambo.outbox.config.PropertiesPollingConig;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OutboxReader extends SourceTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxReader.class);
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
        setOffsetPartition();
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

    private void setOffsetPartition() {
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
        List<SourceRecord> list = new LinkedList<>();
        OffsetRecord offsetValue = null;


        for (var tableName : listOutbox) {
            offsetValue = offsetPartition.get(tableName);

            PreparedStatement stment = null;
            ResultSet resultSet = null;
            try {
                stment = connection.prepareStatement(
                        (offsetValue == null) ?
                                String.format("""
                                        select o.id, o.event_type, o.create_at, o.key, o.data, o.ora_rowscn
                                        from %s o
                                        """, tableName)
                                :
                                String.format("""
                                        select o.id, o.event_type, o.create_at, o.key, o.data, o.ora_rowscn
                                        from %s o
                                        where o.create_at >= ?
                                              and o.ora_rowscn > ?               
                                                        """, tableName));
                if (offsetValue != null) {
                    stment.setDate(1, new java.sql.Date(offsetValue.date()));
                    stment.setString(2, offsetValue.scn());
                }
                resultSet = stment.executeQuery();

                while (resultSet.next()) {
                    LOGGER.debug("Reading...");
                    list.add(RowMapper.GET.record(tableName, resultSet));
                }
            } catch (Exception ex) {
                throw new InterruptedException(ex.getMessage());
            } finally {
                try {
                    if (resultSet != null)
                        resultSet.close();
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(),ex);
                }
                try {
                    if (stment != null)
                        stment.close();
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(),ex);
                }
            }

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
