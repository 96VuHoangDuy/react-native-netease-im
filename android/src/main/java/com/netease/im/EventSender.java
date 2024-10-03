package com.netease.im;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class EventSender {

    WritableArray mainArray;
    private WritableArray backupArray;
    private boolean isSending;
    private Handler handler;
    private Runnable sendEventRunnable;

    public EventSender() {
        this.mainArray = Arguments.createArray();
        this.backupArray = Arguments.createArray();
        this.isSending = false;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public static WritableArray replaceIfSameId(WritableArray mainArray, ReadableMap newItem, String idKey) {
        WritableArray updatedArray = Arguments.createArray(); // New array for holding updated elements
        String newItemId = newItem.getString(idKey);
        boolean isReplaced = false;

        // Iterate over the existing array to find the matching item by idKey
        for (int i = 0; i < mainArray.size(); i++) {
            ReadableMap existingParam = mainArray.getMap(i);

            if (existingParam != null && existingParam.hasKey(idKey)) {
                String existingParamId = existingParam.getString(idKey);

                if (existingParamId.equals(newItemId)) {
                    // Replace the existing param with the new one
                    updatedArray.pushMap(newItem);
                    isReplaced = true;
                } else {
                    // Retain the original param if it's not the one being replaced
                    updatedArray.pushMap(existingParam);
                }
            } else {
                // If the param doesn't have idKey, retain it
                updatedArray.pushMap(existingParam);
            }
        }

        // If no replacement occurred, append the new item to the array
        if (!isReplaced) {
            updatedArray.pushMap(newItem);
        }

        return updatedArray; // Return the updated array with replaced or newly added item
    }

    public void addParam(Object param, String idKey) {
        ReadableMap paramObject = null;

        if (param instanceof  ReadableMap) {
            paramObject = (ReadableMap) param;
        } else if (param instanceof  ReadableArray) {
            ReadableArray paramArray = (ReadableArray) param;
            paramObject = paramArray.getMap(0);
        }

        // Get paramId based on idKey
        String paramId = paramObject.getString(idKey);

//        boolean paramExists = false;

        // Add to backup or main array depending on isSending state

        if (isSending) {
            backupArray.pushMap(paramObject);
        } else {
            mainArray = replaceIfSameId(mainArray, paramObject, idKey);
        }

        Log.d("mainArraymain", mainArray.toString());
    }


    public void sendEventToReactNativeWithType(String type, String eventName, int countLimit) {
        if (isSending || mainArray.size() == 0) {
            return;
        }

        isSending = true;
        int countToSend = Math.min(countLimit, mainArray.size());
        WritableArray paramsToSend = Arguments.createArray();

        for (int i = 0; i < countToSend; i++) {
            paramsToSend.pushMap(mainArray.getMap(i));
        }

        if (paramsToSend.size() > 0) {
            WritableMap param = Arguments.createMap();
            param.putArray("data", paramsToSend);
//            im.getBridge().sendAppEvent(eventName, param);
            ReactCache.getReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, param);
        }

        WritableArray remainingArray = Arguments.createArray();
        for (int i = countToSend; i < mainArray.size(); i++) {
            remainingArray.pushMap(mainArray.getMap(i));
        }
        mainArray = remainingArray;

        if (backupArray.size() > 0) {
            for (int i = 0; i < backupArray.size(); i++) {
                mainArray.pushMap(backupArray.getMap(i));
            }
            backupArray = Arguments.createArray();
        }

        isSending = false;

        if (mainArray.size() > 0) {
            sendEventToReactNativeWithType(type, eventName, countLimit);
        }
    }

    public void triggerSendEventAfterDelay(String type, String eventName, int countLimit) {
        handler.removeCallbacks(sendEventRunnable);
        sendEventRunnable = new Runnable() {
            @Override
            public void run() {
                sendEventToReactNativeWithType(type, eventName, countLimit);
            }
        };
        handler.postDelayed(sendEventRunnable, 500);
    }
}
