package it.agrimontana.salesforce.connector;

import lombok.Data;

import java.util.List;

@Data
public class CompositeRequest {
    private boolean allOrNone = false;
    private List<CompositeSubRequest> compositeRequest;


}

