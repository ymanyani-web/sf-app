package it.agrimontana.salesforce.dto.smiwcon.account.cli;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.agrimontana.salesforce.dto.smiwcon.account.AgentInfo;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SfAccountCli {

    @JsonProperty("Id")
    private String Id;

    @JsonProperty("AccountNumber")
    private String customerReference;   // Riferimento cliente

    @JsonProperty("ERPKey__c")
    private String ERPKey__c;

    @JsonProperty("Agent__r")
    private AgentInfo agentObject;

    @JsonProperty("CapoArea__r")
    private AgentInfo capoAreaObject;

    @JsonProperty("Name")
    private String companyName;         // Ragione sociale cliente

    @JsonProperty("BillingStreet")
    private String address;             // Indirizzo cliente

    @JsonProperty("FirstNamePF__c")
    private String firstName;           // Nome cliente

    @JsonProperty("LastNamePF__c")
    private String lastName;            // Cognome cliente

    @JsonProperty("PreferredPaymentMethod__c")
    private String paymentType;         // 006

    @JsonProperty("BillingCity")
    private String city;                // Città del cliente

    @JsonProperty("BillingState")
    private String province;            // Provincia (SIGLA)

    @JsonProperty("BillingPostalCode")
    private String postalCode;          // CAP della città del cliente

    @JsonProperty("Phone")
    private String phone;               // Numero di telefono

    @JsonProperty("Email__c")
    private String email;               // Email cliente

    @JsonProperty("BillingCountry")
    private String countryIso;          // Codice ISO del cliente

    @JsonProperty("VatNumber__c")
    private String vatNumber;           // Partita IVA del cliente

    @JsonProperty("FiscalCode__c")
    private String FiscalCode__c;

    @JsonProperty("IBAN__c")
    private String iban;

    @JsonProperty("ABI__c")
    private String abi;

    @JsonProperty("CAB__c")
    private String cab;

    @JsonProperty("BankName__c")
    private String nomeBanca;

    @JsonProperty("CreatedDate")
    private String dataCreazione;

    @JsonProperty("Pec__c")
    private String pec;

    @JsonProperty("SdiCode__c")
    private String sdiCode;

    @JsonProperty("TipologiaCliente__c")
    private String tipologiaCliente__c;

    @JsonIgnore
    private String agent; // Codice agente Galileo da associare al cliente

}
