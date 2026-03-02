package it.agrimontana.salesforce.ordine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.agrimontana.salesforce.dto.PriceBook;
import it.agrimontana.salesforce.service.database.As400Service;
import it.agrimontana.salesforce.connector.CompositeGraphQueue;
import it.agrimontana.salesforce.service.salesforce.SalesforceService;
import it.agrimontana.salesforce.ordine.builder.SmiwconOrdine;
import it.agrimontana.salesforce.connector.CompositeRequest;
import it.agrimontana.salesforce.connector.CompositeSubRequest;
import it.agrimontana.salesforce.dto.SalesforceGraphResult;
import it.agrimontana.salesforce.connector.SalesforceQueryResult;
import it.agrimontana.salesforce.dto.response.SmiwconResult;
import it.agrimontana.salesforce.dto.order.SalesforceOrder;
import it.agrimontana.salesforce.dto.order.SalesforceOrderLines;
import it.agrimontana.salesforce.connector.salesforce.CompositeGraphException;
import it.agrimontana.salesforce.service.smiwcon.PreorderService;
import it.agrimontana.salesforce.service.smiwcon.SmiwconRestService;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import it.agrimontana.salesforce.dto.preorder.Preorder;
import it.agrimontana.salesforce.dto.preorder.PreorderAccount;
import it.agrimontana.salesforce.dto.preorder.PreorderLineItem;
import it.agrimontana.salesforce.dto.preorder.PreorderShipping;
import it.agrimontana.salesforce.dto.smiwcon.account.AgentInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class Preordine {
  protected String id;
  protected ObjectMapper salesforceObjectMapper = new ObjectMapper();

  protected SalesforceService salesforceService;
  protected PreorderService preorderService;
  protected SmiwconRestService smiwconRestService;
  protected As400Service as400Service;

  private static final Logger logger = Logger.getLogger(Preordine.class);

  public Preordine(String id, SalesforceService salesforceService, PreorderService preorderService,
                   SmiwconRestService smiwconRestService, As400Service as400Service) {
    this.id = id;
    this.salesforceService = salesforceService;
    this.preorderService = preorderService;
    this.smiwconRestService = smiwconRestService;
    this.as400Service = as400Service;
  }


  public SalesforceOrder getOrder(String letter, String number) {
    return as400Service.getPreorderOrder(letter, number);
  }

  public List<SalesforceOrderLines> getOrderLines(String order, String letter) {
    return as400Service.getPreorderOrderLines(order, letter);
  }

  @SuppressWarnings("unchecked")
  protected String lookupPricebook2IdByErpKey(String erpKey) {
    if (erpKey == null || erpKey.trim().isEmpty()) {
      return null;
    }

    String query = String.format(
        "SELECT+Id+FROM+Pricebook2+WHERE+ERPKey__c='%s'+LIMIT+1",
        erpKey.trim()
    );

    try (Response response = salesforceService.salesforceQueryGet(query)) {
      String json = response.readEntity(String.class);
      Map<String, Object> result = salesforceObjectMapper.readValue(json, Map.class);
      List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
      if (records != null && !records.isEmpty()) {
        return (String) records.get(0).get("Id");
      }
    } catch (Exception e) {
      logger.errorf("Errore lookup Pricebook2 per ERPKey %s: %s", erpKey, e);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected String lookupPricebook2ErpKeyById(String pricebookId) {
    logger.debugf("lookupPricebook2ErpKeyById - inizio lookup per pricebookId: %s", pricebookId);

    if (pricebookId == null || pricebookId.trim().isEmpty()) {
      logger.warnf("lookupPricebook2ErpKeyById - pricebookId nullo o vuoto");
      return null;
    }

    String query = String.format(
        "SELECT+ERPKey__c+FROM+Pricebook2+WHERE+Id='%s'+LIMIT+1",
        pricebookId.trim()
    );
    logger.debugf("lookupPricebook2ErpKeyById - query: %s", query);

    try (Response response = salesforceService.salesforceQueryGet(query)) {
      String json = response.readEntity(String.class);
      logger.debugf("lookupPricebook2ErpKeyById - response json: %s", json);

      Map<String, Object> result = salesforceObjectMapper.readValue(json, Map.class);
      List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
      if (records != null && !records.isEmpty()) {
        String erpKey = (String) records.get(0).get("ERPKey__c");
        logger.infof("lookupPricebook2ErpKeyById - trovato ERPKey: %s per pricebookId: %s", erpKey, pricebookId);
        return erpKey;
      }
      logger.warnf("lookupPricebook2ErpKeyById - nessun record trovato per pricebookId: %s", pricebookId);
    } catch (Exception e) {
      logger.errorf("Errore lookup Pricebook2 ERPKey per Id %s: %s", pricebookId, e);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected Map<String, String>[] queryPricebookEntryPrices(List<String> pricebookEntryIds) {
    Map<String, String> vatPricedMap = new HashMap<>();
    Map<String, String> unitPriceMap = new HashMap<>();

    if (pricebookEntryIds == null || pricebookEntryIds.isEmpty()) {
      return new Map[] { vatPricedMap, unitPriceMap };
    }

    // Costruisce la lista di ID per la clausola IN
    String ids = pricebookEntryIds.stream()
        .filter(id -> id != null && !id.trim().isEmpty())
        .map(id -> "'" + id.trim() + "'")
        .collect(java.util.stream.Collectors.joining(","));

    if (ids.isEmpty()) {
      return new Map[] { vatPricedMap, unitPriceMap };
    }

    String query = String.format(
        "SELECT+Id,+VatPriced__c,+UnitPrice+FROM+PricebookEntry+WHERE+Id+IN+(%s)",
        ids
    );

    try (Response response = salesforceService.salesforceQueryGet(query)) {
      String json = response.readEntity(String.class);
      Map<String, Object> result = salesforceObjectMapper.readValue(json, Map.class);
      List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
      if (records != null) {
        for (Map<String, Object> record : records) {
          String id = (String) record.get("Id");
          Object vatPriced = record.get("VatPriced__c");
          Object unitPrice = record.get("UnitPrice");
          if (id != null) {
            if (vatPriced != null) {
              vatPricedMap.put(id, String.valueOf(vatPriced));
            }
            if (unitPrice != null) {
              unitPriceMap.put(id, String.valueOf(unitPrice));
            }
          }
        }
      }
    } catch (Exception e) {
      logger.errorf("Errore query prezzi PricebookEntry: %s", e);
    }
    return new Map[] { vatPricedMap, unitPriceMap };
  }

  public void sendOrderToSalesforce(List<CompositeSubRequest> requests) {
    int batchSize = 25;
    for (int i = 0; i < requests.size(); i += batchSize) {
      List<CompositeSubRequest> batch = requests.subList(i, Math.min(i + batchSize, requests.size()));
      CompositeRequest composite = new CompositeRequest();
      composite.setAllOrNone(true);
      composite.setCompositeRequest(batch);

      try (Response response = salesforceService.executeComposite(composite)) {
        String jsonResponse = response.readEntity(String.class);
        logger.infof("Composite order creation response: %s", jsonResponse);
        preorderService.checkCompositeResponseForErrors(jsonResponse);
      } catch (Exception e) {
        throw new RuntimeException("Errrore nel caricamento di ordine su salesforce");
      }
    }
  }

  public SmiwconResult sendToSmiwcon(SmiwconOrdine order){
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s",
        Preordine.class.getSimpleName(),
        "sendToSmiwcon"
    );

    try (Response response = smiwconRestService.storeOrderV2(order)) {
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

  public String standardQuery() {
    String preorderLinesQuery = String.format(
        "SELECT Id, Product_Code__c, PricebookEntry__c, Product__c, Quantity__c, NetPrice__c, TotalGrossWithDiscount__c, TotalNetWithoutDiscount__c,  " +
            "Discount1__c, Discount2__c, Discount3__c, Discount4__c, Discount5__c, TipoRigo__c, Note__c, NumeroRigo__c,  " +
            "SourceOrderItem__r.ERPKey__c, SourceOrderItem__r.Order.ERPKey__c, SourceOrderItem__r.OrderId, Product__r.ProductCode, Product__r.Id, Product__r.NPezzi__c  " +
            "FROM PreorderLineItem__c WHERE Preordine__c = '%s'",
        id
    );
    return preorderLinesQuery;
  }

  public Map<String, Object> graphQuery() {
    String apiVersion = "v54.0";
    CompositeGraphQueue graphQueue = new CompositeGraphQueue("preorderGraph");

    // 1. PreOrder (direct sobject request)
    graphQueue.addNode(
        "GET",
        String.format("/services/data/%s/sobjects/PreOrder__c/%s", apiVersion, id),
        "getPreorder"
    );

    // 2. Account (dependency inferred from @{getPreorder...} reference)
    graphQueue.addNode(
        "GET",
        String.format("/services/data/%s/sobjects/Account/@{getPreorder.Account__c}", apiVersion),
        "getAccount"
    );

    // 3. Shipping Address (dependency inferred from @{getPreorder...} reference)
    graphQueue.addNode(
        "GET",
        String.format("/services/data/%s/sobjects/ContactPointAddress/@{getPreorder.ShippingTo__c}", apiVersion),
        "getShippingAddress"
    );

    // 4. Billing Address (dependency inferred from @{getPreorder...} reference)
    graphQueue.addNode(
        "GET",
        String.format("/services/data/%s/sobjects/ContactPointAddress/@{getPreorder.BillingTo__c}", apiVersion),
        "getBillingAddress"
    );

    graphQueue.addNode(
        "GET",
        String.format("/services/data/%s/sobjects/Pricebook2/@{getAccount.Pricebook__c}", apiVersion),
        "getPricebook2"
    );

    return graphQueue.buildGraph();
  }

  public SalesforceGraphResult getGraphFromClient() {
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s",
        Preordine.class.getSimpleName(),
        "getGraphFromClient"
    );

    try (Response graphResponse = salesforceService.executeCompositeGraph(this.graphQuery())) {
      return readGraphResponse(graphResponse);
    } catch (Exception e) {
      logger.errorf(
          "Error class: %s, function: %s, error: %s",
          Preordine.class.getSimpleName(),
          "getGraphFromClient",
          e
      );
      throw new RuntimeException("Errore funzione getGraphFromClient", e);
    }
  }

  public SalesforceGraphResult readGraphResponse(Response response) {
    try {
      String json = response.readEntity(String.class);
      SalesforceGraphResult graphResult =  salesforceObjectMapper.readValue(json, SalesforceGraphResult.class);
      graphResult.throwIfCompositeError();
      return graphResult;
    } catch (CompositeGraphException e) {
      throw new RuntimeException("Deserializzazione composite json fallita, errore salesforce: ", e);
    } catch (Exception e) {
      throw new RuntimeException("Deserializzazione json fallita per ragioni ignote, errore server: ", e);
    }
  }

  public <T> SalesforceQueryResult<T> getStandardFromClient(Class<T> clazz) {
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s",
        Preordine.class.getSimpleName(),
        "getStandardFromClient"
    );

    try (Response standardResponse = salesforceService.salesforceQueryGet(this.standardQuery())) {
      return readStandardResponse(standardResponse, clazz);
    } catch (Exception e) {
      logger.errorf(
          "Error class: %s, function: %s, error: %s",
          Preordine.class.getSimpleName(),
          "getStandardFromClient",
          e
      );
      throw new RuntimeException("Error funzione getStandardFromClient", e);
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

      if ("BLOCKED".equals(status)) {
        List blocks = (List) json.get("blocks");
        String errors = String.valueOf(json.get("errors"));
        if (blocks != null && !blocks.isEmpty()) {
          StringBuilder blockErrors = new StringBuilder();
          for (Object block : blocks) {
            Map blockMap = (Map) block;
            blockErrors.append(blockMap.get("desc")).append(" ");
          }
          logger.warnf("Smiwcon ha restituito un blocco: %s", blockErrors);
          return SmiwconResult.error(blockErrors.toString().trim());
        }
        logger.warnf("Smiwcon ha restituito un blocco: %s", errors);
        return SmiwconResult.error(errors);
      }

      Map data = (Map) json.get("data");
      if (data == null) {
        logger.errorf("Smiwcon response missing 'data' field. Full response: %s", rawValue);
        return SmiwconResult.error("Risposta Smiwcon non contiene il campo 'data'");
      }

      String numero = (String) data.get("numero");
      return SmiwconResult.success(numero);

    } catch (JsonProcessingException e) {
      logger.errorf("Errore deserializzazione JSON: %s", e.getMessage());
      return SmiwconResult.error("Errore deserializzazione JSON: " + e.getMessage());
    } catch (Exception e) {
      logger.errorf("Error processing JGalileo response: %s", e.getMessage());
      return SmiwconResult.error("Errore sconosciuto: " + e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  protected AgentInfo fetchAgentOrCapoArea(PreorderAccount account) {
    String agentId = account.getAgentId();
    String capoAreaId = account.getCapoAreaId();

    String idToFetch = (agentId != null && !agentId.trim().isEmpty())
        ? agentId
        : capoAreaId;

    if (idToFetch == null || idToFetch.trim().isEmpty()) {
      logger.warnf("Account %s non ha né Agent__c né CapoArea__c", account.getId());
      return null;
    }

    logger.debugf("Recupero agente/capo area con ID: %s (Agent__c: %s, CapoArea__c: %s)",
        idToFetch, agentId, capoAreaId);

    String query = String.format("SELECT+Id,+ERPKey__c+FROM+Account+WHERE+Id='%s'+LIMIT+1", idToFetch);

    try (Response response = salesforceService.salesforceQueryGet(query)) {
      String json = response.readEntity(String.class);
      Map<String, Object> result = salesforceObjectMapper.readValue(json, Map.class);
      List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");

      if (records == null || records.isEmpty()) {
        logger.warnf("Nessun agente/capo area trovato con ID: %s", idToFetch);
        return null;
      }

      AgentInfo agentInfo = new AgentInfo();
      agentInfo.setErpKey((String) records.get(0).get("ERPKey__c"));
      return agentInfo;
    } catch (Exception e) {
      logger.errorf("Errore nel recupero agente/capo area: %s", e.getMessage());
      throw new RuntimeException("Errore nel recupero agente/capo area", e);
    }
  }

  protected void validateSalesforceData(Preorder preorder, PreorderAccount account, PriceBook pricebook, PreorderShipping shipping,
                                        PreorderShipping billing,
                                        List<PreorderLineItem> lineItems) {
    if (preorder == null)
      throw new IllegalStateException("Preorder non trovato");

    if (account == null)
      throw new IllegalStateException("Account non trovato");

    if (isBlank(account.getId()))
      throw new IllegalStateException("Account ID mancante");

    if (isBlank(account.getPricebook__c()))
      throw new IllegalStateException("Pricebook ID mancante per account: " + account.getId());

    if(isBlank(account.getTipologiaCliente__c()))
      throw new IllegalStateException("Tipologia cliente assente rtl, labo");

    if(isBlank(pricebook.getERPKey__c()))
      throw new IllegalStateException("Listino cliente assente");

    if (shipping == null)
      throw new IllegalStateException("Indirizzo di spedizione non trovato");

    if (isBlank(shipping.getERPKey__c()))
      throw new IllegalStateException("ERPKey indirizzo spedizione mancante");

    if (billing == null)
      throw new IllegalStateException("Indirizzo di fatturazione non trovato");

    if (isBlank(billing.getERPKey__c()))
      throw new IllegalStateException("ERPKey indirizzo fatturazione mancante");

    if (lineItems == null || lineItems.isEmpty())
      throw new IllegalStateException("Nessuna riga ordine trovata");

    for (int i = 0; i < lineItems.size(); i++) {
      PreorderLineItem item = lineItems.get(i);

      if (isBlank(item.getProduct()))
        throw new IllegalStateException(
            "Product ID mancante per riga " + (i + 1) +
                " (ProductCode: " + item.getProductCode__c() + ")"
        );

      if (isBlank(item.getPricebookEntry()))
        throw new IllegalStateException(
            "PricebookEntry ID mancante per riga " + (i + 1) +
                " (ProductCode: " + item.getProductCode__c() + ")"
        );

      if (item.getNPezzi__c() == null || item.getNPezzi__c() <= 0)
        throw new IllegalStateException(
            "NPezzi__c mancante o non valido per riga " + (i + 1) +
                " (ProductCode: " + item.getProductCode__c() + ")"
        );
    }
  }


  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
