package it.agrimontana.salesforce.dto;

import lombok.Data;

@Data
public class PriceBookEntry {
        private String pricebook2Id; // campo lookup recuperare da salesforce
        private String product2Id; // campo lookup recuperare da salesforce
        private String unitPrice;
        private boolean isActive = true;
    }
