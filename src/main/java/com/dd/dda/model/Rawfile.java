package com.dd.dda.model;

public class Rawfile {
    public byte[] bytes;
    public String filename;

    public Rawfile(byte[] bytes, String filename) {
        this.bytes = bytes;
        this.filename = filename;
    }
}
