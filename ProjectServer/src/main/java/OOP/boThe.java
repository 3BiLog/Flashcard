package OOP;

import java.io.Serializable;

public class boThe implements Serializable {
    private static final long serialVersionUID = 1L;

    private String command;
    private int deckId;
    private int userId;
    private String name;
    private int count_card;


    public boThe(String command, int deckId, int userId, String name, int count_card) {
        this.command = command;
        this.deckId = deckId;
        this.userId = userId;
        this.name = name;
        this.count_card = count_card;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getDeckId() {
        return deckId;
    }

    public void setDeckId(int deckId) {
        this.deckId = deckId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount_card() {
        return count_card;
    }

    public void setCount_card(int count_card) {
        this.count_card = count_card;
    }
}
