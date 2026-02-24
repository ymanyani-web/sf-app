package it.agrimontana.salesforce.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalesforceOrderLines {
  // lookup
  private String Product2Id;

  // lookup
  private String PricebookEntryId;

  // lookup
  private String OrderId;

  // sconti
  private String Discount1__c;
  private String Discount2__c;
  private String Discount3__c;
  private String Discount4__c;

  private String Description;
  private String UnitPrice;
  private String Quantity;
  private String TipoRigo__c;
  private String NumeroRigo__c;
  private String Lotto__c;
  private String Collo__c;
  private String ERPKey__c;
}
