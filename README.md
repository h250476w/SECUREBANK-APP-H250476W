
# Secure Banking Application

## Project Title
Secure Banking Application

## Description
A robust, enterprise-grade console-based Java application engineered to demonstrate advanced Object-Oriented Programming (OOP) concepts alongside critical secure coding practices[cite: 1, 4]. The system handles user lifecycle management, salted cryptographic authentication, account creation, transactional integrity, and safe file-based data persistence without relying on external database management systems[cite: 1, 4].

## Student Details
- **Name:**WITNESS KWANISAI 
- **Registration Number:** H250476W

---

## Detailed Feature Elaborations

### 1. Cryptographic Authentication & Account Protection
- **Salted Password Hashing:** User credentials are secure against rainbow table and dictionary attacks by employing strong cryptographic hashing (`PBKDF2WithHmacSHA256`) combined with unique, cryptographically random 16-byte salts per user[cite: 1, 4]. Plain-text passwords are never stored or logged[cite: 1, 4].
- **Memory Zeroization:** Sensitive password inputs read as character arrays (`char[]`) are immediately wiped from memory (`Arrays.fill(..., ' ')`) post-authentication to mitigate memory inspection vulnerabilities[cite: 1, 4].
- **Timing Attack Mitigation:** Password hash evaluations use constant-time byte array comparisons (`constantTimeEquals`) to prevent attacker timing side-channel analysis[cite: 1, 4].
- **Brute-Force Defense:** Features account lockout tracking (`FailedAttempts`) that automatically restricts further login attempts after successive failed credentials[cite: 4].

### 2. Account Management & Principle of Least Privilege
- **Isolated User Profiles:** Enforces strict access control (Principle of Least Privilege) where authenticated users are restricted exclusively to viewing, depositing into, or withdrawing from accounts tied directly to their own ownership ID (`acc.getOwnerUsername().equals(currentUser.getUsername())`)[cite: 1, 4].
- **Multi-Account Capabilities:** Supports distinct account types (e.g., `SavingsAccount`, `CurrentAccount`) utilizing object inheritance and polymorphism[cite: 4].

### 3. Financial Transaction Processing & Precision
- **Exact Currency Mathematics:** Eliminates IEEE floating-point rounding errors (inherent to primitive `double` or `float` types) by using high-precision `BigDecimal` arithmetic for all balance recalculations[cite: 1, 4].
- **Atomic Operations:** Guarantees boundary checks on withdrawals to prevent overdrawing or negative balances, immediately returning real-time remaining account balance feedback[cite: 1, 4].

### 4. Robust Persistence & File Storage Security
- **Injection Attack Prevention:** Input fields (usernames, amounts, account numbers) are strictly validated via whitelisting regex and sanitized to strip delimiter characters (`|`, `\r`, `\n`), completely defending against plain-text storage file injection attacks[cite: 1, 4].
- **Atomic File Writes:** Prevents storage file corruption or truncation during unexpected application crashes by writing data updates to temporary swap files (`accounts.tmp`) and performing atomic file replacements (`Files.move` with `ATOMIC_MOVE`)[cite: 1, 4].

### 5. Safe Error Handling & Defensive Audit Logging
- **Suppressed Stack Traces:** Catches low-level exceptions globally and displays clean, sanitized messages to standard output to avoid leaking internal system details or stack traces to end users[cite: 1, 4].
- **Automated Audit Trail:** System events (login attempts, deposits, withdrawals, unauthorized access attempts) are logged via `AuditLogger` with precise timestamps and transaction UUIDs for security auditing[cite: 1, 4].

---

## How to Run

1. **Launch Environment:** Open **IntelliJ IDEA**.
2. **Import Project:** Select **File > Open** and choose the `SecureBankApp-H250476W` project root directory[cite: 1].
3. **Configure Runtime:** Ensure **JDK 17 or higher** is configured in IntelliJ IDEA under **File > Project Structure > Project > SDK**.
4. **Execute Application:** Navigate to `src/securebank/Main.java` (or `src/bank/Main.java`), right-click the file, and select **Run 'Main.main()'**[cite: 1, 4].