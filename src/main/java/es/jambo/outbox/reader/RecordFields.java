package es.jambo.outbox.reader;
/**
 * @author Juan Manuel Sánchez Martagón <jmsanchezmartagon@gmail.com>
 */
final class RecordFields {

    private RecordFields() {
        throw new IllegalAccessError();
    }

    public static final String HEADER_ID = "id";
    public static final String OFFSET = "source.offset";
    public static final String PARTITION = "source.partition";

}
