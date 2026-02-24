package it.agrimontana.salesforce.service.smiwcon;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AccountService {
    private static final Logger logger = Logger.getLogger(AccountService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean validNumber(String numero) {
        if (numero == null) return false;

        numero = numero.replaceAll("\\s+", "");

        if (numero.startsWith("+39")) {
            numero = numero.substring(3);
        }

        return numero.matches("\\d{10}");
    }


    public void checkCompositeResponseForErrors(String jsonResponse) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> compositeResponses = (List<Map<String, Object>>) responseMap.get("compositeResponse");

        if (compositeResponses == null) {
            return;
        }

        List<String> errors = new ArrayList<>();
        for (Map<String, Object> subResponse : compositeResponses) {
            Integer httpStatusCode = (Integer) subResponse.get("httpStatusCode");
            String referenceId = (String) subResponse.get("referenceId");

            if (httpStatusCode != null && httpStatusCode >= 400) {
                Object body = subResponse.get("body");
                if (body instanceof List) {
                    List<Map<String, Object>> bodyList = (List<Map<String, Object>>) body;
                    for (Map<String, Object> errorBody : bodyList) {
                        String errorCode = (String) errorBody.get("errorCode");
                        String message = (String) errorBody.get("message");
                        if (!"PROCESSING_HALTED".equals(errorCode)) {
                            errors.add(String.format("[%s] %s: %s", referenceId, errorCode, message));
                        }
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            String errorMessage = String.join("; ", errors);
            logger.errorf("Salesforce composite errors: %s", errorMessage);
            throw new Exception("Salesforce composite request failed: " + errorMessage);
        }
    }
}
