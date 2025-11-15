package entity;

import java.io.Serializable;
import java.util.Objects;

/** base class for all users. */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private String password;
    private boolean firstLogin;

    /** everyone starts with default password, which is "password" */
    protected User(String id, String name) {
        this.id = Objects.requireNonNull(id).trim();
        this.name = Objects.requireNonNull(name).trim();
        this.password = "password";
        this.firstLogin = true;
    }

    /** returns the raw id as entered (for display). */
    public String getId() {
        return id;
    }

    /** returns the canonical form (lowercase + trimmed) for comparisons. */
    public static String canonical(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name).trim();
    }

    /** returns true if supplied password matches the current password. */
    public boolean verifyPassword(String pwd) {
        return Objects.equals(this.password, pwd);
    }

    /** change password only if old password matches. */
    public boolean changePassword(String oldPwd, String newPwd) {
        if (!verifyPassword(Objects.requireNonNull(oldPwd))) return false;
        this.password = Objects.requireNonNull(newPwd);
        return true;
    }

    public boolean forceFirstTimePasswordChange(String newPwd) {
        // only allowed if this is really the first login
        if (!firstLogin) return false;

        if (newPwd == null || newPwd.isBlank()) return false;

        // don't allow reusing the default password
        if ("password".equals(this.password) && "password".equals(newPwd)) {
            return false;
        }

        this.password = newPwd;
        this.firstLogin = false; 
        return true;
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    /** define equality by canonical id so duplicates across user types are prevented. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return canonical(this.id).equals(canonical(other.id));
    }

    @Override
    public int hashCode() {
        return canonical(this.id).hashCode();
    }
}