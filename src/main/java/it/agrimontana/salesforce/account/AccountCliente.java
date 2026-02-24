package it.agrimontana.salesforce.account;

import it.agrimontana.salesforce.service.database.As400Service;
import it.agrimontana.salesforce.service.salesforce.SalesforceService;
import it.agrimontana.salesforce.account.builder.SmiwconCliente;
import it.agrimontana.salesforce.connector.CompositeSubRequest;
import it.agrimontana.salesforce.connector.SalesforceQueryResult;
import it.agrimontana.salesforce.dto.response.StoreResponse;
import it.agrimontana.salesforce.dto.response.SmiwconResult;
import it.agrimontana.salesforce.dto.smiwcon.account.cli.SfAccountCli;
import it.agrimontana.salesforce.dto.smiwcon.account.dest.SfAccountDest;
import it.agrimontana.salesforce.service.smiwcon.AccountService;
import it.agrimontana.salesforce.service.smiwcon.SmiwconRestService;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountCliente extends Account {
  private static final Logger logger = Logger.getLogger(AccountCliente.class);

  private String accountId;
  private SfAccountCli client;
  private SfAccountDest billingDestination;
  private List<SfAccountDest> destinations;
  private StoreResponse response = new StoreResponse();

  public AccountCliente(String id, SalesforceService salesforceService, SmiwconRestService smiwconRestService, AccountService accountService, As400Service as400Service) {
    super(id, salesforceService, smiwconRestService, accountService, as400Service);
    logger.debugf("Nuova istanza oggetto: %s, con id: %s", AccountCliente.class.getSimpleName(), id);

  }

  public void getDataFromSalesforce() {
    SalesforceQueryResult<SfAccountCli> client = getStandardAccountFromClient(SfAccountCli.class);
    SalesforceQueryResult<SfAccountDest> destination = getStandardContactPointFromClient(SfAccountDest.class);

    super.validateSalesforceData(client.getRecords().get(0), destination.getRecords());

    this.client = client.getRecords().get(0);
    this.destinations = new ArrayList<>(destination.getRecords());

    // Debug: verifica Agent e CapoArea
    logger.infof("Account %s - Agent__r: %s, CapoArea__r: %s",
        this.client.getId(),
        this.client.getAgentObject() != null ? this.client.getAgentObject().getErpKey() : "NULL",
        this.client.getCapoAreaObject() != null ? this.client.getCapoAreaObject().getErpKey() : "NULL");

    extractBillingDestination();

    this.accountId = super.checkAccount(this.client.getVatNumber(), this.client.getERPKey__c());
  }

  private void extractBillingDestination() {
    for (SfAccountDest destination : destinations) {
      if ("Billing".equals(destination.getAddressType())) {
        this.billingDestination = destination;
        break;
      }
    }
    if (billingDestination != null) {
      destinations.remove(billingDestination);
    }
  }

  public SmiwconCliente structSmiwconCliente() {
    logger.debugf(
        "Costruzione dto smiwcon, funzione: %s, tipologia: cliente, account: %s",
        "structSmiwconOrder",
        client.getId()
    );

    logger.debugf("ABI, CAB, IBAN: %s, %s, %s", client.getAbi(), client.getCab(), client.getIban());


    SmiwconCliente cliente = new SmiwconCliente.Builder()
        .newCustomer(client.getERPKey__c())
        .username(client.getCompanyName())
        .agent(client.getAgentObject(), client.getCapoAreaObject())
        .customerReference(client.getCompanyName())
        .companyName(client.getCompanyName())
        .firstName(client.getFirstName())
        .lastName(client.getLastName())
        .paymentType(client.getPaymentType())
        .phone(client.getPhone())
        .email(client.getEmail())
        .vatNumber(client.getVatNumber())
        .address(billingDestination.getDestinationAddress())
        .city(billingDestination.getDestinationCity())
        .province(billingDestination.getDestinationProvince())
        .postalCode(billingDestination.getDestinationPostalCode())
        .countryIso(billingDestination.getDestinationCountryIso())
        .taxCode(client.getFiscalCode__c())
        .iban(client.getIban(), client.getAbi(), client.getCab())
        .abi(client.getAbi())
        .cab(client.getCab())
        .supportingBank(client.getNomeBanca())
        .customerAcquisitionDate(client.getDataCreazione())
        .pecAddress(client.getPec())
        .sdiCode(client.getSdiCode())
        .template(client.getTipologiaCliente__c())
        .type("C")
        .build();

    return cliente;

  }

  public void smiwconAccountId() {
    if(super.isBlank(this.accountId)) {
      SmiwconResult result = super.sendToSmiwcon(structSmiwconCliente());
      response.addResult(result);
      if (result.isSuccess()) {
        this.accountId = result.getId();
      }
    } else {
      // Account trovato nel DB, aggiungo risultato di successo
      response.addResult(SmiwconResult.success(this.accountId));
    }
  }

  public void sendSalesforceAccounts() {
    List<CompositeSubRequest> requests = new ArrayList<>();

    if (accountId != null) {
      requests.add(clientAccountPatch(accountId, client.getId()));
      if (billingDestination != null) {
        requests.add(clientBillingPatch(accountId));
      }
    }

    int idx = 0;
    for(SfAccountDest destination : this.destinations) {
      if(isBlank(destination.getCustomerCode())) {
        AccountDestinazione destinazione = new AccountDestinazione(this.accountId, salesforceService, smiwconRestService, accountService, as400Service);
        SmiwconResult result = super.sendToSmiwcon(destinazione.structSmiwconDestinazione(destination, client));
        response.addResult(result);
        if (result.isSuccess()) {
          requests.add(destinationAccountPatch(result.getId(), destination.getId(), idx));
          idx++;
        }
      }
    }

    if (!requests.isEmpty()) {
      super.sendAccountToSalesforce(requests);
    }

    response.calculateStatus();
  }

  public StoreResponse getResponse() {
    return response;
  }

  public CompositeSubRequest destinationAccountPatch(String erpkey, String destinationId, int idx) {
    logger.debugf(
        "Body http nella classe: %s, funzione: %s",
        AccountCliente.class.getSimpleName(),
        "destinationAccountPatch"
    );

    Map<String, Object> destinationBody = new HashMap<>();
    CompositeSubRequest destinationPatch = new CompositeSubRequest();

    destinationPatch.setUrl("/services/data/" + super.salesforceService.getApiVersion() + "/sobjects/ContactPointAddress/" + destinationId);
    destinationPatch.setReferenceId("patchDestination" + idx);
    destinationPatch.setMethod("PATCH");

    destinationBody.put("ERPKey__c", erpkey);
    destinationPatch.setBody(destinationBody);
    return destinationPatch;
  }

  public CompositeSubRequest clientAccountPatch(String erpkey, String clientId) {
    logger.debugf(
        "Body http nella classe: %s, funzione: %s",
        AccountCliente.class.getSimpleName(),
        "clientAccountPatch"
    );

    Map<String, Object> accountBody = new HashMap<>();
    CompositeSubRequest accountPatch = new CompositeSubRequest();

    accountPatch.setUrl("/services/data/" + super.salesforceService.getApiVersion() + "/sobjects/Account/" + clientId);
    accountPatch.setReferenceId("patchAccount");
    accountPatch.setMethod("PATCH");

    accountBody.put("ERPKey__c", erpkey);
    accountPatch.setBody(accountBody);
    return accountPatch;
  }

  public CompositeSubRequest clientBillingPatch(String erpkey) {
    logger.debugf(
        "Body http nella classe: %s, funzione: %s",
        AccountCliente.class.getSimpleName(),
        "clientBillingPatch"
    );

    Map<String, Object> accountBody = new HashMap<>();
    CompositeSubRequest accountPatch = new CompositeSubRequest();

    accountPatch.setUrl("/services/data/" + super.salesforceService.getApiVersion() + "/sobjects/ContactPointAddress/" + this.billingDestination.getId());
    accountPatch.setReferenceId("patchBillingContact");
    accountPatch.setMethod("PATCH");

    accountBody.put("ERPKey__c", erpkey);
    accountPatch.setBody(accountBody);
    return accountPatch;
  }
}
