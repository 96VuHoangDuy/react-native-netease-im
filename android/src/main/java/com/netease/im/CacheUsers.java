package com.netease.im;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.facebook.react.bridge.ReadableMap;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ResponseFetchUser {
    private Map<String, Object> data;

    public Map<String, Object> getData() {
        return this.data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}

public class CacheUsers {
    private final static String TAG = "CacheUsers";
    private final static String HeaderAuthKey = "X-IM-SDK-AUTH-KEY";

    private Map<String, Object> listUsers;
    private Map<String, Object> listCustomerServiceAndChatbot;
    private String apiUrl;
    private String authKey;

    public static CacheUsers cacheUsers = new CacheUsers();

    public CacheUsers() {
        listUsers = new HashMap<>();
        listCustomerServiceAndChatbot = new HashMap<>();
    }

    public static void setApiUrl(String url) {
        Log.e(TAG, "apiUrl: " + url);
        cacheUsers.apiUrl = url;
    }

    public static void setAuthKey(String authKey) {
        Log.e(TAG, "authKey: " + authKey);
        cacheUsers.authKey = authKey;
    }

    public static Map<String, Object> getUser(String accId) {
        return (Map<String, Object>) cacheUsers.listUsers.get(accId);
    }

    public static String getCustomerServiceOrChatbot(String accId) {
        return (String) cacheUsers.listCustomerServiceAndChatbot.get(accId);
    }

    public static void setListCustomerServiceAndChatbot(ReadableMap data) {
        if (cacheUsers.listCustomerServiceAndChatbot.isEmpty()) {
            cacheUsers.listCustomerServiceAndChatbot = MapUtil.readableMaptoMap(data);
        }
    }

    public static void fetchUsers(List<String> accIds, OnCompletion onCompletion) {
        try {
            StringBuilder endpoint = new StringBuilder(cacheUsers.apiUrl + "/api/v1/client/im-sdk/users");
            for(int i = 0; i < accIds.size(); i++) {
                String accId = accIds.get(i);
                if (i == 0) {
                    endpoint.append("?accId=").append(accId);
                    continue;
                }

                endpoint.append("&accId=").append(accId);
            }

            URL url = new URL(endpoint.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty(HeaderAuthKey, cacheUsers.authKey);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                onCompletion.onResult(responseCode);
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            ResponseFetchUser data = JSON.parseObject(response.toString(), ResponseFetchUser.class);

            cacheUsers.listUsers = data.getData();

            onCompletion.onResult(200);
        } catch (Exception e) {
            Log.e(TAG, "CacheUser fetchUsers error: " + e.getMessage());
            onCompletion.onResult(500);
        }
    }

    public interface OnCompletion {
        void onResult(int code);
    }
}
