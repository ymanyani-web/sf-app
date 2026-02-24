package it.agrimontana.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SalesforceAccount {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("CustomerCode__c")
    private String CustomerCode__c;
    @JsonProperty("CodAnagrafico__c")
    private int CodAnagrafico__c;
    @JsonProperty("VatToInclude__c")
    private boolean VatToInclude__c;
    @JsonProperty("VatNumber__c")
    private String VatNumber__c;
    @JsonProperty("FiscalCode__c")
    private String FiscalCode__c;
    @JsonProperty("AreaNielsen__c")
    private String AreaNielsen__c;
    @JsonProperty("SdiCode__c")
    private String SdiCode__c;
    @JsonProperty("ERPKey__c")
    private String ERPKey__c;
    @JsonProperty("email")
    private String email;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("PreferredPaymentMethod__c")
    private String PreferredPaymentMethod__c;
    @JsonProperty("BankName__c")
    private String BankName__c;
    @JsonProperty("ABI__c")
    private String ABI__c;
    @JsonProperty("CAB__C")
    private String CAB__C;
    @JsonProperty("IBAN__c")
    private String IBAN__c;
    @JsonProperty("addresses")
    private List<SalesforceAddresses> addresses;

}