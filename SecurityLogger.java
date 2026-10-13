
package bank.security;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SecurityLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logInfo(String event, String username) {
        System.out.printf("[%s] [INFO] Event: %s | User: %s%n",
                LocalDateTime.now().format(FORMATTER), event, sanitize(username));
    }

    public static void logWarning(String event, String username, String reason) {
        System.out.printf("[%s] [WARN] Event: %s | User: %s | Reason: %s%n",
                LocalDateTime.now().format(FORMATTER), event, sanitize(username), sanitize(reason));
    }

    public static void logError(String event, String username, String safeMessage) {
        System.err.printf("[%s] [ERROR] Event: %s | User: %s | Details: %s%n",
                LocalDateTime.now().format(FORMATTER), event, sanitize(username), sanitize(safeMessage));
    }

    private static String sanitize(String value) {
        if (value == null) return "N/A";
        return value.replaceAll("[\\r\\n]", "_");
    }
}