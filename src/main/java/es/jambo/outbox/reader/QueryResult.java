package es.jambo.outbox.reader;

import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;

record QueryResult(List<SourceRecord> records, String offset) {
}
