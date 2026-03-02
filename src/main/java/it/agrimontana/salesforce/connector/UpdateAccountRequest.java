package it.agrimontana.salesforce.connector;

import it.agrimontana.salesforce.dto.SalesforceAccount;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateAccountRequest {
    private List<SalesforceAccount> customers = new ArrayList<SalesforceAccount>();

}

