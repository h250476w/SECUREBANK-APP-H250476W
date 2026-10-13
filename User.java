
package bank.model;

public class User {
    private final String username;
    private final String passwordHash;
    private final String salt;

    public User(String username, String passwordHash, String salt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt() { return salt; }

    public String toFileFormat() {
        return username + "|" + passwordHash + "|" + salt;
    }

    public static User fromFileFormat(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 3) return null;
        return new User(parts[0], parts[1], parts[2]);
    }
}
