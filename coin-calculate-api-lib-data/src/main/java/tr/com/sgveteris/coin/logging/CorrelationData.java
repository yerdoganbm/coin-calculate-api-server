package tr.com.sgveteris.coin.logging;

import org.slf4j.MDC;

public class CorrelationData {


    public static final String CORRELATION_ID = "X-Correlation-Identifier";
    private static final ThreadLocal<String> id = new ThreadLocal<>();

    private CorrelationData() {
    }

    public static String getId() {
        return id.get();
    }

    public static void setId(String correlationId) {
        MDC.put("correlationId", correlationId);
        id.set(correlationId);
    }
}
