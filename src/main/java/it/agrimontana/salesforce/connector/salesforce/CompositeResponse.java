package it.agrimontana.salesforce.connector.salesforce;


import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CompositeResponse {
  private List<ErrorBody> body;
  private Map<String, Object> httpHeaders;
  private int httpStatusCode;
  private String referenceId;
}
