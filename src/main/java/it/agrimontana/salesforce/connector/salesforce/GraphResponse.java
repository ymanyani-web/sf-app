package it.agrimontana.salesforce.connector.salesforce;

import lombok.Data;

import java.util.List;

@Data
public class GraphResponse {
  private List<CompositeResponse> compositeResponse;
}

