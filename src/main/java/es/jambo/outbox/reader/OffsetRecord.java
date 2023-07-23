package es.jambo.outbox.reader;

record OffsetRecord(String scn) {
    OffsetRecord {
        if (scn == null || scn.isBlank())
            throw new IllegalArgumentException();
    }
}

