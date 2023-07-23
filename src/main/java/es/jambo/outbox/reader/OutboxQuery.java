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
    private static final String OUTBOX_INIT = """
            select o.id, o.event_type, o.create_at, o.key, o.data, o.ora_rowscn
            from %s o
            where o.ora_rowscn IN (
                              select  o.ora_rowscn
                              from outbox o
                              order by create_at ASC, ora_rowscn ASC
                              fetch first 5000 rows only
                              )
            order by create_at ASC, ora_rowscn ASC
            """;
    private static final String OUTBOX_OFFSET = """
            select o.id, o.event_type, o.create_at, o.key, o.data, o.ora_rowscn
            from %s o
            where o.ora_rowscn IN (
                              select  o.ora_rowscn
                              from outbox o
                              where o.create_at >= ?
                                    and o.ora_rowscn > ?
                              order by create_at ASC, ora_rowscn ASC
                              fetch first 5000 rows only
                              )
            order by create_at ASC, ora_rowscn ASC
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

    public QueryResult readRecords(String tableName, OffsetRecord offsetValue) throws InterruptedException {

        LOGGER.debug("Reading...");
        List<SourceRecord> list = new LinkedList<>();
        PreparedStatement stment = null;
        ResultSet resultSet = null;
        OffsetRecord offsetNew = offsetValue;
        try {
            stment = connection.prepareStatement(getOutboxQuery(tableName, offsetValue));
            if (offsetValue != null) {
                stment.setDate(1, new java.sql.Date(offsetValue.date()));
                stment.setString(2, offsetValue.scn());
            }
            resultSet = stment.executeQuery();

            while (resultSet.next()) {
                LOGGER.debug("Record {}", resultSet.getRowId(1));
                list.add(RowMapper.GET.sourceRecord(tableName, resultSet));
                resultSet.getString(OutboxColumns.ORA_ROWSCN.name());
                offsetNew = getOffSet(resultSet);
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

    private String getOutboxQuery(String tableName, OffsetRecord offsetValue) {
        final var query = (offsetValue == null) ? String.format(OUTBOX_INIT, tableName) : String.format(OUTBOX_OFFSET, tableName);
        LOGGER.debug("Query: {}", query);
        LOGGER.debug("Offset: {}", offsetValue);
        return query;
    }


    private OffsetRecord getOffSet(ResultSet resultSet) throws SQLException {
        final var lastSCN = resultSet.getString(OutboxColumns.ORA_ROWSCN.name());
        final var lastDate = resultSet.getDate(OutboxColumns.CREATE_AT.name()).getTime();
        LOGGER.debug("Offset [ date: {}, scn: {} ]", lastDate, lastSCN);
        return new OffsetRecord(lastDate, lastSCN);
    }


    public void stop() {
        try {
            connection.close();
        } catch (SQLException sqlEx) {
            throw new CouldNotCloseConnectionException(sqlEx.getMessage(), sqlEx);
        }
    }
}
