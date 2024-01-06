package es.jambo.outbox.reader;

import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;
/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
record QueryResult(List<SourceRecord> records, String offset) {
}
