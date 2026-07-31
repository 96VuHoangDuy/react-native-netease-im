package com.netease.im;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

// call-ui 4.1.0 — package path xác nhận từ AAR (javap):
//  UI layer:  com.netease.yunxin.nertc.ui.*
//  Core p2p:  com.netease.yunxin.kit.call.p2p[.model|.param].*
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.model.NECallEndInfo;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegateAbs;
import com.netease.yunxin.kit.call.p2p.model.NECallInfo;
import com.netease.yunxin.kit.call.p2p.model.NECallInitRtcMode;
import com.netease.yunxin.kit.call.p2p.model.NECallPushConfig;
import com.netease.yunxin.kit.call.p2p.model.NECallType;
import com.netease.yunxin.kit.call.p2p.model.NEInviteInfo;
import com.netease.yunxin.kit.call.p2p.param.NEHangupParam;
import com.netease.yunxin.nertc.ui.CallKitNotificationConfig;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.CallKitUIOptions;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.base.SoundHelper;
import com.netease.yunxin.nertc.ui.base.UserInfoHelper;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.im.uikit.cache.NimUserInfoCache;

import org.json.JSONException;
import org.json.JSONObject;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

/**
 * Cầu nối NERTC Call Kit (voice call, Phase 3) — audio-only, prebuilt UI.
 *
 * Prebuilt UI: {@link CallKitUI} lo toàn bộ UI gọi/nhận (kể cả inbound — GIỮ BẬT theo quyết định owner).
 * JS chỉ trigger startVoiceCall/hangup + observe state qua event {@code observeCallState}.
 */
public class CallService {
    private static final String TAG = "CallService";
    private static boolean initialized = false;

    // App context (giữ để dọn foreground service của call-ui khi kết thúc cuộc gọi).
    private static Context appContext = null;

    // Foreground service của SDK call-ui phát notification "通话进行中...." (id 1026, ongoing).
    // Neo theo tên class trong AAR manifest — stopService không phụ thuộc notification id nên
    // an toàn kể cả khi id đổi ở version khác; cancel(id) chỉ là lớp phòng thủ thêm.
    private static final String[] CALL_FOREGROUND_SERVICE_CLASSES = {
            "com.netease.yunxin.nertc.ui.service.AudioCallForegroundService",
            "com.netease.yunxin.nertc.ui.service.VideoCallForegroundService",
    };
    private static final int CALL_FOREGROUND_NOTIFICATION_ID = 1026;

    // Channel chuông cho notification cuộc gọi đến do HỆ THỐNG vẽ từ offline push (app bị kill).
    // Không có channel_id trong pushPayload thì push rơi vào channel tin nhắn (importance DEFAULT)
    // → không chuông, không heads-up. v2: bump từ v1 vì sound của channel bất biến sau khi tạo.
    static final String CHANNEL_INCOMING_CALL = "cskh_incoming_call_v2";

    // Channel KHÔNG sound cho notification fallback của SDK khi process còn sống (thiếu overlay →
    // banner fail → generateNotificationAndNotify). Lúc đó SoundHelper đã phát caller_ring; nếu
    // notification cũng kêu sẽ thành 2 tiếng đè nhau.
    static final String CHANNEL_INCOMING_CALL_ALIVE = "cskh_incoming_call_alive_v2";

    // CSKH fix cứng: accid prefix "csr" → tên + avatar logo app cố định (outbound + inbound).
    private static final String CSR_ACCID_PREFIX = "csr";
    private static final String CS_CALL_NAME_DEFAULT = "中越之家客服";
    private static String sCsCallName = null;

    /** Tên hiển thị CSKH (đã localize từ JS). Native dùng khi accid có prefix "csr". */
    public static void setCustomerServiceCallName(String name) {
        sCsCallName = TextUtils.isEmpty(name) ? null : name;
    }

    /** Package-private để {@link CsAudioOnTheCallFragment} chỉ áp UI tuỳ biến cho cuộc gọi CSKH. */
    static boolean isCsrAccid(String accid) {
        return accid != null && accid.startsWith(CSR_ACCID_PREFIX);
    }

    private static String csCallName() {
        return TextUtils.isEmpty(sCsCallName) ? CS_CALL_NAME_DEFAULT : sCsCallName;
    }

    private static String csAvatarUri(Context context) {
        return "android.resource://" + context.getPackageName() + "/" + R.drawable.cs_call_logo;
    }

    /** Init CallKit UI. Gọi 1 lần, sau IM init (V10). Idempotent. */
    public static void init(Context context) {
        if (initialized) {
            return;
        }
        appContext = context.getApplicationContext();
        try {
            ensureIncomingCallChannel(appContext);
            String appKey = readAppKey(context);
            if (TextUtils.isEmpty(appKey)) {
                Log.e(TAG, "appKey rỗng (metadata com.netease.nim.appKey) — bỏ qua init CallKit");
                return;
            }
            CallKitUIOptions options =
                    new CallKitUIOptions.Builder()
                            .rtcAppKey(appKey)
                            .timeOutMillisecond(30 * 1000L)
                            .resumeBGInvitation(true)
                            .rtcSdkOption(new NERtcOption())
                            // IN_NEED: init RTC theo nhu cầu (tránh xung đột init RTC với component khác)
                            .initRtcMode(NECallInitRtcMode.IN_NEED)
                            // Activity call p2p audio tuỳ biến: màn IN-CALL tách background khỏi
                            // avatar (giữ logo "xanh nền trắng" ở ô nhỏ, nền tối riêng để chữ tên đọc rõ).
                            .p2pAudioActivity(CsP2PCallFragmentActivity.class)
                            // Tên + avatar cho call UI (caller "Calling…" + banner callee). Mặc định SDK
                            // chỉ đọc cache NIM local → cold cache hiện accid. Lấy từ NIM profile (app đã
                            // đẩy nick/avatar lúc connect), fetch remote nếu cache miss.
                            .userInfoHelper(userInfoHelper())
                            // Notification fallback khi thiếu overlay (banner fail): mặc định SDK in
                            // raw accid + dùng channel có sound → đè lên chuông SoundHelper. Thay bằng
                            // text cố định + channel không sound.
                            .notificationConfigFetcher(invitedInfo -> new CallKitNotificationConfig(
                                    IMApplication.getNotify_msg_drawable_id(),
                                    CHANNEL_INCOMING_CALL_ALIVE,
                                    "Cuộc gọi đến",
                                    "Bạn có cuộc gọi thoại đến"))
                            .soundHelper(
                                    new SoundHelper() {
                                        @Override
                                        protected Integer soundResources(
                                                AVChatSoundPlayer.RingerTypeEnum type) {
                                            // CONNECTING = nhạc chờ bên gọi, RING = chuông bên nhận.
                                            // Khách yêu cầu cả hai dùng chung một bản nhạc. Các loại
                                            // khác (reject/busy/noResponse) giữ mặc định SDK.
                                            if (type == AVChatSoundPlayer.RingerTypeEnum.CONNECTING
                                                    || type == AVChatSoundPlayer.RingerTypeEnum.RING) {
                                                return R.raw.caller_ring;
                                            }
                                            return super.soundResources(type);
                                        }
                                    })
                            .build();
            CallKitUI.init(context.getApplicationContext(), options);
            // 来电横幅: mặc định tắt. Bật -> cuộc gọi đến hiện banner thay màn gọi full-screen
            // (cần quyền SYSTEM_ALERT_WINDOW, xin ở JS qua requestOverlayPermission).
            // Runtime method — KHÔNG set qua CallKitUIOptions.Builder (field cùng tên là internal).
            CallKitUI.enableIncomingBanner(true);

            NECallEngine.sharedInstance().addCallDelegate(callDelegate);
            initialized = true;
            // DEBUG IMTRACE_CALL (gỡ sau khi xong): xác nhận userInfoHelper đã wire + banner bật.
            Log.d(TAG, "IMTRACE_CALL init: userInfoHelper wired, incomingBanner enabled, callDelegate added");
            Log.i(TAG, "CallKit init done");
        } catch (Throwable t) {
            // Không để lỗi CallKit làm sập init IM/chat (Phase 2 phải luôn chạy).
            Log.e(TAG, "init CallKit lỗi: " + t.getMessage(), t);
        }
    }

    /** Cung cấp tên + avatar cho call UI từ NIM profile; cache miss thì fetch remote rồi notify. */
    private static UserInfoHelper userInfoHelper() {
        return new UserInfoHelper() {
            @Override
            public boolean fetchNickname(String accId, Function1<? super String, Unit> notify) {
                // DEBUG IMTRACE_CALL (gỡ sau khi xong): xác nhận helper có được gọi + rẽ nhánh nào.
                Log.d(TAG, "IMTRACE_CALL fetchNickname accId=" + accId + " isCsr=" + isCsrAccid(accId));
                if (isCsrAccid(accId)) {
                    Log.d(TAG, "IMTRACE_CALL fetchNickname[CSR] name=" + csCallName());
                    notify.invoke(csCallName());
                    return true;
                }
                String name = NimUserInfoCache.getInstance().getUserName(accId); // name hoặc chính accId
                if (!TextUtils.isEmpty(name) && !name.equals(accId)) {
                    Log.d(TAG, "IMTRACE_CALL fetchNickname[cache] name=" + name);
                    notify.invoke(name);
                } else {
                    Log.d(TAG, "IMTRACE_CALL fetchNickname[non-CSR] cacheMiss -> remote fetch, accId=" + accId);
                    NimUserInfoCache.getInstance().getUserInfoFromRemote(accId,
                            new RequestCallbackWrapper<NimUserInfo>() {
                                @Override
                                public void onResult(int code, NimUserInfo r, Throwable t) {
                                    String resolved = r != null && !TextUtils.isEmpty(r.getName())
                                            ? r.getName() : accId;
                                    Log.d(TAG, "IMTRACE_CALL fetchNickname[remote] code=" + code
                                            + " rNull=" + (r == null) + " resolved=" + resolved);
                                    notify.invoke(resolved);
                                }
                            });
                }
                return true;
            }

            @Override
            public boolean fetchAvatar(Context context, String accId,
                    Function2<? super String, ? super Integer, Unit> notify) {
                // DEBUG IMTRACE_CALL (gỡ sau khi xong): xác nhận helper có được gọi + rẽ nhánh nào.
                Log.d(TAG, "IMTRACE_CALL fetchAvatar accId=" + accId + " isCsr=" + isCsrAccid(accId));
                if (isCsrAccid(accId)) {
                    Log.d(TAG, "IMTRACE_CALL fetchAvatar[CSR] uri=" + csAvatarUri(context));
                    notify.invoke(csAvatarUri(context), 0);
                    return true;
                }
                String avatar = NimUserInfoCache.getInstance().getAvatar(accId);
                if (!TextUtils.isEmpty(avatar)) {
                    Log.d(TAG, "IMTRACE_CALL fetchAvatar[cache] avatar=" + avatar);
                    notify.invoke(avatar, 0);
                } else {
                    Log.d(TAG, "IMTRACE_CALL fetchAvatar[non-CSR] cacheMiss -> remote fetch, accId=" + accId);
                    NimUserInfoCache.getInstance().getUserInfoFromRemote(accId,
                            new RequestCallbackWrapper<NimUserInfo>() {
                                @Override
                                public void onResult(int code, NimUserInfo r, Throwable t) {
                                    String resolved = r != null ? r.getAvatar() : "";
                                    Log.d(TAG, "IMTRACE_CALL fetchAvatar[remote] code=" + code
                                            + " rNull=" + (r == null) + " avatar=" + resolved);
                                    notify.invoke(resolved, 0);
                                }
                            });
                }
                return true;
            }
        };
    }

    /**
     * Tạo channel chuông cho notification cuộc gọi đến (hệ thống vẽ từ offline push khi app bị kill).
     * Sound = caller_ring.mp3 — đồng nhất với chuông SoundHelper khi app còn sống. Idempotent.
     */
    private static void ensureIncomingCallChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_INCOMING_CALL, "Cuộc gọi đến", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Thông báo khi có cuộc gọi đến");
        // URI theo tên resource (không dùng id — id đổi theo build): cùng pattern channel tin nhắn raw/msg.
        Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/raw/caller_ring");
        channel.setSound(sound,
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[] {0, 1000, 1000});
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(channel);

        // Channel fallback khi process sống: HIGH để heads-up nhưng sound=null (SoundHelper lo chuông).
        NotificationChannel alive = new NotificationChannel(
                CHANNEL_INCOMING_CALL_ALIVE, "Thông báo cuộc gọi", NotificationManager.IMPORTANCE_HIGH);
        alive.setDescription("Thông báo khi có cuộc gọi đến lúc đang mở ứng dụng");
        alive.setSound(null, null);
        alive.enableVibration(true);
        alive.setVibrationPattern(new long[] {0, 1000, 1000});
        alive.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(alive);
    }

    /**
     * pushPayload cho offline push: điều phối server-side (KHÔNG phải raw FCM) để notification bên
     * callee rơi vào channel chuông. Vị trí channel_id từng vendor theo docs chính chủ
     * (docs/reference/netease-im/PUSH_PAYLOAD_CONFIG.official-raw.md): Xiaomi ở root, còn lại theo field
     * riêng. vivo/honor push nội địa không có field channel_id (máy vivo global đi FCM nên không ảnh hưởng).
     */
    private static String buildCallPushPayloadJson() {
        try {
            JSONObject payload = new JSONObject();
            payload.put("channel_id", CHANNEL_INCOMING_CALL); // Xiaomi
            payload.put("hwField", new JSONObject().put("channel_id", CHANNEL_INCOMING_CALL));
            payload.put("oppoField", new JSONObject().put("channel_id", CHANNEL_INCOMING_CALL));
            payload.put("fcmFieldV1", new JSONObject().put("message", new JSONObject()
                    .put("android", new JSONObject()
                            .put("notification", new JSONObject()
                                    .put("channel_id", CHANNEL_INCOMING_CALL)))));
            return payload.toString();
        } catch (JSONException e) {
            Log.e(TAG, "buildCallPushPayloadJson lỗi: " + e.getMessage());
            return null;
        }
    }

    /**
     * Phát cuộc gọi thoại 1-1 (audio-only) tới accid.
     * pushConfig: bắt buộc để máy callee bị KILL nhận offline push (không set = killed không nhận gì).
     * Chỉ có text (không avatar) — pushContent nên chứa tên người gọi để callee biết ai gọi.
     */
    public static void startVoiceCall(Activity activity, String accid, String pushTitle, String pushContent) {
        String title = TextUtils.isEmpty(pushTitle) ? "Cuộc gọi thoại đến" : pushTitle;
        String content = TextUtils.isEmpty(pushContent) ? "Cuộc gọi thoại đến" : pushContent;
        NECallPushConfig pushConfig =
                new NECallPushConfig(true, title, content, buildCallPushPayloadJson(), true);
        CallParam param =
                new CallParam.Builder()
                        .callType(NECallType.AUDIO)
                        .calledAccId(accid)
                        .pushConfig(pushConfig)
                        .build();
        CallKitUI.startSingleCall(activity, param);
    }

    /** Cúp cuộc gọi hiện tại (channelId=null → cúp cuộc đang diễn ra). */
    public static void hangup() {
        NECallEngine.sharedInstance().hangup(new NEHangupParam(null, null), null);
        forceStopCallForegroundService();
    }

    /**
     * Dọn cứng foreground service + notification "通话进行中...." của SDK call-ui.
     * SDK đôi khi không stopService (race guard channelId nội bộ) → notification kẹt.
     * Chỉ gọi khi cuộc gọi đã kết thúc (hangup/onCallEnd), KHÔNG gọi khi call còn active.
     */
    private static void forceStopCallForegroundService() {
        Context ctx = appContext;
        if (ctx == null) {
            return;
        }
        for (String cls : CALL_FOREGROUND_SERVICE_CLASSES) {
            try {
                Intent intent = new Intent();
                intent.setClassName(ctx.getPackageName(), cls);
                ctx.stopService(intent);
            } catch (Throwable t) {
                Log.e(TAG, "stopService " + cls + " lỗi: " + t.getMessage());
            }
        }
        try {
            NotificationManagerCompat.from(ctx).cancel(CALL_FOREGROUND_NOTIFICATION_ID);
        } catch (Throwable t) {
            Log.e(TAG, "cancel call notification lỗi: " + t.getMessage());
        }
    }

    private static final NECallEngineDelegateAbs callDelegate = new NECallEngineDelegateAbs() {
        @Override
        public void onReceiveInvited(NEInviteInfo info) {
            // DEBUG IMTRACE_CALL (gỡ sau khi xong): accId người gọi thật + có prefix "csr" không.
            Log.d(TAG, "IMTRACE_CALL onReceiveInvited callerAccId=" + info.callerAccId
                    + " isCsr=" + isCsrAccid(info.callerAccId)
                    + " extraInfo=" + info.extraInfo);
            // Inbound giữ mặc định (prebuilt UI tự hiện màn nhận). Chỉ forward state để JS biết.
            emitState("incoming");
        }

        @Override
        public void onCallConnected(NECallInfo info) {
            emitState("connected");
        }

        @Override
        public void onCallEnd(NECallEndInfo info) {
            emitState("ended");
            // Cover mọi kết thúc cuộc gọi (phía CSKH ngắt / hangup từ UI native prebuilt),
            // không chỉ hangup chủ động từ JS.
            forceStopCallForegroundService();
        }
    };

    private static void emitState(String state) {
        try {
            WritableMap map = Arguments.createMap();
            map.putString("state", state);
            ReactCache.emit("observeCallState", map);
        } catch (Throwable t) {
            Log.e(TAG, "emitState lỗi: " + t.getMessage());
        }
    }

    private static String readAppKey(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                Object v = ai.metaData.get("com.netease.nim.appKey");
                return v != null ? String.valueOf(v) : null;
            }
        } catch (Exception e) {
            Log.e(TAG, "readAppKey lỗi: " + e.getMessage());
        }
        return null;
    }
}
