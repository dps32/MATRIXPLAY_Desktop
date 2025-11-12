package com.client;

import java.io.*;
import java.nio.file.*;
import org.json.JSONObject;

import com.shared.ClientData;

public class Config {
    // Ruta específica que solicitas
    private static final String CONFIG_PATH = "/home/super/Documents/GitHub/MATRIXPLAY_Desktop/MatrixPlayDesktop/data/clientConfig.json";
    private static ClientData clientData;

    public static void saveConfig(ClientData data) throws IOException {
        String jsonString = data.toJSON().toString(2); 
        
        // Asegurarse de que el directorio existe
        Path path = Paths.get(CONFIG_PATH);
        Files.createDirectories(path.getParent());
        
        Files.write(path, jsonString.getBytes());
        System.out.println("Configuración guardada en: " + CONFIG_PATH);
    }
    
    public static ClientData loadConfig() throws IOException {
        Path configPath = Paths.get(CONFIG_PATH);
        
        if (!Files.exists(configPath)) {
            System.out.println("Archivo de configuración no encontrado en: " + CONFIG_PATH);
            return null;
        }
        
        String content = new String(Files.readAllBytes(configPath));
        JSONObject json = new JSONObject(content);
        System.out.println("Configuración cargada desde: " + CONFIG_PATH);
        return ClientData.fromJSON(json);
    }
    
    // getter
    public static String getConfigPath() {
        return CONFIG_PATH;
    }
}