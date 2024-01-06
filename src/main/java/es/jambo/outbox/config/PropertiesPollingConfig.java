package es.jambo.outbox.config;

/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
public final class PropertiesPollingConfig {

    private PropertiesPollingConfig() {
        throw new IllegalAccessError();
    }

    public static final String VERSION = "0.0.1";
    public static final String DATASOURCE_URL = "datasource.url";
    public static final String OUTBOX_TABLE_LIST = "outbox.table.list";
    public static final String POOL_INTERVAL_MS = "poll.interval.ms";
    public static final String OUTBOX_LIST_TOKEN = ",";
}
