package it.agrimontana.salesforce.dto;

import lombok.Data;

@Data
public class SalesforceProduct {
    private String Name;
    private String IsActive = "true";
    private String ProductCode;
    private String Description;
    private String QuantityUnitOfMeasure;
    //private String type;
    private String StockKeepingUnit;
    //private boolean quantityScheduleEnabled;
    //private boolean canUseRevenueSchedule;
    //private String quantityInstallmentPeriod;
}