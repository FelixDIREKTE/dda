package com.dd.dda.model;

public class Rawfile {
    public byte[] bytes;
    public String filename;
    public String path;

    public Rawfile(byte[] bytes, String filename, String path) {
        this.bytes = bytes;
        this.filename = filename;
        this.path = path;
    }
}
