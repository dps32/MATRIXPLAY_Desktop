package com.client;

import java.io.*;
import java.nio.file.*;
import org.json.JSONObject;

import com.shared.ClientData;

public class Config {
    private static final String CONFIG_PATH = System.getProperty("user.dir") + "/data/clientConfig.json";
    private static ClientData clientData; // ????????????

    public static void saveConfig(ClientData data) throws IOException {
        String jsonString = data.toJSON().toString(2); 
        Files.write(Paths.get(CONFIG_PATH), jsonString.getBytes());
    }
    
    public static ClientData loadConfig() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(CONFIG_PATH)));
        JSONObject json = new JSONObject(content);
        return ClientData.fromJSON(json);
    }
}