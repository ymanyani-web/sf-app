package it.agrimontana.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SalesforceAddresses {
    @JsonProperty("ERPKey__c")
    private String ERPKey__c;
    @JsonProperty("address")
    private SalesforceAddress address;
    @JsonProperty("Name")
    private String Name;
    @JsonProperty("Email__c")
    private String Email__c;
    @JsonProperty("isPrimary")
    private boolean isPrimary;
    @JsonProperty("Label")
    private String Label;
}
