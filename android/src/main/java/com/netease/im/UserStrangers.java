package com.netease.im;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UserStrangers {
    public final static String TAG = "UserStranger";
    public final static String observeUserStranger = "observeUserStranger";
    private static final long debounceDelay = 2000;

    public static UserStrangers instance;
    private static Map<String, Object> listStrangers;
    private static Timer timer;

    UserStrangers() {
        listStrangers = new HashMap<>();
        timer = new Timer();
    }

    public static UserStrangers getInstance() {
        if (instance == null) {
            instance = new UserStrangers();
        }

        return instance;
    }

    public static void setStranger(String accId) {
        if (listStrangers != null && listStrangers.get(accId) != null) return;

        Map<String, Object> userWithCache = CacheUsers.getUser(accId);
        if (userWithCache != null) return;

        if (listStrangers == null) {
            listStrangers = new HashMap<>();
        }

        listStrangers.put(accId, accId);
        debounce();
    }

    private static void debounce() {
        if (timer == null) {
            timer = new Timer();
        }

        timer.cancel();
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleDebounced();
            }
        }, debounceDelay);
    }

    private static void handleDebounced() {
        Log.e(TAG, "listStrangers " + listStrangers);
        if (!listStrangers.isEmpty()) {
            List<String> accIds = new ArrayList<>(listStrangers.keySet());
            Log.e(TAG, "listStrangers accIds " + accIds);
            CacheUsers.fetchUsers(accIds, new CacheUsers.OnCompletion() {
                @Override
                public void onResult(Map<String, Object> data) {
                    if (data != null) {
                        ReactCache.emit(ReactCache.observeUserStranger, MapUtil.mapToReadableMap(data));
                    }
                }
            });
        }

        listStrangers = new HashMap<>();
    }
}
