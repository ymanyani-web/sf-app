package it.agrimontana.salesforce.monitoring;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;

@ApplicationScoped
public class DashboardDAO {

    @Resource(lookup = "java:/jdbc/RoleDs")
    private DataSource roleDs;

    public Map<String, Object> kpiLastHours(int hours) throws SQLException {
        String sql = "DECLARE @from datetime2 = DATEADD(HOUR, ?, SYSUTCDATETIME()); "+
            "SELECT COUNT(*) AS total_requests, SUM(CASE WHEN error_flag=1 OR status_code>=500 THEN 1 ELSE 0 END) AS total_errors, AVG(CASE WHEN duration_ms IS NULL THEN 0 ELSE duration_ms END) AS avg_ms " +
            "FROM sf_monitoring.mon_request " +
            "WHERE ts_start >= @from";

        try (Connection c = roleDs.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, -hours);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Map<String, Object> m = new HashMap<>();
                m.put("total_requests", rs.getLong("total_requests"));
                m.put("total_errors", rs.getLong("total_errors"));
                m.put("avg_ms", rs.getDouble("avg_ms"));
                return m;
            }
        }
    }

    public List<Map<String, Object>> hitsPerMinuteLastMinutes(int minutes) throws SQLException {
        String sql = "DECLARE @from datetime2 = DATEADD(MINUTE, ?, SYSUTCDATETIME()); " +
            "SELECT DATEADD(MINUTE, DATEDIFF(MINUTE, 0, ts_start), 0) AS t_min, COUNT(*) AS hits, " +
              "SUM(CASE WHEN error_flag=1 OR status_code>=500 THEN 1 ELSE 0 END) AS errors " +
            "FROM sf_monitoring.mon_request " +
            "WHERE ts_start >= @from " +
            "GROUP BY DATEADD(MINUTE, DATEDIFF(MINUTE, 0, ts_start), 0) " +
            "ORDER BY t_min";

        try (Connection c = roleDs.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, -minutes);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> out = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    Timestamp ts = rs.getTimestamp("t_min");
                    row.put("t_min", ts.toInstant().toString());
                    row.put("hits", rs.getInt("hits"));
                    row.put("errors", rs.getInt("errors"));
                    out.add(row);
                }
                return out;
            }
        }
    }

    public List<Map<String, Object>> latestRequests(int limit) throws SQLException {
        String sql = "SELECT TOP (?) " +
            "request_id, ts_start, duration_ms, status_code, " +
            "method, path, actor_username, correlation_id, error_flag " +
            "FROM sf_monitoring.mon_request " +
            "ORDER BY ts_start DESC";

        try (Connection c = roleDs.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> out = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("request_id", rs.getObject("request_id").toString());
                    row.put("ts_start", rs.getTimestamp("ts_start").toInstant().toString());
                    row.put("duration_ms", rs.getObject("duration_ms"));
                    row.put("status_code", rs.getInt("status_code"));
                    row.put("method", rs.getString("method"));
                    row.put("path", rs.getString("path"));
                    row.put("actor_username", rs.getString("actor_username"));
                    row.put("correlation_id", rs.getString("correlation_id"));
                    row.put("error_flag", rs.getBoolean("error_flag"));
                    out.add(row);
                }
                return out;
            }
        }
    }
}