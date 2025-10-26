package entity;

import java.io.Serializable;
import java.util.Objects;

/** Base class for all users. */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private String name;
    private String password;


    /** Everyone starts with default password, which is password */
    protected User(String id, String name) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.password = "password";
    }

    public String getId() { 
        return id; 
    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = Objects.requireNonNull(name); 
    }

    /** Returns true if supplied password matches the current password. */
    public boolean verifyPassword(String pwd) { 
        return Objects.equals(this.password, pwd); 
    }

    /** Change password iff oldPwd is correct. */
    public boolean changePassword(String oldPwd, String newPwd) {
        if (!verifyPassword(Objects.requireNonNull(oldPwd))) return false;
        this.password = Objects.requireNonNull(newPwd);
        return true;
    }
}
