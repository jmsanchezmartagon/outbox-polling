package es.jambo.outbox.reader;

record OffsetRecord(long date, String scn) {

    public String serialize(){
        return String.format("%d#%s",date,scn);
    }
    public static OffsetRecord deserialize(String offsetSerialized){
        if (offsetSerialized == null || offsetSerialized.isBlank())
            return null;
        String [] tokens = offsetSerialized.split("#");
        return new OffsetRecord(Long.parseLong(tokens[0]),tokens[1]);
    }
}
