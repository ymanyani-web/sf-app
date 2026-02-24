package it.agrimontana.salesforce.dto.preorder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreorderAccount {

    @JsonProperty("Id")
    private String Id;

    @JsonProperty("Pricebook__c")
    private String Pricebook__c;

    @JsonProperty("ERPKey__c")
    private String AccountNumber;

    @JsonProperty("Agent__c")
    private String agentId;

    @JsonProperty("CapoArea__c")
    private String capoAreaId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("TipologiaCliente__c")
    private String tipologiaCliente__c;

    @JsonProperty("PreferredPaymentMethod__c")
    private String preferredPaymentMethod;
}
