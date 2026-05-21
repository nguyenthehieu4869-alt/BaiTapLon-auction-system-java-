package org.example.network.protocol;
import org.example.network.protocol.*;
import java.util.HashMap;
import java.util.Map;
public class testMessage
{
    public static void main(String[] args) {
        // tạo data
        Map<String, String> data = new HashMap<>();
        data.put("username", "kien");
        data.put("password", "123");
        // tạo message
        Message msg = new Message(MessageType.LOGIN, data, true);
        // encode
        String json = Protocol.encode(msg);
        System.out.println("JSON:");
        System.out.println(json);
        // decode
        Message decoded = Protocol.decode(json);
        Map map = (Map) decoded.getData();
        System.out.println("\nUsername: " + map.get("username"));
        System.out.println("Password: " + map.get("password"));
    }
}