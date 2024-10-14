package com.netease.im.session.extension;

import com.alibaba.fastjson.JSONObject;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.netease.im.MessageConstant;

public class CustomMessageChatBotAttachment extends CustomAttachment {
    private final String KEY_DATA = "data";
    private final String KEY_CODE = "code";

    private String customServiceType;
    private Integer code;

    public CustomMessageChatBotAttachment() {
        super(CustomAttachmentType.CUSTOM_MESSAGE_CHATBOT);
    }

    public WritableMap getWritableMap() {
        return toReactNative();
    }

    public void setCustomServiceType(String type) {
        customServiceType = type;
    }

    @Override
    public String toJson(boolean send) {
        org.json.JSONObject object = new org.json.JSONObject();
        try {
            if (customServiceType != null) {
                object.put(KEY_DATA, customServiceType);
                object.put(KEY_CODE, 18939912);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    @Override
    protected void parseData(JSONObject data) {
        customServiceType = data.getString(KEY_DATA);
        code = data.getInteger(KEY_CODE);
    }

    @Override
    protected JSONObject packData(){
        JSONObject object = new JSONObject();
        object.put(KEY_DATA, customServiceType);
        object.put(KEY_CODE, code);

        return object;
    }

    @Override
    protected WritableMap toReactNative() {
        WritableMap map = Arguments.createMap();
        map.putString(MessageConstant.CustomMessageChatBot.CUSTOM_SERVICE_TYPE, customServiceType);
        return map;
    }
}
