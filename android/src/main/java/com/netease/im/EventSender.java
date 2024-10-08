package com.netease.im;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.os.AsyncTask;
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
        WritableArray updatedArray = Arguments.createArray();
        String newItemId = newItem.getString(idKey);
        boolean isReplaced = false;

        for (int i = 0; i < mainArray.size(); i++) {
            ReadableMap existingParam = mainArray.getMap(i);

            if (existingParam != null && existingParam.hasKey(idKey)) {
                String existingParamId = existingParam.getString(idKey);

                if (existingParamId.equals(newItemId)) {
                    WritableMap newMap = Arguments.createMap();
                    newMap.merge(newItem); // Create a clone
                    updatedArray.pushMap(newMap);
                    isReplaced = true;
                } else {
                    updatedArray.pushMap(existingParam);
                }
            } else {
                updatedArray.pushMap(existingParam);
            }
        }

        if (!isReplaced) {
            WritableMap newMap = Arguments.createMap();
            newMap.merge(newItem); // Create a clone
            updatedArray.pushMap(newMap);
        }

        return updatedArray;
    }

    public void addParam(Object param, String idKey) {
        ReadableMap paramObject = null;

        if (param instanceof ReadableMap) {
            paramObject = (ReadableMap) param;
        } else if (param instanceof ReadableArray) {
            ReadableArray paramArray = (ReadableArray) param;
            paramObject = paramArray.getMap(0);
        }

        if (this.isSending) {
            WritableMap newMap = Arguments.createMap();
            newMap.merge(paramObject); // Create a clone
            this.backupArray.pushMap(newMap);
        } else {
            this.mainArray = replaceIfSameId(this.mainArray, paramObject, idKey);
        }

        Log.d("mainArraymain", this.mainArray.toString());
    }

    public void sendEventToReactNativeWithType(String type, String eventName, int countLimit) {
        if (this.isSending || this.mainArray.size() == 0) {
            return;
        }

        new SendEventTask(type, eventName, countLimit, this).execute();
    }

    private class SendEventTask extends AsyncTask<Void, Void, WritableMap> {
        private String eventName;
        private int countLimit;
        private String type;
        private WritableArray paramsToSend;

        private EventSender eventSender;

        public SendEventTask(String type, String eventName, int countLimit, EventSender eventSender) {
            this.type = type;
            this.eventName = eventName;
            this.countLimit = countLimit;
            this.eventSender = eventSender;
        }

        @Override
        protected void onPreExecute() {
            this.eventSender.isSending = true;
        }

        @Override
        protected WritableMap doInBackground(Void... voids) {
            int countToSend = Math.min(this.countLimit, this.eventSender.mainArray.size());
            this.paramsToSend = Arguments.createArray();

            for (int i = 0; i < countToSend; i++) {
                this.paramsToSend.pushMap(this.eventSender.mainArray.getMap(i));
            }

            WritableMap param = null;
            if (this.paramsToSend.size() > 0) {
                param = Arguments.createMap();
                param.putArray("data", this.paramsToSend);
            }

            WritableArray remainingArray = Arguments.createArray();
            for (int i = countToSend; i < this.eventSender.mainArray.size(); i++) {
                remainingArray.pushMap(this.eventSender.mainArray.getMap(i));
            }
            this.eventSender.mainArray = remainingArray;

            if (this.eventSender.backupArray.size() > 0) {
                for (int i = 0; i < this.eventSender.backupArray.size(); i++) {
                    this.eventSender.mainArray.pushMap(this.eventSender.backupArray.getMap(i));
                }
                this.eventSender.backupArray = Arguments.createArray();
            }

            return param;
        }

        @Override
        protected void onPostExecute(WritableMap param) {
            if (param != null) {
                ReactCache.getReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(this.eventName, param);
            }

            this.eventSender.isSending = false;

            if (this.eventSender.mainArray.size() > 0) {
                this.eventSender.sendEventToReactNativeWithType(this.type, this.eventName, this.countLimit);
            }
        }
    }

    public void triggerSendEventAfterDelay(String type, String eventName, int countLimit) {
        this.handler.removeCallbacks(this.sendEventRunnable);
        this.sendEventRunnable = new Runnable() {
            @Override
            public void run() {
                sendEventToReactNativeWithType(type, eventName, countLimit);
            }
        };
        this.handler.postDelayed(this.sendEventRunnable, 500);
    }
}
