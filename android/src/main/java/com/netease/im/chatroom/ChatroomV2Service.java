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
import com.netease.nimlib.v2.chatroom.builder.V2NIMChatroomMessageBuilder;
import com.netease.nimlib.sdk.v2.chatroom.attachment.V2NIMChatroomChatBannedNotificationAttachment;
import com.netease.nimlib.sdk.v2.chatroom.attachment.V2NIMChatroomNotificationAttachment;
import com.netease.nimlib.sdk.v2.chatroom.config.V2NIMUserInfoConfig;
import com.netease.nimlib.sdk.v2.chatroom.enums.V2NIMChatroomMemberRole;
import com.netease.nimlib.sdk.v2.chatroom.enums.V2NIMChatroomMessageNotificationType;
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
 * QUYỀN (3 hàm setMember*): chỉ creator + administrator của phòng gọi được; administrator KHÔNG
 *         thao tác được lên creator và administrator khác; không thao tác được lên fictitious user
 *         và anonymous tourist. Gọi sai quyền -> SDK trả lỗi, bridge không tự chặn trước.
 */
public class ChatroomV2Service {

    private static final String TAG = "ChatroomV2Service";

    // roomId -> instance. V2 bind 1-1 instance <-> phòng; giữ map để exit/destroy đúng instance khi
    // JS thoát phòng. App hiện chỉ mở 1 phòng một lúc, nhưng SDK cho phép nhiều nên không hard-code 1.
    private static final Map<String, V2NIMChatroomClient> clients = new ConcurrentHashMap<>();

    // roomId -> {roomNick, roomAvatar} của phiên enter hiện tại. Cần vì SDK KHÔNG truyền
    // roomNick/roomAvatar sang người nhận — bên gửi phải tự đính vào từng tin (withSelfUserInfo).
    private static final Map<String, String[]> selfProfiles = new ConcurrentHashMap<>();

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
            selfProfiles.put(roomId, new String[]{
                    getString(params, "nickname"), getString(params, "avatar")});
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

    /**
     * Gửi text. Kết quả gửi thành công còn bắn thêm qua observeChatroomSendMessage.
     * `serverExtension` là chuỗi tuỳ ý đi kèm tin (JSON của app, ví dụ metadata trích dẫn) —
     * rỗng thì không set. Bên nhận đọc lại ở `serverExtension` của message đã serialize.
     */
    public static void sendTextMessage(String roomId, String text, String serverExtension,
                                       final Promise promise) {
        V2NIMChatroomClient client = clients.get(roomId);
        if (client == null) {
            promise.reject("-1", "chưa vào phòng " + roomId);
            return;
        }
        try {
            V2NIMChatroomMessage created = V2NIMChatroomMessageCreator.createTextMessage(text);
            // Set TRƯỚC withSelfUserInfo: hàm đó dựng lại message bằng builder và có chép
            // serverExtension sang bản mới. Đảo thứ tự là extension bị bản dựng lại nuốt mất.
            if (!TextUtils.isEmpty(serverExtension)) {
                created.setServerExtension(serverExtension);
            }
            V2NIMChatroomMessage message = withSelfUserInfo(created, roomId);
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

    /**
     * Cấm chat VĨNH VIỄN / gỡ. `chatBanned=false` là gỡ.
     * Gỡ cấm vĩnh viễn KHÔNG đụng tới hạn của cấm tạm — hai loại ban độc lập ở server.
     */
    public static void setMemberChatBanned(String roomId, String accountId, boolean chatBanned,
                                           String notificationExtension, final Promise promise) {
        V2NIMChatroomClient client = clients.get(roomId);
        if (client == null) {
            promise.reject("-1", "chưa vào phòng " + roomId);
            return;
        }
        try {
            client.getChatroomService().setMemberChatBannedStatus(
                    accountId, chatBanned, emptyToNull(notificationExtension),
                    unused -> promise.resolve(true),
                    error -> rejectWith(promise, error));
        } catch (Throwable t) {
            Log.e(TAG, "setMemberChatBanned lỗi: " + t.getMessage(), t);
            promise.reject("-1", t.getMessage());
        }
    }

    /**
     * Cấm chat TẠM THỜI / gỡ. `duration` tính bằng GIÂY (không phải ms như timestamp ở file này),
     * một lần tối đa 30 ngày, truyền 0 để gỡ. Set lại là GHI ĐÈ hạn cũ, không cộng dồn.
     */
    public static void setMemberTempChatBanned(String roomId, String accountId, double duration,
                                               boolean notificationEnabled,
                                               String notificationExtension, final Promise promise) {
        V2NIMChatroomClient client = clients.get(roomId);
        if (client == null) {
            promise.reject("-1", "chưa vào phòng " + roomId);
            return;
        }
        try {
            client.getChatroomService().setMemberTempChatBanned(
                    accountId, (long) duration, notificationEnabled,
                    emptyToNull(notificationExtension),
                    unused -> promise.resolve(true),
                    error -> rejectWith(promise, error));
        } catch (Throwable t) {
            Log.e(TAG, "setMemberTempChatBanned lỗi: " + t.getMessage(), t);
            promise.reject("-1", t.getMessage());
        }
    }

    /**
     * Thêm/gỡ danh sách đen NIM — công cụ ENFORCEMENT của "danh sách đen" nghiệp vụ [D-032].
     * Khác cấm chat: người bị chặn còn bị ĐÁ khỏi phòng (`observeChatroomKicked`) và mất kết nối,
     * không chỉ mất quyền gửi.
     * ⚠️ ĐI KÈM ghi DB backend, đừng gọi đơn lẻ. Hai tầng: **DB backend là nguồn sự thật**
     * (giữ trạng thái qua phiên, portal đọc từ đó), NIM blocked chỉ là hiệu lực tức thì.
     * Lệch nhau thì DB thắng — NIM blocked mà DB không có là rác, phải gỡ ở NIM.
     */
    public static void setMemberBlocked(String roomId, String accountId, boolean blocked,
                                        String notificationExtension, final Promise promise) {
        V2NIMChatroomClient client = clients.get(roomId);
        if (client == null) {
            promise.reject("-1", "chưa vào phòng " + roomId);
            return;
        }
        try {
            client.getChatroomService().setMemberBlockedStatus(
                    accountId, blocked, emptyToNull(notificationExtension),
                    unused -> promise.resolve(true),
                    error -> rejectWith(promise, error));
        } catch (Throwable t) {
            Log.e(TAG, "setMemberBlocked lỗi: " + t.getMessage(), t);
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

    /**
     * Đính tên/avatar của mình vào tin trước khi gửi — người nhận đọc ở `userInfoConfig`.
     * Đã đo có tác dụng thật: tin iOS-gửi (iOS set cùng cơ chế) sang máy Android nhận CÓ đủ
     * `senderNickname`/`senderAvatar` — msgId `2d4749d4-b8b0-49f3-9a5f-f46d144a3d0b`, 2026-08-26
     * 20:56. Không set thì bên nhận chỉ có accid, phải tra member list (rỗng với CREATOR, và không
     * tra được người đã rời phòng).
     *
     * ⚠️ INTERNAL API. `V2NIMChatroomMessageCreator` không cho set `userInfoConfig` và
     * `V2NIMChatroomMessage` không có setter, nên đường duy nhất là dựng lại message bằng
     * `V2NIMChatroomMessageBuilder` — lớp này ở `com.netease.nimlib.v2.chatroom.builder`, NGOÀI
     * namespace `com.netease.nimlib.sdk`, tức không phải public API: nâng SDK có thể đổi chữ ký,
     * đổi package, hoặc bị obfuscate. Vì vậy toàn khối bọc try/catch và **fail-soft tuyệt đối**:
     * hỏng thì trả message gốc, tin vẫn gửi đi, chỉ mất tên. Đừng bỏ catch.
     * iOS làm cùng việc này bằng public API (property `userInfoConfig` readwrite) nên không cần bọc.
     */
    private static V2NIMChatroomMessage withSelfUserInfo(V2NIMChatroomMessage message, String roomId) {
        String[] profile = selfProfiles.get(roomId);
        if (profile == null || (TextUtils.isEmpty(profile[0]) && TextUtils.isEmpty(profile[1]))) {
            return message;
        }
        try {
            V2NIMUserInfoConfig userInfo = new V2NIMUserInfoConfig();
            if (!TextUtils.isEmpty(profile[0])) {
                userInfo.setSenderNick(profile[0]);
            }
            if (!TextUtils.isEmpty(profile[1])) {
                userInfo.setSenderAvatar(profile[1]);
            }
            // GIÂY, không phải ms — khớp iOS (`timeIntervalSince1970`). Để 0 thì nghi server coi là
            // metadata cũ rồi bỏ qua.
            userInfo.setUserInfoTimestamp(System.currentTimeMillis() / 1000);

            V2NIMChatroomMessage rebuilt = V2NIMChatroomMessageBuilder.builder()
                    .messageType(message.getMessageType())
                    .subType(message.getSubType())
                    .text(message.getText())
                    .attachment(message.getAttachment())
                    .serverExtension(message.getServerExtension())
                    .userInfoConfig(userInfo)
                    .build();
            return rebuilt == null ? message : rebuilt;
        } catch (Throwable t) {
            Log.e(TAG, "đính userInfoConfig lỗi, gửi tin không kèm tên: " + t.getMessage());
            return message;
        }
    }

    private static void exitInternal(String roomId) {
        selfProfiles.remove(roomId);
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
        // Tên/avatar người gửi đi KÈM message (V2NIMUserInfoConfig), không phải trường cấp 1.
        // Có nó thì JS khỏi tra member list — nguồn cũ hay rỗng: CREATOR trả roomNick '', và
        // người đã rời phòng thì không còn trong list để tra. Absent khi SDK không đính kèm.
        // Rỗng cũng KHÔNG đưa key vào map (không chỉ null): JS phân biệt "không có thông tin"
        // bằng `undefined`, gửi '' xuống là ép UI render tên rỗng thay vì chạy fallback.
        V2NIMUserInfoConfig userInfo = message.getUserInfoConfig();
        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.getSenderNick())) {
                map.putString("senderNickname", userInfo.getSenderNick());
            }
            if (!TextUtils.isEmpty(userInfo.getSenderAvatar())) {
                map.putString("senderAvatar", userInfo.getSenderAvatar());
            }
        }
        map.putString("text", message.getText());
        map.putString("msgType", message.getMessageType() == null ? "" : message.getMessageType().name());
        map.putString("serverExtension", message.getServerExtension());
        map.putString("sendingState",
                message.getSendingState() == null ? "" : message.getSendingState().name());
        map.putInt("subType", message.getSubType() == null ? 0 : message.getSubType());
        map.putDouble("timestamp", message.getCreateTime());
        map.putBoolean("isSelf", message.isSelf());
        putNotification(map, message);
        return map;
    }

    /**
     * Tin hệ thống (`msgType = V2NIM_MESSAGE_TYPE_NOTIFICATION`) mang dữ liệu vào/ra/ban trong
     * ATTACHMENT chứ không phải `text` — không đọc attachment thì JS chỉ thấy tin rỗng `subType:0`
     * và không biết ai vào ai ra.
     * Đây là đường DUY NHẤT biết member vào phòng: callback `onChatroomMemberEnter` chỉ bắn khi
     * console Yunxin bật "聊天室用户进出消息系统下发", không bật thì im lặng (xem README).
     * Field flat + absent khi rỗng, cùng chuẩn `senderNickname`.
     */
    private static void putNotification(WritableMap map, V2NIMChatroomMessage message) {
        if (!(message.getAttachment() instanceof V2NIMChatroomNotificationAttachment)) {
            return;
        }
        V2NIMChatroomNotificationAttachment attachment =
                (V2NIMChatroomNotificationAttachment) message.getAttachment();
        if (attachment.getType() != null) {
            map.putString("notificationType", notificationTypeName(attachment.getType()));
        }
        putStringArrayIfAny(map, "targetIds", attachment.getTargetIds());
        putStringArrayIfAny(map, "targetNicks", attachment.getTargetNicks());
        if (!TextUtils.isEmpty(attachment.getOperatorId())) {
            map.putString("operatorId", attachment.getOperatorId());
        }
        if (!TextUtils.isEmpty(attachment.getOperatorNick())) {
            map.putString("operatorNick", attachment.getOperatorNick());
        }
        if (!TextUtils.isEmpty(attachment.getNotificationExtension())) {
            map.putString("notificationExtension", attachment.getNotificationExtension());
        }

        // Ban vĩnh viễn và ban tạm là HAI trạng thái độc lập ở server. Không có 3 field này thì
        // nhánh GỠ ban không phân biệt được: cả hai loại khi gỡ đều cho `isMuted:false` giống hệt
        // nhau, store buộc phải xoá cả hai cờ [GAP-5]. Chỉ bơm cho attachment ban, không bơm cho
        // MEMBER_ENTER (subclass đó cũng có 3 getter này) — thêm field vào tin vào/ra đang chạy
        // thật là rủi ro regression không đổi lại được gì.
        if (attachment instanceof V2NIMChatroomChatBannedNotificationAttachment) {
            V2NIMChatroomChatBannedNotificationAttachment ban =
                    (V2NIMChatroomChatBannedNotificationAttachment) attachment;
            map.putBoolean("chatBanned", ban.isChatBanned());
            map.putBoolean("tempChatBanned", ban.isTempChatBanned());
            // GIÂY, không phải ms — khác `timestamp` của message (Android ms / iOS giây) nên
            // KHÔNG nhân chia 1000 ở đây. 0 = đã gỡ ban tạm.
            map.putDouble("tempChatBannedDuration", ban.getTempChatBannedDuration());
        }
    }

    private static void putStringArrayIfAny(WritableMap map, String key, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        WritableArray array = Arguments.createArray();
        for (String value : values) {
            array.pushString(value);
        }
        map.putArray(key, array);
    }

    /** Rút gọn `V2NIM_CHATROOM_MESSAGE_NOTIFICATION_TYPE_XXX` -> `XXX`, cùng lối với `roleName`. */
    private static String notificationTypeName(V2NIMChatroomMessageNotificationType type) {
        String name = type.name();
        String prefix = "V2NIM_CHATROOM_MESSAGE_NOTIFICATION_TYPE_";
        return name.startsWith(prefix) ? name.substring(prefix.length()) : name;
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

    /** SDK nhận null cho notificationExtension "không truyền"; JS gửi '' nên phải quy đổi. */
    private static String emptyToNull(String value) {
        return TextUtils.isEmpty(value) ? null : value;
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
