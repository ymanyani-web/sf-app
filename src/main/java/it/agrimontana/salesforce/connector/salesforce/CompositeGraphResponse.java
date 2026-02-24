package it.agrimontana.salesforce.connector.salesforce;

import lombok.Data;

import java.util.List;

@Data
public class CompositeGraphResponse {
  private List<Graph> graphs;
  private boolean isSuccessful;
}
