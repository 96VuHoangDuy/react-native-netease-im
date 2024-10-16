package com.netease.im.session.extension;

import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.netease.im.MessageConstant;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;

import org.json.JSONObject;

public class CustomMessageChatBot implements MsgAttachment {
    private String KEY_DATA = "data";
    private String KEY_CODE = "code";

    private String customerServiceType;

    public void setCustomerServiceType(String type) {
        customerServiceType = type;
    }

    public CustomMessageChatBot() {

    }

    @Override
    public String toJson(boolean send) {
        JSONObject object = new JSONObject();
        try {
            if (customerServiceType != null) {
                object.put(KEY_DATA, customerServiceType);
                object.put(KEY_CODE, 18939912);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return object.toString();
    }


    public WritableMap getWritableMap() {
        return toReactNative();
    }

    protected void parseData(com.alibaba.fastjson.JSONObject data) {
        customerServiceType = data.getString(KEY_DATA);
    }

    protected com.alibaba.fastjson.JSONObject packData(){
        com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
        object.put(KEY_DATA, customerServiceType);
        object.put(KEY_CODE, 18939912);

        return object;
    }

    protected WritableMap toReactNative() {
        WritableMap map = Arguments.createMap();
        map.putString(MessageConstant.CustomMessageChatBot.CUSTOM_SERVICE_TYPE, customerServiceType);
        return map;
    }
}
