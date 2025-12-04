package com.netease.im;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.netease.im.common.push.Extras;
import com.netease.im.session.SessionUtil;
import com.netease.im.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;

/**
 * <h1>接收到推送消息通知启动</h1>
 * <br/>type 启动类型：1. 会话聊天(session)
 * <br/>sessionType 聊天类型，单聊或群组
 * <br/>sessionId 聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID
 * <br/>Created by dowin on 2017/5/2.
 */

public class ReceiverMsgParser {

    public static boolean checkOpen(Intent intent) {
        if (intent != null) {
            // Check for standard keys
            if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT) || intent.hasExtra(Extras.EXTRA_JUMP_P2P) || intent.hasExtra("sessionBody")) {
                return true;
            }
            
            // Check for Honor/Huawei Push specific keys that might contain data
            // Honor Push might send data in different keys
            if (intent.hasExtra("sessionID") || intent.hasExtra("sessionId") || 
                intent.hasExtra("sessionType") || intent.hasExtra("payload") ||
                intent.hasExtra("data") || intent.hasExtra("customData")) {
                return true;
            }
        }
        return false;
    }

    private static Intent result = new Intent();

    public static void setIntent(Intent intent) {
        result = intent;
    }

    public static Intent getIntent() {
        return result;
    }

    public static Bundle openIntent(Intent intent) {
        Bundle result = new Bundle();
        if (intent != null) {
            if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
                ArrayList<IMMessage> messages = (ArrayList<IMMessage>) intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
                if (messages == null || messages.size() > 1) {
                    result.putString("type", "sessionList");
                } else {
                    IMMessage message = messages.get(0);
                    result.putString("type", "session");
                    result.putString("sessionType", Integer.toString(message.getSessionType().getValue()));
                    result.putString("sessionId", message.getSessionId());
                    result.putString("sessionName", message.getSessionId());
                }
            } else if (intent.hasExtra(Extras.EXTRA_JUMP_P2P)) {
                Intent data = intent.getParcelableExtra(Extras.EXTRA_DATA);
                String account = data.getStringExtra(Extras.EXTRA_ACCOUNT);
                if (!TextUtils.isEmpty(account)) {
                    result.putString("type", "session");
                    result.putString("sessionType", Integer.toString(SessionTypeEnum.P2P.getValue()));
                    result.putString("sessionId", account);
                    result.putString("sessionName", account);
                }
            } else if (intent.hasExtra("sessionBody")) {
                String sessionBody = intent.getStringExtra("sessionBody");
                if (!TextUtils.isEmpty(sessionBody)) {
                    try {
                        JSONObject json = JSON.parseObject(sessionBody);
                        result.putString("type", "session");
                        result.putString("sessionType", json.getString("sessionType"));
                        result.putString("sessionId", json.getString("sessionId"));
                        result.putString("sessionName", json.getString("sessionId"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        return result;
    }

    public static WritableMap getWritableMap(Intent intent) {
        WritableMap rr = Arguments.createMap();
        if (intent != null && canAutoLogin()) {

            if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
                ArrayList<IMMessage> messages = (ArrayList<IMMessage>) intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
                if (messages == null || messages.isEmpty()) {
                    rr.putString("type", "sessionList");
                } else {
                    WritableMap r = Arguments.createMap();
                    IMMessage message = messages.get(0);
                    rr.putString("type", "session");
                    r.putString("sessionType", Integer.toString(message.getSessionType().getValue()));
                    r.putString("sessionId", message.getSessionId());
                    r.putString("sessionName", SessionUtil.getSessionName(message.getSessionId(), message.getSessionType(), false));
                    rr.putMap("sessionBody", r);
                }
            } else if (intent.hasExtra(Extras.EXTRA_JUMP_P2P)) {
                Intent data = intent.getParcelableExtra(Extras.EXTRA_DATA);
                String account = data.getStringExtra(Extras.EXTRA_ACCOUNT);
                if (!TextUtils.isEmpty(account)) {
                    WritableMap r = Arguments.createMap();
                    rr.putString("type", "session");
                    r.putString("sessionType", Integer.toString(SessionTypeEnum.P2P.getValue()));
                    r.putString("sessionId", account);
                    r.putString("sessionName", SessionUtil.getSessionName(account, SessionTypeEnum.P2P, false));
                    rr.putMap("sessionBody", r);
                }

            } else if (intent.hasExtra("sessionBody")) {
                String sessionBody = intent.getStringExtra("sessionBody");
                if (!TextUtils.isEmpty(sessionBody)) {
                    try {
                        JSONObject json = JSON.parseObject(sessionBody);
                        WritableMap r = Arguments.createMap();
                        rr.putString("type", "session");
                        String sessionType = json.getString("sessionType");
                        String sessionId = json.getString("sessionId");
                        r.putString("sessionType", sessionType);
                        r.putString("sessionId", sessionId);
                        SessionTypeEnum typeEnum = SessionUtil.getSessionType(sessionType);
                        r.putString("sessionName", SessionUtil.getSessionName(sessionId, typeEnum, false));
                        rr.putMap("sessionBody", r);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // Try to parse Honor/Huawei Push data from alternative keys
                // Honor Push might send data in "data" or "payload" key as JSON string
                String dataStr = null;
                if (intent.hasExtra("data")) {
                    dataStr = intent.getStringExtra("data");
                } else if (intent.hasExtra("payload")) {
                    dataStr = intent.getStringExtra("payload");
                } else if (intent.hasExtra("customData")) {
                    dataStr = intent.getStringExtra("customData");
                }
                
                if (!TextUtils.isEmpty(dataStr)) {
                    try {
                        JSONObject dataJson = JSON.parseObject(dataStr);
                        // Check if it contains session info
                        String sessionId = dataJson.getString("sessionID");
                        if (TextUtils.isEmpty(sessionId)) {
                            sessionId = dataJson.getString("sessionId");
                        }
                        String sessionType = dataJson.getString("sessionType");
                        
                        if (!TextUtils.isEmpty(sessionId) && !TextUtils.isEmpty(sessionType)) {
                            WritableMap r = Arguments.createMap();
                            rr.putString("type", "session");
                            r.putString("sessionType", sessionType);
                            r.putString("sessionId", sessionId);
                            SessionTypeEnum typeEnum = SessionUtil.getSessionType(sessionType);
                            r.putString("sessionName", SessionUtil.getSessionName(sessionId, typeEnum, false));
                            rr.putMap("sessionBody", r);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                // Also try direct keys (sessionID, sessionId, sessionType)
                String sessionId = null;
                String sessionType = null;
                if (intent.hasExtra("sessionID")) {
                    sessionId = intent.getStringExtra("sessionID");
                } else if (intent.hasExtra("sessionId")) {
                    sessionId = intent.getStringExtra("sessionId");
                }
                if (intent.hasExtra("sessionType")) {
                    sessionType = intent.getStringExtra("sessionType");
                }
                
                if (!TextUtils.isEmpty(sessionId) && !TextUtils.isEmpty(sessionType)) {
                    WritableMap r = Arguments.createMap();
                    rr.putString("type", "session");
                    r.putString("sessionType", sessionType);
                    r.putString("sessionId", sessionId);
                    SessionTypeEnum typeEnum = SessionUtil.getSessionType(sessionType);
                    r.putString("sessionName", SessionUtil.getSessionName(sessionId, typeEnum, false));
                    rr.putMap("sessionBody", r);
                }
            }
        }

        return rr;
    }


    /**
     * 已经登陆过，自动登陆
     */
    private static boolean canAutoLogin() {
        return !NIMClient.getStatus().wontAutoLogin();
//        return true;//!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token);
    }
}
