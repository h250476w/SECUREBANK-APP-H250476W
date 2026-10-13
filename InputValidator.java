package bank.security;
import java.math.BigDecimal;
import java.util.regex.Pattern;

public class InputValidator {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern ACCOUNT_NO_PATTERN = Pattern.compile("^ACC[0-9]{6}$");

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && ACCOUNT_NO_PATTERN.matcher(accountNumber).matches();
    }

    public static String sanitizeForStorage(String input) {
        if (input == null) return "";
        // Remove file delimiter characters (| and newline) to prevent file injection
        return input.replaceAll("[|\\r\\n]", "").trim();
    }

    public static BigDecimal parsePositiveAmount(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be empty.");
        }
        try {
            BigDecimal amount = new BigDecimal(input.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }
            if (amount.scale() > 2) {
                throw new IllegalArgumentException("Amount cannot have more than 2 decimal places.");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid monetary amount format.");
        }
    }
}

