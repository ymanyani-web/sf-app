package it.agrimontana.salesforce.service.smiwcon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.agrimontana.salesforce.monitoring.MonitoringContext;
import it.agrimontana.salesforce.monitoring.MonitoringDAO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.UUID;

@ApplicationScoped
public class SmiwconRestService {

  @Inject
  MonitoringDAO monitoringDAO;

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

  // reuse client
  private final Client client = ClientBuilder.newClient();

  // ----------------------------------------------------------------------
  //  AUTH
  // ----------------------------------------------------------------------

  /** LOGIN to B2B endpoint. */
  public synchronized String login() {
    logger.debugf("SMIWCON login()");

    String url = endpointB2B + authUrl;

    MultivaluedHashMap<String, String> form = new MultivaluedHashMap<>();
    form.add("user", user);
    form.add("password", password);

    Response resp = null;
    try {
      final String finalUrl = url;
      resp = withSmiwconCall(
          "SmiwconRestService.login",
          finalUrl,
          "POST",
          new HttpWork() {
            @Override public Response run() {
              return client.target(finalUrl)
                  .request(MediaType.APPLICATION_JSON_TYPE)
                  .post(Entity.form(form));
            }
          }
      );

      int status = resp.getStatus();
      String body = safeReadBody(resp);

      logger.infof("SMIWCON login response: status=%d, body=%s", status, body);

      if (status == 200) {
        String tokenParsed = extractToken(body);
        this.token = tokenParsed;
        return tokenParsed;
      }

      throw new RuntimeException("Login failed: " + status + " body=" + body);

    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("Login exception", e);
    } finally {
      safeClose(resp);
    }
  }

  /** Extracts token from JSON manually (expected {"token":"XYZ"}). */
  private String extractToken(String json) {
    if (json == null) return null;
    String marker = "\"token\"";
    int idx = json.indexOf(marker);
    if (idx < 0) return null;
    int start = json.indexOf(":", idx) + 1;
    int q1 = json.indexOf("\"", start);
    int q2 = json.indexOf("\"", q1 + 1);
    if (q1 < 0 || q2 < 0) return null;
    return json.substring(q1 + 1, q2);
  }

  /** Ensures we have a token; if missing logs in. */
  private String ensureToken() {
    if (token == null || token.isBlank()) {
      login();
    }
    return token;
  }

  // ----------------------------------------------------------------------
  //  HTTP helpers (mon_external_call)
  // ----------------------------------------------------------------------

  @FunctionalInterface
  private interface HttpWork {
    Response run() throws Exception;
  }

  private Response withSmiwconCall(String operation, String target, String httpMethod, HttpWork work) {
    UUID reqId = MonitoringContext.requestId();
    String corr = MonitoringContext.correlationId();

    long callId = -1;
    try {
      callId = monitoringDAO.startExternalCall(
          reqId,
          corr,
          "SMIWCON",
          operation,
          target,
          httpMethod,
          null
      );

      Response resp = work.run();

      // IMPORTANT: do NOT readEntity() here, because caller may read it.
      // We'll close+wrap bodies in our postJsonV2, so here we only mark status.
      int status = (resp != null) ? resp.getStatus() : 0;
      boolean ok = status >= 200 && status < 400;

      monitoringDAO.finishExternalCall(
          callId,
          status,
          ok,
          null,
          null,
          null,
          null
      );

      return resp;

    } catch (Exception e) {
      try {
        if (callId != -1) {
          monitoringDAO.finishExternalCall(callId, null, false, null, null, e, null);
        }
      } catch (Exception ignore) {}
      throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
    }
  }

  private static void safeClose(Response r) {
    try {
      if (r != null) r.close();
    } catch (Exception ignore) {}
  }

  private static String safeReadBody(Response r) {
    try {
      return (r != null) ? r.readEntity(String.class) : null;
    } catch (Exception e) {
      return null;
    }
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  // ----------------------------------------------------------------------
  //  POST JSON (WRAPPED + RETURN SAFE RESPONSE)
  // ----------------------------------------------------------------------

  /**
   * POST JSON body to SMIWCON.
   * Reads body once, closes raw response, returns a new Response with body string.
   * Handles token-expired scenario (500 + rest.login.error.noTokenUser) -> relogin -> retry once.
   */
  private Response postJsonV2(String relativePath, Object body) {
    String tok = ensureToken();
    final String url = endpointB2B + relativePath;

    String jsonBody;
    try {
      jsonBody = mapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      jsonBody = "<serialization error>" + e.getMessage() + "</serialization error>";
    }

    logger.infof("SMIWCON POST %s\nREQUEST BODY:\n%s", url, jsonBody);

    // 1st attempt
    Response raw = null;
    try {
      raw = withSmiwconCall(
          "SmiwconRestService.postJsonV2",
          url,
          "POST",
          new HttpWork() {
            @Override public Response run() {
              WebTarget target = client.target(url);
              return target.request(MediaType.APPLICATION_JSON_TYPE)
                  .header("token", tok)
                  .post(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE));
            }
          }
      );

      int status = raw.getStatus();
      String respBody = safeReadBody(raw);

      // token expired pattern you already used
      if (status == 500 && respBody != null && respBody.contains("rest.login.error.noTokenUser")) {
        logger.info("SMIWCON token expired, relogin and retry once");
        login(); // refresh token
        safeClose(raw);

        final String tok2 = ensureToken();
        Response raw2 = null;
        try {
          raw2 = withSmiwconCall(
              "SmiwconRestService.postJsonV2.retry",
              url,
              "POST",
              new HttpWork() {
                @Override public Response run() {
                  WebTarget target2 = client.target(url);
                  return target2.request(MediaType.APPLICATION_JSON_TYPE)
                      .header("token", tok2)
                      .post(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE));
                }
              }
          );

          int status2 = raw2.getStatus();
          String respBody2 = safeReadBody(raw2);

          logger.infof("SMIWCON POST %s RESPONSE -> status=%d, body=%s", url, status2, respBody2);
          return Response.status(status2).entity(respBody2).build();

        } finally {
          safeClose(raw2);
        }
      }

      logger.infof("SMIWCON POST %s RESPONSE -> status=%d, body=%s", url, status, respBody);
      return Response.status(status).entity(respBody).build();

    } finally {
      safeClose(raw);
    }
  }

  // ----------------------------------------------------------------------
  //  AGRIMONTANA API METHODS
  // ----------------------------------------------------------------------

  /** API 3 - LOAD ORDINE STD  POST /services/icm/orders/store */
  public Response storeOrder(Object dto) {
    return postJsonV2(orderUrl, dto);
  }

  /** CUSTOM STORE ORDER */
  public Response storeOrderV2(Object dto) {
    logger.debugf("SmiwconRestService.storeOrderV2()");
    Response response = postJsonV2(orderUrl, dto);

    // if you still want to throw on 500:
    if (response.getStatus() == 500) {
      String respBody = safeReadBody(response); // safe because our response is a built one
      throw new RuntimeException(respBody);
    }

    return response;
  }

  /** PARTNER CREATION POST /services/agrimontana/partners/createPartnerFromJson */
  public Response createPartner(Object dto) {
    return postJsonV2(clientUrl, dto);
  }

  public Response createPartnerV2(Object dto) {
    logger.debugf("SmiwconRestService.createPartnerV2()");
    Response response = postJsonV2(clientUrl, dto);

    if (response.getStatus() == 500) {
      String respBody = safeReadBody(response);
      throw new RuntimeException(respBody);
    }

    return response;
  }
}