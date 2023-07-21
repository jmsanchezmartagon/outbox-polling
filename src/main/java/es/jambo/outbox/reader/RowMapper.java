package es.jambo.outbox.reader;

import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.header.Headers;
import org.apache.kafka.connect.source.SourceRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

enum RowMapper {
    GET;

    public SourceRecord record(String partitionId, ResultSet resultSet) throws SQLException {

        final var headers = getHeaders(resultSet);
        final var sourcePartition = getSource(partitionId);
        final var sourceOffset = getOffSet(resultSet);
/*
        return new SourceRecord(sourcePartition, sourceOffset, resultSet.getString(OutboxColumns.EVENT_TYPE.name()),
                null, resultSet.getString(OutboxColumns.DATA.name()));
        */
        System.out.println("RECORDs" + resultSet.getString(OutboxColumns.ID.name()));
        return new SourceRecord(sourcePartition, sourceOffset, resultSet.getString(OutboxColumns.EVENT_TYPE.name()),
                null, null, resultSet.getString(OutboxColumns.KEY.name()),
                null, resultSet.getString(OutboxColumns.DATA.name()), resultSet.getDate(OutboxColumns.CREATE_AT.name()).getTime(), headers);
    }

    private Headers getHeaders(ResultSet resultSet) throws SQLException {
        return new ConnectHeaders().addString(RecordFields.HEADER_ID, resultSet.getString(OutboxColumns.ID.name()));
    }

    private Map<String, ?> getSource(String partitionId) {
        return Collections.singletonMap(RecordFields.PARTITION, partitionId);
    }

    private Map<String, String> getOffSet(ResultSet resultSet) throws SQLException {
        final var lastSCN = resultSet.getString(OutboxColumns.ORA_ROWSCN.name());
        final var lastDate = resultSet.getDate(OutboxColumns.CREATE_AT.name()).getTime();
        return Collections.singletonMap(RecordFields.OFFSET, new OffsetRecord(lastDate, lastSCN).serialize());
    }

}
