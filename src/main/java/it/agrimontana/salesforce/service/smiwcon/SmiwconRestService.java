package it.agrimontana.salesforce.service.smiwcon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SmiwconRestService {

  @Inject
  @ConfigProperty(name = "smiwcon.authUrl")
  private String authUrl;

  @Inject
  @ConfigProperty(name = "smiwcon.endpointB2B")
  private String endpointB2B;

  @Inject
  @ConfigProperty(name = "smiwcon.order")
  private String orderUrl;

  @Inject
  @ConfigProperty(name = "smiwcon.client")
  private String clientUrl;

  @Inject
  @ConfigProperty(name = "smiwcon.user")
  private String user;

  @Inject
  @ConfigProperty(name = "smiwcon.password")
  private String password;

  private static final Logger logger = Logger.getLogger(SmiwconRestService.class);
  private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  private volatile String token;

  private Client client = ClientBuilder.newClient();

  /**
   * LOGIN to B2B endpoint.
   */
  public synchronized String login() {
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s",
        SmiwconRestService.class.getSimpleName(),
        "login"
    );

    try {
      String url = endpointB2B + authUrl;

      logger.infof("SmiwconRestService.login -> POST %s", url);

      MultivaluedHashMap<String, String> form = new MultivaluedHashMap<>();
      form.add("user", user);
      form.add("password", password);

      try (Response response = client.target(url)
              .request(MediaType.APPLICATION_JSON_TYPE)
              .post(Entity.form(form))) {

        String body = response.readEntity(String.class);
        logger.infof("login response: status=%d, body=%s", response.getStatus(), body);

        if (response.getStatus() == 200) {
          // RESPONSE SHOULD BE JSON: { "token":"..." }
          String tokenParsed = extractToken(body);
          this.token = tokenParsed;
          return tokenParsed;
        } else {
          throw new RuntimeException("Login failed: " + response.getStatus() + " body=" + body);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Login exception", e);
    }
  }


  /**
   * Extracts token from JSON manually (your DTO ZIP does not contain login DTO).
   */
  private String extractToken(String json) {
    // Tiny and safe parsing (expected format: {"token":"XYZ"})
    String marker = "\"token\"";
    int idx = json.indexOf(marker);
    if (idx < 0) return null;
    int start = json.indexOf(":", idx) + 1;
    int q1 = json.indexOf("\"", start);
    int q2 = json.indexOf("\"", q1 + 1);
    return json.substring(q1 + 1, q2);
  }


  /**
   * Ensures we have a token; if missing logs in.
   */
  private String ensureToken() {
    if ((token == null) || token.isBlank()) login();
    return token;
  }


  /**
   * Generic POST for JSON body.
   */
  private Response postJson(String relativePath, Object body) {
    String tok = ensureToken();

    String url = endpointB2B + relativePath;

    String jsonBody;
    try {
      jsonBody = mapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      jsonBody = "<serialization error>"+e.getMessage()+"</serialization error>";
    }

    logger.infof("POST %s\nREQUEST BODY:\n%s", url, jsonBody);
    WebTarget target = client.target(url);

    try (Response response = target
            .request(MediaType.APPLICATION_JSON_TYPE)
            .header("token", tok)
            .post(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE))) {

      int status = response.getStatus();
      String respBody = response.readEntity(String.class);
      logger.infof("POST %s RESPONSE -> status=%d, body=%s", url, status, respBody);

      return Response.status(status).entity(respBody).build();
    }
  }

  // post json v2
  private Response postJsonV2(String relativePath, Object body) {
    String jsonBody;
    try {
      jsonBody = mapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      jsonBody = "<serialization error>" + e.getMessage() + "</serialization error>";
    }
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s, Body: %s",
        SmiwconRestService.class.getSimpleName(),
        "postJsonV2",
        jsonBody
    );

    String tok = ensureToken();
    String url = endpointB2B + relativePath;

    WebTarget target = client.target(url);
    Response response = target
        .request(MediaType.APPLICATION_JSON_TYPE)
        .header("token", tok)
        .post(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE));

      return response;
    }

  // ----------------------------------------------------------------------
  //
  //  AGRIMONTANA API METHODS
  //
  // ----------------------------------------------------------------------

  /**
   * API 3 - LOAD ORDINE STD
   * POST /services/icm/orders/store
   */
  public Response storeOrder(Object dto) {
    return postJson(orderUrl, dto);
  }

  // CUSTOM STORE ORDER
  public Response storeOrderV2(Object dto) {
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s ",
        SmiwconRestService.class.getSimpleName(),
        "storeOrderV2"
    );
    Response response = postJsonV2(orderUrl, dto);

    // Se token scaduto, eseguo nuovo login e riprovo
    if (response.getStatus() == 500) {
      String respBody = response.readEntity(String.class);
      if (respBody != null && respBody.contains("rest.login.error.noTokenUser")) {
        logger.info("Token scaduto in storeOrderV2, eseguo nuovo login e riprovo");
        login();
        return postJsonV2(orderUrl, dto);
      }
      throw new RuntimeException(respBody);
    }

    return response;
  }

  /**
   * PARTNER CREATION
   * POST /services/agrimontana/partners/createPartnerFromJson
   */
  public Response createPartner(Object dto) {
    return postJson(clientUrl, dto);
  }

  public Response createPartnerV2(Object dto) {
    logger.debugf(
        "Richiesta http nella classe: %s, funzione: %s ",
        SmiwconRestService.class.getSimpleName(),
        "createPartnerV2"
    );
    Response response = postJsonV2(clientUrl, dto);

    // Se token scaduto, eseguo nuovo login e riprovo
    if (response.getStatus() == 500) {
      String respBody = response.readEntity(String.class);
      if (respBody != null && respBody.contains("rest.login.error.noTokenUser")) {
        logger.info("Token scaduto in createPartnerV2, eseguo nuovo login e riprovo");
        login();
        return postJsonV2(clientUrl, dto);
      }
      throw new RuntimeException(respBody);
    }

    return response;
  }
}
