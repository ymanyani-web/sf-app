package it.agrimontana.salesforce.dto.preorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesforceQueryResult<T> {
  // Server per le richieste di tipo query a salesforce
  private int totalSize;
  private boolean done;
  private List<T> records;
}

