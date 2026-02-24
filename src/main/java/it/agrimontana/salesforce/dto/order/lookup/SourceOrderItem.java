package it.agrimontana.salesforce.dto.order.lookup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SourceOrderItem {
  @JsonProperty("ERPKey__c")
  private String ERPKey__c;

  @JsonProperty("Order")
  private SourceOrder Order;

  @JsonProperty("OrderId")
  private String OrderId;
}
