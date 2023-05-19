package es.jambo.outbox.reader;

final class RecordFields {

    private RecordFields() {
        throw new RuntimeException();
    }

    public static final String HEADER_ID = "id";
    public static final String OFFSET = "source.offset";
    public static final String PARTITION = "source.partition";

}
