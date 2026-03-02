package it.agrimontana.salesforce.dto.preorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Preorder {
    @JsonProperty("Id")
    private String Id;

    @JsonProperty("Account__c")
    private String Account__c;

    @JsonProperty("PaymentMethod__c")
    private String PaymentMethod__c;

    @JsonProperty("Order__c")
    private String Order__c;

    @JsonProperty("ERPKey__c")
    private String ERPKey__c;

    @JsonProperty("IsBooking__c")
    private Boolean isBooking;

    @JsonProperty("ShippingTo__c")
    private String ShippingTo__c;

    @JsonProperty("BillingTo__c")
    private String BillingTo__c;

    // W-specific fields
    @JsonProperty("TipologiaVendita__c")
    private String tipologiaVendita;

    @JsonProperty("Pagamento__c")
    private String pagamento;

    @JsonProperty("Tipologia__c")
    private String tipologiaDiVendita;

    @JsonProperty("DataConsegna__c")
    private String dataPrimaScadenza;

    @JsonProperty("Note__c")
    private String note;

    @JsonProperty("Pricebook__c")
    private String pricebook2;

    @JsonProperty("AgentRifKey__c")
    private String riferimentoCliente;
}
