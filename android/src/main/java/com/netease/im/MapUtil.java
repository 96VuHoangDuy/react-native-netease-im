/*
  MapUtil exposes a set of helper methods for working with
  ReadableMap (by React Native), Map<String, Object>, and JSONObject.
 */

package com.netease.im;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import org.json.JSONException;

public class MapUtil {

    public static JSONObject toJSONObject(ReadableMap readableMap) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);

            switch (type) {
                case Null:
                    jsonObject.put(key, null);
                    break;
                case Boolean:
                    jsonObject.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    jsonObject.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    jsonObject.put(key, readableMap.getString(key));
                    break;
                case Map:
                    jsonObject.put(key, MapUtil.toJSONObject(readableMap.getMap(key)));
                    break;
                case Array:
                    jsonObject.put(key, ArrayUtil.toJSONArray(readableMap.getArray(key)));
                    break;
            }
        }

        return jsonObject;
    }

    public static Map<String, Object> toMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> iterator = (Iterator<String>) jsonObject.keySet();

        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                value = MapUtil.toMap((JSONObject) value);
            }
            if (value instanceof JSONArray) {
                value = ArrayUtil.toArray((JSONArray) value);
            }

            map.put(key, value);
        }

        return map;
    }

    public static Map<String, Object> toMap(ReadableMap readableMap) {
        Map<String, Object> map = new HashMap<>();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);

            switch (type) {
                case Null:
                    map.put(key, null);
                    break;
                case Boolean:
                    map.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    map.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    map.put(key, readableMap.getString(key));
                    break;
                case Map:
                    map.put(key, MapUtil.toMap(readableMap.getMap(key)));
                    break;
                case Array:
                    map.put(key, ArrayUtil.toArray(readableMap.getArray(key)));
                    break;
            }
        }

        return map;
    }

    public static WritableMap mapToReadableMap(Map<String, Object> map) {
        WritableMap writableMap = new WritableNativeMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof String) {
                writableMap.putString(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                writableMap.putInt(entry.getKey(), (Integer) entry.getValue());
            } else if (entry.getValue() instanceof Double) {
                writableMap.putDouble(entry.getKey(), (Double) entry.getValue());
            } else if (entry.getValue() instanceof Float) {
                writableMap.putDouble(entry.getKey(), (Float) entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                writableMap.putBoolean(entry.getKey(), (Boolean) entry.getValue());
            } else if (entry.getValue() instanceof List) {
                writableMap.putArray(entry.getKey(), arrayMaptoWritableArray((List<Object>) entry.getValue()));
            } else if (entry.getValue() instanceof Map) {
                writableMap.putMap(entry.getKey(), mapToReadableMap((Map<String, Object>) entry.getValue()));
            }
            // Add more conditions for other data types as needed
        }
        return writableMap;
    }

    public static WritableArray arrayMaptoWritableArray(List<Object> list) {
        WritableArray writableArray = new WritableNativeArray();
        for (Object item : list) {
            if (item instanceof String) {
                writableArray.pushString((String) item);
            } else if (item instanceof Integer) {
                writableArray.pushInt((Integer) item);
            } else if (item instanceof Double) {
                writableArray.pushDouble((Double) item);
            } else if (item instanceof Float) {
                writableArray.pushDouble((Float) item);
            } else if (item instanceof Boolean) {
                writableArray.pushBoolean((Boolean) item);
            } else if (item instanceof List) {
                writableArray.pushArray(arrayMaptoWritableArray((List<Object>) item));
            } else if (item instanceof Map) {
                writableArray.pushMap(mapToReadableMap((Map<String, Object>) item));
            }
            // Add more conditions for other data types as needed
        }
        return writableArray;
    }

    public static WritableMap toWritableMap(JSONObject map) {
        WritableMap writableMap = Arguments.createMap();
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            Object value = pair.getValue();

            if (value == null) {
                writableMap.putNull((String) pair.getKey());
            } else if (value instanceof Boolean) {
                writableMap.putBoolean((String) pair.getKey(), (Boolean) value);
            } else if (value instanceof Double) {
                writableMap.putDouble((String) pair.getKey(), (Double) value);
            } else if (value instanceof Integer) {
                writableMap.putInt((String) pair.getKey(), (Integer) value);
            } else if (value instanceof String) {
                writableMap.putString((String) pair.getKey(), (String) value);
            } else if (value instanceof Map) {
//                Log.d("value instanceof Map", "" + value);
//                Log.d("MapUtil.toWritableMap", "" + MapUtil.toWritableMap((Map<String, Object>) value));

                writableMap.putMap((String) pair.getKey(), MapUtil.toWritableMap((JSONObject) value));
            } else if (value.getClass() != null && value.getClass().isArray()) {
                writableMap.putArray((String) pair.getKey(), ArrayUtil.toWritableArray((JSONArray) value));
            }

            iterator.remove();
        }
//        Log.d("toWritableMap", "" + writableMap.toString());
        return writableMap;
    }

    public static Map<String, Object> readableMaptoMap(ReadableMap readableMap) {
        Map<String, Object> map = new HashMap<>();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    map.put(key, null);
                    break;
                case Boolean:
                    map.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    map.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    map.put(key, readableMap.getString(key));
                    break;
                case Map:
                    map.put(key, readableMaptoMap(readableMap.getMap(key)));
                    break;
                case Array:
                    map.put(key, readableArrayToArray(readableMap.getArray(key)));
                    break;
            }
        }
        return map;
    }

    public static List<Object> readableArrayToArray(ReadableArray readableArray) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    list.add(null);
                    break;
                case Boolean:
                    list.add(readableArray.getBoolean(i));
                    break;
                case Number:
                    list.add(readableArray.getDouble(i));
                    break;
                case String:
                    list.add(readableArray.getString(i));
                    break;
                case Map:
                    list.add(readableMaptoMap(readableArray.getMap(i)));
                    break;
                case Array:
                    list.add(readableArrayToArray(readableArray.getArray(i)));
                    break;
            }
        }
        return list;
    }
}