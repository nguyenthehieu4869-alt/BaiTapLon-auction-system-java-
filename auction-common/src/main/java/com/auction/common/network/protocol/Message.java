package com.auction.common.network.protocol;

public class Message {
    private MessageType type;
    private Object data;
    private boolean success;
    private String message;
    private String requestId;

    public Message(MessageType type, Object data, boolean success) {
        this(type, data, success, null);
    }

    public Message(MessageType type, Object data, boolean success, String message) {
        this(type, data, success, message, null);
    }

    public Message(MessageType type, Object data, boolean success, String message, String requestId) {
        this.type = type;
        this.data = data;
        this.success = success;
        this.message = message;
        this.requestId = requestId;
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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
