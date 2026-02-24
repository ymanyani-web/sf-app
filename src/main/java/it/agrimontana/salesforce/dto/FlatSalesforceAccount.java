package it.agrimontana.salesforce.dto;

import lombok.Data;

@Data
public class FlatSalesforceAccount {

    private String id;
    private String name;
    private String accountNumber;
    private String type;
    private String industry;
    private String rating;
    private String phone;
    private String website;
    private String fax;

    private String billingStreet;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;

    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    //private LocalDateTime createdDate;
    //private LocalDateTime lastModifiedDate;
}