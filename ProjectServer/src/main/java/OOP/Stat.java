package OOP;

import java.io.Serializable;
import java.time.LocalDate;

public class Stat implements Serializable {
    private static final long serialVersionUID = 1L;
    private String command;
    private LocalDate date;
    private int count;
    private int userId;

    public Stat(String command, LocalDate date, int count, int userId) {
        this.command = command;
        this.date = date;
        this.count = count;
        this.userId = userId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}