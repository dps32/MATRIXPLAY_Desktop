package com.shared;

import org.json.JSONObject;

public class ClientData {
    public String name;
    public String serverAddress; 
    
 
    public ClientData(String name, String serverAddress) {
        this.name = name;
        this.serverAddress = serverAddress;
    }

    @Override
    public String toString() {
        return this.toJSON().toString();
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("serverAddress", serverAddress);
        return obj;
    }

    // cliente a partir de JSON
    public static ClientData fromJSON(JSONObject obj) {
        String name = obj.optString("name", "");
        String serverAddress = obj.optString("serverAddress", "");
        
        return new ClientData(name, serverAddress);
    }
}