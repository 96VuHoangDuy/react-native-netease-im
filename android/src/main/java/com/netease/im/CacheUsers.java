package com.netease.im;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.facebook.react.bridge.ReadableMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheUsers {
    private final static String TAG = "CacheUsers";
    private final static String HeaderAuthKey = "X-IM-SDK-AUTH-KEY";

    private static Map<String, Object> listUsers = new HashMap<>();
    private static Map<String, Object> listCustomerServiceAndChatbot = new HashMap<>();
    private static String apiUrl;
    private static String authKey;

    public static void setApiUrl(String url) {
        Log.e(TAG, "apiUrl: " + url);
        apiUrl = url;
    }

    public static void setAuthKey(String key) {
        Log.e(TAG, "authKey: " + key);
        authKey = key;
    }

    public static Map<String, Object> getUser(String accId) {
        return (Map<String, Object>) listUsers.get(accId);
    }

    public static String getCustomerServiceOrChatbot(String accId) {
        return (String) listCustomerServiceAndChatbot.get(accId);
    }

    public static void setListCustomerServiceAndChatbot(ReadableMap data) {
        if (listCustomerServiceAndChatbot == null || listCustomerServiceAndChatbot.isEmpty()) {
            listCustomerServiceAndChatbot = MapUtil.readableMaptoMap(data);
        }
    }

    public static void fetchUsers(List<String> accIds, OnCompletion onCompletion) {
        try {
            StringBuilder endpoint = new StringBuilder(apiUrl + "/api/v1/client/im-sdk/users");
            for(int i = 0; i < accIds.size(); i++) {
                String accId = accIds.get(i);
                if (i == 0) {
                    endpoint.append("?accIds=").append(accId);
                    continue;
                }

                endpoint.append("&accIds=").append(accId);
            }

            Log.e(TAG, "fetchUsers " + endpoint);

            URL url = new URL(endpoint.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty(HeaderAuthKey, authKey);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                onCompletion.onResult(null);
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.e(TAG, "response data string" + response.toString());

            Map<String, Object> data = JSON.parseObject(response.toString(), Map.class);

            Log.e(TAG, "response data " + data);

            if (data == null) {
                onCompletion.onResult(null);
                return;
            }

            for(Map.Entry<String, Object> entry : data.entrySet()) {
                if (listUsers != null && listUsers.containsKey(entry.getKey())) continue;

                listUsers.put(entry.getKey(), entry.getValue());
            }

            onCompletion.onResult(data);
        } catch (Exception e) {
            Log.e(TAG, "CacheUser fetchUsers error: " + e.getMessage());
            onCompletion.onResult(null);
        }
    }

    public interface OnCompletion {
        void onResult(Map<String, Object> data);
    }
}
