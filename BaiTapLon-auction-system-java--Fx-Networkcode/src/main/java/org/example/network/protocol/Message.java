package org.example.network.protocol;

public class Message {
    private MessageType type;
    private Object data;
    private boolean success;

    public Message(MessageType type, Object data, boolean success) {
        this.type = type;
        this.data = data;
        this.success = success;
    }

    public MessageType getType() { return type; }
    public Object getData() { return data; }
    public boolean isSuccess() { return success; }
}
