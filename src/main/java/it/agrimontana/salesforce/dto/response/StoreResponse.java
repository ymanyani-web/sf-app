package it.agrimontana.salesforce.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StoreResponse {
    private String status;
    private List<SmiwconResult> results;

    public StoreResponse() {
        this.results = new ArrayList<>();
    }

    public void addResult(SmiwconResult result) {
        this.results.add(result);
    }

    public void calculateStatus() {
        if (results.isEmpty()) {
            this.status = "ERROR";
            return;
        }

        long successCount = results.stream().filter(SmiwconResult::isSuccess).count();

        if (successCount == results.size()) {
            this.status = "SUCCESS";
        } else if (successCount == 0) {
            this.status = "ERROR";
        } else {
            this.status = "PARTIAL";
        }
    }

    public int getHttpStatusCode() {
        switch (status) {
            case "SUCCESS":
                return 200;
            case "PARTIAL":
                return 207;
            default:
                return 500;
        }
    }
}
