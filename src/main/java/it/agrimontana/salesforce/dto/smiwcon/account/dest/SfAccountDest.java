package it.agrimontana.salesforce.dto.smiwcon.account.dest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.agrimontana.salesforce.dto.smiwcon.account.AgentInfo;
import it.agrimontana.salesforce.dto.smiwcon.account.Parent;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SfAccountDest {

    @JsonProperty("AccountNumber")
    private String customerReference;

    @JsonProperty("AddressType")
    private String addressType;

    @JsonProperty("ERPKey__c")
    private String customerCode;

    private String newDifferentDestination = "S";

    @JsonProperty("PreferredPaymentMethod__c")
    private String paymentType;

    private String type = "D";

    @JsonProperty("Agent__r")
    private AgentInfo agentObject;

    @JsonProperty("Parent")
    private Parent parentObject;

    @JsonIgnore
    private String agent;

    @JsonProperty("City")
    private String destinationCity;

    @JsonProperty("Street")
    private String destinationAddress;

    @JsonProperty("State")
    private String destinationProvince;

    @JsonProperty("PostalCode")
    private String destinationPostalCode;

    @JsonProperty("Phone__c")
    private String destinationPhone;

    @JsonProperty("Email__c")
    private String destinationEmail;

    @JsonProperty("Country")
    private String destinationCountryIso;

    @JsonProperty("VatNumber__c")
    private String destinationVatNumber;

    @JsonProperty("Id")
    private String id;
}
