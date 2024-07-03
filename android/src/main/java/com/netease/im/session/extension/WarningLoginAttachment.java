package com.netease.im.session.extension;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.netease.im.MessageConstant;
import com.alibaba.fastjson.JSONObject;

public class WarningLoginAttachment extends CustomAttachment {
    private String deviceId;
    private String deviceName;
    private String warningType;
    private String timeLogin;


    public WarningLoginAttachment() {
        super(CustomAttachmentType.WARNING_LOGIN);
    }

    public WritableMap getWritableMap() {
        return toReactNative();
    }

    @Override
    protected void parseData(JSONObject data)  {
        deviceId = data.getString(MessageConstant.WarningLogin.DEVICE_ID);
        deviceName = data.getString(MessageConstant.WarningLogin.DEVICE_NAME);
        warningType = data.getString(MessageConstant.WarningLogin.WARINING_TYPE);
        timeLogin = data.getString(MessageConstant.WarningLogin.TIME_LOGIN);
    }

    @Override
    protected JSONObject packData() {
        JSONObject object = new JSONObject();
        object.put(MessageConstant.WarningLogin.TIME_LOGIN, timeLogin);
        object.put(MessageConstant.WarningLogin.DEVICE_ID, deviceId);
        object.put(MessageConstant.WarningLogin.DEVICE_NAME, deviceName);
        object.put(MessageConstant.WarningLogin.WARINING_TYPE, warningType);
        return object;
    }

    @Override
    protected WritableMap toReactNative() {
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString(MessageConstant.WarningLogin.TIME_LOGIN, timeLogin);
        writableMap.putString(MessageConstant.WarningLogin.DEVICE_ID, deviceId);
        writableMap.putString(MessageConstant.WarningLogin.DEVICE_NAME, deviceName);
        writableMap.putString(MessageConstant.WarningLogin.WARINING_TYPE, warningType);
        return writableMap;
    }
}
