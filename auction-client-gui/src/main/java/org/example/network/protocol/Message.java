package org.example.network.protocol;

public class Message {
    private MessageType type;
    private Object data;
    private boolean success;
    private String message;

    public Message(MessageType type, Object data, boolean success) {
        this(type, data, success, null);
    }

    public Message(MessageType type, Object data, boolean success, String message) {
        this.type = type;
        this.data = data;
        this.success = success;
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}