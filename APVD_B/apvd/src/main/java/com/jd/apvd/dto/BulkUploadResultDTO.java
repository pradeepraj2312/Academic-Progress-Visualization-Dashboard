package com.jd.apvd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResultDTO {

    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<String> errors = new ArrayList<>();

    public void incrementTotalRows() {
        this.totalRows++;
    }

    public void incrementSuccessCount() {
        this.successCount++;
    }

    public void addError(String errorMessage) {
        this.failedCount++;
        this.errors.add(errorMessage);
    }
}
