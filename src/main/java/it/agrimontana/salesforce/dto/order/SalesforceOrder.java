package it.agrimontana.salesforce.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalesforceOrder {
  private String Name;

  // lookup
  private String AccountId;

  // lookup
  private String Pricebook2Id;

  // lookup
  private String Status = "Draft";
  private String EffectiveDate;

  // R-type booking flag
  private Boolean IsBooking__c;

  private String OrderType__c;
  private String DataUltimaConsegna__c;

  private String BillingStreet;
  private String BillingCity;
  private String BillingState;
  private String BillingPostalCode;
  private String BillingCountry;

  private String ShippingStreet;
  private String ShippingCity;
  private String ShippingState;
  private String ShippingPostalCode;
  private String ShippingCountry;
}
