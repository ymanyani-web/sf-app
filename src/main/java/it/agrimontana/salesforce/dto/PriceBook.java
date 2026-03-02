package it.agrimontana.salesforce.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceBook {
    private String name;
    private boolean isActive = true;

    @JsonProperty("ERPKey__c")
    private String ERPKey__c;

    @JsonIgnore
    private List<PriceBookEntry> priceBookEntries = new ArrayList<PriceBookEntry>();
}

