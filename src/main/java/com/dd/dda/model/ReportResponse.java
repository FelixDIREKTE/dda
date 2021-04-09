package com.dd.dda.model;

import lombok.Data;

@Data
public class ReportResponse {

    private String fileName;
    private byte[] fileContent;

    public ReportResponse() {
    }

    public ReportResponse(String fileName, byte[] fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }
}
