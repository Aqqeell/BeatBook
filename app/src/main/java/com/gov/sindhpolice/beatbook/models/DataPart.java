package com.gov.sindhpolice.beatbook.models;

public class DataPart {
    private String fileName;
    private byte[] data;
    private String type;

    public DataPart(String fileName, byte[] data, String type) {
        this.fileName = fileName;
        this.data = data;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public String getType() {
        return type;
    }
}
