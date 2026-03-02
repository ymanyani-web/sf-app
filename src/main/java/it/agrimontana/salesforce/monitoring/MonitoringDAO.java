package it.agrimontana.salesforce.monitoring;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class MonitoringDAO {

    @Resource(lookup = "java:/jdbc/RoleDs")
    private DataSource roleDs;

    // ---------- REQUESTS ----------

    /**
     * Call at request start. Returns request_id to attach to logs/external calls.
     */
    public UUID startRequest(String method,
            String path,
            String queryString,
            String clientIp,
            String userAgent,
            String correlationId,
            String username,
            Integer requestBytes,
            byte[] requestHash,
            String requestBodyMasked) throws SQLException {

        Objects.requireNonNull(method);
        Objects.requireNonNull(path);

        String sql = "INSERT INTO sf_monitoring.mon_request " +
                "(method, path, query_string, client_ip, user_agent, correlation_id, actor_username, request_bytes, request_hash, request_body) "
                +
                "OUTPUT INSERTED.request_id " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = roleDs.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, method);
            ps.setString(2, path);
            ps.setString(3, queryString);

            ps.setString(4, clientIp);
            ps.setString(5, userAgent);
            ps.setString(6, correlationId);
            ps.setString(7, username);
            if (requestBytes == null)
                ps.setNull(8, Types.INTEGER);
            else
                ps.setInt(8, requestBytes);
            if (requestHash == null)
                ps.setNull(9, Types.VARBINARY);
            else
                ps.setBytes(9, requestHash);
            ps.setString(10, safeMsg(requestBodyMasked));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Object o = rs.getObject(1);
                if (o == null) return null;
                return (o instanceof UUID) ? (UUID) o : UUID.fromString(o.toString());
            }
        }
    }

    /** Call at request end (success or failure). */
    public void finishRequest(UUID requestId,
            int statusCode,
            Integer responseBytes,
            byte[] responseHash,
            String responseBodyMasked,
            Throwable error) throws SQLException {

        String sql = "UPDATE sf_monitoring.mon_request " +
                "SET ts_end = SYSUTCDATETIME(), " +
                "status_code = ?, " +
                "duration_ms = DATEDIFF(MILLISECOND, ts_start, SYSUTCDATETIME()), " +
                "response_bytes = ?, " +
                "response_hash = ?, " +
                "response_body = ?, " +
                "error_flag = ?, " +
                "error_class = ?, " +
                "error_message = ? " +
                "WHERE request_id = ?";

        try (Connection c = roleDs.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, statusCode);

            if (responseBytes == null)
                ps.setNull(2, Types.INTEGER);
            else
                ps.setInt(2, responseBytes);
            if (responseHash == null)
                ps.setNull(3, Types.VARBINARY);
            else
                ps.setBytes(3, responseHash);
            ps.setString(4, safeMsg(responseBodyMasked));

            boolean hasErr = (error != null);
            ps.setBoolean(5, hasErr);
            ps.setString(6, hasErr ? error.getClass().getName() : null);
            ps.setString(7, hasErr ? safeMsg(error.getMessage()) : null);

            ps.setObject(8, requestId);

            ps.executeUpdate();
        }
    }

    // ---------- LOGS ----------

    public void insertLog(String level,
            String service,
            String loggerName,
            String message,
            UUID requestId,
            String correlationId,
            Throwable ex,
            String metaJson) throws SQLException {

        String sql = "INSERT INTO sf_monitoring.mon_log " +
                "(level, service, logger, message, request_id, correlation_id, " +
                "exception_class, exception_msg, exception_stack, meta_json) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = roleDs.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, level);
            ps.setString(2, service);
            ps.setString(3, loggerName);
            ps.setString(4, message);

            if (requestId == null)
                ps.setNull(5, Types.OTHER);
            else
                ps.setObject(5, requestId);
            ps.setString(6, correlationId);

            ps.setString(7, ex != null ? ex.getClass().getName() : null);
            ps.setString(8, ex != null ? safeMsg(ex.getMessage()) : null);
            ps.setString(9, ex != null ? stackToString(ex) : null);

            ps.setString(10, metaJson);

            ps.executeUpdate();
        }
    }

    // ---------- EXTERNAL CALLS ----------

    /** Insert at external call start, returns call_id (BIGINT). */
    public long startExternalCall(UUID requestId,
            String correlationId,
            String systemName,
            String operation,
            String target,
            String httpMethod,
            String metaJson) throws SQLException {

        String sql = "INSERT INTO sf_monitoring.mon_external_call " +
                "(request_id, correlation_id, actor_username, system_name, operation, target, http_method, meta_json) "
                +
                "OUTPUT INSERTED.call_id " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = roleDs.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            if (requestId == null)
                ps.setNull(1, Types.OTHER);
            else
                ps.setObject(1, requestId);

            ps.setString(2, correlationId);

            // ✅ auto from ThreadLocal
            String actor = MonitoringContext.username();
            ps.setString(3, actor);

            ps.setString(4, systemName);
            ps.setString(5, operation);
            ps.setString(6, target);
            ps.setString(7, httpMethod);
            ps.setString(8, metaJson);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void finishExternalCall(long callId,
            Integer statusCode,
            boolean success,
            byte[] reqHash,
            byte[] resHash,
            Throwable error,
            String metaJson) throws SQLException {

        String sql = "UPDATE sf_monitoring.mon_external_call " +
                "SET ts_end = SYSUTCDATETIME(), " +
                "duration_ms = DATEDIFF(MILLISECOND, ts_start, SYSUTCDATETIME()), " +
                "status_code = ?, " +
                "success_flag = ?, " +
                "request_hash = ?, " +
                "response_hash = ?, " +
                "error_class = ?, " +
                "error_message = ?, " +
                "meta_json = ? " +
                "WHERE call_id = ? ";

        try (Connection c = roleDs.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            if (statusCode == null)
                ps.setNull(1, Types.INTEGER);
            else
                ps.setInt(1, statusCode);
            ps.setBoolean(2, success);

            if (reqHash == null)
                ps.setNull(3, Types.VARBINARY);
            else
                ps.setBytes(3, reqHash);
            if (resHash == null)
                ps.setNull(4, Types.VARBINARY);
            else
                ps.setBytes(4, resHash);

            ps.setString(5, error != null ? error.getClass().getName() : null);
            ps.setString(6, error != null ? safeMsg(error.getMessage()) : null);

            ps.setString(7, metaJson);
            ps.setLong(8, callId);

            ps.executeUpdate();
        }
    }

    // ---------- ERRORS (signature + event) ----------

    /**
     * Upsert error signature, then insert event.
     * signatureHash should be SHA-256 of:
     * endpoint|system|exceptionClass|normalizedMessage
     */
    public long upsertErrorSignatureAndInsertEvent(
            byte[] signatureHash,
            String endpoint,
            String systemName,
            String exceptionClass,
            String title,
            UUID requestId,
            String correlationId,
            Integer statusCode,
            String message,
            String stacktrace,
            String metaJson) throws SQLException {

        // Transaction is important to keep signature/event consistent.
        try (Connection c = roleDs.getConnection()) {
            c.setAutoCommit(false);
            try {
                long signatureId = upsertErrorSignatureTx(c, signatureHash, endpoint, systemName, exceptionClass,
                        title);
                insertErrorEventTx(c, signatureId, requestId, correlationId, statusCode, message, stacktrace, metaJson);
                c.commit();
                return signatureId;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private long upsertErrorSignatureTx(Connection c,
            byte[] signatureHash,
            String endpoint,
            String systemName,
            String exceptionClass,
            String title) throws SQLException {

        // MERGE with HOLDLOCK avoids races
        String sql = "MERGE sf_monitoring.mon_error_signature WITH (HOLDLOCK) AS tgt " +
                "USING (SELECT ? AS signature_hash) AS src " +
                "ON tgt.signature_hash = src.signature_hash " +
                "WHEN MATCHED THEN " +
                "UPDATE SET last_seen = SYSUTCDATETIME(), " +
                "total_count = tgt.total_count + 1 " +
                "WHEN NOT MATCHED THEN " +
                "INSERT (signature_hash, endpoint, system_name, exception_class, title, first_seen, last_seen, total_count) "
                +
                "VALUES (?, ?, ?, ?, ?, SYSUTCDATETIME(), SYSUTCDATETIME(), 1) " +
                "OUTPUT inserted.signature_id; ";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBytes(1, signatureHash);

            // for insert path:
            ps.setBytes(2, signatureHash);
            ps.setString(3, endpoint);
            ps.setString(4, systemName);
            ps.setString(5, exceptionClass);
            ps.setString(6, title);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void insertErrorEventTx(Connection c,
            long signatureId,
            UUID requestId,
            String correlationId,
            Integer statusCode,
            String message,
            String stacktrace,
            String metaJson) throws SQLException {

        String sql = "INSERT INTO sf_monitoring.mon_error_event " +
                "(signature_id, request_id, correlation_id, status_code, message, stacktrace, meta_json) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, signatureId);

            if (requestId == null)
                ps.setNull(2, Types.OTHER);
            else
                ps.setObject(2, requestId);
            ps.setString(3, correlationId);

            if (statusCode == null)
                ps.setNull(4, Types.INTEGER);
            else
                ps.setInt(4, statusCode);
            ps.setString(5, safeMsg(message));
            ps.setString(6, stacktrace);
            ps.setString(7, metaJson);

            ps.executeUpdate();
        }
    }

    // ---------- SYSTEM CHECKS ----------

    public void insertSystemCheck(String systemName,
            String status,
            Integer latencyMs,
            Integer statusCode,
            String errorMessage,
            String metaJson) throws SQLException {

        String sql = "INSERT INTO sf_monitoring.mon_system_check " +
                "(system_name, status, latency_ms, status_code, error_message, meta_json) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection c = roleDs.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, systemName);
            ps.setString(2, status);

            if (latencyMs == null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, latencyMs);
            if (statusCode == null)
                ps.setNull(4, Types.INTEGER);
            else
                ps.setInt(4, statusCode);

            ps.setString(5, safeMsg(errorMessage));
            ps.setString(6, metaJson);

            ps.executeUpdate();
        }
    }

    // ---------- helpers ----------

    private static String safeMsg(String s) {
        if (s == null)
            return null;
        // avoid crazy-long rows
        return s.length() > 2000 ? s.substring(0, 2000) : s;
    }

    private static String stackToString(Throwable t) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append(t).append("\n");
        for (StackTraceElement el : t.getStackTrace()) {
            sb.append("  at ").append(el).append("\n");
            if (sb.length() > 30000)
                break; // keep it bounded
        }
        Throwable cause = t.getCause();
        if (cause != null && cause != t) {
            sb.append("Caused by: ").append(cause).append("\n");
        }
        return sb.toString();
    }
}