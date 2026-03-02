package it.agrimontana.salesforce.monitoring;

import java.util.UUID;

public final class MonitoringContext {
    private MonitoringContext() {}

    private static final ThreadLocal<Ctx> TL = new ThreadLocal<>();

    public static void set(UUID requestId, String correlationId, String username) {
        TL.set(new Ctx(requestId, correlationId, username));
    }

    public static UUID requestId() {
        Ctx c = TL.get();
        return c == null ? null : c.requestId;
    }

    public static String correlationId() {
        Ctx c = TL.get();
        return c == null ? null : c.correlationId;
    }

    public static String username() {
        Ctx c = TL.get();
        return c == null ? null : c.username;
    }

    // ✅ NEW: response capture storage
    public static void setResponse(Integer bytes, byte[] hash, String body) {
        Ctx c = TL.get();
        if (c == null) return;
        c.responseBytes = bytes;
        c.responseHash = hash;
        c.responseBody = body;
    }

    public static Integer responseBytes() {
        Ctx c = TL.get();
        return c == null ? null : c.responseBytes;
    }

    public static byte[] responseHash() {
        Ctx c = TL.get();
        return c == null ? null : c.responseHash;
    }

    public static String responseBody() {
        Ctx c = TL.get();
        return c == null ? null : c.responseBody;
    }

    public static void clear() {
        TL.remove();
    }

    private static final class Ctx {
        final UUID requestId;
        final String correlationId;
        final String username;

        Integer responseBytes;
        byte[] responseHash;
        String responseBody;

        Ctx(UUID requestId, String correlationId, String username) {
            this.requestId = requestId;
            this.correlationId = correlationId;
            this.username = username;
        }
    }
}