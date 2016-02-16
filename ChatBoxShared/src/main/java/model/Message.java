package model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class Message implements Serializable {
    private User to;
    private User sender;
    private String message;
    private Action action;
    private Date date = Calendar.getInstance().getTime();

    public Message(User sender, User to, String message, Action action) {
        this.to = to;
        this.sender = sender;
        this.message = message;
        this.action = action;
    }

    public Message(User sender, String message, Action action) {
        this.sender = sender;
        this.message = message;
        this.action = action;
        this.date = Calendar.getInstance().getTime();
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User from) {
        this.sender = from;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
