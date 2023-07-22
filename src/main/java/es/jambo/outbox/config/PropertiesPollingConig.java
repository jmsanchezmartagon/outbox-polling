package es.jambo.outbox.config;

public final class PropertiesPollingConig {

    private PropertiesPollingConig() {
        throw new IllegalAccessError();
    }

    public static final String VERSION = "0.0.1";
    public static final String DATASOURCE_URL = "datasource.url";
    public static final String OUTBOX_TABLE_LIST = "outbox.table.list";
    public static final String OUTBOX_LIST_TOKEN = ",";
}
