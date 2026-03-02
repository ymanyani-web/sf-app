package it.agrimontana.salesforce.ordine;

import it.agrimontana.salesforce.dto.PriceBook;
import it.agrimontana.salesforce.ordine.builder.SmiwconOrdine;
import it.agrimontana.salesforce.ordine.builder.SmiwconRigheOrdine;
import it.agrimontana.salesforce.connector.CompositeSubRequest;
import it.agrimontana.salesforce.dto.SalesforceGraphResult;
import it.agrimontana.salesforce.connector.SalesforceQueryResult;
import it.agrimontana.salesforce.dto.response.StoreResponse;
import it.agrimontana.salesforce.dto.response.SmiwconResult;
import it.agrimontana.salesforce.dto.preorder.*;
import it.agrimontana.salesforce.dto.order.SalesforceOrder;
import it.agrimontana.salesforce.dto.order.SalesforceOrderLines;
import it.agrimontana.salesforce.service.database.As400Service;
import it.agrimontana.salesforce.service.salesforce.SalesforceService;
import it.agrimontana.salesforce.service.smiwcon.PreorderService;
import it.agrimontana.salesforce.service.smiwcon.SmiwconRestService;
import org.jboss.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PreordinePrenotazione extends Preordine {
  private static final Logger logger = Logger.getLogger(PreordinePrenotazione.class);

  private String preOrderId;
  private Preorder preorder;
  private PreorderAccount account;
  private PreorderShipping shipping;
  private PreorderShipping billing;
  private SalesforceOrder salesforceOrder;
  private PriceBook pricebook;
  private List<PreorderLineItem> preorderlineitem;
  private List<SalesforceOrderLines> salesforceOrderLines;
  private Map<String, String> vatPricedMap;
  private Map<String, String> unitPriceMap;
  private StoreResponse response = new StoreResponse();

  public PreordinePrenotazione(String id, SalesforceService salesforceService, PreorderService preorderService,
                               SmiwconRestService smiwconRestService, As400Service as400Service) {
    super(id, salesforceService, preorderService, smiwconRestService, as400Service);
    logger.debugf("Nuova istanza oggetto: %s, con id: %s", PreordinePrenotazione.class.getSimpleName(), id);
  }

  public void getDataFromSalesforce() {
    // graph
    SalesforceGraphResult graphResult = getGraphFromClient();

    // standard
    SalesforceQueryResult<PreorderLineItem> data = getStandardFromClient(PreorderLineItem.class);

    // estraggo oggetti da risposta graph e standard
    this.preorder = graphResult.getBodyAs("getPreorder", Preorder.class);
    this.account = graphResult.getBodyAs("getAccount", PreorderAccount.class);
    this.pricebook = graphResult.getBodyAs("getPricebook2", PriceBook.class);
    this.shipping = graphResult.getBodyAs("getShippingAddress", PreorderShipping.class);
    this.billing = graphResult.getBodyAs("getBillingAddress", PreorderShipping.class);
    this.preorderlineitem = data.getRecords();

    // Query separata per prezzi dal PricebookEntry
    List<String> pricebookEntryIds = this.preorderlineitem.stream()
        .map(PreorderLineItem::getPricebookEntry)
        .filter(id -> id != null && !id.trim().isEmpty())
        .collect(java.util.stream.Collectors.toList());
    Map<String, String>[] prices = queryPricebookEntryPrices(pricebookEntryIds);
    this.vatPricedMap = prices[0];
    this.unitPriceMap = prices[1];

    // Validazione dati Salesforce
    validateSalesforceData(preorder, account, pricebook, shipping, billing, preorderlineitem);
  }

  public SmiwconOrdine structSmiwconOrder() {
    logger.debugf(
        "Costruzione dto smiwcon, funzione: %s, tipologia: P, ordine: %s",
        "structSmiwconOrder",
        preorder.getId()
    );

    // costruisco dto smiwcon
    List<SmiwconRigheOrdine> rows = new ArrayList<>();
    for(PreorderLineItem item : this.preorderlineitem) {
      logger.debugf("Preordine prenotazione riga ordine, codice prodotto: %s, tipo movimento: %s, qta: %s, preordine: %s",
          item.getProductCode__c(),
          item.getTipoRigo__c(),
          item.getQuantity(),
          preorder.getId()
      );

      // Determina tipo movimento per Smiwcon
      String tipoRigo = item.getTipoRigo__c();
      String tipoMovimentoAcronimo;
      if ("31".equals(tipoRigo)) {
        // Omaggio
        tipoMovimentoAcronimo = item.isGeneratedByBundle() ? "SM" : "ON";
      } else if ("01".equals(tipoRigo)) {
        // Vendita
        tipoMovimentoAcronimo = (Double.parseDouble(item.getDiscount4__c()) != 0) ? "VE" : "VE";
      } else {
        // Altri casi
        tipoMovimentoAcronimo = convertTipoMovimento(tipoRigo);
      }

      String pricebookEntryId = item.getPricebookEntry();

      SmiwconRigheOrdine row = new SmiwconRigheOrdine.Builder()
          .codiceProd(item.getProductCode__c())
          .tipoMovimento(tipoMovimentoAcronimo)
          .commentoInterno(item.getNote__c())
          .prezzoLordo(item.getNetPrice__c())
          .prezzoNetto("0")
          .qta(String.valueOf(
              Double.parseDouble(item.getQuantity()) * item.getNPezzi__c()
          ))
          .sconto9(item.getDiscount1__c())
          .flagSconto9("%")
          .sconto7(item.getDiscount2__c())
          .flagSconto7("%")
          .sconto6(item.getDiscount3__c())
          .flagSconto6("%")
          .sconto1(item.getDiscount4__c())
          .sconto5(item.getDiscount5__c())
          .flagSconto5("%")
          .build();
      rows.add(row);
    }

    SmiwconOrdine order = new SmiwconOrdine.Builder()
        .tipoDocumento("P")
        .riferimentoCliente(preorder.getRiferimentoCliente(), account.getName())
        .cliente(account.getAccountNumber())
        .pagamentoCli(preorder.getPaymentMethod__c(), account.getPreferredPaymentMethod())
        .destinatario(shipping.getAddressType(), shipping.getERPKey__c())
        .dataPrimaScadenza(preorder.getDataPrimaScadenza())
        //.tipologiaVendita(account.getTipologiaCliente__c())
        .tipologiaDiVendita(preorder.getTipologiaDiVendita())
        .commentoInterno(preorder.getNote())
        .noPark("S")
        .listino(lookupPricebook2ErpKeyById(preorder.getPricebook2()))
        .rows(rows)
        .build();

    return order;
  }

  public void smiwconPreOrderId() {
    SmiwconResult result = super.sendToSmiwcon(structSmiwconOrder());
    response.addResult(result);
    if (result.isSuccess()) {
      this.preOrderId = result.getId();
    }
  }

  public void createSalesforceOrder() {
    // costruisco dto salesforce
    logger.infof("Richiesta creazione oggetto salesforce funzione: %s", "createSalesforceOrder");

    String[] parts = this.preOrderId.split("/");

    try {
      this.salesforceOrder = super.getOrder(parts[0], parts[1]);

      // lookup Pricebook2 ID da ERPKey__c
      String pricebookErpKey = this.salesforceOrder.getPricebook2Id();
      if (pricebookErpKey != null && !pricebookErpKey.isBlank()) {
        String pricebookSfId = lookupPricebook2IdByErpKey(pricebookErpKey);
        this.salesforceOrder.setPricebook2Id(pricebookSfId);
      }

      this.salesforceOrderLines = super.getOrderLines(this.salesforceOrder.getName(), parts[0]);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }



    // spedizione
    if (shipping != null) {
      this.salesforceOrder.setShippingStreet(shipping.getStreet());
      this.salesforceOrder.setShippingCity(shipping.getCity());
      this.salesforceOrder.setShippingState(shipping.getState());
      this.salesforceOrder.setShippingPostalCode(shipping.getPostalCode());
      this.salesforceOrder.setShippingCountry(shipping.getCountry());
    }

    // fatturazione
    if (billing != null) {
      this.salesforceOrder.setBillingStreet(billing.getStreet());
      this.salesforceOrder.setBillingCity(billing.getCity());
      this.salesforceOrder.setBillingState(billing.getState());
      this.salesforceOrder.setBillingPostalCode(billing.getPostalCode());
      this.salesforceOrder.setBillingCountry(billing.getCountry());
    }

    this.salesforceOrder.setIsBooking__c(true);
    this.salesforceOrder.setAccountId(this.account.getId());
    this.salesforceOrder.setOrderType__c("P");
    //this.salesforceOrder.setPricebook2Id(this.account.getPricebook__c());
  }

  public void sendSalesforceOrder() {
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s",
        PreordinePrenotazione.class.getSimpleName(),
        "sendSalesforceOrder"
    );

    if (this.preOrderId == null) {
      logger.warn("preOrderId è null, impossibile creare ordine su Salesforce");
      response.calculateStatus();
      return;
    }

    this.createSalesforceOrder();

    // map productcode = productid
    Map<String, String> productIdMap = new HashMap<>();

    // map productcode = pricebookentryid
    Map<String, String> pricebookEntryMap = new HashMap<>();

    for (PreorderLineItem item : this.preorderlineitem) {
      logger.debugf(
          "Riga preordine: %s, codice prodotto: %s, id prodotto: %s, id listino: %s",
          item,
          item.getProductCode__c(),
          item.getProduct(),
          item.getPricebookEntry()
      );

      if (item.getProductCode__c() != null && item.getProduct() != null) {
        productIdMap.put(item.getProductCode__c(), item.getProduct());
      }

      if (item.getProductCode__c() != null && item.getPricebookEntry() != null) {
        pricebookEntryMap.put(item.getProductCode__c(), item.getPricebookEntry());
      }
    }

    List<CompositeSubRequest> requests = new ArrayList<>();
    requests.add(preOrderPatch());
    requests.add(orderPatch());

    int idx = 0;
    for(SalesforceOrderLines line : this.salesforceOrderLines) {
      line.setOrderId("@{upsertOrder.id}");
      line.setERPKey__c("P-" + salesforceOrder.getName() + "-" + line.getNumeroRigo__c());

      // sostituisco codice erp con id salesforce
      String productCode = line.getProduct2Id() != null ? line.getProduct2Id().trim() : null;
      String productId = productIdMap.get(productCode);
      if (productId != null) {
        line.setProduct2Id(productId);
      }

      // aggiungo pricebookentryid dalla query standard
      String pricebookEntryId = pricebookEntryMap.get(productCode);
      if (pricebookEntryId != null) {
        line.setPricebookEntryId(pricebookEntryId);
      } else {
        logger.warnf("Pricebook Entry Id non trovato per ProductCode: %s", productCode);
      }

      requests.add(orderLinesPatch(line, idx));
      idx++;
    }

    super.sendOrderToSalesforce(requests);
    response.calculateStatus();
  }

  public StoreResponse getResponse() {
    return response;
  }

  public CompositeSubRequest preOrderPatch() {
    logger.debugf(
        "Body http nella classe: %s, funzione: %s",
        PreordinePrenotazione.class.getSimpleName(),
        "preOrderPatch"
    );

    Map<String, Object> preorderBody = new HashMap<>();
    CompositeSubRequest preorderPatch = new CompositeSubRequest();

    preorderPatch.setUrl("/services/data/" + super.salesforceService.getApiVersion() + "/sobjects/PreOrder__c/" + super.id);
    preorderPatch.setReferenceId("patchPreorder");
    preorderPatch.setMethod("PATCH");

    preorderBody.put("ERPKey__c", "P-" + salesforceOrder.getName());
    preorderPatch.setBody(preorderBody);

    return preorderPatch;
  }

  public CompositeSubRequest orderPatch() {
    logger.debugf(
        "Body http nella classe: %s, funzione: %s",
        PreordinePrenotazione.class.getSimpleName(),
        "orderPatch"
    );

    CompositeSubRequest orderPatch = new CompositeSubRequest();

    orderPatch.setUrl("/services/data/" + super.salesforceService.getApiVersion() + "/sobjects/Order/ERPKey__c/P-" + salesforceOrder.getName());
    orderPatch.setReferenceId("upsertOrder");
    orderPatch.setMethod("PATCH");

    orderPatch.setBody(salesforceOrder);

    return orderPatch;
  }

  public CompositeSubRequest orderLinesPatch(SalesforceOrderLines line, int idx) {
    logger.debugf(
        "Body http nella classe: %s, funzione: %s",
        PreordinePrenotazione.class.getSimpleName(),
        "orderLinesPatch"
    );

    CompositeSubRequest linePost = new CompositeSubRequest();

    linePost.setUrl("/services/data/" + super.salesforceService.getApiVersion() + "/sobjects/OrderItem");
    linePost.setReferenceId("createOrderLine" + idx);
    linePost.setMethod("POST");

    linePost.setBody(line);

    return linePost;
  }

  private String convertTipoMovimento(String tipoMovimento) {
    if (tipoMovimento == null || tipoMovimento.isEmpty()) {
      return null;
    }

    switch (tipoMovimento) {
      case "01":
        return "VE";
      case "30":
        return "SM";
      case "31":
        return "ON";
      case "02":
        return "BV";
      default:
        return tipoMovimento;
    }
  }

}
