package com.netease.im;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactContext;

public class EventSender {

    private WritableArray mainArray;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable sendEventRunnable;
    private boolean isLooping = false;
    private final int intervalMs = 200;

    public EventSender() {
        this.mainArray = Arguments.createArray();
    }

    private WritableArray replaceIfSameId(WritableArray array, ReadableMap newItem, String idKey) {
        WritableArray updatedArray = Arguments.createArray();
        String newItemId = newItem.getString(idKey);
        boolean replaced = false;

        for (int i = 0; i < array.size(); i++) {
            ReadableMap existing = array.getMap(i);
            if (existing != null && existing.hasKey(idKey)) {
                String existingId = existing.getString(idKey);
                if (existingId.equals(newItemId)) {
                    WritableMap copy = Arguments.createMap();
                    copy.merge(newItem);
                    updatedArray.pushMap(copy);
                    replaced = true;
                } else {
                    updatedArray.pushMap(existing);
                }
            }
        }

        if (!replaced) {
            WritableMap copy = Arguments.createMap();
            copy.merge(newItem);
            updatedArray.pushMap(copy);
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

        if (paramObject == null || !paramObject.hasKey(idKey)) return;

        this.mainArray = replaceIfSameId(this.mainArray, paramObject, idKey);
    }

    public void triggerLoopSendEvent(String eventName, int countLimit) {
        if (isLooping) return;
        isLooping = true;

        sendEventRunnable = new Runnable() {
            @Override
            public void run() {
                if (mainArray.size() == 0) {
                    isLooping = false;
                    return;
                }

                sendEvent(eventName, countLimit);
                handler.postDelayed(this, intervalMs);
            }
        };

        handler.post(sendEventRunnable);
    }

    private void sendEvent(String eventName, int countLimit) {
        if (mainArray.size() == 0) return;

        int countToSend = Math.min(countLimit, mainArray.size());
        WritableArray batch = Arguments.createArray();

        for (int i = 0; i < countToSend; i++) {
            batch.pushMap(mainArray.getMap(i));
        }

        // Cắt phần đã gửi khỏi mảng chính
        WritableArray remaining = Arguments.createArray();
        for (int i = countToSend; i < mainArray.size(); i++) {
            remaining.pushMap(mainArray.getMap(i));
        }
        mainArray = remaining;

        WritableMap wrapper = Arguments.createMap();
        wrapper.putArray("data", batch);

        ReactContext context = ReactCache.getReactContext();
        if (context != null && context.hasActiveCatalystInstance()) {
            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, wrapper);
            Log.d("EventSender", "✅ emit " + eventName + " | batch size = " + batch.size());
        } else {
            Log.w("EventSender", "⚠️ ReactContext not ready for " + eventName);
        }
    }

    // ✅ Hàm gọi tiện lợi: add + send
    public void addAndSend(ReadableMap param, String idKey, String eventName, int countLimit) {
        addParam(param, idKey);
        triggerLoopSendEvent(eventName, countLimit);
    }

    public int getMainSize() {
        return mainArray.size();
    }

    public void clearAll() {
        mainArray = Arguments.createArray();
        isLooping = false;
        handler.removeCallbacksAndMessages(null);
    }
}
