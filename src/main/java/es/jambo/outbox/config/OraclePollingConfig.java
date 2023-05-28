package es.jambo.outbox.config;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public final class OraclePollingConfig extends AbstractConfig {

    private static ConfigDef configDef;

    public OraclePollingConfig(Map<?, ?> originals) {
        super(configPollingDefinition(), originals);
    }


    public static ConfigDef configPollingDefinition() {
        if (configDef == null) {
            configDef = new ConfigDef().define(PropertiesPollingConig.DATASOURCE_URL, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Configuration for jdbc connection")
                    .define(PropertiesPollingConig.OUTBOX_TABLE_LIST, ConfigDef.Type.LIST, ConfigDef.Importance.HIGH, "List at the tables to do polling. schema.table_name");
        }
        return configDef;
    }
}
