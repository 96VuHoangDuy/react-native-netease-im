package com.netease.im.session;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.Tag;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.netease.im.IMApplication;
import com.netease.im.ReactCache;
import com.netease.im.login.LoginService;
import com.netease.im.session.extension.RedPacketOpenAttachement;
import com.netease.im.uikit.cache.NimUserInfoCache;
import com.netease.im.uikit.cache.TeamDataCache;
import com.netease.im.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.nostra13.universalimageloader.utils.L;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import androidx.core.app.NotificationCompat;

/**
 * Created by dowin on 2017/5/2.
 */

public class SessionUtil {

    public final static String CUSTOM_Notification = "1";
    public final static String CUSTOM_Notification_redpacket_open = "2";

    public static SessionTypeEnum getSessionType(String sessionType) {
        SessionTypeEnum sessionTypeE = SessionTypeEnum.None;
        try {
            sessionTypeE = SessionTypeEnum.typeOfValue(Integer.parseInt(sessionType));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return sessionTypeE;
    }

    private static String getTeamNameDefault(String sessionId)  {
        List<TeamMember> members = TeamDataCache.getInstance().getTeamMemberList(sessionId);
        if (members == null || members.isEmpty()) return null;
        Boolean isFirstMember = true;
        StringBuilder result = null;
        String nameCreator = "";

        for(TeamMember member: members) {
            String name = NimUserInfoCache.getInstance().getUserDisplayName(member.getAccount());

            if (member.getTeamNick() == null || name == null) {
                continue;
            }

            String memberName = member.getTeamNick();
            if (memberName.isEmpty()) {
                memberName = name;
            }

            if (member.getType() == TeamMemberType.Owner) {
                nameCreator = memberName;
                continue;
            }

            if (isFirstMember) {
                result = new StringBuilder(memberName);
                isFirstMember = false;
                continue;
            }

            result.append(", ").append(memberName);
        }

        if (result == null) return null;

        return nameCreator + ", " + result.toString();
    }

    public static String getSessionName(String sessionId, SessionTypeEnum sessionTypeEnum, boolean selfName) {
        String name = sessionId;
        if (sessionTypeEnum == SessionTypeEnum.P2P) {
            NimUserInfoCache nimUserInfoCache = NimUserInfoCache.getInstance();
            String pId = selfName ? LoginService.getInstance().getAccount() : sessionId;
            name = nimUserInfoCache.getUserName(pId);
        } else if (sessionTypeEnum == SessionTypeEnum.Team) {
            name = TeamDataCache.getInstance().getTeamName(sessionId);
            if (name.equals("TEAM_NAME_DEFAULT")) {
                String teamName = getTeamNameDefault(sessionId);
                if (teamName == null) {
                    return "群聊";
                }

                return teamName;
            }
        }
        return name;
    }

    private static void appendPushConfig(IMMessage message) {
//        CustomPushContentProvider customConfig = NimUIKit.getCustomPushContentProvider();
//        if (customConfig != null) {
//            String content = customConfig.getPushContent(message);
//            Map<String, Object> payload = customConfig.getPushPayload(message);
//            message.setPushContent(content);
//            message.setPushPayload(payload);
//        }
    }

    /**
     * 设置最近联系人的消息为已读
     *
     * @param enable
     */
    private void enableMsgNotification(boolean enable) {
        if (enable) {
            /**
             * 设置最近联系人的消息为已读
             *
             * @param account,    聊天对象帐号，或者以下两个值：
             *                    {@link #MSG_CHATTING_ACCOUNT_ALL} 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
             *                    {@link #MSG_CHATTING_ACCOUNT_NONE} 目前没有与任何人对话，需要状态栏消息通知
             */
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
        }
    }

    public static void sendMessage(IMMessage message) {

        appendPushConfig(message);
        NIMClient.getService(MsgService.class).sendMessage(message, false);
    }


    /**
     * 添加好友通知
     *
     * @param account
     * @param content
     */
    public static void sendAddFriendNotification(String account, String content) {
        sendCustomNotification(account, SessionTypeEnum.P2P, CUSTOM_Notification, content);
    }

    public static void receiver(NotificationManager manager, CustomNotification customNotification) {
        LogUtil.w("SessionUtil", customNotification.getContent());
        Map<String, Object> map = customNotification.getPushPayload();
        if (map != null && map.containsKey("type")) {
            String type = (String) map.get("type");
            if (SessionUtil.CUSTOM_Notification.equals(type)) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(IMApplication.getContext());
                builder.setContentTitle("请求加为好友");
                builder.setContentText(customNotification.getApnsText());
                builder.setAutoCancel(true);
                PendingIntent contentIntent = PendingIntent.getActivity(
                        IMApplication.getContext(), 0, new Intent(IMApplication.getContext(), IMApplication.getMainActivityClass()), 0);
                builder.setContentIntent(contentIntent);
                builder.setSmallIcon(IMApplication.getNotify_msg_drawable_id());
                manager.notify((int) System.currentTimeMillis(), builder.build());
            }
        } else {
            String content = customNotification.getContent();
            if (!TextUtils.isEmpty(content)) {
//                WritableMap notification = Arguments.createMap();
                JSONObject object = JSON.parseObject(content);
                JSONObject data = object.getJSONObject("data");

//                JSONObject dict = data.getJSONObject("dict");
//                String sendId = dict.getString("sendId");
//                String openId = dict.getString("openId");
//                String hasRedPacket = dict.getString("hasRedPacket");
//                String serialNo = dict.getString("serialNo");
//
////                String timestamp = data.getString("timestamp");
//                long t = customNotification.getTime() / 1000;
////                try {
////                    t = Long.parseLong(timestamp);
////                } catch (NumberFormatException e) {
////                    t = System.currentTimeMillis() / 1000;
////                    e.printStackTrace();
////                }
////                LogUtil.w("timestamp","timestamp:"+timestamp);
////                LogUtil.w("timestamp","t:"+t);
////                LogUtil.w("timestamp",""+data);
//                String sessionId = data.getString("sessionId");
//                String sessionType = data.getString("sessionType");
//                final String id = sessionId;//getSessionType(sessionType) == SessionTypeEnum.P2P ? openId :
//                sendRedPacketOpenLocal(id, getSessionType(sessionType), sendId, openId, hasRedPacket, serialNo, t);

                Integer customNotificationType = data.getInteger("type");

                switch (customNotificationType){
                    case 1:
                        String messageId = data.getString("messageId");
                        SessionService.getInstance().queryMessage(messageId, new SessionService.OnMessageQueryListener() {
                            @Override
                            public int onResult(int code, IMMessage message) {

                                if (message != null) {
                                    SessionService.getInstance().deleteItem(message, true);
                                }
                                return 0;
                            }
                        });
                        break;
                    case 2:
                        break;
                }
                ReactCache.emit(ReactCache.observeCustomNotification, ReactCache.createCustomSystemMsg(customNotification));
            }
        }

    }

    /**
     * @param account
     * @param sessionType
     * @param type
     * @param content
     */
    public static void sendCustomNotification(String account, SessionTypeEnum sessionType, String type, String content) {
        CustomNotification notification = new CustomNotification();
        notification.setSessionId(account);
        notification.setSessionType(sessionType);

        notification.setContent(content);
        notification.setSendToOnlineUserOnly(false);
        notification.setApnsText(content);

        Map<String, Object> pushPayload = new HashMap<>();
        pushPayload.put("type", type);
        pushPayload.put("content", content);
        notification.setPushPayload(pushPayload);

        NIMClient.getService(MsgService.class).sendCustomNotification(notification);
    }

    public static void sendRedPacketOpenLocal(String sessionId, SessionTypeEnum sessionType,
                                              String sendId, String openId, String hasRedPacket, String serialNo, long timestamp) {

        CustomMessageConfig config = new CustomMessageConfig();
        config.enableUnreadCount = false;
        config.enablePush = false;
        RedPacketOpenAttachement attachment = new RedPacketOpenAttachement();
        attachment.setParams(sendId, openId, hasRedPacket, serialNo);
        IMMessage message = MessageBuilder.createCustomMessage(sessionId, sessionType, attachment.getTipMsg(true), attachment, config);
        message.setStatus(MsgStatusEnum.success);

        message.setConfig(config);
        NIMClient.getService(MsgService.class).saveMessageToLocalEx(message, true, timestamp * 1000);
    }

    public static void sendRedPacketOpenNotification(String sessionId, SessionTypeEnum sessionType,
                                                     String sendId, String openId, String hasRedPacket, String serialNo, long timestamp) {

        if (TextUtils.equals(sendId, openId)) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        Map<String, String> dict = new HashMap<>();
        dict.put("sendId", sendId);
        dict.put("openId", openId);
        dict.put("hasRedPacket", hasRedPacket);
        dict.put("serialNo", serialNo);

        data.put("dict", dict);
        data.put("timestamp", Long.toString(timestamp));
        data.put("sessionId", sessionId);
        data.put("sessionType", Integer.toString(sessionType.getValue()));

        CustomNotification notification = new CustomNotification();
        notification.setSessionId(sendId);
        notification.setSessionType(SessionTypeEnum.P2P);
        CustomNotificationConfig config = new CustomNotificationConfig();
        config.enablePush = false;
        config.enableUnreadCount = false;
        notification.setConfig(config);

        notification.setSendToOnlineUserOnly(false);

        Map<String, Object> pushPayload = new HashMap<>();
        pushPayload.put("type", CUSTOM_Notification_redpacket_open);
        pushPayload.put("data", data);
        notification.setPushPayload(pushPayload);

        NIMClient.getService(MsgService.class).sendCustomNotification(notification);
    }
}
