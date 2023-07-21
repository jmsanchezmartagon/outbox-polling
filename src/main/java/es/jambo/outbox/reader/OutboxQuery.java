package es.jambo.outbox.reader;

import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

final class OutboxQuery {


    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxQuery.class);
    private static final String OUTBOX_INIT="""
                                        select o.id, o.event_type, o.create_at, o.key, o.data, o.ora_rowscn
                                        from %s o
                                        """;
    private static final String OUTBOX_OFFSET="""
                                        select o.id, o.event_type, o.create_at, o.key, o.data, o.ora_rowscn
                                        from %s o
                                        where o.create_at >= ?
                                              and o.ora_rowscn > ?               
                                                        """;
    enum OutboxColumns {
        ID, CREATE_AT, EVENT_TYPE, KEY, DATA, ORA_ROWSCN;
    }


    public Connection connection;

    public OutboxQuery(Connection connection) {
        this.connection = connection;
    }


    public OffsetRecord addRecords(String tableName, OffsetRecord offsetValue, List<SourceRecord> list) throws InterruptedException{

        PreparedStatement stment = null;
        ResultSet resultSet = null;
        try {
            stment = connection.prepareStatement(getOutboxQuery(tableName, offsetValue));
            if (offsetValue != null) {
                stment.setDate(1, new java.sql.Date(offsetValue.date()));
                stment.setString(2, offsetValue.scn());
            }
            resultSet = stment.executeQuery();

            while (resultSet.next()) {
                LOGGER.debug("Reading...");
                list.add(RowMapper.GET.record(tableName, resultSet));
            }

            return offsetValue;
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

    private String getOutboxQuery(String tableName, OffsetRecord offsetValue) {
        return (offsetValue == null) ? String.format(OUTBOX_INIT, tableName) : String.format(OUTBOX_OFFSET, tableName);
    }

}
