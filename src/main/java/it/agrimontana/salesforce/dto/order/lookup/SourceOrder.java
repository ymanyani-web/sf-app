package it.agrimontana.salesforce.dto.order.lookup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SourceOrder {

  @JsonProperty("ERPKey__c")
  private String ERPKey__c;
}
