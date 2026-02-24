package it.agrimontana.salesforce.dto.preorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.agrimontana.salesforce.dto.order.lookup.SourceOrderItem;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreorderLineItem {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("NetPrice__c")
    private String netPrice__c;

    @JsonProperty("TotalGrossWithDiscount__c")
    private String totalGrossWithDiscount__c;

    @JsonProperty("Discount1__c")
    private String discount1__c;

    @JsonProperty("Discount2__c")
    private String discount2__c;

    @JsonProperty("Discount3__c")
    private String discount3__c;

    @JsonProperty("Discount4__c")
    private String discount4__c;

    @JsonProperty("Discount5__c")
    private String discount5__c;

    @JsonProperty("TipoRigo__c")
    private String tipoRigo__c;

    @JsonProperty("Note__c")
    private String note__c;

    @JsonProperty("SourceOrderItem__r")
    private SourceOrderItem sourceOrderItem__r;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("CurrencyIsoCode")
    private String currencyIsoCode;


    @JsonProperty("Preordine__c")
    private String preordine;

    @JsonProperty("Discount123AppliedType__c")
    private String discount123AppliedType;

    @JsonProperty("ERPKey__c")
    private String erpKey;

    @JsonProperty("IdRow__c")
    private String idRow;

    @JsonProperty("Line_Total__c")
    private String lineTotal;


    @JsonProperty("OrderProduct__c")
    private String orderProduct;

    @JsonProperty("Product__c")
    private String product;

    @JsonProperty("PricebookEntry__c")
    private String pricebookEntry;

    @JsonProperty("QuantityOmaggio__c")
    private String quantityOmaggio;

    @JsonProperty("QuantityScontoMerce__c")
    private String quantityScontoMerce;

    @JsonProperty("QuantitySold__c")
    private String quantitySold;

    @JsonProperty("Quantity__c")
    private String quantity;

    @JsonProperty("SortNumber__c")
    private String sortNumber;

    @JsonProperty("Status__c")
    private String status;


    @JsonProperty("TotalNetWithDiscount__c")
    private String totalNetWithDiscount;

    @JsonProperty("TotalNetWithoutDiscount__c")
    private String totalNetWithoutDiscount;

    @JsonProperty("VatPercentage__c")
    private String vatPercentage;

    @JsonProperty("toSynch__c")
    private String toSynch;

    @JsonProperty("Product_Code__c")
    private String productCode__c;

    @JsonProperty("Product_ReportFormula__c")
    private String productReportFormula;

    @JsonProperty("Total_Discount__c")
    private String totalDiscount;

    @JsonProperty("ExplainJSON__c")
    private String explainJson;

    @JsonProperty("BundleDefinitionId__c")
    private String bundleDefinitionId;

    @JsonProperty("GeneratedByBundle__c")
    private boolean generatedByBundle;

    // R-specific fields
    @JsonProperty("TipoOfferta__c")
    private String tipoOfferta;

    @JsonProperty("NumeroOfferta__c")
    private String numeroOfferta;

    @JsonProperty("NumeroRigaOfferta__c")
    private String numeroRigaOfferta;

    @JsonProperty("NumeroRigo__c")
    private String numeroRigo;

    @JsonProperty("attributes")
    private Attributes attributes;

    @JsonProperty("Product__r")
    private ProductRef productRef;

    public Double getNPezzi__c() {
        return productRef != null ? productRef.getNPezzi() : null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attributes {

        @JsonProperty("type")
        private String type;

        @JsonProperty("url")
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductRef {

        @JsonProperty("NPezzi__c")
        private Double nPezzi;
    }
}
