package by.epam.jmp.domain;

import java.time.LocalDateTime;

/**
 * Created by Vitali on 02.11.2016.
 */
public class Message {
    private LocalDateTime date;
    private String text;
    private Type type;


    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static enum Type {
        INCOMING, OUTGOING
    }
}
