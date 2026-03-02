package it.agrimontana.salesforce.service.salesforce;

import it.agrimontana.salesforce.connector.CompositeRequest;
import it.agrimontana.salesforce.connector.CompositeSubRequest;
import it.agrimontana.salesforce.connector.UpdateAccountRequest;
import it.agrimontana.salesforce.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SalesforceService {

    private static final Logger logger = Logger.getLogger(SalesforceService.class);

    @Inject
    @ConfigProperty(name = "salesforce.authUrl")
    private String authUrl;

    @Inject
    @ConfigProperty(name = "salesforce.baseUrl")
    private String baseUrl;

    @Inject
    @ConfigProperty(name = "salesforce.clientId")
    private String clientId;

    @Inject
    @ConfigProperty(name = "salesforce.clientSecret")
    private String clientSecret;

    @Inject
    @ConfigProperty(name = "salesforce.refreshToken")
    private String refreshToken;

    @Inject
    @ConfigProperty(name = "salesforce.apiVersion")
    private String apiVersion;

    private volatile String token;

    public synchronized String getAccessToken() {

        logger.debugf("Richiesta http per token di salesforce");

        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(authUrl)
                    .queryParam("grant_type", "refresh_token")
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("refresh_token", refreshToken);

            logger.debug("##SALESFORCE## URL access/bearer token: " + target.getUri());
            try (Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(null)) {
                if (response.getStatus() != 200) {
                    throw new RuntimeException("Errore Salesforce OAuth: " +
                            response.getStatus() + " - " + response.readEntity(String.class));
                }
                Map<?, ?> json = response.readEntity(Map.class);
                String accessToken = (String) json.get("access_token");
                logger.debug("AccessToken returnet: " + accessToken);
                this.token = accessToken;
                return accessToken;
            }
        }
    }


    public Response insertProducts(List<SalesforceProduct> products) {
        String accessToken = getAccessToken();

        // 🔹 spezzetta in blocchi da 25
        int batchSize = 25;
        int fromIndex = 0;
        int status = 200;
        StringBuilder allResponses = new StringBuilder();

        try (Client client = ClientBuilder.newClient()) {
            while (fromIndex < products.size()) {
                int toIndex = Math.min(fromIndex + batchSize, products.size());
                List<SalesforceProduct> chunk = products.subList(fromIndex, toIndex);

                List<CompositeSubRequest> requests = new ArrayList<>();
                int idx = fromIndex + 1;
                for (SalesforceProduct prod : chunk) {
                    CompositeSubRequest sub = new CompositeSubRequest();
                    sub.setReferenceId("newProduct" + idx++);
                    sub.setUrl("/services/data/" + apiVersion + "/sobjects/Product2");
                    sub.setMethod("POST");
                    sub.setBody(prod);
                    requests.add(sub);
                }

                CompositeRequest composite = new CompositeRequest();
                composite.setCompositeRequest(requests);

                WebTarget target = client.target(baseUrl + "/services/data/" + apiVersion + "/composite");

                // debug JSON
                try {
                    String bodyJson = new ObjectMapper()
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(composite);
                    logger.info("Request URL: " + target.getUri());
                    logger.info("Request Body: " + bodyJson);
                } catch (Exception e) {
                    logger.error("Errore serializzazione JSON", e);
                }

                Entity<CompositeRequest> entity = Entity.entity(composite, MediaType.APPLICATION_JSON_TYPE);
                try (Response post = target.request(MediaType.APPLICATION_JSON_TYPE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .post(entity)) {
                    status = post.getStatus();
                    String body = post.readEntity(String.class);
                    logger.infof("POST %s -> status: %d, body: %s", target.getUri(), status, body);
                    allResponses.append(body).append("\n");
                }

                fromIndex = toIndex; // vai al prossimo blocco
            }
        }

        return Response.status(status).entity(allResponses.toString()).build();
    }

    public Response insertAccountNew(List<SalesforceAccount> accounts) {
        String accessToken = getAccessToken();

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setCustomers(accounts);

        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(baseUrl + "/services/apexrest/erp/v1/customers");
            try (Response post = target.request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))) {
                int status = post.getStatus();
                String body = post.readEntity(String.class);
                try {
                    logger.info("Update Account NEW \n\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(request) + "\n*********\n\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.infof("POST %s -> status: %d, body: %s", target.getUri(), status, body);
                return Response.status(status).entity(body).build();
            }
        }
    }

    public Response compositeInsertPriceBook(PriceBook priceBook,List<PriceBookEntry> entries) {
        String accessToken = getAccessToken();

        List<CompositeSubRequest> requests = new ArrayList<>();
        CompositeSubRequest sub1 = CompositeSubRequest.newPriceBook();
        sub1.setReferenceId("NewPricebook");
        sub1.setBody(priceBook);
        requests.add(sub1);

        int idx = 1;
        for (PriceBookEntry entry : entries) {
            // Query
            CompositeSubRequest query = CompositeSubRequest.newQuery("SELECT+Id+FROM+Product2+WHERE+ProductCode='"+entry.getProduct2Id()+"'+LIMIT+1");
            query.setReferenceId("lookupProduct" + idx);
            requests.add(query);
            CompositeSubRequest sub = CompositeSubRequest.newPriceBookEntry();
            sub.setReferenceId("PBEntry" + idx);
            entry.setPricebook2Id("@{NewPricebook.id}");
            entry.setProduct2Id("@{lookupProduct" + idx + ".id}");
            sub.setBody(entry);
            requests.add(sub);
            idx++;
        }

        CompositeRequest composite = new CompositeRequest();
        composite.setCompositeRequest(requests);

        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(baseUrl + "/services/data/" + apiVersion + "/composite");
            // debug JSON
            try {
                String bodyJson = new ObjectMapper()
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(composite);
                logger.info("Request URL: " + target.getUri());
                logger.info("Request Body: " + bodyJson);
            } catch (Exception e) {
                logger.error("Errore serializzazione JSON", e);
            }

            Entity<CompositeRequest> entity = Entity.entity(composite, MediaType.APPLICATION_JSON_TYPE);
            try (Response post = target.request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .post(entity)) {
                int status = post.getStatus();
                String body = post.readEntity(String.class);
                logger.infof("POST %s -> status: %d, body: %s", target.getUri(), status, body);
                return Response.status(status).entity(body).build();
            }
        }
    }


    public Response salesforceGet(String sobject) {
        String url = String.format("%s/services/data/"+ apiVersion +"/sobjects/%s", baseUrl, sobject);
        String accessToken = getAccessToken();

        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(url);
            logger.infof("SALESFORCE REQUEST GET %s", sobject);
            return target.request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .get();
        }
    }

     public Response salesforceQueryGet(String query) {
         logger.debugf(
             "Richiesta http invocata per: %s, per funzione: %s, con payload: %b",
             getClass().getSimpleName(),
             "executeCompositeGraph",
             query != null
         );

        String url = String.format("%s/services/data/"+apiVersion+"/query?q=%s", baseUrl, query);

        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(url);

            logger.debugf(
                "Caratteristiche richiesta url: %s, body: %S",
                target.getUri(),
                query
            );

            Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.token)
                    .get();

            int status = response.getStatus();
            String responseBody = response.readEntity(String.class);

            // token scaduto (401 Unauthorized o INVALID_SESSION_ID)
            if (status == 401 || (responseBody != null && responseBody.contains("INVALID_SESSION_ID"))) {
                logger.info("Token Salesforce scaduto in executeCompositeGraph, eseguo nuovo login e riprovo");
                getAccessToken();
                return salesforceQueryGet(query);
            }

            return Response.status(status)
                .entity(responseBody)
                .build();
        }
    }

    public Response salesforcePost(String sobject, Entity<?> entity) {
        String url = String.format("%s/services/data/"+apiVersion+"/sobjects/%s", baseUrl, sobject);
        String accessToken = getAccessToken();
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(url);

            logger.infof("GET SALESFORCE POST: %s", url);

            return target.request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .post(entity);
        }
    }

    public Response salesforcePatch(String sobject, Entity<?> entity) {
        String url = String.format("%s/services/data/"+apiVersion+"/sobjects/%s", baseUrl, sobject);
        String accessToken = getAccessToken();
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(url);

            logger.infof("PATCH %s", url);

            return target.request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .method("PATCH", entity);
        }
    }

    public Response executeComposite(CompositeRequest composite) {
        String accessToken = getAccessToken();
        String url = baseUrl + "/services/data/" + apiVersion + "/composite";

        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(url);

            try {
                String bodyJson = new ObjectMapper()
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(composite);
                logger.infof("Composite Request URL: %s", target.getUri());
                logger.infof("Composite Request Body: %s", bodyJson);
            } catch (Exception e) {
                logger.error("Error serializing composite JSON", e);
            }

            Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .post(Entity.entity(composite, MediaType.APPLICATION_JSON_TYPE));

            logger.infof("Composite Response status: %d", response.getStatus());
            return response;
        }
    }


    public Response executeCompositeGraph(Object graphPayload) {
        logger.debugf(
            "Richiesta http invocata per: %s, per funzione: %s, con payload: %b",
            getClass().getSimpleName(),
            "executeCompositeGraph",
            graphPayload != null
        );

        String url = baseUrl + "/services/data/" + apiVersion + "/composite/graph";

        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(url);

            try {
                String bodyJson = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(graphPayload);

                logger.debugf(
                    "Caratteristiche richiesta url: %s, body: %S",
                    target.getUri(),
                    bodyJson
                );

            } catch (Exception e) {
                logger.errorf(
                    "Error class: %s, function: %s, error: %s",
                    SalesforceService.class.getSimpleName(),
                    "executeCompositeGraph",
                    e
                );
            }

            Response response = target.request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.token)
                .post(Entity.entity(graphPayload, MediaType.APPLICATION_JSON_TYPE));

            int status = response.getStatus();
            String responseBody = response.readEntity(String.class);

            logger.debugf(
                "Composite Graph Response status: %d, body: %s",
                status,
                responseBody
            );

            // token scaduto (401 Unauthorized o INVALID_SESSION_ID)
            if (status == 401 || (responseBody != null && responseBody.contains("INVALID_SESSION_ID"))) {
                logger.info("Token Salesforce scaduto in executeCompositeGraph, eseguo nuovo login e riprovo");
                getAccessToken();
                return executeCompositeGraph(graphPayload);
            }

            return Response.status(status)
                .entity(responseBody)
                .build();
        }
    }


    public String getApiVersion() {
        return apiVersion;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

}
