package es.jambo.outbox.reader;

import es.jambo.outbox.CouldNotOpenConnectionException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Testcontainers
class OutboxQueryTest {

    private final String database = "OBX";
    private final String user = "JAMBO";
    private final String passwd = "JAMBO";
    private String jdbcURL = null;
    @Container
    private OracleContainer oracle = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
            .withDatabaseName(database)
            .withUsername(user)
            .withPassword(passwd);

    @BeforeEach
    public void initDatabase() throws SQLException, FileNotFoundException {
        if (jdbcURL == null) {
            jdbcURL = oracle.getJdbcUrl().replace("@", String.format("%s/%s@", user, passwd));
            var conn = DriverManager.getConnection(jdbcURL);

            Scanner scan = new Scanner(new File("database/02_create_table.sql")).useDelimiter(";");
            while (scan.hasNext()) {
                var st = conn.createStatement();
                st.execute(scan.next());
                st.close();
            }

            scan = new Scanner(new File("src/test/resources/data.sql")).useDelimiter(";");
            while (scan.hasNext()) {
                var st = conn.createStatement();
                st.execute(scan.next());
                st.close();
            }
            conn.close();
        }
    }

    @Test
    void should_throwException_when_jdbcURLIsNotValid() {
        Assertions.assertThatThrownBy(() -> {
            new OutboxQuery("");
        }).isInstanceOf(CouldNotOpenConnectionException.class);
    }

    @Test
    void should_getAll_when_readRecordsWithoutOffset() throws InterruptedException {
        OutboxQuery outboxQuery = new OutboxQuery(jdbcURL);

        final var records = outboxQuery.readRecords("OUTBOX", null);
        Assertions.assertThat(records).isNotNull();
        Assertions.assertThat(records.records()).size().isEqualTo(20);
    }

    @Test
    void should_getNone_when_readRecordsWithLastOffset() throws InterruptedException {
        OutboxQuery outboxQuery = new OutboxQuery(jdbcURL);

        var records = outboxQuery.readRecords("OUTBOX", null);
        Assertions.assertThat(records).isNotNull();
        Assertions.assertThat(records.offset()).isNotNull();

        var newRecords = outboxQuery.readRecords("OUTBOX", records.offset());
        Assertions.assertThat(newRecords.records()).size().isEqualTo(0);
        Assertions.assertThat(newRecords.offset()).isEqualTo(records.offset());

    }

    @Test
    void should_closeConnection_when_stop() throws InterruptedException {
        OutboxQuery outboxQuery = new OutboxQuery(jdbcURL);
        assertDoesNotThrow(() -> outboxQuery.stop());
    }
}