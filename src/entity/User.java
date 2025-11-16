package entity;

import java.io.Serializable;
import java.util.Objects;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private String password;
    private boolean firstLogin;

    protected User(String id, String name) {
        this.id = Objects.requireNonNull(id).trim();
        this.name = Objects.requireNonNull(name).trim();
        this.password = "password";
        this.firstLogin = true;
    }

    /** 
     * @return String
     */
    public String getId() {
        return id;
    }

    /** 
     * @param raw
     * @return String
     */
    public static String canonical(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }

    /** 
     * @return String
     */
    public String getName() {
        return name;
    }

    /** 
     * @param name
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(name).trim();
    }

    /** 
     * @param pwd
     * @return boolean
     */
    public boolean verifyPassword(String pwd) {
        return Objects.equals(this.password, pwd);
    }

    /** 
     * @param oldPwd
     * @param newPwd
     * @return boolean
     */
    public boolean changePassword(String oldPwd, String newPwd) {
        if (!verifyPassword(Objects.requireNonNull(oldPwd))) return false;
        this.password = Objects.requireNonNull(newPwd);
        return true;
    }

    /** 
     * @param newPwd
     * @return boolean
     */
    public boolean forceFirstTimePasswordChange(String newPwd) {
        if (!firstLogin) return false;

        if (newPwd == null || newPwd.isBlank()) return false;

        if ("password".equals(this.password) && "password".equals(newPwd)) {
            return false;
        }

        this.password = newPwd;
        this.firstLogin = false; 
        return true;
    }

    /** 
     * @return boolean
     */
    public boolean isFirstLogin() {
        return firstLogin;
    }

    /** 
     * @param firstLogin
     */
    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    /** 
     * @param o
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return canonical(this.id).equals(canonical(other.id));
    }

    /** 
     * @return int
     */
    @Override
    public int hashCode() {
        return canonical(this.id).hashCode();
    }
}