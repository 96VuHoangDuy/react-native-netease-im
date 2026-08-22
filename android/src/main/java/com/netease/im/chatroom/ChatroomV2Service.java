package com.netease.im.chatroom;

import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.netease.im.ReactCache;
import com.netease.im.RNNeteaseImModule;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.chatroom.V2NIMChatroomClient;
import com.netease.nimlib.sdk.v2.chatroom.V2NIMChatroomClientListener;
import com.netease.nimlib.sdk.v2.chatroom.V2NIMChatroomListener;
import com.netease.nimlib.sdk.v2.chatroom.V2NIMChatroomMessageCreator;
import com.netease.nimlib.sdk.v2.chatroom.enums.V2NIMChatroomMemberRole;
import com.netease.nimlib.sdk.v2.chatroom.enums.V2NIMChatroomStatus;
import com.netease.nimlib.sdk.v2.chatroom.model.V2NIMChatroomInfo;
import com.netease.nimlib.sdk.v2.chatroom.model.V2NIMChatroomKickedInfo;
import com.netease.nimlib.sdk.v2.chatroom.model.V2NIMChatroomMember;
import com.netease.nimlib.sdk.v2.chatroom.model.V2NIMChatroomMessage;
import com.netease.nimlib.sdk.v2.chatroom.option.V2NIMChatroomMemberQueryOption;
import com.netease.nimlib.sdk.v2.chatroom.option.V2NIMChatroomMessageListOption;
import com.netease.nimlib.sdk.v2.chatroom.params.V2NIMChatroomEnterParams;
import com.netease.nimlib.sdk.v2.chatroom.option.V2NIMChatroomLoginOption;
import com.netease.nimlib.sdk.v2.chatroom.params.V2NIMSendChatroomMessageParams;
import com.netease.nimlib.sdk.v2.chatroom.provider.V2NIMChatroomTokenProvider;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginAuthType;
import com.netease.nimlib.sdk.v2.chatroom.provider.V2NIMChatroomLinkProvider;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageQueryDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FLOW: -
 * ROLE: Bridge NIM Chatroom V2 (V2NIMChatroomClient) cho Android — enter/exit phòng, gửi/nhận tin,
 *       history, member list, và forward mọi event realtime của phòng sang JS.
 * BREAKS: Đổi tên event ở đây mà không đổi listener JS -> màn chat đứng im (không có tin mới,
 *         member/ban không cập nhật) nhưng KHÔNG báo lỗi. Đổi key trong map trả về -> store mobile
 *         đọc undefined, tin nhắn render rỗng.
 */
public class ChatroomV2Service {

    private static final String TAG = "ChatroomV2Service";

    // roomId -> instance. V2 bind 1-1 instance <-> phòng; giữ map để exit/destroy đúng instance khi
    // JS thoát phòng. App hiện chỉ mở 1 phòng một lúc, nhưng SDK cho phép nhiều nên không hard-code 1.
    private static final Map<String, V2NIMChatroomClient> clients = new ConcurrentHashMap<>();

    private ChatroomV2Service() {
    }

    // ---------------------------------------------------------------- lifecycle

    /**
     * Vào phòng: tạo instance mới, gắn listener, enter bằng DYNAMIC token (authType + tokenProvider).
     * accid/token/appKey là matched set do JS lấy từ một lần gọi `POST /client/chat/login`.
     */
    public static void enter(ReadableMap params, final Promise promise) {
        final String roomId = getString(params, "roomId");
        if (TextUtils.isEmpty(roomId)) {
            promise.reject("-1", "roomId rỗng");
            return;
        }
        // Chặn sớm: thiếu accid/token thì NIM trả 102404/102302 rất khó lần ngược về nguyên nhân thật.
        if (TextUtils.isEmpty(getString(params, "accid"))
                || TextUtils.isEmpty(getString(params, "token"))) {
            promise.reject("-1", "thiếu accid hoặc token");
            return;
        }
        try {
            // Backend cấp appKey riêng, khác appKey khoá cứng trong manifest. Phòng thủ: đồng bộ
            // trước khi enter, no-op nếu đã khớp (login IM thường đã updateAppKey nên thực tế luôn
            // khớp sẵn). KHÔNG phải nguyên nhân của 102302 — đã đo bằng đối chứng A/B, xem [D-020].
            RNNeteaseImModule.syncAppKey(getString(params, "appKey"));

            // Vào lại phòng đang mở: dọn instance cũ trước, nếu không SDK giữ 2 instance cùng roomId
            // và listener cũ vẫn bắn -> JS nhận tin nhắn nhân đôi.
            exitInternal(roomId);

            final V2NIMChatroomClient client = V2NIMChatroomClient.newInstance();
            clients.put(roomId, client);
            client.addChatroomClientListener(clientListener(roomId));
            client.getChatroomService().addChatroomListener(roomListener(roomId));

            final List<String> addrs = getStringList(params, "addrs");
            final String token = getString(params, "token");
            // Backend cấp DYNAMIC token (JWT có hạn), không phải static token thường trực.
            // Dynamic thì KHÔNG truyền qua withToken() — phải khai authType + tokenProvider,
            // nếu không NIM trả 102302 invalid token. Cho JS chọn để còn đối chứng khi debug.
            final boolean isStaticToken = "static".equals(getString(params, "authType"));

            V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder builder =
                    V2NIMChatroomEnterParams.V2NIMChatroomEnterParamsBuilder
                            // linkProvider = addr từ backend (chatroom/addr). Null khi JS không truyền
                            // -> chỉ còn LBS. Docs NIM: enableLbs và linkProvider không được cùng rỗng.
                            .builder(addrs.isEmpty() ? null : linkProvider(addrs))
                            .withEnableLbs(true)
                            .withAccountId(getString(params, "accid"))
                            .withRoomNick(getString(params, "nickname"))
                            .withRoomAvatar(getString(params, "avatar"));

            if (isStaticToken) {
                builder.withToken(token);
            } else {
                builder.withLoginOption(
                        V2NIMChatroomLoginOption.V2NIMChatroomLoginOptionBuilder.builder()
                                .withAuthType(
                                        V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN)
                                // ⚠️ Provider trả token CỐ ĐỊNH của lần enter này. Dynamic token có
                                // hạn, nên nếu SDK gọi lại provider lúc reconnect sau khi token hết
                                // hạn thì vẫn nhận token cũ -> reconnect fail. Muốn đúng hoàn toàn
                                // thì provider phải hỏi ngược JS lấy token mới (chưa làm, Phase 1
                                // chưa gặp vì phiên chatroom ngắn).
                                .withTokenProvider(new V2NIMChatroomTokenProvider() {
                                    @Override
                                    public String getToken(String roomId, String account) {
                                        return token;
                                    }
                                })
                                .build());
            }

            V2NIMChatroomEnterParams enterParams = builder.build();

            client.enter(roomId, enterParams,
                    result -> {
                        WritableMap map = fromInfo(result.getChatroom());
                        map.putBoolean("isLoginSuccess", true);
                        map.putMap("selfMember", fromMember(result.getSelfMember()));
                        promise.resolve(map);
                    },
                    error -> {
                        // enter fail -> instance vô dụng, không giữ lại (lần enter sau tạo mới).
                        exitInternal(roomId);
                        rejectWith(promise, error);
                    });
        } catch (Throwable t) {
            Log.e(TAG, "enter lỗi: " + t.getMessage(), t);
            promise.reject("-1", t.getMessage());
        }
    }

    /**
     * Chẩn đoán 102302: appKey mà SDK ĐANG dùng lúc runtime, sau mọi lần `updateAppKey`.
     * Token của backend chỉ hợp lệ dưới đúng appKey đã cấp nó — lệch là invalid token.
     */
    public static void getSdkAppKey(Promise promise) {
        try {
            WritableMap map = Arguments.createMap();
            map.putString("sdkAppKey", com.netease.nimlib.sdk.NIMClient.getAppKey());
            promise.resolve(map);
        } catch (Throwable t) {
            promise.reject("-1", t.getMessage());
        }
    }

    /** Thoát phòng + huỷ instance. Gọi lại khi không ở trong phòng là no-op. */
    public static void exit(String roomId, Promise promise) {
        try {
            exitInternal(roomId);
            promise.resolve(true);
        } catch (Throwable t) {
            Log.e(TAG, "exit lỗi: " + t.getMessage(), t);
            promise.reject("-1", t.getMessage());
        }
    }

    /** Info phòng lấy từ cache của instance (không gọi mạng). */
    public static void getChatroomInfo(String roomId, Promise promise) {
        V2NIMChatroomClient client = clients.get(roomId);
        if (client == null) {
            promise.reject("-1", "chưa vào phòng " + roomId);
            return;
        }
        V2NIMChatroomInfo info = client.getChatroomInfo();
        WritableMap map = fromInfo(info);
        map.putBoolean("isLoginSuccess", info != null);
        promise.resolve(map);
    }

    // ---------------------------------------------------------------- message

    /** Gửi text. Kết quả gửi thành công còn bắn thêm qua observeChatroomSendMessage. */
    public static void sendTextMessage(String roomId, String text, final Promise promise) {
        V2NIMChatroomClient client = clients.get(roomId);
        if (client == null) {
            promise.reject("-1", "chưa vào phòng " + roomId);
            return;
        }
        try {
            V2NIMChatroomMessage message = V2NIMChatroomMessageCreator.createTextMessage(text);
            client.getChatroomService().sendMessage(
                    message,
                    new V2NIMSendChatroomMessageParams(),
                    result -> promise.resolve(fromMessage(result.getMessage())),
                    error -> rejectWith(promise, error),
                    null);
        } catch (Throwable t) {
            Log.e(TAG, "sendTextMessage lỗi: " + t.getMessage(), t);
            promise.reject("-1", t.getMessage());
        }
    }

    /**
     * Lịch sử tin nhắn (server giữ mặc định 10 ngày).
     * beginTime = 0 -> SDK lấy từ thời điểm hiện tại lùi về; phân trang bằng createTime của tin cũ nhất
     * đang có. V2 phân trang theo THỜI GIAN, không theo messageId như bản old-gen của iOS.
     */
    public static void getMessageList(String roomId, int limit, double beginTime, String direction,
                                      final Promise promise) {
        V2NIMChatroomClient client = clients.get(roomId);
        if (client == null) {
            promise.reject("-1", "chưa vào phòng " + roomId);
            return;
        }
        try {
            V2NIMChatroomMessageListOption option = new V2NIMChatroomMessageListOption();
            option.setLimit(limit <= 0 ? 20 : limit);
            option.setBeginTime((long) beginTime);
            option.setDirection("ASC".equalsIgnoreCase(direction)
                    ? V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_ASC
                    : V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);

            client.getChatroomService().getMessageList(option,
                    messages -> promise.resolve(fromMessages(messages)),
                    error -> rejectWith(promise, error));
        } catch (Throwable t) {
            Log.e(TAG, "getMessageList lỗi: " + t.getMessage(), t);
            promise.reject("-1", t.getMessage());
        }
    }

    // ---------------------------------------------------------------- member

    /**
     * Member list phân trang. pageToken rỗng = trang đầu; kết quả trả kèm pageToken/finished.
     * `onlyOnline = false` để lấy cả thành viên offline có role cố định (creator/manager).
     */
    public static void getMemberList(String roomId, int limit, String pageToken, boolean onlyOnline,
                                     final Promise promise) {
        V2NIMChatroomClient client = clients.get(roomId);
        if (client == null) {
            promise.reject("-1", "chưa vào phòng " + roomId);
            return;
        }
        try {
            V2NIMChatroomMemberQueryOption option = new V2NIMChatroomMemberQueryOption();
            option.setLimit(limit <= 0 ? 100 : limit);
            // Cờ do màn hình gọi quyết định, không hard-code ở đây: header phòng chat cần
            // người đang trong phòng (true), danh sách thành viên §4.5.1 có thể cần cả offline (false).
            option.setOnlyOnline(onlyOnline);
            if (!TextUtils.isEmpty(pageToken)) {
                option.setPageToken(pageToken);
            }
            client.getChatroomService().getMemberListByOption(option,
                    result -> {
                        WritableMap map = Arguments.createMap();
                        map.putString("pageToken", result.getPageToken());
                        map.putBoolean("finished", result.isFinished());
                        map.putArray("members", fromMembers(result.getMemberList()));
                        promise.resolve(map);
                    },
                    error -> rejectWith(promise, error));
        } catch (Throwable t) {
            Log.e(TAG, "getMemberList lỗi: " + t.getMessage(), t);
            promise.reject("-1", t.getMessage());
        }
    }

    /** Lấy member theo danh sách accountId (dùng cho profile tối thiểu khi bấm vào 1 người). */
    public static void getMemberByIds(String roomId, ReadableArray accountIds, final Promise promise) {
        V2NIMChatroomClient client = clients.get(roomId);
        if (client == null) {
            promise.reject("-1", "chưa vào phòng " + roomId);
            return;
        }
        try {
            List<String> ids = new ArrayList<>();
            for (int i = 0; accountIds != null && i < accountIds.size(); i++) {
                ids.add(accountIds.getString(i));
            }
            client.getChatroomService().getMemberByIds(ids,
                    members -> promise.resolve(fromMembers(members)),
                    error -> rejectWith(promise, error));
        } catch (Throwable t) {
            Log.e(TAG, "getMemberByIds lỗi: " + t.getMessage(), t);
            promise.reject("-1", t.getMessage());
        }
    }

    // ---------------------------------------------------------------- listeners

    private static V2NIMChatroomClientListener clientListener(final String roomId) {
        return new V2NIMChatroomClientListener() {
            @Override
            public void onChatroomStatus(V2NIMChatroomStatus status, V2NIMError error) {
                WritableMap map = base(roomId);
                map.putString("status", status == null ? "" : status.name());
                if (error != null) {
                    map.putInt("code", error.getCode());
                    map.putString("message", error.getDesc());
                }
                emit(ReactCache.observeChatroomStatus, map);
            }

            @Override
            public void onChatroomEntered() {
                emit(ReactCache.observeChatroomStatus, statusMap(roomId, "ENTERED"));
            }

            @Override
            public void onChatroomExited(V2NIMError error) {
                emit(ReactCache.observeChatroomStatus, statusMap(roomId, "EXITED"));
            }

            @Override
            public void onChatroomKicked(V2NIMChatroomKickedInfo info) {
                WritableMap map = base(roomId);
                map.putString("reason", info == null || info.getKickedReason() == null
                        ? "" : info.getKickedReason().name());
                emit(ReactCache.observeChatroomKicked, map);
            }
        };
    }

    private static V2NIMChatroomListener roomListener(final String roomId) {
        return new V2NIMChatroomListener() {
            @Override
            public void onReceiveMessages(List<V2NIMChatroomMessage> messages) {
                WritableMap map = base(roomId);
                map.putArray("messages", fromMessages(messages));
                emit(ReactCache.observeChatroomMessage, map);
            }

            @Override
            public void onSendMessage(V2NIMChatroomMessage message) {
                WritableMap map = base(roomId);
                map.putMap("message", fromMessage(message));
                emit(ReactCache.observeChatroomSendMessage, map);
            }

            @Override
            public void onChatroomMemberEnter(V2NIMChatroomMember member) {
                WritableMap map = base(roomId);
                map.putMap("member", fromMember(member));
                emit(ReactCache.observeChatroomMemberIn, map);
            }

            @Override
            public void onChatroomMemberExit(String accountId) {
                WritableMap map = base(roomId);
                map.putString("userId", accountId);
                emit(ReactCache.observeChatroomMemberOut, map);
            }

            @Override
            public void onChatroomMemberRoleUpdated(V2NIMChatroomMemberRole previousRole,
                                                    V2NIMChatroomMember member) {
                WritableMap map = base(roomId);
                map.putString("previousType", roleName(previousRole));
                map.putMap("member", fromMember(member));
                emit(ReactCache.observeChatroomMemberRoleUpdated, map);
            }

            @Override
            public void onChatroomMemberInfoUpdated(V2NIMChatroomMember member) {
                WritableMap map = base(roomId);
                map.putMap("member", fromMember(member));
                emit(ReactCache.observeChatroomMemberInfoUpdated, map);
            }

            @Override
            public void onSelfChatBannedUpdated(boolean chatBanned) {
                WritableMap map = base(roomId);
                map.putBoolean("isMuted", chatBanned);
                map.putBoolean("isTempMuted", false);
                emit(ReactCache.observeChatroomSelfBanned, map);
            }

            @Override
            public void onSelfTempChatBannedUpdated(boolean tempChatBanned, long duration) {
                WritableMap map = base(roomId);
                map.putBoolean("isMuted", tempChatBanned);
                map.putBoolean("isTempMuted", tempChatBanned);
                map.putDouble("tempMuteDuration", duration);
                emit(ReactCache.observeChatroomSelfBanned, map);
            }

            @Override
            public void onChatroomInfoUpdated(V2NIMChatroomInfo info) {
                emit(ReactCache.observeChatroomInfoUpdated, fromInfo(info));
            }

            @Override
            public void onChatroomChatBannedUpdated(boolean chatBanned) {
                WritableMap map = base(roomId);
                map.putBoolean("isChatBanned", chatBanned);
                emit(ReactCache.observeChatroomChatBanned, map);
            }

            @Override
            public void onMessageRevokedNotification(String messageClientId, long revokeTime) {
                WritableMap map = base(roomId);
                map.putString("msgId", messageClientId);
                map.putDouble("revokeTime", revokeTime);
                emit(ReactCache.observeChatroomMessageRevoked, map);
            }

            @Override
            public void onChatroomTagsUpdated(List<String> tags) {
                // Tag không dùng Phase 1 — bỏ qua để không đẻ event thừa phía JS.
            }
        };
    }

    // ---------------------------------------------------------------- helpers

    private static void exitInternal(String roomId) {
        V2NIMChatroomClient client = clients.remove(roomId);
        if (client == null) {
            return;
        }
        try {
            client.exit();
        } catch (Throwable t) {
            Log.e(TAG, "exit instance lỗi: " + t.getMessage());
        }
        // destroyInstance BẮT BUỘC sau exit: chỉ exit thì instance vẫn nằm trong getInstanceList()
        // và giữ listener -> rò bộ nhớ + event của phòng cũ vẫn bắn sau khi thoát.
        V2NIMChatroomClient.destroyInstance(client.getInstanceId());
    }

    private static V2NIMChatroomLinkProvider linkProvider(final List<String> addrs) {
        return (roomId, accountId) -> addrs;
    }

    private static void emit(String event, WritableMap data) {
        try {
            ReactCache.emit(event, data);
        } catch (Throwable t) {
            Log.e(TAG, "emit " + event + " lỗi: " + t.getMessage());
        }
    }

    private static WritableMap base(String roomId) {
        WritableMap map = Arguments.createMap();
        map.putString("roomId", roomId);
        return map;
    }

    private static WritableMap statusMap(String roomId, String status) {
        WritableMap map = base(roomId);
        map.putString("status", status);
        return map;
    }

    private static void rejectWith(Promise promise, V2NIMError error) {
        if (error == null) {
            promise.reject("-1", "unknown error");
            return;
        }
        promise.reject(String.valueOf(error.getCode()), error.getDesc());
    }

    private static WritableMap fromInfo(V2NIMChatroomInfo info) {
        WritableMap map = Arguments.createMap();
        if (info == null) {
            return map;
        }
        map.putString("roomId", info.getRoomId());
        map.putString("name", info.getRoomName());
        map.putString("announcement", info.getAnnouncement());
        map.putString("broadcastUrl", info.getLiveUrl());
        map.putString("creatorAccountId", info.getCreatorAccountId());
        map.putString("serverExtension", info.getServerExtension());
        map.putInt("onlineUserCount", info.getOnlineUserCount());
        map.putBoolean("isChatBanned", info.isChatBanned());
        map.putBoolean("isValidRoom", info.isValidRoom());
        return map;
    }

    private static WritableArray fromMembers(List<V2NIMChatroomMember> members) {
        WritableArray array = Arguments.createArray();
        if (members == null) {
            return array;
        }
        for (V2NIMChatroomMember member : members) {
            array.pushMap(fromMember(member));
        }
        return array;
    }

    private static WritableMap fromMember(V2NIMChatroomMember member) {
        WritableMap map = Arguments.createMap();
        if (member == null) {
            return map;
        }
        // Key giữ đúng type NIMChatroomMember phía JS (userId/nickname/avatar/type/isMuted...),
        // đổi tên key ở đây là vỡ ChatroomMembersStore mobile.
        map.putString("roomId", member.getRoomId());
        map.putString("userId", member.getAccountId());
        map.putString("nickname", member.getRoomNick());
        map.putString("avatar", member.getRoomAvatar());
        map.putString("avatarThumbnail", member.getRoomAvatar());
        map.putString("type", roleName(member.getMemberRole()));
        map.putBoolean("isOnline", member.isOnline());
        map.putBoolean("isBlocked", member.isBlocked());
        map.putBoolean("isMuted", member.isChatBanned());
        map.putBoolean("isTempMuted", member.isTempChatBanned());
        map.putDouble("tempMuteDuration", member.getTempChatBannedDuration());
        map.putDouble("enterTime", member.getEnterTime());
        map.putDouble("updateTime", member.getUpdateTime());
        return map;
    }

    private static WritableArray fromMessages(List<V2NIMChatroomMessage> messages) {
        WritableArray array = Arguments.createArray();
        if (messages == null) {
            return array;
        }
        for (V2NIMChatroomMessage message : messages) {
            array.pushMap(fromMessage(message));
        }
        return array;
    }

    private static WritableMap fromMessage(V2NIMChatroomMessage message) {
        WritableMap map = Arguments.createMap();
        if (message == null) {
            return map;
        }
        map.putString("msgId", message.getMessageClientId());
        map.putString("roomId", message.getRoomId());
        map.putString("fromAccount", message.getSenderId());
        map.putString("text", message.getText());
        map.putString("msgType", message.getMessageType() == null ? "" : message.getMessageType().name());
        map.putString("serverExtension", message.getServerExtension());
        map.putString("sendingState",
                message.getSendingState() == null ? "" : message.getSendingState().name());
        map.putInt("subType", message.getSubType() == null ? 0 : message.getSubType());
        map.putDouble("timestamp", message.getCreateTime());
        map.putBoolean("isSelf", message.isSelf());
        return map;
    }

    /** Rút gọn enum V2NIM_CHATROOM_MEMBER_ROLE_XXX -> "XXX" cho khớp enum ChatroomMemberType phía JS. */
    private static String roleName(V2NIMChatroomMemberRole role) {
        if (role == null) {
            return "";
        }
        String name = role.name();
        String prefix = "V2NIM_CHATROOM_MEMBER_ROLE_";
        return name.startsWith(prefix) ? name.substring(prefix.length()) : name;
    }

    private static String getString(ReadableMap params, String key) {
        if (params == null || !params.hasKey(key) || params.isNull(key)) {
            return "";
        }
        return params.getString(key);
    }

    private static List<String> getStringList(ReadableMap params, String key) {
        if (params == null || !params.hasKey(key) || params.isNull(key)) {
            return Collections.emptyList();
        }
        ReadableArray array = params.getArray(key);
        List<String> list = new ArrayList<>();
        for (int i = 0; array != null && i < array.size(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }
}
