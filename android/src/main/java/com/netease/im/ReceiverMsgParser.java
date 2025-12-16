package com.netease.im;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

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
            // 1. Check for standard Netease IM keys
            if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT) || 
                intent.hasExtra(Extras.EXTRA_JUMP_P2P) || 
                intent.hasExtra("sessionBody")) {
                Log.d("ReceiverMsgParser", "checkOpen: Found standard IM keys → TRUE");
                return true;
            }
            
            // 2. Check for notification from ConfirmActivity (parsed by parseUniversalPushData)
            // Handles: ALL vendors - Xiaomi, Oppo, Vivo, Honor, Huawei, FCM, etc.
            if (intent.hasExtra("from_notification") && intent.hasExtra("notification_data")) {
                Log.d("ReceiverMsgParser", "checkOpen: Found from_notification → TRUE");
                return true;
            }
            
            // 3. Check for Xiaomi/Oppo/Vivo Push raw keys (if not processed by ConfirmActivity)
            if (intent.hasExtra("key_message") || intent.hasExtra("mipush_notified")) {
                Log.d("ReceiverMsgParser", "checkOpen: Found Xiaomi keys → TRUE");
                return true;
            }
            
            // 4. Check for Honor/Huawei Push specific keys
            // When user clicks notification in killed state, system launches MainActivity with raw vendor keys
            if (intent.hasExtra("sessionID") || intent.hasExtra("sessionId") || 
                intent.hasExtra("sessionType") ||
                intent.hasExtra("_push_msgid") || intent.hasExtra("_hw_from") || 
                intent.hasExtra("_push_notifyid") || intent.hasExtra("_push_cmd_type")) {
                Log.d("ReceiverMsgParser", "checkOpen: Found Honor/Huawei vendor keys → TRUE");
                return true;
            }
            
            // 5. Check for generic push payload keys
            if (intent.hasExtra("payload") || intent.hasExtra("data") || 
                intent.hasExtra("customData") || intent.hasExtra("pushData")) {
                Log.d("ReceiverMsgParser", "checkOpen: Found generic payload keys → TRUE");
                return true;
            }
            
            // 6. Check for Oppo/Vivo specific keys
            if (intent.hasExtra("messageId") || intent.hasExtra("pushId") ||
                intent.hasExtra("notificationId")) {
                Log.d("ReceiverMsgParser", "checkOpen: Found Oppo/Vivo keys → TRUE");
                return true;
            }
            
            Log.d("ReceiverMsgParser", "checkOpen: No notification keys found → FALSE");
        } else {
            Log.d("ReceiverMsgParser", "checkOpen: Intent is NULL → FALSE");
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
        
        // Return null if no intent - JS will handle appropriately
        if (intent == null) {
            Log.d("ReceiverMsgParser", "getWritableMap: Intent is null → returning null");
            return null;
        }
        
        if (!canAutoLogin()) {
            Log.d("ReceiverMsgParser", "getWritableMap: Cannot auto login → returning null");
            return null;
        }
        
        Log.d("ReceiverMsgParser", "getWritableMap: Parsing intent...");
        
        if (true) { // Always try to parse

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
            } else if (intent.hasExtra("from_notification") && intent.hasExtra("notification_all_data")) {
                // ============================================================
                // NEW FORMAT: Handle notification from ConfirmActivity (Universal Push Parser)
                // ConfirmActivity sends ALL extras as JSON for JS to parse
                // ============================================================
                String allData = intent.getStringExtra("notification_all_data");
                String vendor = intent.getStringExtra("notification_vendor");
                
                if (!TextUtils.isEmpty(allData)) {
                    try {
                        Log.d("ReceiverMsgParser", "📱 Parsing notification from [" + vendor + "]");
                        Log.d("ReceiverMsgParser", "   All data: " + allData);
                        
                        JSONObject json = JSON.parseObject(allData);
                        
                        // Check if this has path (non-IM) or sessionId (IM)
                        String path = json.containsKey("path") ? json.getString("path") : null;
                        String sessionId = json.containsKey("sessionId") ? json.getString("sessionId") : null;
                        String sessionType = json.containsKey("sessionType") ? json.getString("sessionType") : null;
                        
                        // FCM case: Check if payload contains sessionBody (nested JSON string)
                        if (TextUtils.isEmpty(sessionId) && json.containsKey("payload")) {
                            try {
                                String payloadStr = json.getString("payload");
                                if (!TextUtils.isEmpty(payloadStr)) {
                                    // payload might be a JSON string, try to parse it
                                    JSONObject payloadJson = JSON.parseObject(payloadStr);
                                    
                                    // Check if payload has sessionBody (which is a JSON string)
                                    if (payloadJson.containsKey("sessionBody")) {
                                        String sessionBodyStr = payloadJson.getString("sessionBody");
                                        if (!TextUtils.isEmpty(sessionBodyStr)) {
                                            // sessionBody is a JSON string, parse it
                                            JSONObject sessionBodyJson = JSON.parseObject(sessionBodyStr);
                                            sessionId = sessionBodyJson.containsKey("sessionId") ? sessionBodyJson.getString("sessionId") : null;
                                            sessionType = sessionBodyJson.containsKey("sessionType") ? sessionBodyJson.getString("sessionType") : null;
                                            Log.d("ReceiverMsgParser", "   Extracted from payload.sessionBody: sessionId=" + sessionId + ", sessionType=" + sessionType);
                                        }
                                    }
                                    // Also check if payload has sessionId/sessionType directly
                                    if (TextUtils.isEmpty(sessionId) && payloadJson.containsKey("sessionId")) {
                                        sessionId = payloadJson.getString("sessionId");
                                    }
                                    if (TextUtils.isEmpty(sessionType) && payloadJson.containsKey("sessionType")) {
                                        sessionType = payloadJson.getString("sessionType");
                                    }
                                }
                            } catch (Exception e) {
                                Log.d("ReceiverMsgParser", "   Failed to parse payload, trying as direct key: " + e.getMessage());
                            }
                        }
                        
                        if (!TextUtils.isEmpty(path)) {
                            // Non-IM notification (goods_order, visa_order, etc.)
                            Log.d("ReceiverMsgParser", "🔔 Non-IM notification with path: " + path);
                            rr.putString("type", "notification");
                            rr.putString("path", path);
                            
                            // Include all other fields from JSON
                            if (json.containsKey("statusOrder")) {
                                rr.putString("statusOrder", json.getString("statusOrder"));
                            }
                            if (json.containsKey("notificationId")) {
                                rr.putString("notificationId", json.getString("notificationId"));
                            }
                            
                            Log.d("ReceiverMsgParser", "✅ Non-IM notification parsed successfully");
                        } else if (!TextUtils.isEmpty(sessionId) && !TextUtils.isEmpty(sessionType)) {
                            // IM notification
                            Log.d("ReceiverMsgParser", "💬 IM notification");
                            WritableMap r = Arguments.createMap();
                            rr.putString("type", "session");
                            r.putString("sessionType", sessionType);
                            r.putString("sessionId", sessionId);
                            SessionTypeEnum typeEnum = SessionUtil.getSessionType(sessionType);
                            r.putString("sessionName", SessionUtil.getSessionName(sessionId, typeEnum, false));
                            rr.putMap("sessionBody", r);
                            
                            Log.d("ReceiverMsgParser", "✅ IM notification parsed successfully");
                        } else {
                            Log.w("ReceiverMsgParser", "⚠️ Notification has neither path nor sessionId");
                        }
                    } catch (Exception e) {
                        Log.e("ReceiverMsgParser", "❌ Error parsing notification_all_data", e);
                        e.printStackTrace();
                    }
                }
            } else if (intent.hasExtra("from_notification") && intent.hasExtra("notification_data")) {
                // ============================================================
                // OLD FORMAT: Backward compatibility for old format
                // ============================================================
                String notificationData = intent.getStringExtra("notification_data");
                if (!TextUtils.isEmpty(notificationData)) {
                    try {
                        Log.d("ReceiverMsgParser", "📱 Parsing old format notification_data");
                        JSONObject json = JSON.parseObject(notificationData);
                        
                        String path = json.getString("path");
                        
                        if (!TextUtils.isEmpty(path)) {
                            rr.putString("type", "notification");
                            rr.putString("path", path);
                            if (json.containsKey("statusOrder")) rr.putString("statusOrder", json.getString("statusOrder"));
                            if (json.containsKey("notificationId")) rr.putString("notificationId", json.getString("notificationId"));
                        } else if (json.containsKey("sessionId") && json.containsKey("sessionType")) {
                            WritableMap r = Arguments.createMap();
                            rr.putString("type", "session");
                            String sessionType = json.getString("sessionType");
                            String sessionId = json.getString("sessionId");
                            r.putString("sessionType", sessionType);
                            r.putString("sessionId", sessionId);
                            SessionTypeEnum typeEnum = SessionUtil.getSessionType(sessionType);
                            r.putString("sessionName", SessionUtil.getSessionName(sessionId, typeEnum, false));
                            rr.putMap("sessionBody", r);
                        }
                    } catch (Exception e) {
                        Log.e("ReceiverMsgParser", "Error parsing old format notification_data", e);
                        e.printStackTrace();
                    }
                }
            } else if (intent.hasExtra("from_notification") && !intent.hasExtra("notification_all_data") && !intent.hasExtra("notification_data")) {
                // ============================================================
                // NotificationClickActivity case: from_notification=true but no notification_all_data
                // This happens when NotificationClickActivity forwards Intent with direct keys
                // (sessionId, sessionType, path) from custom click action
                // ============================================================
                Log.d("ReceiverMsgParser", "📱 Parsing notification from NotificationClickActivity (direct keys)");
                
                // Check for path (non-IM)
                String path = intent.getStringExtra("path");
                if (!TextUtils.isEmpty(path)) {
                    Log.d("ReceiverMsgParser", "🔔 Non-IM notification from NotificationClickActivity");
                    rr.putString("type", "notification");
                    rr.putString("path", path);
                    if (intent.hasExtra("statusOrder")) {
                        rr.putString("statusOrder", intent.getStringExtra("statusOrder"));
                    }
                    if (intent.hasExtra("notificationId")) {
                        rr.putString("notificationId", intent.getStringExtra("notificationId"));
                    }
                } else {
                    // Check for sessionId (IM)
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
                        Log.d("ReceiverMsgParser", "💬 IM notification from NotificationClickActivity");
                        WritableMap r = Arguments.createMap();
                        rr.putString("type", "session");
                        r.putString("sessionType", sessionType);
                        r.putString("sessionId", sessionId);
                        SessionTypeEnum typeEnum = SessionUtil.getSessionType(sessionType);
                        r.putString("sessionName", SessionUtil.getSessionName(sessionId, typeEnum, false));
                        rr.putMap("sessionBody", r);
                        
                        Log.d("ReceiverMsgParser", "✅ IM notification from NotificationClickActivity parsed successfully");
                    } else {
                        Log.w("ReceiverMsgParser", "⚠️ NotificationClickActivity: No path, sessionId, or sessionType found");
                    }
                }
            } else {
                // ============================================================
                // FALLBACK: Try parsing from various vendor-specific keys
                // Handles: Honor, Huawei, Oppo, Vivo (if not processed by ConfirmActivity), FCM
                // ============================================================
                Log.d("ReceiverMsgParser", "🔍 FALLBACK: Trying alternative vendor keys...");
                
                String dataStr = null;
                String vendor = "Unknown";
                
                // Try common keys where vendors put JSON data
                if (intent.hasExtra("payload")) {
                    dataStr = intent.getStringExtra("payload");
                    vendor = "Honor/Huawei (payload)";
                } else if (intent.hasExtra("data")) {
                    dataStr = intent.getStringExtra("data");
                    vendor = "Generic (data)";
                } else if (intent.hasExtra("customData")) {
                    dataStr = intent.getStringExtra("customData");
                    vendor = "Custom (customData)";
                } else if (intent.hasExtra("key_message")) {
                    // Raw Xiaomi/Oppo/Vivo that wasn't processed by ConfirmActivity
                    Object keyMsgObj = intent.getExtras().get("key_message");
                    if (keyMsgObj != null) {
                        String keyMsg = keyMsgObj.toString();
                        vendor = "Xiaomi/Oppo/Vivo (raw)";
                        // Try to extract content from key_message
                        int contentStart = keyMsg.indexOf("content={");
                        if (contentStart != -1) {
                            contentStart += "content={".length();
                            int contentEnd = keyMsg.indexOf("},", contentStart);
                            if (contentEnd == -1) contentEnd = keyMsg.indexOf("}", contentStart);
                            if (contentEnd != -1) {
                                try {
                                    String encoded = keyMsg.substring(contentStart, contentEnd);
                                    dataStr = java.net.URLDecoder.decode(encoded, "UTF-8");
                                } catch (Exception e) {
                                    Log.e("ReceiverMsgParser", "Failed to decode key_message", e);
                                }
                            }
                        }
                    }
                }
                
                if (!TextUtils.isEmpty(dataStr)) {
                    Log.d("ReceiverMsgParser", "📱 Found data in [" + vendor + "]: " + dataStr);
                    try {
                        JSONObject dataJson = JSON.parseObject(dataStr);
                        
                        // Check if it's a non-IM notification (has path)
                        String path = dataJson.getString("path");
                        if (!TextUtils.isEmpty(path)) {
                            Log.d("ReceiverMsgParser", "🔔 Non-IM notification from [" + vendor + "] with path: " + path);
                            rr.putString("type", "notification");
                            rr.putString("path", path);
                            String statusOrder = dataJson.containsKey("statusOrder") ? dataJson.getString("statusOrder") : "";
                            String notificationId = dataJson.containsKey("notificationId") ? dataJson.getString("notificationId") : "";
                            if (!TextUtils.isEmpty(statusOrder)) rr.putString("statusOrder", statusOrder);
                            if (!TextUtils.isEmpty(notificationId)) rr.putString("notificationId", notificationId);
                        } else {
                            // Check if it's an IM notification (has sessionId)
                            String sessionId = dataJson.getString("sessionID");
                            if (TextUtils.isEmpty(sessionId)) {
                                sessionId = dataJson.getString("sessionId");
                            }
                            String sessionType = dataJson.getString("sessionType");
                            
                            if (!TextUtils.isEmpty(sessionId) && !TextUtils.isEmpty(sessionType)) {
                                Log.d("ReceiverMsgParser", "💬 IM notification from [" + vendor + "]");
                                WritableMap r = Arguments.createMap();
                                rr.putString("type", "session");
                                r.putString("sessionType", sessionType);
                                r.putString("sessionId", sessionId);
                                SessionTypeEnum typeEnum = SessionUtil.getSessionType(sessionType);
                                r.putString("sessionName", SessionUtil.getSessionName(sessionId, typeEnum, false));
                                rr.putMap("sessionBody", r);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ReceiverMsgParser", "Failed to parse JSON from [" + vendor + "]", e);
                        e.printStackTrace();
                    }
                }
                
                // ============================================================
                // FALLBACK 2: Try direct keys (for simple key-value format)
                // ============================================================
                if (!rr.hasKey("type")) {
                    Log.d("ReceiverMsgParser", "🔍 FALLBACK 2: Trying direct keys...");
                    
                    // Check for path (non-IM)
                    String path = intent.getStringExtra("path");
                    if (!TextUtils.isEmpty(path)) {
                        Log.d("ReceiverMsgParser", "🔔 Non-IM notification from direct keys");
                        rr.putString("type", "notification");
                        rr.putString("path", path);
                        if (intent.hasExtra("statusOrder")) {
                            rr.putString("statusOrder", intent.getStringExtra("statusOrder"));
                        }
                    } else {
                        // Check for sessionId (IM)
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
                            Log.d("ReceiverMsgParser", "💬 IM notification from direct keys");
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
            }
        }
        
        // If no notification data was found, return null instead of empty map
        // This prevents JS from thinking there's a notification when there isn't
        if (!rr.hasKey("type")) {
            Log.d("ReceiverMsgParser", "ℹ️ No notification data found → returning null (not empty map)");
            return null;
        }
        
        Log.d("ReceiverMsgParser", "✅ Successfully parsed notification, type: " + rr.getString("type"));
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
