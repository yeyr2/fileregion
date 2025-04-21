package org.Component.tool;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.GsonBuilder;

public class Json {
    static GsonBuilder gb = new GsonBuilder();
    static {
        gb.disableHtmlEscaping();
    }

    public static String pojoToJson(Object obj){
        return gb.create().toJson(obj);
    }

    public static <T> T jsonToPojo(String json,Class<T>tClass){
        T t = JSONObject.parseObject(json,tClass);
        return t;
    }
}
