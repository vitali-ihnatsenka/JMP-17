package by.epam.jmp.domain;

import java.util.List;

/**
 * Created by Vitali on 02.11.2016.
 */
public class Friend {
    private int id;
    private String name;
    private List<Message> messages;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
