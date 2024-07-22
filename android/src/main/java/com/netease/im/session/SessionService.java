package com.netease.im.session;

import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.netease.im.IMApplication;
import com.netease.im.MapUtil;
import com.netease.im.MessageConstant;
import com.netease.im.MessageUtil;
import com.netease.im.ReactCache;
import com.netease.im.login.LoginService;
import com.netease.im.session.extension.BankTransferAttachment;
import com.netease.im.session.extension.CardAttachment;
import com.netease.im.session.extension.CustomAttachment;
import com.netease.im.session.extension.CustomAttachmentType;
import com.netease.im.session.extension.DefaultCustomAttachment;
import com.netease.im.session.extension.ForwardMultipleTextAttachment;
import com.netease.im.session.extension.RedPacketAttachement;
import com.netease.im.session.extension.RedPacketOpenAttachement;
import com.netease.im.uikit.cache.NimUserInfoCache;
import com.netease.im.uikit.cache.TeamDataCache;
import com.netease.im.uikit.common.util.file.FileUtil;
import com.netease.im.uikit.common.util.log.LogUtil;
import com.netease.im.uikit.common.util.media.ImageUtil;
import com.netease.im.uikit.common.util.string.MD5;
import com.netease.im.uikit.session.helper.MessageHelper;
import com.netease.im.uikit.session.helper.MessageListPanelHelper;
import com.netease.im.uikit.session.helper.TeamNotificationHelper;
import com.netease.im.uikit.uinfo.UserInfoHelper;
import com.netease.im.uikit.uinfo.UserInfoObservable;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.LocationAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.msg.model.MessageReceipt;
import com.netease.nimlib.sdk.msg.model.MsgSearchOption;
import com.netease.nimlib.sdk.msg.model.NIMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.MemberChangeAttachment;
import com.netease.nimlib.sdk.team.model.MuteMemberAttachment;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;

import java.io.File;
import java.lang.annotation.Target;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;

import static com.netease.im.ReactCache.setLocalExtension;
import static com.netease.nimlib.sdk.NIMClient.getService;
import static com.netease.nimlib.sdk.NIMSDK.getMsgService;

/**
 * Created by dowin on 2017/5/10.
 */

public class SessionService {

    final static String TAG = "SessionService";

    private static final int LOAD_MESSAGE_COUNT = 20;


    private SessionTypeEnum sessionTypeEnum = SessionTypeEnum.None;
    private String sessionId;

    private IMMessage fistMessage;
    private IMMessage lastMessage;
    /************************* 时间显示处理 ************************/

    private Set<String> timedItems = new HashSet<>(); // 需要显示消息时间的消息ID
    private IMMessage lastShowTimeItem; // 用于消息时间显示,判断和上条消息间的时间间隔

    private Handler handler;
    private boolean mute = false;

    private String sessionName = "";
    private boolean isFriend = true;

    private boolean isSeenMessage = true;

    private SessionService() {
    }


    static class InstanceHolder {
        final static SessionService instance = new SessionService();
    }

    public static SessionService getInstance() {
        return InstanceHolder.instance;
    }


    public String getSessionId() {
        return sessionId;
    }

    public void updateIsSeenMessage(Boolean isSeen) {
        isSeenMessage = isSeen;
    }

    public SessionTypeEnum getSessionTypeEnum() {
        return sessionTypeEnum;
    }


    private IMMessage anchorMessage(QueryDirectionEnum direction) {

        IMMessage message = direction == QueryDirectionEnum.QUERY_NEW ? lastMessage : fistMessage;
        if (message == null) {
            message = MessageBuilder.createEmptyMessage(sessionId, sessionTypeEnum, 0);
        }
        return message;
    }


    /**
     * 接收消息
     *
     * @param messages
     */
    public void onIncomingMessage(@NonNull List<IMMessage> messages) {
        boolean needRefresh = false;
        List<IMMessage> addedListItems = new ArrayList<>(messages.size());
        for (IMMessage message : messages) {
            if (isMyMessage(message)) {
//                handleInComeMultiMediaMessage(message, "");

                addedListItems.add(message);
                needRefresh = true;
            }
        }
        if (needRefresh) {
            sortMessages(addedListItems);
        }
        if (addedListItems.size() > 0) {
            updateShowTimeItem(addedListItems, false);
        }
        List<IMMessage> r = onQuery(addedListItems);
        if (r.size() > 0) {
            IMMessage m = messages.get(0);
            if (!this.mute && m.getDirect() == MsgDirectionEnum.In) {
                if (showMsg(m)) {
                    if (m.getAttachment() != null && (m.getAttachment() instanceof RedPacketAttachement)) {
                        AudioPlayService.getInstance().playAudio(handler, ReactCache.getReactContext(), AudioManager.STREAM_RING, "raw", "rp");
                    } else {
                        AudioPlayService.getInstance().playAudio(handler, ReactCache.getReactContext(), AudioManager.STREAM_RING, "raw", "msg");
                    }
                }

            }
        }
        refreshMessageList(r);
    }

    boolean showMsg(IMMessage m) {
        return !(m.getMsgType() == MsgTypeEnum.notification || m.getMsgType() == MsgTypeEnum.tip
                || (m.getAttachment() != null && (m.getAttachment() instanceof RedPacketOpenAttachement)));
    }

    public boolean isMyMessage(IMMessage message) {
        return message.getSessionType() == sessionTypeEnum
                && message.getSessionId() != null
                && message.getSessionId().equals(sessionId);
    }

    /**
     * 列表加入新消息时，更新时间显示
     *
     * @param items
     * @param isQuery
     */
    public void updateShowTimeItem(List<IMMessage> items, boolean isQuery) {
//        IMMessage anchor = isQuery ? items.get(0) : lastMessage;
//
//        for (IMMessage message : items) {
//            if (setShowTimeFlag(message, anchor)) {
//                anchor = message;
//            }
//        }

        if (!isQuery && fistMessage != null) {
            fistMessage = items.get(0);
        }

        if (isQuery && lastMessage != null) {
            lastMessage = items.get(items.size() - 1);
        }
    }

    /**
     * 是否显示时间item
     *
     * @param message
     * @param anchor
     * @return
     */
    private boolean setShowTimeFlag(IMMessage message, IMMessage anchor) {
        boolean update = false;

        if (hideTimeAlways(message)) {
            setShowTime(message, false);
        } else {
            if (anchor == null) {
                setShowTime(message, true);
                update = true;
            } else {
                long time = anchor.getTime();
                long now = message.getTime();

                if (now - time == 0) {
                    // 消息撤回时使用
                    setShowTime(message, true);
                    lastShowTimeItem = message;
                    update = true;
                } else if (now - time < (long) (5 * 60 * 1000)) {
                    setShowTime(message, false);
                } else {
                    setShowTime(message, true);
                    update = true;
                }
            }
        }

        return update;
    }

    private void setShowTime(IMMessage message, boolean show) {
        if (show) {
            timedItems.add(message.getUuid());
        } else {
            timedItems.remove(message.getUuid());
        }
    }

    private boolean hideTimeAlways(IMMessage message) {
        switch (message.getMsgType()) {
            case notification:
                return true;
            default:
                return false;
        }
    }


    /**
     * 发送消息后，更新本地消息列表
     *
     * @param message
     */
    public void onMsgSend(IMMessage message) {
        List<IMMessage> addedListItems = new ArrayList<>(1);
        addedListItems.add(message);
        updateShowTimeItem(addedListItems, false);
    }

    /**
     * 删除消息
     *
     * @param messageItem
     * @param isRelocateTime
     */
    public void deleteItem(IMMessage messageItem, boolean isRelocateTime) {
        if (messageItem == null) {
            return;
        }
        getMsgService().deleteChattingHistory(messageItem, true);
    }

    public void deleteMessage(IMMessage message, final Promise promise) {
        if (message == null){
            promise.resolve("SUCCESS");
            return;
        }

        NIMClient.getService(MsgService.class).deleteChattingHistory(message);

        if (!message.getMsgType().equals(MsgTypeEnum.image) && !message.getMsgType().equals(MsgTypeEnum.video)) {
            promise.resolve("SUCCESS");
            return;
        }

        Map<String, Object> remoteExt = message.getRemoteExtension();
        if (remoteExt == null) {
            promise.resolve("SUCCESS");
            return;
        }

        String parentId = (String) remoteExt.get("parentId");
        if (parentId == null) {
            promise.resolve("SUCCESS");
            return;
        }

        MsgSearchOption option = new MsgSearchOption();
        option.setSearchContent(parentId);

        List<MsgTypeEnum> messageTypes = new ArrayList<>();
        messageTypes.add(MsgTypeEnum.text);
        messageTypes.add(MsgTypeEnum.image);
        messageTypes.add(MsgTypeEnum.video);

        option.setMessageTypes(messageTypes);

        NIMClient.getService(MsgService.class).searchAllMessage(option).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS) {
                    promise.reject("error: " + code, "error");
                    return;
                }

                if (result == null) {
                    promise.resolve("SUCCESS");
                    return;
                }

                if (result.size() == 1) {
                    IMMessage messageParent = result.get(0);

                    NIMClient.getService(MsgService.class).deleteChattingHistory(messageParent);

                    promise.resolve(messageParent.getUuid());
                    return;
                }

                promise.resolve("SUCCESS");
                return;
            }
        });
    }

    /**
     * @return
     */
    private IMMessage getLastReceiptMessage(List<IMMessage> messageList) {
        IMMessage lastMessage = null;
        for (int i = messageList.size() - 1; i >= 0; i--) {
            if (sendReceiptCheck(messageList.get(i))) {
                lastMessage = messageList.get(i);
                break;
            }
        }

        return lastMessage;
    }

    public void handleInComeMultiMediaMessage(IMMessage message, String callFrom) {
        Log.d("handle message", callFrom + "..." + isMyMessage(message));
        if (callFrom.equals("NIMViewController") && message.getSessionId().equals(sessionId)) return;
        Log.d("handle message 22", callFrom + "..." + isMyMessage(message));

        Map<String, Object> msgLocalExt = message.getRemoteExtension();

        if (msgLocalExt == null || (msgLocalExt != null && !msgLocalExt.containsKey("parentId"))) return;

        String parentMediaId = (String) msgLocalExt.get("parentId");
        MsgSearchOption option = new MsgSearchOption();
        option.setSearchContent(parentMediaId);

        RequestCallbackWrapper callback = new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                Boolean isParentMessageExits = false;

                for (IMMessage  messageResult: result) {
                    if (messageResult.getContent().equals(parentMediaId)) {
                        isParentMessageExits = true;
                        break;
                    }
                }
                if (isParentMessageExits) return;

                IMMessage localMessage = MessageBuilder.createTextMessage(message.getSessionId(), message.getSessionType(), parentMediaId);
                Map<String, Object> localExt = MapBuilder.newHashMap();
                localExt.put("isLocalMsg", true);
                localExt.put("parentMediaId", parentMediaId);
                localMessage.setLocalExtension(localExt);
                localMessage.setFromAccount(message.getFromAccount());
                localMessage.setDirect(message.getDirect());

                CustomMessageConfig config = new CustomMessageConfig();
                config.enablePush = false;
                config.enableUnreadCount = false;
                localMessage.setConfig(config);
                NIMSDK.getMsgService().saveMessageToLocal(localMessage, true);

//                NIMSDK.getMsgService().updateIMMessage(localMessage);
            }
        };

        NIMClient.getService(MsgService.class).searchMessage(sessionTypeEnum, message.getSessionId(), option)
                .setCallback(callback);
    }

    private boolean sendReceiptCheck(final IMMessage msg) {
        if (msg == null || msg.getDirect() != MsgDirectionEnum.In ||
                msg.getMsgType() == MsgTypeEnum.tip || msg.getMsgType() == MsgTypeEnum.notification) {
            return false; // 非收到的消息，Tip消息和通知类消息，不要发已读回执
        }

        return true;
    }

    /**
     * 发送已读回执（需要过滤）
     *
     * @param messageList
     */

    public void sendMsgReceipt(@NonNull List<IMMessage> messageList) {
        if (sessionId == null || sessionTypeEnum != SessionTypeEnum.P2P || !isSeenMessage) {
            return;
        }

        IMMessage message = getLastReceiptMessage(messageList);
        if (!sendReceiptCheck(message)) {
            return;
        }

        getMsgService().sendMessageReceipt(sessionId, message);
    }

    /**
     * 消息接收观察者
     */
    Observer<List<IMMessage>> incomingMessageObserver = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(List<IMMessage> messages) {
            if (messages == null || messages.isEmpty()) {
                return;
            }

            for (IMMessage message : messages) {
                RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(message.getSessionId(), message.getSessionType());

                if (recent != null) {
                    Map<String, Object> extension = recent.getExtension();

                    if (extension == null) {
                        extension = new HashMap<String, Object>();
                    }

                    extension.put("lastReadMessageId", message.getUuid());

                    recent.setExtension(extension);

                    NIMSDK.getMsgService().updateRecent(recent);
                }
            }

            sendMsgReceipt(messages); // 发送已读回执
            onIncomingMessage(messages);

        }
    };

    /**
     * 收到已读回执（更新VH的已读label）
     */

    private void receiveReceipt(List<MessageReceipt> messageReceipts) {//TODO
        Log.d("receiveReceipt", messageReceipts.toString());
        IMMessage   anchor = MessageBuilder.createEmptyMessage(sessionId, sessionTypeEnum, 0);

        getMsgService().queryMessageListEx(anchor, QueryDirectionEnum.QUERY_OLD, 1, true).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> messageList, Throwable throwable) {
                Log.d("queryMessageList", messageList.toString());

                refreshMessageList(messageList);
            }
        });
    }

    private void onMessageStatusChange(IMMessage message, boolean isSend) {
        Map<String, Object> localExtension = message.getLocalExtension();
        if (message.getStatus() == MsgStatusEnum.success && message.getDirect() == MsgDirectionEnum.Out) {
            List<IMMessage> list = new ArrayList<>(1);
            list.add(message);
            Object a = ReactCache.createMessageList(list);
            ReactCache.emit(ReactCache.observeMsgStatus, a);
        } else {
            if (localExtension != null && localExtension.containsKey("downloadStatus") && localExtension.get("downloadStatus").equals("downloading")) {
                return;
            }
            if (isMyMessage(message) || isSend) {
                List<IMMessage> list = new ArrayList<>(1);
                list.add(message);
                Object a = ReactCache.createMessageList(list);
                ReactCache.emit(ReactCache.observeMsgStatus, a);
            }
        }
//        Map<String, Object> localExtension = message.getLocalExtension();
//        if (localExtension != null && localExtension.containsKey("downloadStatus") && localExtension.get("downloadStatus").equals("downloading")) {
//            return;
//        }
//        if (isMyMessage(message) || isSend) {
//            List<IMMessage> list = new ArrayList<>(1);
//            list.add(message);
//            Object a = ReactCache.createMessageList(list);
//            ReactCache.emit(ReactCache.observeMsgStatus, a);
//        }
    }

    /**
     * 收到已读回执
     */
    private Observer<List<MessageReceipt>> messageReceiptObserver = new Observer<List<MessageReceipt>>() {
        @Override
        public void onEvent(List<MessageReceipt> messageReceipts) {
            receiveReceipt(messageReceipts);
        }
    };


    /**
     * 消息状态变化观察者
     */
    Observer<IMMessage> messageStatusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage message) {
            onMessageStatusChange(message, false);
        }
    };
    /**
     * 消息附件上传/下载进度观察者
     */
    Observer<AttachmentProgress> attachmentProgressObserver = new Observer<AttachmentProgress>() {
        @Override
        public void onEvent(AttachmentProgress progress) {
//            onAttachmentProgressChange(progress);
        }
    };

    /**
     * 本地消息接收观察者
     */
    MessageListPanelHelper.LocalMessageObserver incomingLocalMessageObserver = new MessageListPanelHelper.LocalMessageObserver() {
        @Override
        public void onAddMessage(IMMessage message) {
           if (message == null || !sessionId.equals(message.getSessionId())) {
               return;
           }

            onMsgSend(message);
        }

        @Override
        public void onClearMessages(String account) {
            refreshMessageList(null);
        }
    };

    /**
     * 消息撤回观察者
     */
    Observer<RevokeMsgNotification> revokeMessageObserver = new Observer<RevokeMsgNotification>() {
        @Override
        public void onEvent(RevokeMsgNotification item) {
            if (item == null) {return;}
            IMMessage message = item.getMessage();
            if (message == null || sessionId == null || !sessionId.equals(message.getSessionId())) {
                return;
            }

            deleteItem(message, false);
//            revokMessage(message);
        }
    };
    private UserInfoObservable.UserInfoObserver uinfoObserver;

    private void registerUserInfoObserver() {
        if (uinfoObserver == null) {
            uinfoObserver = new UserInfoObservable.UserInfoObserver() {
                @Override
                public void onUserInfoChanged(List<String> accounts) {
                    if (sessionTypeEnum == SessionTypeEnum.P2P) {
                        if (accounts.contains(sessionId) || accounts.contains(LoginService.getInstance().getAccount())) {
                            //TODO 刷新
                        }
                    } else { // 群的，简单的全部重刷
                        //TODO 刷新
                    }
                }
            };
        }

        UserInfoHelper.registerObserver(uinfoObserver);
    }

    private void unregisterUserInfoObserver() {
        if (uinfoObserver != null) {
            UserInfoHelper.unregisterObserver(uinfoObserver);
        }
    }

    /**
     * anchor 查询锚点
     *
     * @param anchor
     * @param limit  查询结果的条数限制
     */
    public void queryMessageListEx(IMMessage anchor, final QueryDirectionEnum direction, final int limit, final OnMessageQueryListListener onMessageQueryListListener) {

        if (anchor == null) {
            anchor = MessageBuilder.createEmptyMessage(sessionId, sessionTypeEnum, 0);
        }
        getMsgService().queryMessageListEx(anchor, direction, limit, direction == QueryDirectionEnum.QUERY_NEW ? true : false)
                .setCallback(new RequestCallbackWrapper<List<IMMessage>>() {

                    @Override
                    public void onResult(int code, List<IMMessage> result, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            if (result != null && result.size() > 0) {
                                fistMessage = result.get(0);
                                updateShowTimeItem(result, true);

                                final int size = result.size();
                                boolean isLimit = size >= limit;
                                List<IMMessage> r = onQuery(result);

                                if (r.size() == 0) {
                                    queryMessageListEx(fistMessage, direction, size - r.size(), onMessageQueryListListener);
                                } else {
                                    onMessageQueryListListener.onResult(code, r, timedItems);

                                    if (r.size() < size && isLimit) {
                                        fistMessage = result.get(0);
//                                    queryMessageListEx(fistMessage, direction, size - r.size(), onMessageQueryListListener);
                                    }
                                }

                                return;
                            }
                        }
                        onMessageQueryListListener.onResult(code, null, null);
                    }
                });
    }

    List<IMMessage> onQuery(List<IMMessage> result) {//TODO


        for (int i = result.size() - 1; i >= 0; i--) {
            IMMessage message = result.get(i);
            if (message == null) {
                result.remove(i);
            }
            MsgAttachment attachment = message.getAttachment();
            if (attachment != null) {
                if (message.getMsgType() == MsgTypeEnum.custom) {
                    CustomAttachment customAttachment = (CustomAttachment) attachment;
                    if (customAttachment.getType() == CustomAttachmentType.RedPacketOpen) {
                        RedPacketOpenAttachement rpOpen = (RedPacketOpenAttachement) attachment;
                        if (!rpOpen.isSelf()) {
                            result.remove(i);
                        }
                    }
                }
            }
        }
        return result;
    }

    boolean hasRegister;

    private void registerObservers(boolean register) {
        if (hasRegister && register) {
            return;
        }
        hasRegister = register;
        MsgServiceObserve service = getService(MsgServiceObserve.class);
        service.observeReceiveMessage(incomingMessageObserver, register);
        service.observeMessageReceipt(messageReceiptObserver, register);

        service.observeMsgStatus(messageStatusObserver, register);
        service.observeRevokeMessage(revokeMessageObserver, register);
        observerAttachProgress(register);
        if (register) {
            registerUserInfoObserver();
        } else {
            unregisterUserInfoObserver();
        }

        MessageListPanelHelper.getInstance().registerObserver(incomingLocalMessageObserver, register);
    }

    /****************************** 排序 ***********************************/
    private void sortMessages(List<IMMessage> list) {
        if (list.size() == 0) {
            return;
        }
        Collections.sort(list, comp);
    }

    private static Comparator<IMMessage> comp = new Comparator<IMMessage>() {

        @Override
        public int compare(IMMessage o1, IMMessage o2) {
            long time = o1.getTime() - o2.getTime();
            return time == 0 ? 0 : (time < 0 ? -1 : 1);
        }
    };

    /****************************** 消息处理 ***********************************/

    public void startSession(Handler handler, String sessionId, String type) {
        clear();
        this.handler = handler;
        this.sessionId = sessionId;

        if (NIMClient.getStatus().wontAutoLogin()) {
            Toast.makeText(IMApplication.getContext(), "您的帐号已在别的设备登录，请重新登陆", Toast.LENGTH_SHORT).show();
        }
        sessionTypeEnum = SessionUtil.getSessionType(type);

        if (sessionTypeEnum == SessionTypeEnum.P2P) {
            sessionName = NimUserInfoCache.getInstance().getUserName(sessionId);
            isFriend = NIMClient.getService(FriendService.class).isMyFriend(sessionId);

            this.mute = !NIMClient.getService(FriendService.class).isNeedMessageNotify(sessionId);
        } else {
            Team t = TeamDataCache.getInstance().getTeamById(sessionId);
            if (t != null) {
                this.mute = t.mute();
            } else {
            }
        }
        registerObservers(true);
        getMsgService().setChattingAccount(sessionId, sessionTypeEnum);
    }

    void clear() {
        sessionId = null;
        timedItems.clear();
        fistMessage = null;
        lastMessage = null;
        lastShowTimeItem = null;
    }

    public void stopSession() {
        clear();
        registerObservers(false);
        getMsgService().setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE,
                SessionTypeEnum.None);
    }

    private void refreshMessageList(List<IMMessage> messageList) {
        if (messageList == null || messageList.isEmpty()) {
            return;
        }
        Object a = ReactCache.createMessageList(messageList);
        ReactCache.emit(ReactCache.observeReceiveMessage, a);
    }

    /**
     * 重发消息到服务器
     *
     * @param item
     */
    public void resendMessage(IMMessage item) {
        // 重置状态为unsent
        item.setStatus(MsgStatusEnum.sending);
        deleteItem(item, true);
//                onMsgSend(item);
//                appendPushConfig(item);
//                getMsgService().sendMessage(item, true);
        sendMessageSelf(item, null, true, false);
    }

    public void updateMessageSentStickerBirthday(String sessionId, String type, String messageId, OnMessageQueryListener onMessageQueryListener) {
        List<String> listMessageId = new ArrayList<String>();
        listMessageId.add(messageId);
        NIMSDK.getMsgService().queryMessageListByUuid(listMessageId).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(List<IMMessage> result) {
                if (result.size() != 1) {
                    onMessageQueryListener.onResult(0, null);
                }

                IMMessage message = result.get(0);

                Map<String, Object> localExt = message.getLocalExtension();
                if (localExt == null) {
                    localExt = new HashMap<String, Object>();
                }

                localExt.put("isSentBirthday", true);

                message.setLocalExtension(localExt);

                NIMSDK.getMsgService().updateIMMessage(message);

                onMessageQueryListener.onResult(1, message);
            }

            @Override
            public void onFailed(int code) {
                onMessageQueryListener.onResult(0, null);
            }

            @Override
            public void onException(Throwable exception) {
                onMessageQueryListener.onResult(0, null);
            }
        });
    }

    public void createNotificationBirthday(String sessionId, String type) {
        this.createNotificationBirthday(sessionId, type, null, null);
    }
    public void createNotificationBirthday(String sessionId, String type, String memberContactId, String memberName) {
        SessionTypeEnum sessionType = SessionUtil.getSessionType(type);
        IMMessage lastMessage = NIMClient.getService(MsgService.class).queryLastMessage(sessionId, sessionType);
        RecentContact contact = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, sessionType);
        String name = memberName;
        if (memberName == null) {
            name = NimUserInfoCache.getInstance().getUserDisplayName(sessionId);
        }

        String content = "NO_TEXT";
        String msgType =  "text";
        Map<String, Object> localExt = new HashMap<String, Object>();
        Map<String, Object> msgExtend = new HashMap<String, Object>();
        Team team = null;
        if (sessionType == SessionTypeEnum.Team) {
            team = TeamDataCache.getInstance().getTeamById(sessionId);
        }

        localExt.put("notificationType", "BIRTHDAY");
        localExt.put("isSentBirthday", false);
        localExt.put("birthdayMemberName", name);

        if (memberContactId != null) {
            localExt.put("birthdayMemberContactId", memberContactId);
        }

        if (lastMessage != null) {
            MsgAttachment msgAttachment = lastMessage.getAttachment();

            switch (lastMessage.getMsgType()) {
                case text:
                {
                    if (lastMessage.getContent() != null && !lastMessage.getContent().isEmpty() && !lastMessage.getContent().equals("(null)")) {
                        content = lastMessage.getContent();
                    }

                    Map<String, Object> extend = lastMessage.getRemoteExtension();

                    if (extend != null && extend.containsKey("extendType")) {
                        String extendType = (String) extend.get("extendType");

                        if (extendType != null) {
                            if (extendType.equals("forwardMultipleText")) {
                                msgType = "forwardMultipleText";
                                content = "[聊天记录]";
                            }

                            if (extendType.equals("TEAM_NOTIFICATION_MESSAGE")) {
                                msgType = "notification";
                                msgExtend = extend;
                            }

                            if (extendType.equals("card")) {
                                msgType = "card";
                                content = "[个人名片]";
                            }

                            if (extendType.equals("gif")) {
                                msgType = "gif";
                                content = "[动图]";
                            }
                        }
                    }
                    break;
                }
                case image:
                    msgType = "image";
                    content = "[图片]";
                    break;
                case video:
                    msgType = "video";
                    content = "[视频]";
                    break;
                case audio:
                    msgType = "voice";
                    content = "[语音消息]";
                    break;
                case location:
                    msgType = "location";
                    content = "[位置]";
                    break;
                case tip:
                    List<String> uuids = new ArrayList<>();
                    uuids.add(contact.getRecentMessageId());
                    List<IMMessage> messages = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
                    if (messages != null && !messages.isEmpty()) {
                        content = messages.get(0).getContent();
                    }
                    msgType = "tip";
                    break;
                case file:
                    content = "[文件]";
                    msgType = "file";
                    break;
                case notification:
                    if (sessionType == SessionTypeEnum.Team && team != null) {
                        msgType = "notification";
                        NotificationAttachment attachment = (NotificationAttachment) contact.getAttachment();
                        NotificationType operationType = attachment.getType();
                        String sourceId = lastMessage.getFromAccount();

                        Map<String, Object> sourceIdMap = new HashMap<String, Object>();
                        sourceIdMap.put("sourceName", TeamDataCache.getInstance().getTeamMemberDisplayName(sessionId, sourceId));
                        sourceIdMap.put("sourceId", sourceId);

                        msgExtend.put("operationType", operationType.getValue());
                        msgExtend.put("sourceId", sourceIdMap);

                        switch (operationType) {
                            case InviteMember:
                            case KickMember:
                            case PassTeamApply:
                            case TransferOwner:
                            case AddTeamManager:
                            case RemoveTeamManager:
                            case AcceptInvite:
                            case MuteTeamMember:
                            {
                                MemberChangeAttachment memberAttachment = (MemberChangeAttachment) attachment;
                                ArrayList<String> targets = memberAttachment.getTargets();

                                ArrayList<Map<String, Object>> listTargets = new ArrayList<Map<String, Object>>();

                                for (String targetId : targets) {
                                    String targetName = TeamDataCache.getInstance().getTeamMemberDisplayName(sessionId, targetId);

                                    Map<String, Object> target = new HashMap<String, Object>();
                                    target.put("targetName", targetName);
                                    target.put("targetId", targetId);

                                    listTargets.add(target);
                                }

                                if (operationType == NotificationType.MuteTeamMember && msgAttachment != null) {
                                    MuteMemberAttachment muteMemberAttachment = (MuteMemberAttachment) msgAttachment;
                                    msgExtend.put("isMute", muteMemberAttachment.isMute() ? "mute" : "unmute");
                                }

                                msgExtend.put("target", listTargets);
                                break;
                            }
                        case UpdateTeam:
                            Map<TeamFieldEnum, String> mockUpKeys = new HashMap<TeamFieldEnum, String>();
                            mockUpKeys.put(TeamFieldEnum.Name, "NIMTeamUpdateTagName");
                            mockUpKeys.put(TeamFieldEnum.Introduce, "NIMTeamUpdateTagIntro");
                            mockUpKeys.put(TeamFieldEnum.Announcement, "NIMTeamUpdateTagAnouncement");
                            mockUpKeys.put(TeamFieldEnum.VerifyType, "NIMTeamUpdateTagJoinMode");
                            mockUpKeys.put(TeamFieldEnum.ICON, "NIMTeamUpdateTagAvatar");
                            mockUpKeys.put(TeamFieldEnum.InviteMode, "NIMTeamUpdateTagInviteMode");
                            mockUpKeys.put(TeamFieldEnum.BeInviteMode, "NIMTeamUpdateTagBeInviteMode");
                            mockUpKeys.put(TeamFieldEnum.TeamUpdateMode, "NIMTeamUpdateTagUpdateInfoMode");
                            mockUpKeys.put(TeamFieldEnum.AllMute, "NIMTeamUpdateTagMuteMode");

                            UpdateTeamAttachment updateTeamAttachment = (UpdateTeamAttachment) msgAttachment;
                            Set<Map.Entry<TeamFieldEnum, Object>> updateTeamAttachmentDetail = updateTeamAttachment.getUpdatedFields().entrySet();
                            HashMap<String, Object> updateDetail = new HashMap<String, Object>();

                            for (Map.Entry<TeamFieldEnum, Object> field : updateTeamAttachmentDetail) {
                                updateDetail.put("type", mockUpKeys.get(field.getKey()));
                                updateDetail.put("value", field.getValue().toString());
                            }

                            msgExtend.put("updateDetail", updateDetail);
                            break;
                        default:
                            break;
                        }
                    }
                    break;
                default:
                    msgType = "unknown";
                    break;

            }

            String fromAccount = contact.getFromAccount();
            if (fromAccount != null && !fromAccount.isEmpty() && !content.equals("NO_TEXT")) {
                if (sessionType == SessionTypeEnum.P2P && !TextUtils.equals(LoginService.getInstance().getAccount(), fromAccount)) {
                    String sessionName = NimUserInfoCache.getInstance().getUserDisplayName(sessionId);

                    if (sessionName != null && !sessionName.isEmpty()) {
                        content = sessionName + " : " + content;
                    }
                }

                if (sessionType == SessionTypeEnum.Team && !TextUtils.equals(LoginService.getInstance().getAccount(), fromAccount)) {
                    String teamMemberName = TeamDataCache.getInstance().getTeamMemberDisplayName(sessionId, fromAccount);

                    if (teamMemberName != null && !teamMemberName.isEmpty()) {
                        content = teamMemberName + " : " + content;
                    }
                }
            }
        }

        if (!msgExtend.isEmpty()) {
            localExt.put("notificationExtend", msgExtend);
        }

        String text = "NOTIFICATION_BIRTHDAY:"+msgType+":("+content+"):["+name+"]";
        IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionType, text);

        message.setContent(text);

        CustomMessageConfig config = new CustomMessageConfig();
        config.enablePush = false;
        config.enableUnreadCount = true;

        message.setConfig(config);
        message.setLocalExtension(localExt);
        message.setStatus(MsgStatusEnum.success);

        NIMSDK.getMsgService().insertLocalMessage(message, sessionId);
    }

    public void removeReactionMessage(String sessionId, String sessionType, String messageId, String accId, Boolean isSendMessage, final Promise promise) {
        List<String> messageIds = new ArrayList<String>();
        messageIds.add(messageId);
        NIMClient.getService(MsgService.class).queryMessageListByUuid(messageIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> messages, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS) {
                    promise.reject("code: "  + code, "error");
                    return;
                }

                if (messages.isEmpty()) {
                    promise.resolve("200");
                    return;
                }

                IMMessage message = messages.get(0);
                Map<String, Object> localExt = message.getLocalExtension();
                Log.e(TAG, "remove reaction message =>>>>>>>>" + localExt);
                if (localExt == null || localExt.get("reactions") == null) {
                    promise.resolve("200");
                    return;
                }

                List<Map<String, Object>> reactions = (List<Map<String, Object>>) localExt.get("reactions");
                List<Map<String, Object>> updateReactions = new ArrayList<Map<String, Object>>();
                for(Map<String, Object> reaction : reactions) {
                    String reactionAccId = (String) reaction.get("accId");
                    if (reactionAccId == null || !reactionAccId.equals(accId)) {
                        updateReactions.add(reaction);
                    }
                }

                localExt.put("reactions", updateReactions);

                message.setLocalExtension(localExt);
                NIMClient.getService(MsgService.class).updateIMMessage(message);

                if (isSendMessage) {
                    SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
                    IMMessage newMessage = MessageBuilder.createTextMessage(sessionId, sessionTypeEnum, "");
                    Map<String, Object> remoteExt = new HashMap<String, Object>();
                    Map<String, Object> dataRemoveReaction = new HashMap<String, Object>();
                    dataRemoveReaction.put("sessionId", sessionId);
                    dataRemoveReaction.put("sessionType", sessionType);
                    dataRemoveReaction.put("messageId", messageId);
                    dataRemoveReaction.put("accId", accId);
                    remoteExt.put("dataRemoveReaction", dataRemoveReaction);

                    CustomMessageConfig config = new CustomMessageConfig();
                    config.enablePush = false;
                    config.enableUnreadCount  = false;

                    newMessage.setSubtype(3);
                    newMessage.setRemoteExtension(remoteExt);
                    newMessage.setConfig(config);

                    NIMClient.getService(MsgService.class).sendMessage(newMessage, false).setCallback(new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void result, Throwable exception) {
                            if (code != ResponseCode.RES_SUCCESS) {
                                promise.reject("code: " + code, "error");
                                return;
                            }

                            promise.resolve("200");
                        }
                    });
                    return;
                }

                promise.resolve("200");
            }
        });
    }

    public void updateReactionMessage(String sessionId, String sessionType, String messageId, String messageNotifyReactionId, ReadableMap reaction, final Promise promise) {
        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        List<String> messageIds = new ArrayList<String>();
        messageIds.add(messageId);
        NIMClient.getService(MsgService.class).queryMessageListByUuid(messageIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> messages, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS) {
                    promise.reject("message not found", "err");
                    return;
                }

                if (messages.isEmpty()) {
                    promise.resolve("NO_MESSAGE_IN_LOCAL");
                    return;
                }

                IMMessage message = messages.get(0);
                Map<String, Object> localExt = message.getLocalExtension();
                if (localExt == null) {
                    localExt = new HashMap<String, Object>();
                }

                String reactionId = reaction.getString("id");

                List<Map<String, Object>> reactions = (List<Map<String, Object>>) localExt.get("reactions");
                if (reactions == null) {
                    reactions = new ArrayList<Map<String, Object>>();
                }

                Boolean isReaction = false;
                for(Map<String, Object> r : reactions) {
                    String rId = (String) r.get("id");

                    if (rId != null && rId.equals(reactionId)) {
                        isReaction = true;
                        break;
                    }
                }

                if (isReaction) {
                    promise.resolve("REACTION_READY");
                    return;
                }

                reactions.add(MapUtil.readableMaptoMap(reaction));
                localExt.put("reactions", reactions);

                message.setLocalExtension(localExt);

                NIMClient.getService(MsgService.class).updateIMMessage(message);

                List<String> messageNotifyReactionIds = new ArrayList<String>();
                messageNotifyReactionIds.add(messageNotifyReactionId);
                NIMClient.getService(MsgService.class).queryMessageListByUuid(messageNotifyReactionIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
                    @Override
                    public void onResult(int code, List<IMMessage> result, Throwable exception) {
                        if (code != ResponseCode.RES_SUCCESS || result.isEmpty()) {
                            promise.reject("code: " + code, "error");
                            return;
                        }

                        IMMessage messageNotifyReaction = result.get(0);

                        NIMClient.getService(MsgService.class).deleteChattingHistory(messageNotifyReaction);

                        promise.resolve("SUCCESS");
                    }
                });
            }
        });
    }

    public void reactionMessage(String sessionId, String sessionType, String messageId, ReadableMap reaction, final Promise promise) {
        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, sessionTypeEnum);
        if (recentContact == null) {
            promise.resolve("200");
            return;
        }

        List<String> messageIds = new ArrayList<String>();
        messageIds.add(messageId);
        NIMClient.getService(MsgService.class).queryMessageListByUuid(messageIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> messages, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS) {
                    promise.reject("code: " + code, "error");
                    return;
                }

                if (messages.isEmpty()) {
                    promise.reject("message not found", "error");
                    return;
                }

                IMMessage message = messages.get(0);
                if (message == null) {
                    promise.reject("message not found", "error");
                    return;
                }

                Map<String, Object> localExt = message.getLocalExtension();
                if (localExt == null) {
                    localExt = new HashMap<String, Object>();
                }

                List<Map<String, Object>> reactions = (List<Map<String, Object>>) localExt.get("reactions");
                if (reactions == null) {
                    reactions = new ArrayList<Map<String, Object>>();
                }

                reactions.add(MapUtil.readableMaptoMap(reaction));
                localExt.put("reactions", reactions);

                message.setLocalExtension(localExt);

                NIMClient.getService(MsgService.class).updateIMMessage(message);

                IMMessage newMessage = MessageBuilder.createTextMessage(sessionId, sessionTypeEnum, messageId);
                Map<String, Object> remoteExt = new HashMap<String, Object>();
                remoteExt.put("reaction", MapUtil.readableMaptoMap(reaction));

                newMessage.setRemoteExtension(remoteExt);
                newMessage.setSubtype(2);

                CustomMessageConfig config = new CustomMessageConfig();
                config.enablePush = false;
                config.enableUnreadCount = false;

                newMessage.setConfig(config);

                NIMClient.getService(MsgService.class).sendMessage(newMessage, false).setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void result, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            promise.resolve("200");
                            return;
                        }

                        promise.reject("error", "error");
                    }
                });
            }
        });

    }

    public void sendTextMessageWithSession(String content, String sessionId, String sessionType, String sessionName,  Integer messageSubType, OnSendMessageListener onSendMessageListener) {
        SessionTypeEnum sessionT = SessionUtil.getSessionType(sessionType);
        IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionT, content);
        if (!messageSubType.equals(0)) {
            message.setSubtype(messageSubType);
        }
        sendMessageSelf(message, onSendMessageListener, false, false);
    }

    /**
     * @param content
     */
    public void sendTextMessage(String content, List<String> selectedMembers, Boolean isCustomerService, Integer messageSubType,OnSendMessageListener onSendMessageListener) {

        IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionTypeEnum, content);
        if (!messageSubType.equals(0)) {
            message.setSubtype(messageSubType);
        } else {
            message.setSubtype(0);
        }

        if (selectedMembers != null && !selectedMembers.isEmpty()) {
            MemberPushOption option = createMemPushOption(selectedMembers, message);
//            message.setPushContent("有人@了你");
            message.setMemberPushOption(option);
        }
        sendMessageSelf(message, onSendMessageListener, false, isCustomerService);
    }

    public void sendGifMessageWithSession(String url,String aspectRatio, String sessionId, String typeStr, String sessionName, OnSendMessageListener onSendMessageListener) {
        SessionTypeEnum sessionType = SessionUtil.getSessionType(typeStr);
        IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionType, "[动图]");

        Map<String, Object> remoteExt = MapBuilder.newHashMap();
        remoteExt.put("extendType", "gif");
        remoteExt.put("path", url);
        remoteExt.put("aspectRatio", aspectRatio);

        message.setRemoteExtension(remoteExt);

        sendMessageSelf(message,onSendMessageListener, false, false);
    }

    public void sendGifMessage(String url,String aspectRatio, List<String> selectedMembers, Boolean isCustomerService,OnSendMessageListener onSendMessageListener) {

        IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionTypeEnum, "[动图]");

        Map<String, Object> remoteExt = MapBuilder.newHashMap();
        remoteExt.put("extendType", "gif");
        remoteExt.put("path", url);
        remoteExt.put("aspectRatio", aspectRatio);

        message.setRemoteExtension(remoteExt);

        if (selectedMembers != null && !selectedMembers.isEmpty()) {
            MemberPushOption option = createMemPushOption(selectedMembers, message);
//            message.setPushContent("有人@了你");
            message.setMemberPushOption(option);
        }
        sendMessageSelf(message, onSendMessageListener, false, isCustomerService);
    }


    public void sendMessageTeamNotificationRequestJoin(ReadableMap sourceId, ReadableArray targets,Integer type, OnSendMessageListener onSendMessageListener) {
        IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionTypeEnum, "TEAM_NOTIFICATION_MESSAGE");

        Map<String, Object> remoteExt = MapBuilder.newHashMap();
        remoteExt.put("extendType", "TEAM_NOTIFICATION_MESSAGE");
        remoteExt.put("operationType", type);
        remoteExt.put("sourceId", MapUtil.readableMaptoMap(sourceId));
        remoteExt.put("targets", MapUtil.readableArrayToArray(targets));

        message.setRemoteExtension(remoteExt);

        CustomMessageConfig config = new CustomMessageConfig();
        config.enablePush = false; // 不推送
        config.enableUnreadCount = false;
        message.setConfig(config);

        sendMessageSelf(message, onSendMessageListener, false, false);
    }

    /**
     * @param content
     */
    public void sendTipMessage(String content, OnSendMessageListener onSendMessageListener) {
        sendTipMessage(content, onSendMessageListener, false, true);
    }

    public void sendTipMessage(String content, OnSendMessageListener onSendMessageListener, boolean local, boolean enableUnreadCount) {
        CustomMessageConfig config = new CustomMessageConfig();
        config.enablePush = false; // 不推送
        config.enableUnreadCount = enableUnreadCount;
        IMMessage message = MessageBuilder.createTipMessage(sessionId, sessionTypeEnum);
        if (sessionTypeEnum == SessionTypeEnum.Team) {
            Map<String, Object> contentMap = new HashMap<>(1);
            contentMap.put("content", content);
            message.setRemoteExtension(contentMap);
            message.setConfig(config);
            message.setStatus(MsgStatusEnum.success);
            getMsgService().saveMessageToLocal(message, true);
        } else {

            message.setContent(content);
            message.setConfig(config);
            if (local) {
                message.setStatus(MsgStatusEnum.success);
                getMsgService().saveMessageToLocal(message, true);
            } else {
                sendMessageSelf(message, onSendMessageListener, false, false);
            }
        }
    }

    private void handleForwardMessage(IMMessage message, String sessionId, SessionTypeEnum sessionTypeEnum, String parentId, Boolean isHaveMultiMedia) {
        if (message.getMsgType() == MsgTypeEnum.location) {
            LocationAttachment locationAttachment = (LocationAttachment) message.getAttachment();
            Log.e(TAG, "location test => " + locationAttachment);
            if (locationAttachment == null) {
                return;
            }

            String title;

            try {
                Map<String, Object> titleDic = (Map<String, Object>) JSONObject.parse(locationAttachment.getAddress());
                titleDic.put("isForwardMessage", true);
                JSONObject jsonObject = new JSONObject(titleDic);
                title = jsonObject.toString();
            } catch (JSONException err) {
                Log.e(TAG, err.toString());
                return;
            }

            Log.e(TAG, "location title =>>>>> " + title);
            if (title == null || title.isEmpty()) {
                return;
            }

            IMMessage messageLocation = MessageBuilder.createLocationMessage(sessionId, sessionTypeEnum, locationAttachment.getLatitude(), locationAttachment.getLongitude(), title);

            sendMessageSelf(messageLocation, null, false, false);
            return;
        }

        Map<String, Object> remoteExt = message.getRemoteExtension();

        if ((message.getRemoteExtension() != null && message.getRemoteExtension().containsKey("parentId")) || message.getMsgType() == MsgTypeEnum.image
                || message.getMsgType() == MsgTypeEnum.video) {

            if (isHaveMultiMedia) {
                remoteExt.put("parentId", parentId);
            } else if (message.getRemoteExtension() != null) {
                remoteExt.remove(remoteExt.get("parentId"));
            }

        }
        message.setRemoteExtension(remoteExt);

        IMMessage messageForward = MessageBuilder.createForwardMessage(message, sessionId, sessionTypeEnum);

        if (messageForward == null) {
            return;
        }

        Map<String, Object> localExt = new HashMap<String, Object>();
        messageForward.setLocalExtension(localExt);

        sendMessageSelf(messageForward, null, false, false);
    }

    public void handleForwardMessageToRecipient(List<String> messageIds, String sessionId, SessionTypeEnum sessionType, String content, String parentId, Boolean isHaveMultiMedia) {
        NIMClient.getService(MsgService.class).queryMessageListByUuid(messageIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> messages, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS || messages == null || messages.isEmpty()) {
                    return;
                }

                for(IMMessage message : messages) {
                    handleForwardMessage(message, sessionId, sessionTypeEnum, parentId, isHaveMultiMedia);
                }

                if (parentId != null && !parentId.isEmpty() && isHaveMultiMedia) {
                    IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionType, parentId);
                    Map<String, Object> remoteExt = new HashMap<String, Object>();
                    remoteExt.put("parentMediaId", parentId);

                    CustomMessageConfig config = new CustomMessageConfig();
                    config.enableUnreadCount = false;
                    config.enablePush = false;

                    message.setRemoteExtension(remoteExt);
                    message.setConfig(config);

                    sendMessageSelf(message, null, false, false);
                }

                if (content != null && !content.isEmpty()) {
                    IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionTypeEnum, content);
                    sendMessageSelf(message, null, false, false);
                }
            }
        });
    }

    public void handleForwardMultiTextMessageToRecipient(String sessionId, SessionTypeEnum sessionTypeEnum, String messageText, String content) {
        IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionTypeEnum, messageText);

        Map<String, Object> remoteExt = MapBuilder.newHashMap();
        remoteExt.put("extendType", "forwardMultipleText");
        message.setRemoteExtension(remoteExt);

        sendMessageSelf(message, null, false, false);

        if (content != null && !content.isEmpty()) {
            IMMessage messageContent = MessageBuilder.createTextMessage(sessionId, sessionTypeEnum, content);

            sendMessageSelf(messageContent, null, false, false);
        }
    }

    public void sendMultiMediaMessage(ReadableArray data, Boolean isCustomerService, String parentId, final Promise promise) {
        List<Object> listMedia = MapUtil.readableArrayToArray(data);
        final int batchSize = 3;
        final int delay = 2000;
        final Handler handler = new Handler();
        final Runnable[] sendBatchRunnable = new Runnable[1];

        sendBatchRunnable[0] = new Runnable() {
            int startIndex = 0;

            @Override
            public void run() {
                int endIndex = Math.min(startIndex + batchSize, listMedia.size());
                List<Object> batch = listMedia.subList(startIndex, endIndex);

                for (Object dataMedia : batch) {
            Map<String, Object> media = (Map<String, Object>) dataMedia;

            String mediaType = (String) media.get("type");
            if (mediaType == null || (!mediaType.equals("image") && !mediaType.equals("video"))) {
                promise.reject("-1", "media type is invalid");
                return;
            }

            Map<String, Object> mediaData = (Map<String, Object>) media.get("data");
            if (mediaData == null) {
                promise.reject("-1", "media data is invalid");
                return;
            }

            if (mediaType.equals("image")) {
                String file = (String) mediaData.get("file");
                if (file == null) {
                    continue;
                }

                String displayName = (String) mediaData.get("displayName");
                if (displayName == null) {
                    displayName = "";
                }

                Boolean isHighQuality = (Boolean) mediaData.get("isHighQuality");
                if (isHighQuality == null) {
                    isHighQuality = false;
                }

                sendImageMessage(file, displayName, isCustomerService, isHighQuality, parentId, (Double) media.get("indexCount"), null);
                continue;
            }

            String file = (String) mediaData.get("file");
            if (file == null) {
                continue;
            }

            String displayName = (String) mediaData.get("displayName");
            if (displayName == null) {
                displayName = "";
            }

            Integer width = (Integer) mediaData.get("width");
            Integer height = (Integer) mediaData.get("height");
            String duration = (String) mediaData.get("duration");
            if (width == null || height == null || duration == null) {
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(file);
                    duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) + "";
                    width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    String metaRotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                    int rotation = metaRotation == null ? 0 : Integer.parseInt(metaRotation);

                    if (rotation == 90 || rotation == 270) {
                        Integer widthTemp = width;
                        width = height;
                        height = widthTemp;
                    }

                    retriever.release();
                } catch (Exception e) {
                    e.printStackTrace();
                    promise.reject("-1", e.getMessage());
                    return;
                }
            }

            sendVideoMessage(file, duration, width, height, displayName, isCustomerService, parentId, (Double) media.get("indexCount"), null);
        }

                if (parentId != null && startIndex == 0) {
                    IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionTypeEnum, parentId);
                    Map<String, Object> remoteExt = new HashMap<String, Object>();
                    remoteExt.put("parentMediaId", parentId);

                    message.setRemoteExtension(remoteExt);
                    message.setFromAccount(message.getFromAccount());
                    message.setDirect(message.getDirect());


                    NIMClient.getService(MsgService.class).sendMessage(message, false);

//                    if (isCustomerService) {
//                        List<IMMessage> list = new ArrayList<>(1);
//                        list.add(message);
//                        Object a = ReactCache.createMessageList(list);
//                        ReactCache.emit(ReactCache.observeMsgStatus, a);
//                    }
                }

                startIndex += batchSize;
                if (startIndex < listMedia.size()) {
                    handler.postDelayed(sendBatchRunnable[0], delay);
                } else {
                    promise.resolve("success");
                }
            }
        };

        // Post the first batch immediately
        handler.post(sendBatchRunnable[0]);
    }


    public void sendImageMessageWithSession(String file, String fileName, String sessionId, String sessionType, String sessionName, OnSendMessageListener onSendMessageListener) {
        file = Uri.parse(file).getPath();
        File f = new File(file);
        File temp = ImageUtil.getScaledImageFileWithMD5(f, FileUtil.getMimeType(f.getPath()), true);
        if (temp != null) {
            f = temp;
        }
        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        IMMessage message = MessageBuilder.createImageMessage(sessionId, sessionTypeEnum, f, TextUtils.isEmpty(fileName) ? f.getName() : fileName);
        Map<String, Object> remoteExt = MapBuilder.newHashMap();

        message.setRemoteExtension(remoteExt);

        sendMessageSelf(message, onSendMessageListener, false,false);
    }

    public void sendImageMessage(String file, String displayName, boolean isCustomerService,boolean isHighQuality, String parentId,Double indexCount, OnSendMessageListener onSendMessageListener) {
        file = Uri.parse(file).getPath();
        File f = new File(file);
        LogUtil.w(TAG, "path:" + f.getPath() + "-size:" + FileUtil.formatFileSize(f.length()));
        File temp = ImageUtil.getScaledImageFileWithMD5(f, FileUtil.getMimeType(f.getPath()), isHighQuality);
        if (temp != null) {
            f = temp;
        }
        LogUtil.w(TAG, "path:" + f.getPath() + "-size:" + FileUtil.formatFileSize(f.length()));
        IMMessage message = MessageBuilder.createImageMessage(sessionId, sessionTypeEnum, f, TextUtils.isEmpty(displayName) ? f.getName() : displayName);
        Map<String, Object> remoteExt = MapBuilder.newHashMap();

        if (parentId != null) {
            remoteExt.put("parentId", parentId);
            message.setContent(parentId);
        }

        if (indexCount != null) {
            remoteExt.put("indexCount", indexCount);
        }

        message.setRemoteExtension(remoteExt);


        sendMessageSelf(message, onSendMessageListener, false,isCustomerService);
    }

    public void sendFileMessageWitSession(String filePath, String fileName,String sessionId, String sessionType, String sessionName, OnSendMessageListener onSendMessageListener) {
        File file = new File(filePath);
        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        IMMessage message = MessageBuilder.createFileMessage(sessionId, sessionTypeEnum, file, fileName);
        message.setContent(fileName);

        sendMessageSelf(message, onSendMessageListener, false, false);
    }

    public void sendFileMessage(String filePath, String fileName, boolean isCustomerService, OnSendMessageListener onSendMessageListener) {
        File file = new File(filePath);
        IMMessage message = MessageBuilder.createFileMessage(sessionId, sessionTypeEnum, file, fileName);
        message.setContent(fileName);
        sendMessageSelf(message, onSendMessageListener, false,isCustomerService);
    }

    public void sendAudioMessage(String file, long duration, boolean isCustomerService,OnSendMessageListener onSendMessageListener) {
        file = Uri.parse(file).getPath();
        File f = new File(file);

        IMMessage message = MessageBuilder.createAudioMessage(sessionId, sessionTypeEnum, f, duration);
        sendMessageSelf(message, onSendMessageListener, false,isCustomerService);
    }

    //        String md5Path = StorageUtil.getWritePath(filename, StorageType.TYPE_VIDEO);
//        MediaPlayer mediaPlayer = getVideoMediaPlayer(f);
//        long duration = mediaPlayer == null ? 0 : mediaPlayer.getDuration();
//        int height = mediaPlayer == null ? 0 : mediaPlayer.getVideoHeight();
//        int width = mediaPlayer == null ? 0 : mediaPlayer.getVideoWidth();
    public void sendVideoMessage(String file, String duration, int width, int height, String displayName, boolean isCustomerService,String parentId, Double indexCount,  OnSendMessageListener onSendMessageListener) {

//        String filename = md5 + "." + FileUtil.getExtensionName(file);
        file = Uri.parse(file).getPath();
        String md5 = TextUtils.isEmpty(displayName) ? MD5.getStreamMD5(file) : displayName;
        File f = new File(file);
        long durationL = 0;
        try {
            durationL = Long.parseLong(duration);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        IMMessage message = MessageBuilder.createVideoMessage(sessionId, sessionTypeEnum, f, durationL, width, height, md5);

        Map<String, Object> remoteExt = MapBuilder.newHashMap();

        if (parentId != null) {
            remoteExt.put("parentId", parentId);
            message.setContent(parentId);
        }

        if (indexCount != null) {
            remoteExt.put("indexCount", indexCount);
        }

        message.setRemoteExtension(remoteExt);

        sendMessageSelf(message, onSendMessageListener, false, isCustomerService);
    }

    public void sendVideoMessageWithSession(String file, String sessionId, String sessionType, String sessionName, OnSendMessageListener onSendMessageListener) {
        file = Uri.parse(file).getPath();
        String md5 = MD5.getStreamMD5(file);
        File f = new File(file);
        long durationL = 0;
        int width = 0;
        int height = 0;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file);
            durationL = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            String metaRotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            int rotation = metaRotation == null ? 0 : Integer.parseInt(metaRotation);

            if (rotation == 90 || rotation == 270) {
                Integer widthTemp = width;
                width = height;
                height = widthTemp;
            }

            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        IMMessage message = MessageBuilder.createVideoMessage(sessionId, sessionTypeEnum, f, durationL, width, height, md5);

        Map<String, Object> remoteExt = MapBuilder.newHashMap();


        message.setRemoteExtension(remoteExt);

        sendMessageSelf(message, onSendMessageListener, false, false);
    }

    public void sendLocationMessage(String sessionId, String sessionType, String latitude, String longitude, String address, OnSendMessageListener onSendMessageListener) {
        double lat = 23.12504;
        try {
            lat = Double.parseDouble(latitude);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        double lon = 113.327474;
        try {
            lon = Double.parseDouble(longitude);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        SessionTypeEnum sessionTypeE = SessionUtil.getSessionType(sessionType);
        IMMessage message = MessageBuilder.createLocationMessage(sessionId, sessionTypeE, lat, lon, address);
        NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                onSendMessageListener.onResult(ResponseCode.RES_SUCCESS,message);
            }

            @Override
            public void onFailed(int code) {
                onSendMessageListener.onResult(code, message);
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    public void sendDefaultMessage(String type, String digst, String content, OnSendMessageListener onSendMessageListener) {
        CustomMessageConfig config = new CustomMessageConfig();
        DefaultCustomAttachment attachment = new DefaultCustomAttachment(type);
        attachment.setDigst(digst);
        attachment.setContent(content);
        IMMessage message = MessageBuilder.createCustomMessage(sessionId, sessionTypeEnum, digst, attachment, config);
        sendMessageSelf(message, onSendMessageListener, false, false);
    }

    public void sendRedPacketOpenMessage(String sendId, String openId, String hasRedPacket, String serialNo, OnSendMessageListener onSendMessageListener) {
//        CustomMessageConfig config = new CustomMessageConfig();
//        config.enableUnreadCount = false;
//        config.enablePush = false;
//        RedPacketOpenAttachement attachment = new RedPacketOpenAttachement();
//        attachment.setParams(sendId, openId, hasRedPacket, serialNo);
//        IMMessage message = MessageBuilder.createCustomMessage(sessionId, sessionTypeEnum, sendId + ";" + openId, attachment, config);
//
////        message.
//        sendMessageSelf(message, onSendMessageListener,false);
        long timestamp = new Date().getTime() / 1000;
        SessionUtil.sendRedPacketOpenNotification(sessionId, sessionTypeEnum, sendId, openId, hasRedPacket, serialNo, timestamp);
        SessionUtil.sendRedPacketOpenLocal(sessionId, sessionTypeEnum, sendId, openId, hasRedPacket, serialNo, timestamp);
    }

    public void sendRedPacketMessage(String type, String comments, String serialNo, OnSendMessageListener onSendMessageListener) {
        CustomMessageConfig config = new CustomMessageConfig();
        RedPacketAttachement attachment = new RedPacketAttachement();
        attachment.setParams(type, comments, serialNo);
        IMMessage message = MessageBuilder.createCustomMessage(sessionId, sessionTypeEnum, comments, attachment, config);
        sendMessageSelf(message, onSendMessageListener, false, false);
    }

    public void sendCardMessage(String toSessionType,String toSessionId, String name, String imgPath, String cardSessionId, String cardSessionType, OnSendMessageListener onSendMessageListener) {
        SessionTypeEnum sessionTypeE = SessionUtil.getSessionType(toSessionType);
        IMMessage message = MessageBuilder.createTextMessage(toSessionId, sessionTypeE, "[个人名片]");

        Map<String, Object> remoteExt = MapBuilder.newHashMap();
        remoteExt.put("extendType", "card");
        remoteExt.put("type", cardSessionType);
        remoteExt.put("name", name);
        remoteExt.put("sessionId", cardSessionId);
        remoteExt.put("imgPath", imgPath);

        message.setRemoteExtension(remoteExt);

        sendMessageSelf(message, onSendMessageListener, false, false);
    }

    public void forwardMultipleTextMessage(ReadableMap dataDict,  String sessionId,  String sessionType,  String content, OnSendMessageListener onSendMessageListener) {
        SessionTypeEnum sessionTypeE = SessionUtil.getSessionType(sessionType);
        IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionTypeE, dataDict.getString("messages"));

        Map<String, Object> remoteExt = MapBuilder.newHashMap();
        remoteExt.put("extendType", "forwardMultipleText");
        message.setRemoteExtension(remoteExt);

        sendMessageSelf(message, onSendMessageListener, false, false);

        if (content.isEmpty()) {
            return;
        }

        IMMessage messageText = MessageBuilder.createTextMessage(sessionId, sessionTypeE, content);
        sendMessageSelf(messageText, onSendMessageListener, false, false);
    }

    public void sendBankTransferMessage(String amount, String comments, String serialNo, OnSendMessageListener onSendMessageListener) {
        CustomMessageConfig config = new CustomMessageConfig();
        BankTransferAttachment attachment = new BankTransferAttachment();
        attachment.setParams(amount, comments, serialNo);
        IMMessage message = MessageBuilder.createCustomMessage(sessionId, sessionTypeEnum, comments, attachment, config);
        sendMessageSelf(message, onSendMessageListener, false, false);
    }

    public int sendForwardMessage(List<IMMessage> selectMessages, final String sessionId, final String sessionType, String content, final String parentId, final boolean isHaveMultiMedia, OnSendMessageListener onSendMessageListener) {
        if (selectMessages == null) {
            return 0;
        }
        SessionTypeEnum sessionTypeE = SessionUtil.getSessionType(sessionType);

//        if (MessageUtil.shouldIgnore(selectMessages)) {
//            return 1;
//        }

        for (IMMessage _message : selectMessages) {
            Map<String, Object> remoteExt = _message.getRemoteExtension();

            if ((_message.getRemoteExtension() != null && _message.getRemoteExtension().containsKey("parentId")) || _message.getMsgType() == MsgTypeEnum.image
            || _message.getMsgType() == MsgTypeEnum.video) {
                if (isHaveMultiMedia) {
                    remoteExt.replace("parentId", parentId);
                } else if (_message.getRemoteExtension() != null) {
                    remoteExt.remove(remoteExt.get("parentId"));
                }
            }
            _message.setRemoteExtension(remoteExt);

            IMMessage message = MessageBuilder.createForwardMessage(_message, sessionId, sessionTypeE);

            if (message == null) {
                return 1;
            }

            Map<String, Object> localExt = new HashMap<String, Object>();
            message.setLocalExtension(localExt);

            sendMessageSelf(message, onSendMessageListener, false, false);
        }

       if (parentId != null && isHaveMultiMedia) {
           IMMessage localMessage = MessageBuilder.createTextMessage(sessionId, sessionTypeE, parentId);
           Map<String, Object> remoteExt = new HashMap<String, Object>();
           remoteExt.put("parentMediaId", parentId);
           localMessage.setRemoteExtension(remoteExt);

           CustomMessageConfig config = new CustomMessageConfig();
           config.enablePush = false;
           config.enableUnreadCount = false;
           localMessage.setConfig(config);

           sendMessageSelf(localMessage,onSendMessageListener, false, false);
       }

        if(!content.isEmpty()) {
            IMMessage messageSelf = MessageBuilder.createTextMessage(sessionId, sessionTypeE, content);
            sendMessageSelf(messageSelf, onSendMessageListener, false, false);
        }
        return 2;
    }

    void revokMessage(IMMessage message) {
        WritableMap msg = Arguments.createMap();
        msg.putString(MessageConstant.Message.MSG_ID, message.getUuid());
        ReactCache.emit(ReactCache.observeDeleteMessage, msg);
    }

    public int revokeMessage(final IMMessage selectMessage, final OnSendMessageListener onSendMessageListener) {
//        if (selectMessage == null) {
//            return 0;
//        }
//        if (MessageUtil.shouldIgnoreRevoke(selectMessage)) {
//            return 1;
//        }
//
//        Boolean isOutOfTime;
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        Long periodOfTime = timestamp.getTime() - selectMessage.getTime();
//        if(periodOfTime > 30000){
//            isOutOfTime = true;
//        }else {
//            isOutOfTime = false;
//        }
//
//        if(isOutOfTime){
//            onSendMessageListener.onResult(ResponseCode.RES_OVERDUE, selectMessage);
//        }else {
//            try{
//                WritableMap contentObject = Arguments.createMap();
//                contentObject.putInt("type",1);
//                contentObject.putString("messageId", selectMessage.getUuid());
//                contentObject.putString("sessionId", selectMessage.getSessionId());
//                contentObject.putBoolean("isObserveReceiveRevokeMessage", true);
//
//                deleteItem(selectMessage, false);
////                revokMessage(selectMessage);
//
////              send a message to session that has message need to revoke
//                IMMessage message = MessageBuilder.createTextMessage(sessionId, sessionTypeEnum, "revoked_success");
//                message.setSubtype(4);
//                Map<String, Object> remoteExtObj = new HashMap<String, Object>();
//                remoteExtObj.put("sessionId", sessionId);
//                remoteExtObj.put("messageId", selectMessage.getUuid());
//                Map<String, Object> remoteExt = new HashMap<String, Object>();
//                remoteExt.put("revokeMessage", remoteExtObj);
//                message.setRemoteExtension(remoteExt);
//
//                CustomMessageConfig customMessageConfig = new CustomMessageConfig();
//                customMessageConfig.enablePush = false;
//                customMessageConfig.enableUnreadCount = false;
//                message.setConfig(customMessageConfig);
//                sendMessageSelf(message, onSendMessageListener, false, false);
////
//                WritableMap content = Arguments.createMap();
//                content.putMap("data",contentObject);
//
//                CustomNotification notification = new CustomNotification();
//                notification.setContent(content.toString());
//                notification.setSessionId(selectMessage.getSessionId());
//                notification.setSessionType(selectMessage.getSessionType());
//
//                NIMClient.getService(MsgService.class).sendCustomNotification(notification);
//
//                MessageHelper.getInstance().onRevokeMessage(selectMessage);
//
//                if (onSendMessageListener != null) {
//                    onSendMessageListener.onResult(ResponseCode.RES_SUCCESS, selectMessage);
//                }
//            }
//            catch(Exception error){
//            }
//        }
//
//        return 2;

        if (selectMessage == null) {
            return 0;
        }
        if (MessageUtil.shouldIgnoreRevoke(selectMessage)) {
            return 1;
        }
        getMsgService().revokeMessage(selectMessage).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void aVoid, Throwable throwable) {
                if (code == ResponseCode.RES_SUCCESS) {
                    deleteItem(selectMessage, false);
                    revokMessage(selectMessage);
                    MessageHelper.getInstance().onRevokeMessage(selectMessage);
                }
                if (onSendMessageListener != null) {
                    onSendMessageListener.onResult(code, selectMessage);
                }
            }
        });
        return 2;
    }

    public void readAllMessageOnlineServiceByListSession(ArrayList<String> listSessionId) {
        for(String sessionId : listSessionId) {
            IMMessage message = NIMClient.getService(MsgService.class).queryLastMessage(sessionId, SessionTypeEnum.P2P);
            if (message != null) {
                getMsgService().clearUnreadCount(sessionId, SessionTypeEnum.P2P);
            }
        }
    }

    public void queryMessage(String selectMessageId, final OnMessageQueryListener messageQueryListener) {
        if (messageQueryListener == null) {
            return;
        }
        if (TextUtils.isEmpty(selectMessageId)) {
            messageQueryListener.onResult(-1, null);
            return;
        }
        List<String> uuids = new ArrayList<>();
        uuids.add(selectMessageId);
        getMsgService().queryMessageListByUuid(uuids).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> messageList, Throwable throwable) {

                if (messageList == null || messageList.isEmpty()) {
                    messageQueryListener.onResult(code, null);
                    return;
                }
                LogUtil.w(TAG, messageList.get(0).getUuid() + "::" + messageList.get(0).getContent());
                messageQueryListener.onResult(code, messageList.get(0));
            }
        });
        return;
    }

    MsgService msgService;

    public MsgService getMsgService() {
        if (msgService == null) {
            synchronized (SessionService.class) {
                if (msgService == null) {
                    msgService = getService(MsgService.class);
                }
            }
        }
        return msgService;
    }

    public void updateMessage(final IMMessage message, MsgStatusEnum statusEnum) {
        message.setStatus(statusEnum);
        getMsgService().updateIMMessageStatus(message);
    }

    public void sendMessageSelf(final IMMessage message, final OnSendMessageListener onSendMessageListener, boolean resend, boolean isCustomerService) {
        appendPushConfig(message);
        if (sessionTypeEnum == SessionTypeEnum.P2P) {
            sessionName = NimUserInfoCache.getInstance().getUserName(sessionId);


            isFriend = NIMClient.getService(FriendService.class).isMyFriend(sessionId);
            LogUtil.w(TAG, "isFriend:" + isFriend);
            if (!isFriend && !isCustomerService) {
                Map<String, Object> localExt = new HashMap<String, Object>();

                if (!isFriend) {
                    localExt.put("isCancelResend", true);
                }

                message.setStatus(MsgStatusEnum.fail);
                message.setLocalExtension(localExt);
                CustomMessageConfig config = new CustomMessageConfig();
                config.enablePush = false;
                config.enableUnreadCount = false;
                message.setConfig(config);
                getMsgService().saveMessageToLocal(message, true);
                sendTipMessage("SEND_MESSAGE_FAILED_WIDTH_STRANGER", null, true, false);
                return;
            }
        }
        getMsgService().sendMessage(message, resend).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }

            @Override
            public void onFailed(int code) {
                LogUtil.w(TAG, "code:" + code);
                if (code == ResponseCode.RES_IN_BLACK_LIST) {
                    Map<String, Object> map = MapBuilder.newHashMap();
                    map.put("resend", false);
                    message.setLocalExtension(map);
                    getMsgService().updateIMMessage(message);
                    sendTipMessage("消息已发出，但被对方拒收了。", null, true, false);
                }
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.w(TAG, "throwable:" + throwable.getLocalizedMessage());
            }
        });
        onMessageStatusChange(message, true);

    }

    private  String convertMessageContent(String content) {
        String pattern = "@\\[(.+?)\\]\\(\\w+\\)";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(content);

        String modifiedString = matcher.replaceAll("@$1");
        Log.e(TAG, "modifiedString => " + modifiedString);

        return modifiedString;
    }

    private void appendPushConfig(IMMessage message) {
//        CustomPushContentProvider customConfig = null;//NimUIKit.getCustomPushContentProvider();
//        if (customConfig != null) {
//            String content = customConfig.getPushContent(message);
//            Map<String, Object> payload = customConfig.getPushPayload(message);
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> body = new HashMap<>();

        body.put("sessionType", String.valueOf(message.getSessionType().getValue()));
        if (message.getSessionType() == SessionTypeEnum.P2P) {
            body.put("sessionId", LoginService.getInstance().getAccount());
        } else if (message.getSessionType() == SessionTypeEnum.Team) {
            body.put("sessionId", message.getSessionId());
        }
        body.put("sessionName", SessionUtil.getSessionName(sessionId, message.getSessionType(), true));
        String pushContent = message.getContent();

        switch (message.getMsgType()) {
            case image:
                pushContent = "[图片]";
                break;
            case video:
                pushContent = "[视频]";
                break;
            case audio:
                MsgAttachment attachment = message.getAttachment();
                if (attachment instanceof AudioAttachment) {
                    AudioAttachment audioAttachment = (AudioAttachment) attachment;

                    pushContent = "[语音]" + " " + Long.toString(Math.round(audioAttachment.getDuration() / 1000)) + "s";

                } else {
                    pushContent = "[语音]";
                }
                break;
            case location:
                pushContent ="[地点]";
                break;
            default:
                pushContent = convertMessageContent(message.getContent());
                break;
        }

        if (message != null && message.getRemoteExtension() != null) {
            Map<String, Object> extensionMsg = message.getRemoteExtension();

            if (extensionMsg.containsKey("extendType")) {
                String extendType = extensionMsg.get("extendType").toString();
                if (extendType.equals("forwardMultipleText")) {
                    pushContent = "[聊天记录]";
                }
            }
        }


        if (message.getSessionType() == SessionTypeEnum.P2P) {
            payload.put("pushTitle", message.getFromNick());
            message.setPushContent(pushContent);
        } else {
            payload.put("pushTitle", SessionUtil.getSessionName(sessionId, message.getSessionType(), true));
            message.setPushContent(message.getFromNick() + ": " + pushContent);
        }

        payload.put("sessionBody", body);
        message.setPushPayload(payload);
    }

    private MemberPushOption createMemPushOption(List<String> selectedMembers, IMMessage message) {

        if (selectedMembers.isEmpty()) {
            return null;
        }

        MemberPushOption memberPushOption = new MemberPushOption();
        memberPushOption.setForcePush(true);
//        memberPushOption.setForcePushContent(message.getContent());
        memberPushOption.setForcePushContent("有人@了你");
        memberPushOption.setForcePushList(selectedMembers);
        return memberPushOption;
    }

    private boolean isOriginImageHasDownloaded(final IMMessage message) {

//        if (message.getAttachStatus() == AttachStatusEnum.transferred) {
//            FileAttachment attachment = null;
//            try {
//                attachment = (FileAttachment) message.getAttachment();
////                AudioAttachment audioAttachment;
////                VideoAttachment videoAttachment;
////                ImageAttachment imageAttachment;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            if (attachment != null && !TextUtils.isEmpty(attachment.getPath())) {
//                LogUtil.w(TAG, "attachmentPath:" + attachment.getPath());
//                return true;
//            }
//        }
        if (message.getAttachStatus() == AttachStatusEnum.transferred &&
                !TextUtils.isEmpty(((FileAttachment) message.getAttachment()).getPath())) {
            return true;
        }
        return false;
    }

    void observerAttachProgress(boolean register) {
        getService(MsgServiceObserve.class).observeAttachmentProgress(new Observer<AttachmentProgress>() {
            @Override
            public void onEvent(AttachmentProgress attachmentProgress) {
                ReactCache.emit(ReactCache.observeProgressSend, ReactCache.createAttachmentProgress(attachmentProgress));
            }
        }, register);
    }
    // 下载附件，参数1位消息对象，参数2为是下载缩略图还是下载原图。
// 因为下载的文件可能会很大，这个接口返回类型为 AbortableFuture ，允许用户中途取消下载。

    public void downloadAttachment(IMMessage message, boolean isThumb) {
        if (isOriginImageHasDownloaded(message)) {
            return;
        }

        ReactCache.DownloadCallback callback = new ReactCache.DownloadCallback() {
            @Override
            public void onSuccess(Void result) {
                Map<String, Object> newLocalExt = MapBuilder.newHashMap();
                newLocalExt.put("downloadStatus", "success");
                setLocalExtension(message, newLocalExt);
                ReactCache.createMessage(message, true);
                Log.d("onSuccess download", "onSuccess download" + "");
            }

            @Override
            public void onFailed(int code) {
                Log.d("onFailed resultresult", String.valueOf(code));
            }

            @Override
            public void onException(Throwable exception) {
                Log.d("onException result", String.valueOf(exception));
            }
        };
        Map<String, Object> newLocalExt = MapBuilder.newHashMap();
        newLocalExt.put("downloadStatus", "downloading");

        setLocalExtension(message, newLocalExt);

        AbortableFuture future = getService(MsgService.class).downloadAttachment(message, isThumb);
        future.setCallback(callback);
    }

    public void sendCustomNotification(ReadableMap dataDict, String toSessionId, String toSessionType, final OnCustomNotificationListener onCustomNotificationListener){
        CustomNotification notification = new CustomNotification();

        try {
            SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(toSessionType);

            notification.setContent(dataDict.toString());
            notification.setSessionId(toSessionId);
            notification.setSessionType(sessionTypeEnum);

            NIMClient.getService(MsgService.class).sendCustomNotification(notification);
            if(onCustomNotificationListener != null){
                onCustomNotificationListener.onResult(ResponseCode.RES_SUCCESS, notification);
            }
        } catch (Exception exception){
            onCustomNotificationListener.onResult(ResponseCode.RES_EUNKNOWN, notification);
        }

    }

    public interface OnSendMessageListener {
        int onResult(int code, IMMessage message);
    }

    public interface OnMessageQueryListListener {
        public int onResult(int code, List<IMMessage> messageList, Set<String> timedItems);
    }

    public interface OnMessageQueryListener {
        public int onResult(int code, IMMessage message);
    }

    public interface OnCustomNotificationListener {
        public int onResult(int code, CustomNotification customNotification);
    }
}
