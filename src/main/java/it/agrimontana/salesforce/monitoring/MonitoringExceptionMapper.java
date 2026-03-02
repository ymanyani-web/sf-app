package it.agrimontana.salesforce.monitoring;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.sql.SQLException;
import java.util.UUID;

@Provider
@ApplicationScoped
public class MonitoringExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    MonitoringDAO monitoringDAO;

    @Override
    public Response toResponse(Throwable error) {

        UUID requestId = MonitoringContext.requestId();

        System.out.println("=== [MonitoringExceptionMapper] CALLED ===");
        System.out.println("[MonitoringExceptionMapper] requestId=" + requestId);
        System.out.println("[MonitoringExceptionMapper] corr=" + MonitoringContext.correlationId());
        System.out.println("[MonitoringExceptionMapper] daoInjected=" + (monitoringDAO != null));
        System.out.println("[MonitoringExceptionMapper] errorClass=" + error.getClass().getName());
        System.out.println("[MonitoringExceptionMapper] errorMsg=" + error.getMessage());
        error.printStackTrace();

        // try to persist error into mon_request
        try {
            if (monitoringDAO == null) {
                System.out.println("[MonitoringExceptionMapper] ❌ monitoringDAO is NULL -> CDI injection problem");
            } else if (requestId == null) {
                System.out.println("[MonitoringExceptionMapper] ❌ requestId is NULL -> startRequest not inserted or TL lost");
            } else {
                monitoringDAO.finishRequest(
                        requestId,
                        500,
                        MonitoringContext.responseBytes(),
                        MonitoringContext.responseHash(),
                        MonitoringContext.responseBody(),
                        error
                );
                System.out.println("[MonitoringExceptionMapper] ✅ finishRequest(error) executed");
            }
        } catch (SQLException e) {
            System.out.println("[MonitoringExceptionMapper] ❌ SQLException during finishRequest(error)");
            e.printStackTrace();
        } catch (Throwable t) {
            System.out.println("[MonitoringExceptionMapper] ❌ Throwable during finishRequest(error)");
            t.printStackTrace();
        }

        String body = "{\"status\":\"KO\",\"message\":\"Internal Server Error\"}";
        return Response.status(500).type(MediaType.APPLICATION_JSON).entity(body).build();
    }
}