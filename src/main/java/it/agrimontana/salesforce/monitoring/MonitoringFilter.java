package it.agrimontana.salesforce.monitoring;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.UUID;

import jakarta.ws.rs.container.PreMatching;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 100)
public class MonitoringFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

    @Inject
    MonitoringDAO monitoringDAO;

    private static final String REQ_ID_PROP = "sfmon.requestId";
    private static final String CORR_PROP = "sfmon.correlationId";
    private static final String USER_PROP = "sfmon.username";

    // store bodies in requestContext properties
    private static final String REQ_BODY_PROP = "sfmon.reqBody";
    private static final String REQ_BYTES_PROP = "sfmon.reqBytes";
    private static final String REQ_HASH_PROP = "sfmon.reqHash";

    private static final String RES_BODY_PROP = "sfmon.resBody";
    private static final String RES_BYTES_PROP = "sfmon.resBytes";
    private static final String RES_HASH_PROP = "sfmon.resHash";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String correlationId = null;
        String username = "anonymous";

        System.out.println("=== [MonitoringFilter] REQUEST START ===");

        try {
            // ---- correlation id
            correlationId = requestContext.getHeaderString("X-Correlation-Id");
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
                System.out.println("[MonitoringFilter] Generated correlationId=" + correlationId);
            } else {
                System.out.println("[MonitoringFilter] Incoming correlationId=" + correlationId);
            }

            // ---- security context
            username = resolveUsername(requestContext);
            System.out.println("[MonitoringFilter] Final resolved username=" + username);
            if ("anonymous".equals(username)) {
                username = systemActor();
                System.out.println("[MonitoringFilter] Fallback systemActor username=" + username);
            }

            // ---- capture REQUEST body (simple)
            Integer requestBytes = null;
            byte[] requestHash = null;
            String requestBody = null;

            System.out.println("[MonitoringFilter] hasEntity=" + requestContext.hasEntity());
            System.out.println("[MonitoringFilter] method=" + requestContext.getMethod());
            System.out.println("[MonitoringFilter] entityStream=" + requestContext.getEntityStream());

            if (requestContext.hasEntity() && requestContext.getEntityStream() != null) {
                byte[] bodyBytes = readAll(requestContext.getEntityStream());
                requestContext.setEntityStream(new ByteArrayInputStream(bodyBytes)); // IMPORTANT restore stream

                requestBytes = bodyBytes.length;
                requestHash = sha256(bodyBytes);
                requestBody = new String(bodyBytes); // keep it simple

                requestContext.setProperty(REQ_BYTES_PROP, requestBytes);
                requestContext.setProperty(REQ_HASH_PROP, requestHash);
                requestContext.setProperty(REQ_BODY_PROP, requestBody);
                System.out.println("[MonitoringFilter] request body bytes=" + bodyBytes.length);
                System.out.println("[MonitoringFilter] request body (first 200)="
                        + (requestBody == null ? "null"
                                : requestBody.substring(0, Math.min(200, requestBody.length()))));
                System.out.println("[MonitoringFilter] requestHash null? " + (requestHash == null));
            }
            // ---- set context BEFORE DB
            MonitoringContext.set(null, correlationId, username);
            System.out.println("[MonitoringFilter] MonitoringContext set (pre-insert)");
            System.out.println("[MonitoringFilter] about to startRequest - requestBytes=" + requestBytes
                    + " hashNull=" + (requestHash == null)
                    + " bodyNull=" + (requestBody == null));
            // ---- insert request row (now passing body/hash/bytes)
            UUID requestId = monitoringDAO.startRequest(
                    requestContext.getMethod(),
                    "/" + requestContext.getUriInfo().getPath(),
                    requestContext.getUriInfo().getRequestUri().getQuery(),
                    requestContext.getHeaderString("X-Forwarded-For"),
                    requestContext.getHeaderString("User-Agent"),
                    correlationId,
                    username,
                    requestBytes,
                    requestHash,
                    requestBody);

            System.out.println("[MonitoringFilter] DB request inserted, requestId=" + requestId);

            // ---- update context with requestId
            MonitoringContext.set(requestId, correlationId, username);
            System.out.println("[MonitoringFilter] MonitoringContext updated with requestId");

            requestContext.setProperty(REQ_ID_PROP, requestId);
            requestContext.setProperty(CORR_PROP, correlationId);
            requestContext.setProperty(USER_PROP, username);

        } catch (Exception e) {
            System.out.println("[MonitoringFilter] ❌ ERROR in request filter");
            e.printStackTrace();

            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            MonitoringContext.set(null, correlationId, username);
        }
    }

    /**
     * Capture RESPONSE body (this is the only reliable place).
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext ctx) throws IOException {
        OutputStream original = ctx.getOutputStream();
        ByteArrayOutputStream copy = new ByteArrayOutputStream();

        ctx.setOutputStream(new TeeOutputStream(original, copy));
        ctx.proceed();
        ctx.setOutputStream(original);

        byte[] bytes = copy.toByteArray();
        ctx.setProperty(RES_BYTES_PROP, bytes.length);
        ctx.setProperty(RES_HASH_PROP, sha256(bytes));
        ctx.setProperty(RES_BODY_PROP, new String(bytes));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        System.out.println("=== [MonitoringFilter] RESPONSE END ===");

        try {
            UUID requestId = (UUID) requestContext.getProperty(REQ_ID_PROP);
            String correlationId = (String) requestContext.getProperty(CORR_PROP);

            System.out.println("[MonitoringFilter] requestId=" + requestId);
            System.out.println("[MonitoringFilter] correlationId=" + correlationId);
            System.out.println("[MonitoringFilter] responseStatus=" + responseContext.getStatus());

            // ---- get captured RESPONSE values
            Integer responseBytes = (Integer) requestContext.getProperty(RES_BYTES_PROP);
            byte[] responseHash = (byte[]) requestContext.getProperty(RES_HASH_PROP);
            String responseBody = (String) requestContext.getProperty(RES_BODY_PROP);

            if (requestId != null) {
                // if mapper already finished on error, skip here
                // simplest skip: only finish here when status < 500
                if (responseContext.getStatus() < 500) {
                    monitoringDAO.finishRequest(
                            requestId,
                            responseContext.getStatus(),
                            MonitoringContext.responseBytes(),
                            MonitoringContext.responseHash(),
                            MonitoringContext.responseBody(),
                            null);
                } else {
                    System.out.println("[MonitoringFilter] status>=500 -> error already handled by ExceptionMapper");
                }
            }

            if (correlationId != null) {
                responseContext.getHeaders().putSingle("X-Correlation-Id", correlationId);
            }

        } catch (Exception e) {
            System.out.println("[MonitoringFilter] ❌ ERROR in response filter");
            e.printStackTrace();
        } finally {
            MonitoringContext.clear();
            System.out.println("[MonitoringFilter] MonitoringContext cleared");
        }
    }

    // ---------------- helpers ----------------

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1)
            out.write(buf, 0, r);
        return out.toByteArray();
    }

    private static byte[] sha256(byte[] data) {
        if (data == null)
            return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (Exception e) {
            return null;
        }
    }

    static final class TeeOutputStream extends OutputStream {
        private final OutputStream a;
        private final OutputStream b;

        TeeOutputStream(OutputStream a, OutputStream b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public void write(int x) throws IOException {
            a.write(x);
            b.write(x);
        }

        @Override
        public void write(byte[] buf) throws IOException {
            a.write(buf);
            b.write(buf);
        }

        @Override
        public void write(byte[] buf, int off, int len) throws IOException {
            a.write(buf, off, len);
            b.write(buf, off, len);
        }

        @Override
        public void flush() throws IOException {
            a.flush();
            b.flush();
        }

        @Override
        public void close() throws IOException {
            a.close();
            b.close();
        }
    }

    // ---------------- your existing username helpers ----------------

    private static String resolveUsername(ContainerRequestContext ctx) {
        if (ctx.getSecurityContext() != null && ctx.getSecurityContext().getUserPrincipal() != null) {
            String p = ctx.getSecurityContext().getUserPrincipal().getName();
            if (p != null && !p.isBlank())
                return p;
        }

        String u;
        u = ctx.getHeaderString("X-Forwarded-User");
        if (!isBlank(u))
            return u;
        u = ctx.getHeaderString("X-Authenticated-User");
        if (!isBlank(u))
            return u;
        u = ctx.getHeaderString("X-Remote-User");
        if (!isBlank(u))
            return u;
        u = ctx.getHeaderString("X-Client-Id");
        if (!isBlank(u))
            return u;
        u = ctx.getHeaderString("X-MS-CLIENT-PRINCIPAL-NAME");
        if (!isBlank(u))
            return u;
        u = ctx.getHeaderString("X-User");
        if (!isBlank(u))
            return u;

        return "anonymous";
    }

    private static String systemActor() {
        String host = System.getenv("COMPUTERNAME");
        if (isBlank(host))
            host = System.getenv("HOSTNAME");
        if (isBlank(host))
            host = "unknown-host";

        String osUser = System.getProperty("user.name");
        if (isBlank(osUser))
            osUser = "unknown-user";

        long pid;
        try {
            pid = ProcessHandle.current().pid();
        } catch (Throwable t) {
            pid = -1;
        }

        return pid > 0 ? (host + "\\" + osUser + " (pid:" + pid + ")") : (host + "\\" + osUser);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}