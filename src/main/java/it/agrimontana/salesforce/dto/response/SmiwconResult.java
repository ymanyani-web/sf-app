package it.agrimontana.salesforce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmiwconResult {
    private int statusCode;
    private String status;
    private String id;
    private String errorCode;
    private String errorMessage;

    private SmiwconResult() {}

    public static SmiwconResult success(String id) {
        SmiwconResult result = new SmiwconResult();
        result.statusCode = 200;
        result.status = "SUCCESS";
        result.id = id;
        return result;
    }

    public static SmiwconResult error(String errorMessage) {
        SmiwconResult result = new SmiwconResult();
        result.statusCode = 500;
        result.status = "ERROR";
        result.errorCode = "JGALILEO_ERROR";
        result.errorMessage = errorMessage;
        return result;
    }

    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }
}
