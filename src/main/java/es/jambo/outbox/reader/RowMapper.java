package es.jambo.outbox.reader;

import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.header.Headers;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
enum RowMapper {
    GET;

    private static final Logger LOGGER = LoggerFactory.getLogger(RowMapper.class);

    public SourceRecord sourceRecord(String partitionId, ResultSet resultSet) throws SQLException {

        final var headers = getHeaders(resultSet);
        final var sourcePartition = getSource(partitionId);
        final var sourceOffset = getOffSet(resultSet);

        final var sourceRecord = new SourceRecord(sourcePartition, sourceOffset, resultSet.getString(OutboxColumns.EVENT_TYPE.name()),
                null, null, resultSet.getString(OutboxColumns.KEY.name()),
                null, resultSet.getString(OutboxColumns.DATA.name()),
                resultSet.getDate(OutboxColumns.CREATE_AT.name()).getTime(), headers);
        LOGGER.debug("SourceRecord: {}", sourceRecord);
        return sourceRecord;
    }

    private Headers getHeaders(ResultSet resultSet) throws SQLException {
        return new ConnectHeaders().addString(RecordFields.HEADER_ID, resultSet.getString(OutboxColumns.EVENT_ID.name()));
    }

    private Map<String, ?> getSource(String partitionId) {
        return Collections.singletonMap(RecordFields.PARTITION, partitionId);
    }

    private Map<String, String> getOffSet(ResultSet resultSet) throws SQLException {
        final var rowId = resultSet.getString(OutboxColumns.OFFSET_ID.name());
        return Collections.singletonMap(RecordFields.OFFSET, rowId);
    }
}
