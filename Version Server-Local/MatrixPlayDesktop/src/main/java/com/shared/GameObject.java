package com.shared;

/* CAMBIAR JSON PARA COORDENADA

import org.json.JSONObject;

public class GameObject {
    public String id;
    public int x;
    public int y;
    public int col;
    public int row;
    public boolean estaSiendoArrastrada;

    public GameObject(String id, int x, int y, int cols, int rows) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.col = cols;
        this.row = rows;
    }

    @Override
    public String toString() {
        return this.toJSON().toString();
    }
    
    // Converteix l'objecte a JSON
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("x", x);
        obj.put("y", y);
        obj.put("cols", col);
        obj.put("rows", row);
        this.estaSiendoArrastrada = false; 
        return obj;
    }

    // Crea un GameObjects a partir de JSON
    public static GameObject fromJSON(JSONObject obj) {
        GameObject go = new GameObject(
            obj.optString("id", null),
            obj.optInt("x", 0),
            obj.optInt("y", 0),
            obj.optInt("cols", 1),
            obj.optInt("rows", 1)
        );
        return go;
    }
}

 */