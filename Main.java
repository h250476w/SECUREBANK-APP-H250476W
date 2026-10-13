package bank;

import bank.model.Account;
import bank.model.Transaction;
import bank.security.InputValidator;
import bank.security.SecurityLogger;
import bank.service.AccountService;
import bank.service.AuthenticationService;
import bank.service.DataPersistenceService;

import java.io.Console;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {

    // ---- Menu choices ----
    private static final String AUTH_REGISTER = "1";
    private static final String AUTH_LOGIN = "2";
    private static final String AUTH_EXIT = "3";

    private static final String MENU_CREATE_ACCOUNT = "1";
    private static final String MENU_BALANCE = "2";
    private static final String MENU_DEPOSIT = "3";
    private static final String MENU_WITHDRAW = "4";
    private static final String MENU_HISTORY = "5";
    private static final String MENU_LOGOUT = "6";

    // ---- Security settings ----
    private static final int MAX_FAILED_LOGINS = 3;
    private static final long LOCKOUT_MILLIS = 30_000L;          // 30 seconds
    private static final long IDLE_TIMEOUT_MILLIS = 5 * 60_000L; // 5 minutes
    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final Scanner scanner = new Scanner(System.in);
    private static DataPersistenceService persistenceService;
    private static AuthenticationService authService;
    private static AccountService accountService;

    // Failed-login tracking (keyed by lower-case username)
    private static final Map<String, Integer> failedAttempts = new HashMap<>();
    private static final Map<String, Long> lockedUntil = new HashMap<>();

    private static long lastActivity = System.currentTimeMillis();

    public static void main(String[] args) {
        try {
            persistenceService = new DataPersistenceService();
            authService = new AuthenticationService(persistenceService);
            accountService = new AccountService(persistenceService);

            System.out.println("=========================================");
            System.out.println("   WELCOME TO SECURE BANKING SYSTEM      ");
            System.out.println("=========================================");

            boolean running = true;
            while (running) {
                if (!authService.isAuthenticated()) {
                    running = showAuthMenu();
                } else {
                    showUserMenu();
                }
            }
            System.out.println("Thank you for using Secure Banking System. Goodbye!");
        } catch (Exception e) {
            SecurityLogger.logError("FATAL_APP_ERROR", "SYSTEM", sanitizeForLog(e.getMessage()));
            System.out.println("An unexpected internal system error occurred. Please contact support.");
        }
    }

    // =====================================================================
    // Authentication
    // =====================================================================

    private static boolean showAuthMenu() {
        System.out.println("\n1. Register New User");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Select an option: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case AUTH_REGISTER:
                handleRegister();
                break;
            case AUTH_LOGIN:
                handleLogin();
                break;
            case AUTH_EXIT:
                return false;
            default:
                System.out.println("Invalid option. Please choose 1, 2, or 3.");
        }
        return true;
    }

    private static void handleRegister() {
        System.out.println("\n--- REGISTRATION ---");
        System.out.print("Enter Desired Username (3-20 chars, e.g. john_doe): ");
        String username = scanner.nextLine().trim();

        char[] password = readPassword("Enter Strong Password (min " + MIN_PASSWORD_LENGTH
                + " chars, upper, lower, digit, symbol): ");
        char[] confirmPassword = readPassword("Confirm Password: ");

        try {
            if (!Arrays.equals(password, confirmPassword)) {
                System.out.println(">>> Registration FAILED! Passwords do not match.");
                return;
            }

            String weakness = checkPasswordStrength(password, username);
            if (weakness != null) {
                System.out.println(">>> Registration FAILED! " + weakness);
                return;
            }

            if (authService.register(username, password)) {
                System.out.println(">>> Registration successful! You may now log in.");
            } else {
                System.out.println(">>> Registration FAILED! Verify username format (3-20 chars) "
                        + "and that the username is not already taken.");
            }
        } finally {
            wipe(password);
            wipe(confirmPassword);
        }
    }

    private static void handleLogin() {
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();
        String key = username.toLowerCase(Locale.ROOT);

        // Check lockout BEFORE asking for the password
        Long unlockTime = lockedUntil.get(key);
        if (unlockTime != null) {
            long remaining = unlockTime - System.currentTimeMillis();
            if (remaining > 0) {
                System.out.println("\nToo many failed attempts. Try again in "
                        + ((remaining + 999) / 1000) + " second(s).");
                return;
            }
            lockedUntil.remove(key);
            failedAttempts.remove(key);
        }

        char[] password = readPassword("Enter Password: ");
        try {
            if (authService.login(username, password)) {
                failedAttempts.remove(key);
                lastActivity = System.currentTimeMillis();
                System.out.println("\nLogin successful! Welcome, "
                        + authService.getCurrentUser().getUsername() + ".");
            } else {
                registerFailedLogin(key, username);
                System.out.println("\nInvalid credentials provided.");
            }
        } finally {
            wipe(password);
        }
    }

    private static void registerFailedLogin(String key, String username) {
        int attempts = failedAttempts.merge(key, 1, Integer::sum);
        if (attempts >= MAX_FAILED_LOGINS) {
            lockedUntil.put(key, System.currentTimeMillis() + LOCKOUT_MILLIS);
            failedAttempts.remove(key);
            SecurityLogger.logError("LOGIN_LOCKOUT", sanitizeForLog(username),
                    "Account temporarily locked after " + MAX_FAILED_LOGINS + " failed logins");
            System.out.println("Too many failed attempts. Login disabled for "
                    + (LOCKOUT_MILLIS / 1000) + " seconds.");
        }
    }

    // =====================================================================
    // User menu
    // =====================================================================

    private static void showUserMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Create New Bank Account");
        System.out.println("2. View Account Balance");
        System.out.println("3. Deposit Funds");
        System.out.println("4. Withdraw Funds");
        System.out.println("5. View Account Transaction History");
        System.out.println("6. Logout");
        System.out.print("Select an option: ");

        String choice = scanner.nextLine().trim();

        // Session timeout: checked once input arrives
        long now = System.currentTimeMillis();
        if (now - lastActivity > IDLE_TIMEOUT_MILLIS) {
            authService.logout();
            System.out.println("\nSession expired due to inactivity. Please log in again.");
            return;
        }
        lastActivity = now;

        switch (choice) {
            case MENU_CREATE_ACCOUNT:
                handleCreateAccount();
                break;
            case MENU_BALANCE:
                handleViewBalance();
                break;
            case MENU_DEPOSIT:
                handleDeposit();
                break;
            case MENU_WITHDRAW:
                handleWithdrawal();
                break;
            case MENU_HISTORY:
                handleViewTransactions();
                break;
            case MENU_LOGOUT:
                authService.logout();
                System.out.println("Logged out successfully.");
                break;
            default:
                System.out.println("Invalid menu option.");
        }
    }

    private static void handleCreateAccount() {
        Account acc = accountService.createAccount(authService.getCurrentUser());
        if (acc != null) {
            System.out.println("Account successfully created! Assigned Account Number: "
                    + acc.getAccountNumber());
        } else {
            System.out.println("Account could not be created. Please try again.");
        }
    }

    private static void handleViewBalance() {
        String accNo = promptForAccountNumber();
        if (accNo == null) return;

        Account account = accountService.getAccountIfOwned(accNo, authService.getCurrentUser());
        if (account != null) {
            System.out.println(String.format(Locale.US, "Account: %s | Balance: $%.2f",
                    account.getAccountNumber(), account.getBalance()));
        } else {
            System.out.println("Access Denied or Account Not Found.");
        }
    }

    private static void handleDeposit() {
        String accNo = promptForAccountNumber();
        if (accNo == null) return;

        System.out.print("Enter Deposit Amount: $");
        String inputAmount = scanner.nextLine();

        try {
            BigDecimal amount = InputValidator.parsePositiveAmount(inputAmount);
            if (accountService.deposit(accNo, amount, authService.getCurrentUser())) {
                Account acc = accountService.getAccountIfOwned(accNo, authService.getCurrentUser());
                if (acc != null) {
                    System.out.println(String.format(Locale.US,
                            ">>> Deposit successful! Account: %s | New Balance: $%.2f",
                            acc.getAccountNumber(), acc.getBalance()));
                }
            } else {
                System.out.println("Transaction failed. Access Denied or Account Not Found.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid Amount: " + e.getMessage());
        }
    }

    private static void handleWithdrawal() {
        String accNo = promptForAccountNumber();
        if (accNo == null) return;

        System.out.print("Enter Withdrawal Amount: $");
        String inputAmount = scanner.nextLine();

        try {
            BigDecimal amount = InputValidator.parsePositiveAmount(inputAmount);
            if (accountService.withdraw(accNo, amount, authService.getCurrentUser())) {
                Account acc = accountService.getAccountIfOwned(accNo, authService.getCurrentUser());
                if (acc != null) {
                    System.out.println(String.format(Locale.US,
                            ">>> Withdrawal successful! Account: %s | Remaining Balance: $%.2f",
                            acc.getAccountNumber(), acc.getBalance()));
                }
            } else {
                System.out.println("Transaction failed. Check account ownership or insufficient funds.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid Amount: " + e.getMessage());
        }
    }

    private static void handleViewTransactions() {
        String accNo = promptForAccountNumber();
        if (accNo == null) return;

        List<Transaction> txs = accountService.getAccountTransactions(accNo, authService.getCurrentUser());
        if (txs.isEmpty()) {
            System.out.println("No records found or unauthorized access.");
        } else {
            System.out.println("\n--- TRANSACTION HISTORY ---");
            for (Transaction tx : txs) {
                System.out.println(String.format(Locale.US, "[%s] %s - $%.2f",
                        tx.getTimestamp(), tx.getType(), tx.getAmount()));
            }
        }
    }

    private static String promptForAccountNumber() {
        List<Account> owned = accountService.getUserAccounts(authService.getCurrentUser());
        if (owned.isEmpty()) {
            System.out.println("You currently do not own any bank accounts. Create one first.");
            return null;
        }

        System.out.println("Your accounts:");
        for (Account a : owned) {
            System.out.println("  - " + a.getAccountNumber());
        }

        System.out.print("Enter Bank Account Number (e.g. ACC123456): ");
        String input = scanner.nextLine().trim();
        if (!InputValidator.isValidAccountNumber(input)) {
            System.out.println("Invalid Account Number Format.");
            return null;
        }
        return input;
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private static char[] readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] pw = console.readPassword("%s", prompt);
            return pw != null ? pw : new char[0];
        }
        // Fallback for IDE consoles: input is NOT hidden
        System.out.println("[WARNING: no secure console detected - password input will be visible]");
        System.out.print(prompt);
        return scanner.nextLine().toCharArray();
    }

    private static void wipe(char[] data) {
        if (data != null) {
            Arrays.fill(data, '\0');
        }
    }

    /** Returns null if the password is acceptable, otherwise a message explaining why not. */
    private static String checkPasswordStrength(char[] password, String username) {
        if (password.length < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.";
        }
        boolean upper = false, lower = false, digit = false, symbol = false;
        for (char c : password) {
            if (Character.isUpperCase(c)) upper = true;
            else if (Character.isLowerCase(c)) lower = true;
            else if (Character.isDigit(c)) digit = true;
            else symbol = true;
        }
        if (!(upper && lower && digit && symbol)) {
            return "Password must contain an upper-case letter, a lower-case letter, a digit and a symbol.";
        }
        if (!username.isEmpty()
                && new String(password).toLowerCase(Locale.ROOT).contains(username.toLowerCase(Locale.ROOT))) {
            return "Password must not contain your username.";
        }
        return null;
    }

    /** Strips line breaks and control characters so user-controlled text cannot forge log entries. */
    private static String sanitizeForLog(String text) {
        if (text == null) return "null";
        String cleaned = text.replaceAll("[\\r\\n\\t]", "_").replaceAll("\\p{Cntrl}", "");
        return cleaned.length() > 100 ? cleaned.substring(0, 100) : cleaned;
    }
}
