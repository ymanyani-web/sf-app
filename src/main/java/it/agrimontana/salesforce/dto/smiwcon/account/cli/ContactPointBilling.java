package it.agrimontana.salesforce.dto.smiwcon.account.cli;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactPointBilling {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Email__c")
    private String email;

    @JsonProperty("Address")
    private AddressCompound address;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressCompound {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
