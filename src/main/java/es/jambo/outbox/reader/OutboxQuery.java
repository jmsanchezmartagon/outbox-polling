package es.jambo.outbox.reader;

import es.jambo.outbox.CouldNotCloseConnectionException;
import es.jambo.outbox.CouldNotOpenConnectionException;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

final class OutboxQuery {


    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxQuery.class);
    private static final String OUTBOX_OFFSET = """
            select o.id, o.event_id, o.event_type, o.create_at, o.key, o.data
            from %s o
            where o.ID > ?
            order by o.id ASC
            fetch first 5000 rows only
                            """;

    private final Connection connection;

    public OutboxQuery(String urlConnection) {
        this.connection = createConnection(urlConnection);
    }


    private Connection createConnection(final String urlConnection) {
        try {
            var conn = DriverManager.getConnection(urlConnection);
            try (final var statement = conn.createStatement()) {
                statement.execute("call DBMS_APPLICATION_INFO.SET_CLIENT_INFO('Outbox')");
            }
            return conn;
        } catch (SQLException e) {
            throw new CouldNotOpenConnectionException(e.getMessage(), e);
        }
    }

    public QueryResult readRecords(String tableName, String offsetValue) throws InterruptedException {

        LOGGER.debug("Reading...");
        List<SourceRecord> list = new LinkedList<>();
        PreparedStatement stment = null;
        ResultSet resultSet = null;
        var offsetNew = offsetValue;
        try {
            stment = connection.prepareStatement(getOutboxQuery(tableName));
            if (offsetValue != null) {
                LOGGER.debug("Offset: {}", offsetValue);
                stment.setString(1, offsetValue);
            } else {
                LOGGER.debug("Offset: None, set min value");
                stment.setInt(1, Integer.MIN_VALUE);
            }
            resultSet = stment.executeQuery();

            while (resultSet.next()) {
                list.add(RowMapper.GET.sourceRecord(tableName, resultSet));
                offsetNew = resultSet.getString(OutboxColumns.ID.name());
            }

            return new QueryResult(list, offsetNew);
        } catch (Exception ex) {
            throw new InterruptedException(ex.getMessage());
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            try {
                if (stment != null)
                    stment.close();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }

    }

    private String getOutboxQuery(String tableName) {
        final var query = String.format(OUTBOX_OFFSET, tableName);
        LOGGER.debug("Query: {}", query);
        return query;
    }


    public void stop() {
        try {
            connection.close();
        } catch (SQLException sqlEx) {
            throw new CouldNotCloseConnectionException(sqlEx.getMessage(), sqlEx);
        }
    }
}
