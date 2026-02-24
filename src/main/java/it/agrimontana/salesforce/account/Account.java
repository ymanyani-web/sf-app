package it.agrimontana.salesforce.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.agrimontana.salesforce.dto.order.SalesforceOrder;
import it.agrimontana.salesforce.dto.preorder.Preorder;
import it.agrimontana.salesforce.dto.preorder.PreorderAccount;
import it.agrimontana.salesforce.dto.preorder.PreorderLineItem;
import it.agrimontana.salesforce.dto.preorder.PreorderShipping;
import it.agrimontana.salesforce.dto.smiwcon.account.cli.SfAccountCli;
import it.agrimontana.salesforce.dto.smiwcon.account.dest.SfAccountDest;
import it.agrimontana.salesforce.service.database.As400Service;
import it.agrimontana.salesforce.service.salesforce.SalesforceService;
import it.agrimontana.salesforce.ordine.Preordine;
import it.agrimontana.salesforce.connector.CompositeRequest;
import it.agrimontana.salesforce.connector.CompositeSubRequest;
import it.agrimontana.salesforce.connector.SalesforceQueryResult;
import it.agrimontana.salesforce.dto.response.SmiwconResult;
import it.agrimontana.salesforce.service.smiwcon.AccountService;
import it.agrimontana.salesforce.service.smiwcon.SmiwconRestService;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static it.agrimontana.salesforce.service.smiwcon.AccountService.validNumber;

public class Account {
  protected String id;
  protected ObjectMapper salesforceObjectMapper = new ObjectMapper();
  protected SalesforceService salesforceService;
  protected SmiwconRestService smiwconRestService;
  protected AccountService accountService;
  protected As400Service as400Service;

  private static final Logger logger = Logger.getLogger(Account.class);

  public Account(String id, SalesforceService salesforceService, SmiwconRestService smiwconRestService, AccountService accountService, As400Service as400Service) {
    this.id = id;
    this.salesforceService = salesforceService;
    this.smiwconRestService = smiwconRestService;
    this.accountService = accountService;
    this.as400Service = as400Service;
  }

  public String standardQueryAccount() {
    return String.format(
        "SELECT Id, Agent__r.ERPKey__c, CapoArea__r.ERPKey__c, ERPKey__c, Name, FirstNamePF__c, LastNamePF__c, ABI__c, CAB__c, BankName__c, CreatedDate, " +
            "PreferredPaymentMethod__c, FiscalCode__c, IBAN__c, SdiCode__c, Pec__c, Phone, Email__c, VatNumber__c, TipologiaCliente__c FROM Account WHERE Id = '%s'",
        id);
  }

  public String standardQueryContactPoint() {
    return String.format(
        "SELECT Id, ERPKey__c, AddressType, Email__c, Phone__c, Parent.Name, City, State, PostalCode, Country, Street " +
            "FROM ContactPointAddress WHERE ParentId = '%s'",
        id);
  }

  public <T>SalesforceQueryResult<T> getStandardAccountFromClient(Class<T> clazz) {
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s",
        Account.class.getSimpleName(),
        "getStandardAccountFromClient"
    );

    try (Response standardResponse = salesforceService.salesforceQueryGet(this.standardQueryAccount())) {
      return readStandardResponse(standardResponse, clazz);
    } catch (Exception e) {
      logger.errorf(
          "Error class: %s, function: %s, error: %s",
          Account.class.getSimpleName(),
          "getStandardAccountFromClient",
          e
      );
      throw new RuntimeException("Error funzione getStandardAccountFromClient", e);
    }
  }

  public <T>SalesforceQueryResult<T> getStandardContactPointFromClient(Class<T> clazz) {
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s",
        Account.class.getSimpleName(),
        "getStandardContactPointFromClient"
    );

    try (Response standardResponse = salesforceService.salesforceQueryGet(this.standardQueryContactPoint())) {
      return readStandardResponse(standardResponse, clazz);
    } catch (Exception e) {
      logger.errorf(
          "Error class: %s, function: %s, error: %s",
          Account.class.getSimpleName(),
          "getStandardContactPointFromClient",
          e
      );
      throw new RuntimeException("Error funzione getStandardContactPointFromClient", e);
    }
  }

  public <T> SalesforceQueryResult<T> readStandardResponse(Response response, Class<T> clazz) {
    try {
      String json = response.readEntity(String.class);

      JavaType type = salesforceObjectMapper.getTypeFactory()
          .constructParametricType(SalesforceQueryResult.class, clazz);

      return salesforceObjectMapper.readValue(json, type);

    } catch (Exception e) {
      throw new RuntimeException(
          "Deserializzazione standard json fallita, errore salesforce", e
      );
    }
  }

  public SmiwconResult sendToSmiwcon(Object account) {
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s",
        Account.class.getSimpleName(),
        "sendToSmiwcon"
    );

    try (Response response = smiwconRestService.createPartnerV2(account)) {
      return readSmiwconResponse(response);
    } catch (Exception e) {
      logger.errorf(
          "Error class: %s, function: %s, error: %s",
          Preordine.class.getSimpleName(),
          "sendToSmiwcon",
          e
      );
      return SmiwconResult.error(e.getMessage());
    }
  }

  public SmiwconResult readSmiwconResponse(Response response) {
    try {
      String rawValue = response.readEntity(String.class);
      logger.debugf("Smiwcon raw response: %s", rawValue);

      Map json = salesforceObjectMapper.readValue(rawValue, Map.class);
      String status = (String) json.get("status");

      if ("ERROR".equals(status)) {
        String errors = String.valueOf(json.get("errors"));
        String errorsRow = String.valueOf(json.get("errorsRow"));
        String allErrors = errors + " " + errorsRow;
        logger.errorf("Smiwcon ha restituito un errore: errors=%s, errorsRow=%s", errors, errorsRow);
        return SmiwconResult.error(allErrors);
      }

      String erpKey = (String) json.get("newPartnerCode");
      return SmiwconResult.success(erpKey);

    } catch (JsonProcessingException e) {
      logger.errorf("Errore deserializzazione JSON: %s", e.getMessage());
      return SmiwconResult.error("Errore deserializzazione JSON: " + e.getMessage());
    } catch (Exception e) {
      logger.errorf("Error processing JGalileo response: %s", e.getMessage());
      return SmiwconResult.error("Errore sconosciuto: " + e.getMessage());
    }
  }

  public void sendAccountToSalesforce(List<CompositeSubRequest> requests) {
    int batchSize = 25;
    for (int i = 0; i < requests.size(); i += batchSize) {
      List<CompositeSubRequest> batch = requests.subList(i, Math.min(i + batchSize, requests.size()));
      CompositeRequest composite = new CompositeRequest();
      composite.setAllOrNone(true);
      composite.setCompositeRequest(batch);

      try (Response response = salesforceService.executeComposite(composite)) {
        String jsonResponse = response.readEntity(String.class);
        logger.infof("Composite order creation response: %s", jsonResponse);
        accountService.checkCompositeResponseForErrors(jsonResponse);
      } catch (Exception e) {
        throw new RuntimeException("Errrore nel caricamento di ordine su salesforce");
      }
    }
  }

  public String checkAccount(String partitaIva, String erpkeyInput) {

    String erpkeyDb = as400Service.findAccountRecord(partitaIva);
    // Account già esistente
    if (erpkeyDb != null) {
      if (isBlank(erpkeyInput)) {
        logger.infof(
            "Account senza ERPKey ma PIVA %s già presente nel DB con ERPKey: %s",
            partitaIva, erpkeyDb
        );
      } else if (!Objects.equals(erpkeyDb, erpkeyInput)) {
        logger.debugf(
            "ERPKEY diverso per PIVA %s - DB: %s, INPUT: %s",
            partitaIva, erpkeyDb, erpkeyInput
        );
      }
      return erpkeyDb;
    }
    return erpkeyInput;
  }

  protected void validateSalesforceData(SfAccountCli account, List<SfAccountDest> destinations) {

    if (account == null)
      throw new IllegalStateException("Account non esiste");

    if (isBlank(account.getCompanyName()))
      throw new IllegalStateException("Name non è presente su salesforce");

    if (isBlank(account.getVatNumber()))
      throw new IllegalStateException("Vat non è presente su salesforce");

    if (isBlank(account.getPhone()))
      throw new IllegalStateException("Phone non è presente su salesforce");

    if (!validNumber(account.getPhone()))
      throw new IllegalStateException("Phone errato su salesforce");

    if (isBlank(account.getEmail()))
      throw new IllegalStateException("Email non è presente su salesforce");

    if (isBlank(account.getPaymentType()))
      throw new IllegalStateException("Payment Type non è presente su salesforce o errato");

    if (account.getId() == null)
      throw new IllegalStateException("Account ID mancante");


    if (destinations == null || destinations.isEmpty())
      throw new IllegalStateException("Nessuna destinazione presente su salesforce");

    boolean isBilling = false;
    for (int i = 0; i < destinations.size(); i++) {
      SfAccountDest dest = destinations.get(i);

      if (dest == null)
        throw new IllegalStateException("Destinazione nulla in posizione " + i);

      if ("Billing".equals(dest.getAddressType())) {
        isBilling = true;
      }

      if (isBlank(dest.getDestinationAddress()))
        throw new IllegalStateException("DestinationAddress mancante nella destinazione " + i);

      if (isBlank(dest.getDestinationCity()))
        throw new IllegalStateException("DestinationCity mancante nella destinazione " + i);

      if (isBlank(dest.getDestinationPostalCode()))
        throw new IllegalStateException("DestinationPostalCode mancante nella destinazione " + i);

      if (isBlank(dest.getDestinationProvince()))
        throw new IllegalStateException("DestinationProvince mancante nella destinazione " + i);

      if (isBlank(dest.getDestinationCountryIso()))
        throw new IllegalStateException("DestinationCountryIso mancante nella destinazione " + i);
    }

    if(!isBilling)
      throw new IllegalStateException("Aggiungere destinazione indirizzo di fatturazione");
  }

  protected boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
