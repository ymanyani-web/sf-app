package it.agrimontana.salesforce.connector;

import lombok.Data;

@Data
public  class CompositeSubRequest {
    public static final int ACCOUNT= 0;
    public static final int PRICEBOOK= 1;
    public static final int PRICEBOOKENTRY= 2;
    public static final int QUERY= 3;
    private String method = "POST";
    private String url;
    private String referenceId;
    private Object body;

    public CompositeSubRequest(){
    }

    public CompositeSubRequest(int type) {
        if (type == ACCOUNT)
            url = "/services/data/v54.0/sobjects/Account";
        if (type == PRICEBOOK)
            url = "/services/data/v54.0/sobjects/Pricebook2";
        if (type == PRICEBOOKENTRY)
            url = "/services/data/v54.0/sobjects/PricebookEntry";
        if (type == QUERY)
            url = "/services/data/v54.0/query/?q=";

    }
    public static CompositeSubRequest newAccount() {
        return new CompositeSubRequest(ACCOUNT);
    }
    public static CompositeSubRequest newPriceBook() {
        return new CompositeSubRequest(PRICEBOOK);
    }
    public static CompositeSubRequest newPriceBookEntry() {
        return new CompositeSubRequest(PRICEBOOKENTRY);
    }
    public static CompositeSubRequest newQuery(String query) {
        CompositeSubRequest csr =  new CompositeSubRequest(QUERY);
        csr.setMethod("GET");
        csr.setUrl(csr.getUrl()+query);
        return csr;
    }

}