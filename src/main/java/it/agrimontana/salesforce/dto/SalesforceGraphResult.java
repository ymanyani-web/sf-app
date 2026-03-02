package it.agrimontana.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.agrimontana.salesforce.connector.salesforce.CompositeGraphException;
import it.agrimontana.salesforce.connector.salesforce.ErrorBody;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesforceGraphResult {
    private List<Graph> graphs;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Graph {
        private String graphId;
        private GraphResponse graphResponse;
        private boolean isSuccessful;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GraphResponse {
        private List<CompositeResponse> compositeResponse;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompositeResponse {
        private Object body;
        private Map<String, String> httpHeaders;
        private int httpStatusCode;
        private String referenceId;
    }

    /**
     * Helper method to get body by referenceId and convert to specific type
     */
    public <T> T getBodyAs(String referenceId, Class<T> clazz) {
        if (graphs == null || graphs.isEmpty()) return null;

        Graph graph = graphs.get(0);
        if (graph.getGraphResponse() == null || graph.getGraphResponse().getCompositeResponse() == null) {
            return null;
        }

        for (CompositeResponse response : graph.getGraphResponse().getCompositeResponse()) {
            if (referenceId.equals(response.getReferenceId())) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.convertValue(response.getBody(), clazz);
            }
        }
        return null;
    }

    /**
     * Check if all requests were successful
     */
    public boolean isAllSuccessful() {
        if (graphs == null || graphs.isEmpty()) return false;
        return graphs.stream().allMatch(Graph::isSuccessful);
    }

    public void throwIfCompositeError() {

        CompositeResponse rootCause = null;

        if (graphs == null || graphs.isEmpty()) {
            return;
        }

        for (Graph graph : graphs) {
            if (graph.getGraphResponse() == null
                || graph.getGraphResponse().getCompositeResponse() == null) {
                continue;
            }

            for (CompositeResponse cr : graph.getGraphResponse().getCompositeResponse()) {
                if (cr.getHttpStatusCode() >= 400 && hasRealError(cr)) {
                    rootCause = cr;
                    break;
                }
            }
            if (rootCause != null) break;
        }

        if (rootCause == null) {
            for (Graph graph : graphs) {
                if (graph.getGraphResponse() == null
                    || graph.getGraphResponse().getCompositeResponse() == null) {
                    continue;
                }

                for (CompositeResponse cr : graph.getGraphResponse().getCompositeResponse()) {
                    if (cr.getHttpStatusCode() >= 400) {
                        rootCause = cr;
                        break;
                    }
                }
                if (rootCause != null) break;
            }
        }

        if (rootCause == null) {
            return;
        }

        ErrorBody err = extractError(rootCause.getBody());

        throw new CompositeGraphException(
            err.getMessage(),
            rootCause.getHttpStatusCode(),
            err != null ? err.getErrorCode() : null,
            err != null ? String.format("Errore campo non valido o assente %s",rootCause.getReferenceId()) : "Errore Salesforce Composite"
        );
    }


    private boolean hasRealError(CompositeResponse cr) {
        ErrorBody err = extractError(cr.getBody());
        return err != null && !"PROCESSING_HALTED".equals(err.getErrorCode());
    }

    private ErrorBody extractError(Object body) {
        if (body == null) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            List<?> list = mapper.convertValue(body, List.class);
            if (list.isEmpty()) {
                return null;
            }
            return mapper.convertValue(list.get(0), ErrorBody.class);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }



}
