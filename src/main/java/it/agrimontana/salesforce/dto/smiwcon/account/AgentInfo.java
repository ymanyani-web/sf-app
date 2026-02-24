package it.agrimontana.salesforce.dto.smiwcon.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentInfo {
    @JsonProperty("ERPKey__c")
    private String erpKey;
}
