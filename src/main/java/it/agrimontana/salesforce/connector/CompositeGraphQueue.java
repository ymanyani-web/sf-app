package it.agrimontana.salesforce.connector;

import java.util.*;

public class CompositeGraphQueue {

    private final List<Map<String, Object>> compositeRequest = new ArrayList<>();
    private String graphId = "graph1";

    public CompositeGraphQueue() {}

    public CompositeGraphQueue(String graphId) {
        this.graphId = graphId;
    }

    public void addNode(String method, String url, String referenceId) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("method", method);
        node.put("url", url);
        node.put("referenceId", referenceId);
        compositeRequest.add(node);
    }

    public void addNodeWithBody(String method, String url, String referenceId, Object body) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("method", method);
        node.put("url", url);
        node.put("referenceId", referenceId);
        node.put("body", body);
        compositeRequest.add(node);
    }

    public Map<String, Object> buildGraph() {
        Map<String, Object> graph = new LinkedHashMap<>();
        graph.put("graphId", graphId);
        graph.put("compositeRequest", compositeRequest);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("graphs", Arrays.asList(graph));
        return payload;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }
}
