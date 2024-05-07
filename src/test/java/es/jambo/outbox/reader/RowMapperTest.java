package es.jambo.outbox.reader;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;
/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
@ExtendWith(MockitoExtension.class)
class RowMapperTest {

    @Mock
    private ResultSet resultSet;

    @Test
    void should_getSourceRecord_when_mapResultSet() throws SQLException {
        final var partitionId = UUID.randomUUID().toString();
        final var lastRow = UUID.randomUUID().toString();
        final var id = UUID.randomUUID().toString();
        final var key = UUID.randomUUID().toString();
        final var type = UUID.randomUUID().toString();
        final var data = "{}";

        BDDMockito.when(resultSet.getString(OutboxColumns.OFFSET_ID.name())).thenReturn(lastRow);
        BDDMockito.when(resultSet.getString(OutboxColumns.EVENT_ID.name())).thenReturn(id);
        BDDMockito.when(resultSet.getString(OutboxColumns.EVENT_TYPE.name())).thenReturn(type);
        BDDMockito.when(resultSet.getString(OutboxColumns.KEY.name())).thenReturn(key);
        BDDMockito.when(resultSet.getString(OutboxColumns.DATA.name())).thenReturn(data);
        BDDMockito.when(resultSet.getDate(OutboxColumns.CREATE_AT.name())).thenReturn(new Date(System.currentTimeMillis()));

        final var record = RowMapper.GET.sourceRecord(partitionId, resultSet);

        Assertions.assertThat(record).isNotNull();
        record.headers().forEach(
                header -> {
                    Assertions.assertThat(header.key()).isEqualTo(RecordFields.HEADER_ID);
                    Assertions.assertThat(header.value()).isEqualTo(id);
                }
        );

        Assertions.assertThat(record.sourcePartition()).isEqualTo(Collections.singletonMap(RecordFields.PARTITION, partitionId));
        Assertions.assertThat(record.sourceOffset()).isEqualTo(Collections.singletonMap(RecordFields.OFFSET, lastRow));
    }

}