package it.agrimontana.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Odps {
    @JsonProperty("Odps__c")
    private String odp;

}