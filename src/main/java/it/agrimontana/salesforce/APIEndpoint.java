package it.agrimontana.salesforce;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.agrimontana.salesforce.account.AccountCliente;
import it.agrimontana.salesforce.ordine.PreordinePrenotazione;
import it.agrimontana.salesforce.ordine.PreordineRichiamo;
import it.agrimontana.salesforce.ordine.PreordineWeb;
import it.agrimontana.salesforce.dto.*;
import it.agrimontana.salesforce.dto.response.StoreResponse;
import it.agrimontana.salesforce.service.database.As400Service;
import it.agrimontana.salesforce.service.salesforce.SalesforceService;
import it.agrimontana.salesforce.service.smiwcon.AccountService;
import it.agrimontana.salesforce.service.smiwcon.PreorderService;
import it.agrimontana.salesforce.service.smiwcon.SmiwconRestService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import java.util.*;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class APIEndpoint {
    private static final Logger logger = Logger.getLogger(APIEndpoint.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    As400Service as400Service;
    @Inject
    SalesforceService salesforceService;
    @Inject
    SmiwconRestService smiwconService;
    @Inject
    AccountService accountService;
    @Inject
    PreorderService preorderService;

    @GET
    @Path("/pingdb")
    @Produces(MediaType.TEXT_PLAIN)
    @Tag(name = "Diagnostics")
    @Operation(
            summary = "Ping al database ",
            description = "verifica la connessione con il database AS400"
    )
    public Response pingDb() {
        String result = as400Service.pingDb();
        return Response.ok().entity(result).build();
    }

    @GET
    @Path("/accounts")
    @Tag(name = "JGalileo / AS400")
    @Operation(
            summary = "get Accounts",
            description = "list account (with pagination)")
    public Response getAccounts(@QueryParam("cursor") String cursor,
                                @QueryParam("limit") @DefaultValue("20") int limit) {
        return Response.ok().entity(as400Service.getAccounts(cursor,limit)).build();
    }

    @GET
    @Path("/syncAccount")
    @Tag(name = "Synchronization")
    @Operation(summary = "Sync an account", description = "Sync single account by jgalileo code")
    public Response syncAccount(@QueryParam("code") String code) {
        SalesforceAccount account = as400Service.getAccount(code);
        List<SalesforceAccount> customers = new ArrayList<SalesforceAccount>();
        customers.add(account);
        logger.info("syncAccount: " + customers.toString() + "");
        salesforceService.insertAccountNew(customers);
        return Response.ok().entity(customers).build();
    }

    @GET
    @Path("/syncCompositeAccounts")
    @Tag(name = "Synchronization")
    public Response syncCompositeAccounts() {
        List<FlatSalesforceAccount> accounts = as400Service.getAccounts("COS",10);
        logger.info("syncCompositeAccounts: " + accounts.toString() + "");
        //salesforceService.insertAccounts(accounts);
        //salesforceService.insertAccounts(customers);
        return Response.ok().entity(accounts).build();
    }



    @GET
    @Path("/products")
    @Tag(name = "JGalileo / AS400")
    @Operation(
            summary = "get all products",
            description = "list all products without pagination (slow)")
    public Response getProducts() {
        return Response.ok().entity(as400Service.getProducts()).build();
    }

    @GET
    @Path("/getAccessToken")
    @Produces(MediaType.TEXT_PLAIN)
    @Tag(name = "Diagnostics")
    @Operation(
            summary = "Get salesforce access token",
            description = "retrieve an updated access token"
    )
    public Response getAccessToken() {
        String result = salesforceService.getAccessToken();
        return Response.ok().entity(result).build();
    }

    @GET
    @Path("/bulkCreateAccounts")
    @Tag(name = "Bulk API")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Salesforce bulk Create Accounts",
            description = "Crea massivamente gli accounts in Salesforce"
    )
    public Response bulkCreateAccounts() {
        List<FlatSalesforceAccount> accounts =  as400Service.getAccounts("",20);
        //salesforceService.insertAccounts(accounts);
        return Response.ok().entity("OK").build();
    }

    @GET
    @Path("/bulkCreateProducts")
    @Produces(MediaType.TEXT_PLAIN)
    @Tag(name = "Bulk API")
    @Operation(
            summary = "Salesforce bulk Create Products",
            description = "Crea massivamente i prodotti in Salesforce"
    )
    public Response bulkCreateProducts() {
        List<SalesforceProduct> products =  as400Service.getProducts();
        salesforceService.insertProducts(products);
        return Response.ok().entity("OK").build();
    }
    @GET
    @Path("/syncPriceBook")
    @Produces(MediaType.TEXT_PLAIN)
    @Tag(name = "Synchronization")
    @Operation(
            summary = "Salesforce Sync pricebook ",
            description = "Crea  il listino in Salesforce"
    )
    public Response syncPriceBook(@QueryParam("code") String code) {
        PriceBook priceBook = as400Service.getPricebook(code);
        logger.info("syncPriceBook: " + priceBook.toString() + "");
        salesforceService.compositeInsertPriceBook(priceBook,priceBook.getPriceBookEntries());
        return Response.ok().entity(priceBook).build();
    }
    @GET
    @Path("/smiwcon/gettoken")
    @Produces(MediaType.TEXT_PLAIN)
    @Tag(name = "Diagnostics")
    @Operation(
            summary = "Get smiwcom access token",
            description = "Ottiene un smiwcom access token aggiornato"
    )
    public Response getSmiwconToken() {
        String result = smiwconService.login();
        return Response.ok().entity(result).build();
    }

    @POST
    @Path("/smiwcon/createOrderJson")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "Smiwcon")
    @Operation(
            summary = "create Order in JGalileo",
            description = "create a new order with a json in input, without doing any operation in salesforce"
    )
    public Response createOrderJson(Object dto) {
        return smiwconService.storeOrder(dto);
    }

    @POST
    @Path("/smiwcon/createPartnerJson")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "Smiwcon")
    @Operation(
            summary = "create account in JGalileo",
            description = "create a new account with a json in input, without doing any operation in salesforce"
    )
    public Response createAccountJson(Object dto) {
        return smiwconService.createPartner(dto);
    }


    @POST
    @Path("/smiwcon/createAccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "Smiwcon")
    @Operation(
            summary = "create account in JGalileo",
            description = "create a new account in JGalileo, also updating salesforce with an upsert to update ERPKEY_ID"
    )
    public Response createAccount(SalesforceAccount account) {

        return smiwconService.createPartner(account);
    }


    @POST
    @Path("/account/store")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "API Middleware", description = "Salesforce <-> AS400 Integration Layer")
    @Operation(
        summary = "Store account cliente and destinations in JGalileo",
        description = "Store customer account in JGalileo, then store all destinations and update Salesforce with ERPKEY_ID"
    )
    public Response storeAccountv2(String accountId) {
        logger.debugf("Richiesta creazione account cliente con id: %s", accountId);

        try {

            AccountCliente accountCliente = new AccountCliente(accountId, salesforceService, smiwconService, accountService, as400Service);
            accountCliente.getDataFromSalesforce();
            accountCliente.smiwconAccountId();
            accountCliente.sendSalesforceAccounts();

            StoreResponse storeResponse = accountCliente.getResponse();
            return Response.status(storeResponse.getHttpStatusCode()).entity(storeResponse).build();

        } catch (IllegalStateException e) {

            return Response.status(400)
                .entity(e.getMessage())
                .build();
        }

    }


    @POST
    @Path("/preorder/store/{type}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "API Middleware", description = "Salesforce <-> AS400 Integration Layer")
    @Operation(
        summary = "Store the preorder in JGalileo and the related order in Salesforce",
        description = "Store preorder in JGalileo and create the Order in Salesforce, then updating salesforce with an upsert to update ERPKEY_ID"
    )
    public Response storev2(@PathParam("type") String type, String preorderId) {
        logger.debugf("Richiesta creazione preordine: %s , con id: %s", type, preorderId);
        StoreResponse storeResponse;

        try {
            switch (type) {
                case "p":
                    PreordinePrenotazione pp = new PreordinePrenotazione(preorderId, salesforceService, preorderService, smiwconService, as400Service);
                    pp.getDataFromSalesforce();
                    pp.smiwconPreOrderId();
                    pp.sendSalesforceOrder();
                    storeResponse = pp.getResponse();
                    break;
                case "r":
                    PreordineRichiamo pr = new PreordineRichiamo(preorderId, salesforceService, preorderService, smiwconService, as400Service);
                    pr.getDataFromSalesforce();
                    pr.smiwconPreOrderId();
                    pr.sendSalesforceOrder();
                    storeResponse = pr.getResponse();
                    break;
                case "w":
                    PreordineWeb pw = new PreordineWeb(preorderId, salesforceService, preorderService, smiwconService, as400Service);
                    pw.getDataFromSalesforce();
                    pw.smiwconPreOrderId();
                    pw.sendSalesforceOrder();
                    storeResponse = pw.getResponse();
                    break;
                default:
                    return Response.status(400).entity("Tipo ordine non esiste").build();
            }

            return Response.status(storeResponse.getHttpStatusCode()).entity(storeResponse).build();

        } catch (IllegalArgumentException e) {
            return Response.status(400)
                .entity(e.getMessage())
                .build();
        }
    }
}
