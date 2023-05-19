package es.jambo.outbox.reader;

import es.jambo.outbox.CouldNotOpenConnectionException;
import es.jambo.outbox.config.PropertiesPollingConig;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OutboxReaderOLD extends SourceTask {

    private Connection connection;
    private String urlConnection;
    private String[] listOutbox;
    private Date lastDate = null;
    private String lastSCN = null;

    public void initialize(SourceTaskContext context) {
        this.context = context;
    }

    @Override
    public String version() {
        return "0.0.1";
    }

    @Override
    public void start(Map<String, String> map) {
        urlConnection = map.get(PropertiesPollingConig.DATASOURCE_URL);
        String listOutbox = map.get(PropertiesPollingConig.OUTBOX_TABLE_LIST);
        try {
            connection = getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            throw new CouldNotOpenConnectionException(e.getMessage(), e);
        }

        //this.context.offsetStorageReader();//offset("offset.time").get("offset.time");
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List<SourceRecord> list = new LinkedList<>();
        try {
            PreparedStatement stment = connection.prepareStatement(
                    (lastDate != null) ?
                            """
                                    select o.id, o.event_type, o.create_at, o.key, o.data, o.ora_rowscn
                                    from outbox o
                                    """
                            :
                            """
                                    select o.id, o.event_type, o.create_at, o.key, o.data, o.ora_rowscn
                                    from outbox o
                                    where create_at >= ?
                                          and row_scn > ?                
                                                    """);
            if (lastDate != null) {
                stment.setDate(0, new java.sql.Date(lastDate.getTime()));
                stment.setString(1, lastSCN);
            }
            var resultSet = stment.executeQuery();
            while (resultSet.next()) {
                lastSCN = resultSet.getString("row_scn");
                lastDate = resultSet.getDate("create_at");
                final Map<String, ?> sourceOffset = new HashMap<>() {{
                    put("offset.time", lastDate);
                    put("offset.scn", lastSCN);
                }};
                final Map<String, ?> source = new HashMap<>() {{
                    put("source.read", resultSet.getString("outbox"));
                }};

                final var headers = new ConnectHeaders().addString("id", resultSet.getString("ID"));
                var record = new SourceRecord(source, sourceOffset, resultSet.getString("topic"), null, null, resultSet.getString("key"), null, resultSet.getString("value"), null, headers);
                list.add(record);
            }
        } catch (Exception ex) {
            throw new InterruptedException(ex.getMessage());
        }
        return list;
    }

    @Override
    public void stop() {
        try {
            connection.close();
        } catch (SQLException sqlEx) {
            throw new CouldNotOpenConnectionException(sqlEx.getMessage(), sqlEx);
        }
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection(urlConnection);
        try (final var statement = conn.createStatement()) {
            statement.execute("call DBMS_APPLICATION_INFO.SET_CLIENT_INFO('Outbox')");
        }
        return conn;
    }
}
