package com.netease.im.login;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.netease.im.IMApplication;
import com.netease.im.ReactCache;
import com.netease.im.session.SessionUtil;
import com.netease.im.team.TeamListService;
import com.netease.im.uikit.LoginSyncDataStatusObserver;
import com.netease.im.uikit.cache.DataCacheManager;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.V2NIMFailureCallback;
import com.netease.nimlib.sdk.v2.V2NIMSuccessCallback;
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService;
import com.netease.nimlib.sdk.v2.auth.V2NIMTokenProvider;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginAuthType;
import com.netease.nimlib.sdk.v2.auth.option.V2NIMLoginOption;
// IMTRACE probe (diagnostic) — V2 login listeners
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginDetailListener;
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginListener;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMConnectStatus;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncLevel;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientChange;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus;
import com.netease.nimlib.sdk.v2.auth.model.V2NIMKickedOfflineDetail;
import com.netease.nimlib.sdk.v2.auth.model.V2NIMLoginClient;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

/**
 * Created by dowin on 2017/4/28.
 */

public class LoginService {


    final static String TAG = "LoginService";
    // 自己的用户帐号
    private String account;
    private String token;
    private AbortableFuture<LoginInfo> loginInfoFuture;

    private LoginService() {

    }

    static class InstanceHolder {
        final static LoginService instance = new LoginService();
    }

    public static LoginService getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 设置当前登录用户的帐号
     *
     * @param account 帐号
     */
    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public LoginInfo getLoginInfo(Context context) {
        LoginInfo info = new LoginInfo(account, token);
        return info;
    }

    void initLogin(LoginInfo loginInfo) {

    }

    public void autoLogin() {
        login(getLoginInfo(null), null);
    }

    public void login(final LoginInfo loginInfoP, final RequestCallback<LoginInfo> callback) {
        intentionalLogout = false; // bắt đầu login → mọi LOGOUT sau đây coi là phiên rớt cần recover
        // === V10 login (V2NIMLoginService) — cần cho NERTC CallKit V2 signalling (fix 191001 misuse) ===
        final String accountId = loginInfoP.getAccount();
        final String tokenP = loginInfoP.getToken();
        V2NIMLoginOption option = new V2NIMLoginOption();
        // Giữ dynamic-token như V9 (authType=1 / iOS NIMSDKAuthTypeDynamicToken). App có sẵn token string
        // từ JS → bọc vào tokenProvider trả đúng string đó (SDK gọi lại khi reconnect), không đổi backend.
        option.setAuthType(V2NIMLoginAuthType.V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN);
        // V10 default false: xung đột đa端 fail 417 thay vì kick session cũ (V9 luôn kick).
        option.setForceMode(true);
        // Default là FULL → mỗi lần login lại sync cả TEAM_MEMBER + SUPER_TEAM_MEMBER, kéo dài
        // thời gian tới lúc conversation list sẵn sàng khi app resume từ background. BASIC chỉ sync
        // dữ liệu chính (gồm conversation); team member vẫn được app load riêng khi cần.
        option.setSyncLevel(V2NIMDataSyncLevel.V2NIM_DATA_SYNC_TYPE_LEVEL_BASIC);
        option.setTokenProvider(new V2NIMTokenProvider() {
            @Override
            public String getToken(String account) {
                return tokenP;
            }
        });
        NIMClient.getService(V2NIMLoginService.class).login(accountId, tokenP, option,
                new V2NIMSuccessCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.e("IMTRACE", "V2 login onSuccess acc=" + accountId);
                        account = accountId;
                        token = tokenP;
                        initLogin(loginInfoP);
                        if (callback != null) {
                            // V2 success không trả LoginInfo; dùng loginInfoP (đủ getAccount() cho caller).
                            callback.onSuccess(loginInfoP);
                        }
                        registerObserver(true);
                        startLogin();
                    }
                },
                new V2NIMFailureCallback() {
                    @Override
                    public void onFailure(V2NIMError error) {
                        Log.e("IMTRACE", "V2 login onFailure code=" + (error == null ? -1 : error.getCode()) + " desc=" + (error == null ? "" : error.getDesc()));
                        if (callback != null) {
                            callback.onFailed(error == null ? -1 : error.getCode());
                        }
                        registerObserver(true);
                    }
                });

        // === V9 login (rollback) ===
        // loginInfoFuture = NIMClient.getService(AuthService.class).login(loginInfoP);
        // loginInfoFuture.setCallback(new RequestCallback<LoginInfo>() {
        //     @Override public void onSuccess(LoginInfo loginInfo) {
        //         account = loginInfo.getAccount(); token = loginInfoP.getToken(); initLogin(loginInfo);
        //         if (callback != null) callback.onSuccess(loginInfo);
        //         registerObserver(true); startLogin(); loginInfoFuture = null;
        //     }
        //     @Override public void onFailed(int code) {
        //         if (callback != null) callback.onFailed(code);
        //         registerObserver(true); loginInfoFuture = null;
        //     }
        //     @Override public void onException(Throwable exception) {
        //         if (callback != null) callback.onException(exception);
        //         registerObserver(false); loginInfoFuture = null;
        //     }
        // });
    }


    private void startLogin() {
        new AsyncTask<Object, Object, Object>() {

            @Override
            protected Object doInBackground(Object[] params) {
                Log.e("IMTRACE", "startLogin begin → buildDataCache + queryRecentContacts");
                DataCacheManager.buildDataCacheAsync();
                SysMessageObserver.getInstance().loadMessages(false);
                queryRecentContacts();
                startSystemMsgUnreadCount();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void queryRecentContacts() {
        recentContactObserver.queryRecentContacts();
    }

    volatile boolean hasRegister;
    // Phân biệt logout chủ động (user bấm) vs phiên rớt (token hết hạn / lỗi) để KHÔNG re-login nhầm
    // sau khi user logout. Set true trong logout(), reset false khi login().
    private volatile boolean intentionalLogout = false;

    // Emit observeOnlineStatus ra JS (shared ConnectStatusIMStore map: "14"=connected→resync,
    // "3"=connecting, "10"=reconnect→fetch token mới+login, "7"=kick).
    private void emitOnlineStatus(String code) {
        WritableMap r = Arguments.createMap();
        r.putString("status", code);
        ReactCache.emit(ReactCache.observeOnlineStatus, r);
    }

    RecentContactObserver recentContactObserver = RecentContactObserver.getInstance();
//    SysMessageObserver sysMessageObserver = new SysMessageObserver();

    // ===== IMTRACE probe (diagnostic) — V2 login listeners =====
    // Mục đích: (1) xem V2 data-sync fire khi nào; (2) khi COMPLETED thử re-query recentContacts
    // → nếu ra data thì đây chính là hook fix Stage 2 (thay observeLoginSyncDataStatus V9 đã chết dưới login V10).
    private final V2NIMLoginDetailListener v2DetailListener = new V2NIMLoginDetailListener() {
        @Override
        public void onConnectStatus(V2NIMConnectStatus status) { }
        @Override
        public void onDisconnected(V2NIMError error) { }
        @Override
        public void onConnectFailed(V2NIMError error) { }
        @Override
        public void onDataSync(V2NIMDataSyncType type, V2NIMDataSyncState state, V2NIMError error) {
            Log.e("IMTRACE", "[RESUME_TRACE] onDataSync type=" + type + " state=" + state
                    + " t=" + System.currentTimeMillis());
            // Chỉ nghe TYPE_MAIN: conversation nằm trong nhóm dữ liệu chính. TEAM_MEMBER/
            // SUPER_TEAM_MEMBER xong sau và không đổi recent list → query lại ở đó là thừa.
            if (state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED
                    && type == V2NIMDataSyncType.V2NIM_DATA_SYNC_MAIN) {
                Log.e("IMTRACE", "V2 dataSync MAIN COMPLETED → re-query recentContacts");
                recentContactObserver.queryRecentContacts();
            }
        }
    };
    private final V2NIMLoginListener v2LoginListener = new V2NIMLoginListener() {
        @Override
        public void onLoginStatus(V2NIMLoginStatus status) {
            Log.e("IMTRACE", "V2 onLoginStatus=" + status + " intentionalLogout=" + intentionalLogout);
            // Dưới login V2, observeOnlineStatus V1 (userStatusObserver) không đáng tin → nguồn
            // trạng thái kết nối/phiên chính là listener V2 này, emit code cho JS xử lý recovery.
            if (status == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
                emitOnlineStatus("14");
            } else if (status == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING
                    || status == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_UNLOGIN) {
                emitOnlineStatus("3");
            } else if (status == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT) {
                // Phiên terminated. Nếu KHÔNG phải logout chủ động: tokenProvider cache token cũ nên
                // SDK không tự re-login được → nhờ JS fetch token mới + login lại.
                if (!intentionalLogout) {
                    emitOnlineStatus("10");
                }
            }
        }
        @Override
        public void onLoginFailed(V2NIMError error) {
            Log.e("IMTRACE", "V2 onLoginFailed code=" + (error == null ? -1 : error.getCode()));
            // Auto-login thất bại (thường do token hết hạn) → nhờ JS re-login token mới.
            if (!intentionalLogout) {
                emitOnlineStatus("10");
            }
        }
        @Override
        public void onKickedOffline(V2NIMKickedOfflineDetail detail) {
            Log.e("IMTRACE", "V2 onKickedOffline reason=" + (detail == null ? null : detail.getReason()));
            emitOnlineStatus("7");
        }
        @Override
        public void onLoginClientChanged(V2NIMLoginClientChange change, java.util.List<V2NIMLoginClient> clients) { }
    };

    synchronized void registerObserver(boolean register) {
        if (hasRegister && register) {
            return;
        }
        hasRegister = register;

        recentContactObserver.registerRecentContactObserver(register);
        // IMTRACE probe: đăng ký/gỡ V2 login listeners (diagnostic)
        if (register) {
            NIMClient.getService(V2NIMLoginService.class).addLoginListener(v2LoginListener);
            NIMClient.getService(V2NIMLoginService.class).addLoginDetailListener(v2DetailListener);
        } else {
            NIMClient.getService(V2NIMLoginService.class).removeLoginListener(v2LoginListener);
            NIMClient.getService(V2NIMLoginService.class).removeLoginDetailListener(v2DetailListener);
        }
//        sysMessageObserver.registerSystemObserver(register);
//        NIMClient.getService(SystemMessageObserver.class).observeReceiveSystemMsg(systemMessageObserver, register);
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(notificationObserver, register);
        SysMessageObserver.getInstance().register(register);
    }

    private NotificationManager notificationManager;
    private Observer<CustomNotification> notificationObserver = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {

            SessionUtil.receiver(getNotificationManager(), customNotification);
        }
    };

    public NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) IMApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private Observer<SystemMessage> systemMessageObserver = new Observer<SystemMessage>() {
        @Override
        public void onEvent(SystemMessage systemMessage) {
            if (systemMessage.getType() == SystemMessageType.AddFriend) {
                AddFriendNotify attachData = (AddFriendNotify) systemMessage.getAttachObject();
                if (attachData != null && attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {//TODO

                }
            }
        }
    };

    public boolean deleteRecentContact(String rContactId) {
        return recentContactObserver.deleteRecentContact(rContactId);
    }

    public void logout() {
        intentionalLogout = true; // logout chủ động → onLoginStatus(LOGOUT) KHÔNG trigger re-login

        // V10 logout (V2NIMLoginService). No-op callback để tránh NPE nếu SDK không nhận null.
        NIMClient.getService(V2NIMLoginService.class).logout(
                new V2NIMSuccessCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) { }
                },
                new V2NIMFailureCallback() {
                    @Override
                    public void onFailure(V2NIMError error) { }
                });
        // V9 (rollback): NIMClient.getService(AuthService.class).logout();

        registerObserver(false);//取消登录注册
        TeamListService.getInstance().clear();//群清理
        // 清理缓存&注销监听&清除状态
        DataCacheManager.clearDataCache();
        account = null;
        token = null;
        LoginSyncDataStatusObserver.getInstance().reset();
    }

    public void startSystemMsgUnreadCount() {
        registerSystemMsgUnreadCount(true);
        int unread = NIMClient.getService(SystemMessageService.class).querySystemMessageUnreadCountBlock();
        ReactCache.emit(ReactCache.observeUnreadCountChange, Integer.toString(unread));
    }

    boolean hasRegisterSystemMsgUnreadCount;
    private Observer<Integer> sysMsgUnreadCountChangedObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer unreadCount) {
            int unread = unreadCount == null ? 0 : unreadCount;
            ReactCache.emit(ReactCache.observeUnreadCountChange, Integer.toString(unread));
        }
    };

    public void registerSystemMsgUnreadCount(boolean register) {
        if (hasRegisterSystemMsgUnreadCount && register) {
            return;
        }
        hasRegisterSystemMsgUnreadCount = register;
        NIMClient.getService(SystemMessageObserver.class).observeUnreadCountChange(sysMsgUnreadCountChangedObserver, register);
    }

}
