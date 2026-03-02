package it.agrimontana.salesforce.service.smiwcon;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.agrimontana.salesforce.monitoring.MonitoringContext;
import it.agrimontana.salesforce.monitoring.MonitoringDAO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class PreorderService {

    private static final Logger logger = Logger.getLogger(PreorderService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    MonitoringDAO monitoringDAO;

    @SuppressWarnings("unchecked")
    public void checkCompositeResponseForErrors(String jsonResponse) throws Exception {

        Map<String, Object> responseMap =
                objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});

        Object cr = responseMap.get("compositeResponse");
        if (!(cr instanceof List)) {
            return;
        }

        List<Map<String, Object>> compositeResponses = (List<Map<String, Object>>) cr;

        List<String> errors = new ArrayList<>();

        for (Map<String, Object> subResponse : compositeResponses) {
            // Can be Integer or Long depending on Jackson -> use Number
            Number httpStatusCodeN = (Number) subResponse.get("httpStatusCode");
            int httpStatusCode = httpStatusCodeN == null ? 0 : httpStatusCodeN.intValue();

            String referenceId = (String) subResponse.get("referenceId");

            if (httpStatusCode >= 400) {
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
                } else if (body instanceof Map) {
                    // sometimes SF returns object instead of array
                    Map<String, Object> err = (Map<String, Object>) body;
                    String message = String.valueOf(err.get("message"));
                    errors.add(String.format("[%s] %s", referenceId, message));
                } else {
                    errors.add(String.format("[%s] HTTP %d", referenceId, httpStatusCode));
                }
            }
        }

        if (!errors.isEmpty()) {
            String errorMessage = String.join("; ", errors);

            // app log
            logger.errorf("Salesforce composite errors: %s", errorMessage);

            // monitoring DB log (must never break business flow)
            try {
                UUID reqId = MonitoringContext.requestId();
                String corr = MonitoringContext.correlationId();

                monitoringDAO.insertLog(
                        "ERROR",
                        "SMIWCON",
                        getClass().getName(),
                        "Salesforce composite request failed: " + errorMessage,
                        reqId,
                        corr,
                        null,
                        null
                );
            } catch (Exception ignore) {
            }

            throw new Exception("Salesforce composite request failed: " + errorMessage);
        }
    }
}