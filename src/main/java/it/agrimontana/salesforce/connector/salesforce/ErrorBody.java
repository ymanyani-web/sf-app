package it.agrimontana.salesforce.connector.salesforce;

import lombok.Data;

@Data
public class ErrorBody {
  private String errorCode;
  private String message;
}

