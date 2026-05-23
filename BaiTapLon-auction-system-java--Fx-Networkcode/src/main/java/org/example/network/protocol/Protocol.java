package org.example.network.protocol;

import com.google.gson.Gson;

public class Protocol {
    private static final Gson gson = new Gson();

    public static String encode(Message msg) {
        return gson.toJson(msg);
    }

    public static Message decode(String json) {
        return gson.fromJson(json, Message.class);
    }

    public static Gson gson() {
        return gson;
    }
}


