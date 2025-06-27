package OOP;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String command;
    private int id;
    private String email;
    private String username;
    private String password;

    public User(String command,String username, String email, String password) {
        this.email = email;
        this.command = command;
        this.username = username;
        this.password = password;
    }

    public User(String command, int id, String username, String email) {
        this.command  = command;
        this.id       = id;
        this.username = username;
        this.email    = email;
        this.password = null;
    }

    public User(String command, int id, String username, String email, String password) {
        this.command = command;
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}