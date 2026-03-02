package it.agrimontana.salesforce.connector;

import lombok.Data;

import java.util.List;

@Data
public class SalesforceQueryResult<T> {
    private int totalSize;
    private boolean done;
    private List<T> records;
}
