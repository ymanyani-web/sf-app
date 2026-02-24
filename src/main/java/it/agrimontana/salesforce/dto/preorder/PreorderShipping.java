package it.agrimontana.salesforce.dto.preorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreorderShipping {
    @JsonProperty("ERPKey__c")
    private String ERPKey__c;

    @JsonProperty("AddressType")
    private String addressType;

    @JsonProperty("Street")
    private String street;

    @JsonProperty("City")
    private String city;

    @JsonProperty("State")
    private String state;

    @JsonProperty("PostalCode")
    private String postalCode;

    @JsonProperty("Country")
    private String country;
}
