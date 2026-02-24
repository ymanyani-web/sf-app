package it.agrimontana.salesforce.connector.salesforce;

public class CompositeGraphException extends RuntimeException {

  private final String referenceId;
  private final int httpStatusCode;
  private final String errorCode;

  public CompositeGraphException(String message) {
    super(message);
    this.referenceId = null;
    this.httpStatusCode = -1;
    this.errorCode = null;
  }

  public CompositeGraphException(String referenceId,
                                 int httpStatusCode,
                                 String errorCode,
                                 String message) {
    super(message);
    this.referenceId = referenceId;
    this.httpStatusCode = httpStatusCode;
    this.errorCode = errorCode;
  }

  public CompositeGraphException(String referenceId,
                                 int httpStatusCode,
                                 String errorCode,
                                 String message,
                                 Throwable cause) {
    super(message, cause);
    this.referenceId = referenceId;
    this.httpStatusCode = httpStatusCode;
    this.errorCode = errorCode;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public int getHttpStatusCode() {
    return httpStatusCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}

