
package com.netease.im;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.netease.im.common.ImageLoaderKit;
import com.netease.im.common.ResourceUtil;
import com.netease.im.contact.BlackListObserver;
import com.netease.im.contact.FriendListService;
import com.netease.im.contact.FriendObserver;
import com.netease.im.login.LoginService;
import com.netease.im.login.RecentContactObserver;
import com.netease.im.login.SysMessageObserver;
import com.netease.im.session.AudioMessageService;
import com.netease.im.session.AudioPlayService;
import com.netease.im.session.SessionService;
import com.netease.im.session.SessionUtil;
import com.netease.im.session.extension.CustomMessageChatBotAttachment;
import com.netease.im.team.TeamListService;
import com.netease.im.team.TeamObserver;
import com.netease.im.uikit.cache.NimUserInfoCache;
import com.netease.im.uikit.cache.SimpleCallback;
import com.netease.im.uikit.cache.TeamDataCache;
import com.netease.im.uikit.common.util.file.FileUtil;
import com.netease.im.uikit.common.util.log.LogUtil;
import com.netease.im.uikit.common.util.sys.NetworkUtil;
import com.netease.im.uikit.common.util.sys.TimeUtil;
import com.netease.im.uikit.contact.core.model.ContactDataList;
import com.netease.im.uikit.permission.MPermission;
import com.netease.im.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.im.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.im.uikit.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.im.uikit.session.helper.MessageHelper;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMPushSDK;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.FriendFieldEnum;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MsgSearchOption;
import com.netease.nimlib.sdk.msg.model.NIMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.SearchOrderEnum;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.nimlib.sdk.util.NIMUtil;

import java.io.File;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.netease.im.ReactCache.setLocalExtension;
import static com.netease.im.ReceiverMsgParser.getIntent;
import static com.netease.nimlib.sdk.NIMClient.getService;
import static com.netease.nimlib.sdk.NIMSDK.getMsgService;

import androidx.annotation.RawRes;


public class RNNeteaseImModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {

    final static int BASIC_PERMISSION_REQUEST_CODE = 100;
    private final static String TAG = "RNNeteaseIm";
    private final static String NAME = "RNNeteaseIm";
    private final ReactApplicationContext reactContext;
    private AudioMessageService audioMessageService;
    private AudioPlayService audioPlayService;
    FriendListService friendListService;
    FriendObserver friendObserver;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ContentObserver galleryObserver;

    public RNNeteaseImModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
        reactContext.addLifecycleEventListener(this);
        ReactCache.setReactContext(reactContext);
        audioMessageService = AudioMessageService.getInstance();
        audioPlayService = AudioPlayService.getInstance();
        friendListService = new FriendListService();
        friendObserver = new FriendObserver();
    }

    @Override
    public void initialize() {
        LogUtil.w(TAG, "initialize");
    }

    @Override
    public void onCatalystInstanceDestroy() {
        LogUtil.w(TAG, "onCatalystInstanceDestroy");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void startObserverMediaChange() {
        ContentResolver contentResolver = this.reactContext.getContentResolver();

        Handler handler = new Handler();
        galleryObserver = new GalleryObserver(handler, this.reactContext);

        // Register the content observer
        contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, galleryObserver);
    }

    // Unregister the content observer
    @ReactMethod
    public void stopObserverMediaChange() {
        if (galleryObserver != null) {
            this.reactContext.getContentResolver().unregisterContentObserver(galleryObserver);
        }
    }

    @ReactMethod
    public void init(Promise promise) {
        LogUtil.w(TAG, "init");
        promise.resolve("200");
    }

    @ReactMethod
    public void createNotificationBirthday(String sessionId, String sessionType, String memberContactId, String memberName,final  Promise promise) {
        if (memberContactId != null && memberName != null) {
            sessionService.createNotificationBirthday(sessionId,sessionType, memberContactId, memberName);
        } else {
            sessionService.createNotificationBirthday(sessionId, sessionType);
        }

        promise.resolve("success");
    }

    @ReactMethod
    public void replyMessage(ReadableMap params, final  Promise promise) {
        Map<String, Object> data = MapUtil.readableMaptoMap(params);

//        Boolean isSkipTipForStranger = (Boolean) data.get("isSkipTipForStranger");
//        Boolean isSkipFriendCheck = (Boolean) data.get("isSkipFriendCheck");

        List<String> uuids = new ArrayList<>();
        String messageId = (String) data.get("messageId");
        uuids.add(messageId);
        List<IMMessage> replymessage = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
        if (replymessage == null && replymessage.isEmpty()) {
            promise.reject("error");
        }

        IMMessage message = MessageBuilder.createTextMessage(sessionService.getSessionId(), sessionService.getSessionTypeEnum(), (String) data.get("content"));
        Map<String, Object> localExt = message.getLocalExtension();
        if (localExt == null) {
            localExt = new HashMap<String, Object>();
        }
        localExt.put("repliedId", messageId);
        message.setRemoteExtension(localExt);

        sessionService.appendPushConfig(message);
//
//        if (message.getSessionType() == SessionTypeEnum.P2P) {
//            Boolean isFriend = NIMClient.getService(FriendService.class).isMyFriend(message.getSessionId());
//            if (!isFriend && !isSkipFriendCheck && !isSkipTipForStranger) {
//                SessionService.getInstance().sendTipMessage("SEND_MESSAGE_FAILED_WIDTH_STRANGER", null, true, false);
//                promise.resolve("200");
//                return;
//            }
//        }

        NIMClient.getService(MsgService.class).replyMessage(message, replymessage.get(0), false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                //回复消息成功
                promise.resolve("200");
            }

            @Override
            public void onFailed(int code) {
                promise.reject("" + code, "");
                //回复消息失败，code为错误码
            }

            @Override
            public void onException(Throwable exception) {
                //回复消息产生异常
            }
        });
    }

    @ReactMethod
    public void setListCustomerServiceAndChatbot(ReadableMap data, final Promise promise) {
        CacheUsers.setListCustomerServiceAndChatbot(data);

        promise.resolve("success");
    }

    @ReactMethod
    public void updateMessageSentStickerBirthday(String sessionId, String sessionType, String messageId,  Promise promise) {
        sessionService.updateMessageSentStickerBirthday(sessionId, sessionType, messageId, new SessionService.OnMessageQueryListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                if (message == null) {
                    promise.reject("error");
                    return code;
                }

                promise.resolve("success");

                return code;
            }
        });
    }

    @ReactMethod
    public  void updateIsSeenMessage(Boolean isSeenMessage) {
        sessionService.updateIsSeenMessage(isSeenMessage);
    }

    /**
     * 登陆
     *
     * @param contactId
     * @param token
     * @param promise
     */
    @ReactMethod
    public void login(String contactId, String token, String appKey, final Promise promise) {
        LogUtil.w(TAG, "_id:" + contactId);
        LogUtil.w(TAG, "t:" + token);
//        LogUtil.w(TAG, "md5:" + MD5.getStringMD5(token));

        NIMClient.getService(AuthService.class).openLocalCache(contactId);
        LogUtil.w(TAG, "s:" + NIMClient.getStatus().name());
        LoginService.getInstance().login(new LoginInfo.LoginInfoBuilder(contactId, token, 1, "").withAppKey(appKey).build(), new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo loginInfo) {

                promise.resolve(loginInfo == null ? "" : loginInfo.getAccount());
            }

            @Override
            public void onFailed(int code) {
                String msg;
                if (code == 302 || code == 404) {
                    msg = ResourceUtil.getString(R.string.login_failed);
                } else {
                    msg = ResourceUtil.getString(R.string.login_erro) + code;
                }
                promise.reject(Integer.toString(code), msg);
            }

            @Override
            public void onException(Throwable throwable) {
                promise.reject(Integer.toString(ResponseCode.RES_EXCEPTION), ResourceUtil.getString(R.string.login_exception));

            }
        });
    }

    @ReactMethod
    public void autoLogin(String contactId, String token, String appKey, final Promise promise) {
        LogUtil.w(TAG, "_id:" + contactId);
        LogUtil.w(TAG, "t:" + token);
//        LogUtil.w(TAG, "md5:" + MD5.getStringMD5(token));


        NIMClient.getService(AuthService.class).openLocalCache(contactId);
        LogUtil.w(TAG, "s:" + NIMClient.getStatus().name());
        LoginService.getInstance().login(new LoginInfo.LoginInfoBuilder(contactId, token, 1, "").withAppKey(appKey).build(), new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo loginInfo) {

                promise.resolve(loginInfo == null ? "" : loginInfo.getAccount());
            }

            @Override
            public void onFailed(int code) {
                String msg;
                if (code == 302 || code == 404) {
                    msg = ResourceUtil.getString(R.string.login_failed);
                } else {
                    msg = ResourceUtil.getString(R.string.login_erro) + code;
                }
                promise.reject(Integer.toString(code), msg);
            }

            @Override
            public void onException(Throwable throwable) {
                promise.reject(Integer.toString(ResponseCode.RES_EXCEPTION), ResourceUtil.getString(R.string.login_exception));

            }
        });
    }

    /**
     * 退出
     */
    @ReactMethod
    public void logout() {
        LogUtil.w(TAG, "logout");
        status = "";
        LoginService.getInstance().logout();

    }

    /**********Friend 好友**************/


    /**
     * 进入好友
     *
     * @param promise
     */
    @ReactMethod
    public void startFriendList(final Promise promise) {
        LogUtil.w(TAG, "startFriendList");
        friendObserver.startFriendList();

    }

    /**
     * 退出好友
     *
     * @param promise
     */
    @ReactMethod
    public void stopFriendList(final Promise promise) {
        LogUtil.w(TAG, "stopFriendList");
        friendObserver.stopFriendList();
    }

    /**
     * 获取本地用户资料
     *
     * @param contactId
     * @param promise
     */
    @ReactMethod
    public void getUserInfo(String contactId, final Promise promise) {
        LogUtil.w(TAG, "getUserInfo" + contactId);
        NimUserInfo userInfo = NimUserInfoCache.getInstance().getUserInfo(contactId);
        promise.resolve(ReactCache.createUserInfo(userInfo));
    }

    /**
     * 获取服务器用户资料
     *
     * @param contactId
     * @param promise
     */
    @ReactMethod
    public void fetchUserInfo(String contactId, final Promise promise) {
        LogUtil.w(TAG, "fetchUserInfo" + contactId);
        NimUserInfoCache.getInstance().getUserInfoFromRemote(contactId, new RequestCallbackWrapper<NimUserInfo>() {
            @Override
            public void onResult(int i, NimUserInfo userInfo, Throwable throwable) {
                promise.resolve(ReactCache.createUserInfo(userInfo));
            }
        });
    }


    /**
     * 添加好友
     *
     * @param contactId
     * @param verifyType 1 直接添加
     * @param msg        备注
     * @param promise
     */
    @ReactMethod
    public void addFriendWithType(final String contactId, String verifyType, String msg, final Promise promise) {
        VerifyType verifyTypeAdd = VerifyType.VERIFY_REQUEST;
        if ("1".equals(verifyType)) {
            verifyTypeAdd = VerifyType.DIRECT_ADD;
        }
        LogUtil.w(TAG, "addFriend" + contactId);
        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(contactId, verifyTypeAdd, msg))
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void aVoid, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            String name = NimUserInfoCache.getInstance().getUserName(LoginService.getInstance().getAccount());
                            SessionUtil.sendAddFriendNotification(contactId, name + " 请求加为好友");
                            promise.resolve("" + code);
                        } else {
                            promise.reject("" + code, "");
                        }
                    }
                });
    }

    /**
     * 添加好友
     *
     * @param contactId
     * @param msg       备注
     * @param promise
     */
    @ReactMethod
    public void addFriend(final String contactId, String msg, final Promise promise) {
        LogUtil.w(TAG, "addFriend" + contactId);
        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(contactId, VerifyType.VERIFY_REQUEST, msg))
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void aVoid, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            String name = NimUserInfoCache.getInstance().getUserName(LoginService.getInstance().getAccount());
                            SessionUtil.sendAddFriendNotification(contactId, name + " 请求加为好友");
                            promise.resolve("" + code);
                        } else {
                            promise.reject("" + code, "");
                        }
                    }
                });
    }

    /**
     * 删除好友
     *
     * @param contactId
     * @param promise
     */
    @ReactMethod
    public void deleteFriend(String contactId, final Promise promise) {
        LogUtil.w(TAG, "deleteFriend" + contactId);
        NIMClient.getService(FriendService.class).deleteFriend(contactId)
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void aVoid, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            promise.resolve("" + code);
                        } else {
                            promise.reject("" + code, "");
                        }
                    }
                });
        SysMessageObserver sysMessageObserver = SysMessageObserver.getInstance();
        sysMessageObserver.loadMessages(false);
        sysMessageObserver.deleteSystemMessageById(contactId, false);
    }

    /*************Black 黑名单***********/

    BlackListObserver blackListObserver = new BlackListObserver();

    /**
     * 进入黑名单列表
     *
     * @param promise
     */
    @ReactMethod
    public void startBlackList(final Promise promise) {
        blackListObserver.startBlackList();
    }

    /**
     * 退出黑名单列表
     *
     * @param promise
     */
    @ReactMethod
    public void stopBlackList(final Promise promise) {
        blackListObserver.stopBlackList();
    }

    /**
     * 获取黑名单列表
     *
     * @param promise
     */
    @ReactMethod
    public void getBlackList(final Promise promise) {
        final List<String> accounts = NIMClient.getService(FriendService.class).getBlackList();
        List<String> unknownAccounts = new ArrayList<>();
        final List<UserInfo> data = new ArrayList<>();
        for (String contactId : accounts) {
            if (!NimUserInfoCache.getInstance().hasUser(contactId)) {
                unknownAccounts.add(contactId);
            } else {
                data.add(NimUserInfoCache.getInstance().getUserInfo(contactId));
            }
        }

        if (!unknownAccounts.isEmpty()) {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(unknownAccounts, new RequestCallbackWrapper<List<NimUserInfo>>() {
                @Override
                public void onResult(int code, List<NimUserInfo> users, Throwable exception) {
                    if (code == ResponseCode.RES_SUCCESS) {
                        data.addAll(users);
                    }
                    promise.resolve(ReactCache.createBlackList(data));
                }
            });
        } else {
            promise.resolve(ReactCache.createBlackList(data));
        }
    }

    /**
     * 加入黑名单
     *
     * @param contactId
     * @param promise
     */
    @ReactMethod
    public void addToBlackList(String contactId, final Promise promise) {
        blackListObserver.addToBlackList(contactId, new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void aVoid, Throwable throwable) {
                if (code == ResponseCode.RES_SUCCESS) {
                    promise.resolve("" + code);
                } else {
                    promise.reject("" + code, "");
                }
            }
        });
    }

    /**
     * 移出黑名单
     *
     * @param contactId
     * @param promise
     */
    @ReactMethod
    public void removeFromBlackList(String contactId, final Promise promise) {
        blackListObserver.removeFromBlackList(contactId, new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void aVoid, Throwable throwable) {
                if (code == ResponseCode.RES_SUCCESS) {
                    promise.resolve("" + code);
                } else {
                    promise.reject("" + code, "");
                }
            }
        });
    }

    /************Team 群组************/

    TeamObserver teamObserver = new TeamObserver();

    /**
     * 进入群组列表
     *
     * @param promise
     */
    @ReactMethod
    public void startTeamList(Promise promise) {
        teamObserver.startTeamList();
    }

    /**
     * 退出群组列表
     *
     * @param promise
     */
    @ReactMethod
    public void stopTeamList(Promise promise) {
        teamObserver.stopTeamList();
    }


    /**
     * 获取本地群资料
     *
     * @param teamId
     * @param promise
     */
    @ReactMethod
    public void getTeamInfo(String teamId, Promise promise) {

        Team team = TeamDataCache.getInstance().getTeamById(teamId);
        promise.resolve(ReactCache.createTeamInfo(team));
    }

    @ReactMethod
    public void getOwnedGroupCount(final Promise promise) {
        NIMClient.getService(TeamService.class).queryTeamList().setCallback(new RequestCallbackWrapper<List<Team>>() {
            @Override
            public void onResult(int code, List<Team> result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && result != null && !result.isEmpty()) {
                    int ownedGroupCount = 0;
                    for(Team team : result) {
                      if (LoginService.getInstance().getAccount().equals(team.getCreator())) {
                          ownedGroupCount++;
                      }
                    }

                    promise.resolve(ownedGroupCount);
                    return;
                }

                promise.reject("error", "getOwnedGroupCount error code " + code);
            }
        });
    }

    @ReactMethod
    public void queryAllTeams(final  Promise promise) {
        NIMClient.getService(TeamService.class).queryTeamList().setCallback(new RequestCallbackWrapper<List<Team>>() {
            @Override
            public void onResult(int code, List<Team> result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && result != null && !result.isEmpty()) {
                    WritableArray arr = Arguments.createArray();
                    int ownedGroupCount = 0;
                    for(Team team : result) {
                        WritableMap teamInfo = Arguments.createMap();

                        teamInfo.putString("teamId", team.getId());
                        teamInfo.putString("name", team.getName());
                        teamInfo.putString("avatar", team.getIcon());
                        teamInfo.putString("avatarLocal", ImageLoaderKit.getMemoryCachedAvatar(team.getIcon()));
                        teamInfo.putString("type", Integer.toString(team.getType().getValue()));
                        teamInfo.putString("introduce", team.getIntroduce());
                        teamInfo.putString("createTime", TimeUtil.getTimeShowString(team.getCreateTime(), true));
                        teamInfo.putString("creator", team.getCreator());
                        teamInfo.putString("mute", ReactCache.getMessageNotifyType(team.getMessageNotifyType()));
                        teamInfo.putString("memberCount", Integer.toString(team.getMemberCount()));
                        teamInfo.putString("memberLimit", Integer.toString(team.getMemberLimit()));

                        boolean isOwner = LoginService.getInstance().getAccount().equals(team.getCreator());
                        teamInfo.putBoolean("isOwner", isOwner);
                        if (isOwner) {
                            ownedGroupCount++;
                        }

                        arr.pushMap(teamInfo);
                    }

WritableMap _result = Arguments.createMap();
                    _result.putInt("ownedGroupCount", ownedGroupCount);
                    _result.putArray("teams", arr);

                    promise.resolve(_result);
                    return;
                }

                WritableArray arr = Arguments.createArray();
                promise.resolve(arr);
            }
        });
    }

    @ReactMethod
    public void queryTeamByName(String name, final Promise promise) {
        NIMClient.getService(TeamService.class).searchTeamsByKeyword(name).setCallback(new RequestCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> result) {
                WritableArray arr = Arguments.createArray();

                for(Team team : result) {
                    WritableMap teamInfo = Arguments.createMap();

                    teamInfo.putString("teamId", team.getId());
                    teamInfo.putString("name", team.getName());
                    teamInfo.putString("avatar", team.getIcon());
                    teamInfo.putString("avatarLocal", ImageLoaderKit.getMemoryCachedAvatar(team.getIcon()));
                    teamInfo.putString("type", Integer.toString(team.getType().getValue()));
                    teamInfo.putString("introduce", team.getIntroduce());
                    teamInfo.putString("createTime", TimeUtil.getTimeShowString(team.getCreateTime(), true));
                    teamInfo.putString("creator", team.getCreator());
                    teamInfo.putString("mute", ReactCache.getMessageNotifyType(team.getMessageNotifyType()));
                    teamInfo.putString("memberCount", Integer.toString(team.getMemberCount()));
                    teamInfo.putString("memberLimit", Integer.toString(team.getMemberLimit()));

                    arr.pushMap(teamInfo);
                }

                promise.resolve(arr);
            }

            @Override
            public void onFailed(int code) {
                promise.reject("error", "" + code);
            }

            @Override
            public void onException(Throwable exception) {
                promise.reject("error", exception.getMessage());
            }
        });
    }

    /**
     * 开启/关闭消息提醒 好友
     *
     * @param contactId
     * @param mute
     * @param promise
     */
    @ReactMethod
    public void setMessageNotify(String contactId, String mute, final Promise promise) {
        NIMClient.getService(FriendService.class).setMessageNotify(contactId, string2Boolean(mute))
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void aVoid, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            RecentContactObserver.getInstance().refreshMessages(true);
                            promise.resolve("" + code);
                        } else {
                            promise.reject("" + code, "");
                        }
                    }
                });
    }

    /**
     * 开启/关闭消息提醒 群组
     *
     * @param teamId
     * @param mute
     * @param promise
     */
    @ReactMethod
    public void setTeamNotify(String teamId, String mute, final Promise promise) {

        TeamMessageNotifyTypeEnum typeEnum = TeamMessageNotifyTypeEnum.All;
        if ("0".equals(mute)) {
            typeEnum = TeamMessageNotifyTypeEnum.Mute;
        } else if ("1".equals(mute)) {
            typeEnum = TeamMessageNotifyTypeEnum.All;
        } else if ("2".equals(mute)) {
            typeEnum = TeamMessageNotifyTypeEnum.Manager;
        }
        NIMClient.getService(TeamService.class).muteTeam(teamId, typeEnum)//!string2Boolean(mute)
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void aVoid, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            RecentContactObserver.getInstance().refreshMessages(true);
                            promise.resolve("" + code);
                        } else {
                            promise.reject("" + code, "");
                        }
                    }
                });
    }

    /**
     * 群成员禁言
     *
     * @param teamId
     * @param contactId
     * @param mute
     * @param promise
     */
    @ReactMethod
    public void setTeamMemberMute(String teamId, String contactId, String mute, final Promise promise) {

        NIMClient.getService(TeamService.class).muteTeamMember(teamId, contactId, string2Boolean(mute))
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void aVoid, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            promise.resolve("" + code);
                        } else {
                            promise.reject("" + code, "");
                        }
                    }
                });
    }

    /**
     * add manager to Team
     *
     * @param teamId
     * @param userIds
     * @param promise
     */
    @ReactMethod
    public void addManagersToTeam(String teamId, ReadableArray userIds, final Promise promise) {
        ArrayList<String> strList = (ArrayList<String>) (ArrayList<?>) (userIds.toArrayList());

        NIMClient.getService(TeamService.class)
                .addManagers(teamId, strList)
                .setCallback(new RequestCallback<List<TeamMember>>() {
                    @Override
                    public void onSuccess(List<TeamMember> managers) {
                        promise.resolve("" + teamId);
                    }

                    @Override
                    public void onFailed(int code) {
                        promise.reject("" + code, "");
                    }

                    @Override
                    public void onException(Throwable exception) {
                    }
                });
    }

    /**
     * remove manager to Team
     *
     * @param teamId
     * @param userIds
     * @param promise
     */
    @ReactMethod
    public void removeManagersFromTeam(String teamId, ReadableArray userIds, final Promise promise) {
        ArrayList<String> strList = (ArrayList<String>) (ArrayList<?>) (userIds.toArrayList());

        NIMClient.getService(TeamService.class)
                .removeManagers(teamId, strList)
                .setCallback(new RequestCallback<List<TeamMember>>() {
                    @Override
                    public void onSuccess(List<TeamMember> managers) {
                        promise.resolve("" + teamId);
                    }

                    @Override
                    public void onFailed(int code) {
                        promise.reject("" + code, "");
                    }

                    @Override
                    public void onException(Throwable exception) {
                        // 错误
                    }
                });
    }

    /**
     * 获取本地群成员资料
     *
     * @param teamId
     * @param promise
     */
    @ReactMethod
    public void getTeamMemberList(String teamId, Promise promise) {

        List<TeamMember> teamMemberList = TeamDataCache.getInstance().getTeamMemberList(teamId);
        promise.resolve(ReactCache.createTeamMemberList(teamMemberList));
    }

    /**
     * 获取服务器群资料
     *
     * @param teamId
     * @param promise
     */
    @ReactMethod
    public void fetchTeamInfo(String teamId, final Promise promise) {
        TeamDataCache.getInstance().fetchTeamById(teamId, new SimpleCallback<Team>() {
            @Override
            public void onResult(boolean success, Team team) {
                if (success && team != null) {
                    promise.resolve(ReactCache.createTeamInfo(team));
                } else {
                    promise.reject("-1", "");
                }
            }
        });
    }

    /**
     * 获取服务器群成员资料
     *
     * @param teamId
     * @param promise
     */
    @ReactMethod
    public void fetchTeamMemberList(String teamId, final Promise promise) {
        TeamDataCache.getInstance().fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> result) {
                if (success && result != null) {
                    promise.resolve(ReactCache.createTeamMemberList(result));
                } else {
                    promise.reject("-1", "");
                }
            }
        });
    }

    /**
     * 更新群成员名片
     *
     * @param teamId
     * @param contactId
     * @param nick
     * @param promise
     */
    @ReactMethod
    public void updateMemberNick(String teamId, String contactId, String nick, final Promise promise) {
        NIMClient.getService(TeamService.class).updateMyTeamNick(teamId, nick)
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void aVoid, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            promise.resolve("" + code);
                        } else {
                            promise.reject("-1", "");
                        }
                    }
                });

    }

    /**
     * 获取群成员资料及设置
     *
     * @param teamId
     * @param contactId
     * @param promise
     */
    @ReactMethod
    public void fetchTeamMemberInfo(String teamId, String contactId, final Promise promise) {
        TeamMember teamMember = TeamDataCache.getInstance().getTeamMember(teamId, contactId);
        if (teamMember != null) {
            promise.resolve(ReactCache.createTeamMemberInfo(teamMember));
        } else {
            // 请求群成员
            TeamDataCache.getInstance().fetchTeamMember(teamId, contactId, new SimpleCallback<TeamMember>() {
                @Override
                public void onResult(boolean success, TeamMember member) {
                    if (success && member != null) {
                        promise.resolve(ReactCache.createTeamMemberInfo(member));
                    } else {
                        promise.reject("-1", "");
                    }
                }
            });
        }
    }


    /**
     * 创建群组
     * verifyType 验证类型 0 允许任何人加入 1 需要身份验证2 不允许任何人申请加入
     * inviteMode 邀请他人类型 0管理员邀请 1所有人邀请
     * beInviteMode 被邀请人权限 0需要验证 1不需要验证
     * teamUpdateMode 群资料修改权限 0管理员修改 1所有人修改
     *
     * @param fields
     * @param type
     * @param accounts
     * @param promise
     */
    @ReactMethod
    public void createTeam(ReadableMap fields, String type, ReadableArray accounts, final Promise promise) {
        LogUtil.w(TAG, fields + "\n" + type + "\n" + accounts);
        TeamTypeEnum teamTypeEnum = TeamTypeEnum.Advanced;
        try {
            teamTypeEnum = TeamTypeEnum.typeOfValue(Integer.parseInt(type));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        HashMap<TeamFieldEnum, Serializable> fieldsMap = new HashMap<TeamFieldEnum, Serializable>();
        String teamName = teamTypeEnum == TeamTypeEnum.Normal ? "讨论组" : "高级群";
        if (fields != null) {
            if (fields.hasKey("name")) {
                teamName = fields.getString("name");
            }
            if (teamTypeEnum == TeamTypeEnum.Advanced) {
                if (fields.hasKey("introduce"))
                    fieldsMap.put(TeamFieldEnum.Introduce, fields.getString("introduce"));
                VerifyTypeEnum verifyTypeEnum = VerifyTypeEnum.Free;
                if (fields.hasKey("verifyType")) {//验证类型 0 允许任何人加入 1 需要身份验证2 不允许任何人申请加入
                    try {
                        verifyTypeEnum = VerifyTypeEnum.typeOfValue(Integer.parseInt(fields.getString("verifyType")));

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                fieldsMap.put(TeamFieldEnum.VerifyType, verifyTypeEnum);

                TeamBeInviteModeEnum teamBeInviteModeEnum = TeamBeInviteModeEnum.NoAuth;
                if (fields.hasKey("beInviteMode")) {//被邀请人权限 0需要验证 1不需要验证
                    try {
                        teamBeInviteModeEnum = TeamBeInviteModeEnum.typeOfValue(Integer.parseInt(fields.getString("beInviteMode")));

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                fieldsMap.put(TeamFieldEnum.BeInviteMode, teamBeInviteModeEnum);

                TeamInviteModeEnum teamInviteModeEnum = TeamInviteModeEnum.All;
                if (fields.hasKey("inviteMode")) {//邀请他人类型 0管理员邀请 1所有人邀请
                    try {
                        teamInviteModeEnum = TeamInviteModeEnum.typeOfValue(Integer.parseInt(fields.getString("inviteMode")));

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                fieldsMap.put(TeamFieldEnum.InviteMode, teamInviteModeEnum);
                TeamUpdateModeEnum teamUpdateModeEnum = TeamUpdateModeEnum.All;
                if (fields.hasKey("teamUpdateMode")) {//邀请他人类型 0管理员邀请 1所有人邀请
                    try {
                        teamUpdateModeEnum = TeamUpdateModeEnum.typeOfValue(Integer.parseInt(fields.getString("teamUpdateMode")));

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                fieldsMap.put(TeamFieldEnum.TeamUpdateMode, teamUpdateModeEnum);
            }
        }
        fieldsMap.put(TeamFieldEnum.Name, teamName);
        final String finalTeamName = teamName;
        NIMClient.getService(TeamService.class).createTeam(fieldsMap, teamTypeEnum, "", array2ListString(accounts))
                .setCallback(new RequestCallbackWrapper<CreateTeamResult>() {
                    @Override
                    public void onResult(int code, CreateTeamResult createTeamResult, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {

                            Team team = createTeamResult.getTeam();
                            // MessageHelper.getInstance().onCreateTeamMessage(team);
                            WritableMap id = Arguments.createMap();
                            id.putString("teamId", team.getId());
                            promise.resolve(id);
                        } else if (code == 801) {
                            promise.reject("" + code, reactContext.getString(R.string.over_team_member_capacity, 200));
                        } else if (code == 806) {
                            promise.reject("" + code, reactContext.getString(R.string.over_team_capacity));
                        } else {
                            promise.reject("" + code, "创建" + finalTeamName + "失败");
                        }
                    }
                });
    }

    @ReactMethod
    public void upload(String file, final Promise promise) {
        if (TextUtils.isEmpty(file)) {
            return;
        }

        File f = new File(file);
        if (f == null) {
            return;
        }
        NIMClient.getService(NosService.class).upload(f, "image/jpeg").setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    promise.resolve(url);
                } else {
                    promise.reject("" + code, "" + url);
                }
            }
        });
    }

    /**
     * 更新群资料
     * verifyType 验证类型 0 允许任何人加入 1 需要身份验证2 不允许任何人申请加入
     * inviteMode 邀请他人类型 0管理员邀请 1所有人邀请
     * beInviteMode 被邀请人权限 0需要验证 1不需要验证
     * teamUpdateMode 群资料修改权限 0管理员修改 1所有人修改
     *
     * @param teamId
     * @param fieldType:name(群组名称) icon(头像) introduce(群组介绍) announcement(群组公告)
     *                             verifyType(验证类型) inviteMode(邀请他人类型) beInviteMode(被邀请人权限) teamUpdateMode(群资料修改权限)
     * @param value
     * @param promise
     */
    @ReactMethod
    public void updateTeam(String teamId, String fieldType, String value, final Promise promise) {

        if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(fieldType) || (!TextUtils.equals(fieldType, "introduce") && TextUtils.isEmpty(value))) {
            promise.reject("-1", "不能为空");
            return;
        }
        TeamFieldEnum teamFieldEnum = null;
        Serializable fieldValue = null;
        switch (fieldType) {
            case "name":
                teamFieldEnum = TeamFieldEnum.Name;
                fieldValue = value;
                break;
            case "icon":
                teamFieldEnum = TeamFieldEnum.ICON;
                fieldValue = value;
                break;
            case "introduce":
                teamFieldEnum = TeamFieldEnum.Introduce;
                fieldValue = value;
                break;
            case "announcement":
                teamFieldEnum = TeamFieldEnum.Announcement;
                fieldValue = value;
                break;
            case "verifyType":
                teamFieldEnum = TeamFieldEnum.VerifyType;
                try {
                    fieldValue = VerifyTypeEnum.typeOfValue(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
            case "inviteMode":
                teamFieldEnum = TeamFieldEnum.InviteMode;
                try {
                    fieldValue = TeamInviteModeEnum.typeOfValue(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
            case "beInviteMode":
                teamFieldEnum = TeamFieldEnum.BeInviteMode;
                try {
                    fieldValue = TeamBeInviteModeEnum.typeOfValue(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
            case "teamUpdateMode":
                teamFieldEnum = TeamFieldEnum.TeamUpdateMode;
                try {
                    fieldValue = TeamUpdateModeEnum.typeOfValue(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        if (teamFieldEnum == null || fieldValue == null) {
            promise.reject("-1", "类型错误");
            return;
        }
        NIMClient.getService(TeamService.class).updateTeam(teamId, teamFieldEnum, fieldValue).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void aVoid, Throwable throwable) {
                if (code == ResponseCode.RES_SUCCESS) {
                    promise.resolve("" + code);
                }
            }
        });
    }

    @ReactMethod
    public void updateTeamFields(String teamId, ReadableMap fields, final Promise promise) {
        Map<TeamFieldEnum, Serializable> fieldsMap = null;
        NIMClient.getService(TeamService.class).updateTeamFields(teamId, fieldsMap).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void aVoid, Throwable throwable) {

            }
        });
    }

    /**
     * 申请加入群组
     *
     * @param teamId
     * @param reason
     * @param promise
     */
    @ReactMethod
    public void applyJoinTeam(String teamId, String reason, final Promise promise) {
        NIMClient.getService(TeamService.class).applyJoinTeam(teamId, reason).setCallback(new RequestCallbackWrapper<Team>() {
            @Override
            public void onResult(int code, Team team, Throwable throwable) {
                if (code == ResponseCode.RES_SUCCESS) {
                    promise.resolve("" + code);
                } else {
                    promise.reject("" + code, "");
                }
            }
        });
    }

    /**
     * 解散群组
     *
     * @param teamIds
     * @param promise
     */
    @ReactMethod
    public void dismissTeams(ReadableArray teamIds, final Promise promise) {
        String[] teamIdArray = new String[teamIds.size()];
        for (int i = 0; i < teamIds.size(); i++) {
            teamIdArray[i] = teamIds.getString(i);
        }


        final int totalTeams = teamIdArray.length;
        final AtomicInteger completedCount = new AtomicInteger(0);
        final AtomicBoolean hasErrorOccurred = new AtomicBoolean(false);

        for(String teamId : teamIdArray){
            NIMClient.getService(TeamService.class).dismissTeam(teamId)
                    .setCallback(new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void aVoid, Throwable throwable) {
                            if(hasErrorOccurred.get()){
                                return;
                            }

                            if(code != ResponseCode.RES_SUCCESS){
                                hasErrorOccurred.set(true);
                                promise.reject("" + code, "");
                                return;
                            }

                            if(completedCount.incrementAndGet() == totalTeams){
                                promise.resolve("" + code);
                            }
                        }
                    });
        }
    }

    List<String> array2ListString(ReadableArray accounts) {
        List<String> memberAccounts = new ArrayList<>();
        if (accounts != null && accounts.size() > 0) {
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.getType(i) == ReadableType.String) {
                    String account = accounts.getString(i);
                    if (TextUtils.isEmpty(account)) {
                        continue;
                    }
                    memberAccounts.add(account);
                }
            }
        }
        return memberAccounts;
    }

    /**
     * 拉人入群
     *
     * @param teamId
     * @param accounts
     * @param promise
     */
    @ReactMethod
    public void addMembers(String teamId, ReadableArray accounts,String type,final Promise promise) {


        NIMClient.getService(TeamService.class).addMembersEx(teamId, array2ListString(accounts), "", type)
                .setCallback(new RequestCallbackWrapper<List<String>>() {
                    @Override
                    public void onResult(int code, List<String> strings, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            promise.resolve("" + code);
                        } else if (code == ResponseCode.RES_TEAM_INVITE_SUCCESS) {
                            promise.resolve("" + code);
                        } else {
                            promise.reject("" + code, "");
                        }
                    }
                });
    }

    /**
     * 踢人出群
     *
     * @param teamId
     * @param accounts
     * @param promise
     */
    @ReactMethod
    public void removeMember(String teamId, ReadableArray accounts, final Promise promise) {

        NIMClient.getService(TeamService.class).removeMembers(teamId, array2ListString(accounts))
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void aVoid, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            promise.resolve("" + code);
                        } else {
                            promise.reject("" + code, "");
                        }
                    }
                });
    }

    /**
     * 主动退群
     *
     * @param teamIds
     * @param promise
     */
    @ReactMethod
    public void quitTeams(ReadableArray teamIds, final Promise promise) {
        String[] teamIdArray = new String[teamIds.size()];
        for (int i = 0; i < teamIds.size(); i++) {
            teamIdArray[i] = teamIds.getString(i);
        }

        final int totalTeams = teamIdArray.length;
        final AtomicInteger completedCount = new AtomicInteger(0);
        final AtomicBoolean hasErrorOccurred = new AtomicBoolean(false);

        for (String teamId : teamIdArray){
            NIMClient.getService(TeamService.class).quitTeam(teamId)
                    .setCallback(new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void aVoid, Throwable throwable) {
                            if(hasErrorOccurred.get()){
                                return;
                            }

                            if(code != ResponseCode.RES_SUCCESS){
                                hasErrorOccurred.set(true);
                                promise.reject("" + code, "");
                                return;
                            }

                            if(completedCount.incrementAndGet() == totalTeams){
                                promise.resolve("" + code);
                            }
                        }
                    });
        }
    }

    boolean string2Boolean(String bool) {
        return TextUtils.isEmpty(bool) ? false : !"0".equals(bool);
    }

    /**
     * 转让群组
     *
     * @param teamId    群ID
     * @param contactId 新任拥有者的用户帐号
     * @param quit      转移时是否要同时退出该群
     * @param promise
     * @return InvocationFuture 可以设置回调函数，如果成功，视参数 quit 值：
     * quit为false：参数仅包含原拥有着和当前拥有者的(即操作者和 contactId)，权限已被更新。
     * quit为true: 参数为空。
     */
    @ReactMethod
    public void transferTeam(String teamId, String contactId, String quit, final Promise promise) {

        NIMClient.getService(TeamService.class).transferTeam(teamId, contactId, string2Boolean(quit))
                .setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
                    @Override
                    public void onResult(int code, List<TeamMember> teamMembers, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            promise.resolve("" + code);
                        } else {
                            promise.reject("" + code, "");
                        }
                    }
                });
    }

    @ReactMethod
    public void updateTeamAvatar(String teamId, String avatarUrl, final Promise promise) {
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.ICON, avatarUrl).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    promise.resolve("success");
                } else {
                    promise.reject("" + code, "");
                }
            }
        });
    }

    /**
     * 修改的群名称
     *
     * @param teamId
     * @param nick
     * @param promise
     */
    @ReactMethod
    public void updateTeamName(String teamId, String nick, final Promise promise) {
        NIMClient.getService(TeamService.class).updateName(teamId, nick)
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void aVoid, Throwable throwable) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            promise.resolve("" + code);
                        } else {
                            promise.reject("" + code, "");
                        }
                    }
                });
    }

    @ReactMethod
    public void cancelSendingMessage(String sessionId, String sessionType, String messageId, final Promise promise) {
        List<String> messageIds = new ArrayList<String>();
        messageIds.add(messageId);
        NIMClient.getService(MsgService.class).queryMessageListByUuid(messageIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS) {
                    promise.reject("CANCEL_SENDING_MESSAGE", "error");
                    return;
                }

                IMMessage message = result.get(0);

                NIMClient.getService(MsgService.class).cancelUploadAttachment(message);

                sessionService.deleteItem(message, true);

//                Log.e(TAG, "test message status =>>>>>>." + message.getStatus());
//
//                NIMClient.getService(MsgService.class).queryMessageListByUuid(messageIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
//                    @Override
//                    public void onResult(int c, List<IMMessage> r, Throwable exception) {
//                        if (c == ResponseCode.RES_SUCCESS) {
//                            IMMessage msg = r.get(0);
//
//                            if (msg.getStatus() == MsgStatusEnum.fail) {
//                                List<IMMessage> list = new ArrayList<>(1);
//                                list.add(msg);
//                                Object a = ReactCache.createMessageList(list);
//                                ReactCache.emit(ReactCache.observeMsgStatus, a);
//                            }
//                        }
//                    }
//                });

                promise.resolve("success");
            }
        });
    }

    @ReactMethod
    public void removeReactionMessage(String sessionId, String sessionType, String messageId, String accId, Boolean isSendMessage, final Promise promise) {
        sessionService.removeReactionMessage(sessionId, sessionType, messageId, accId, isSendMessage, promise);
    }

    @ReactMethod
    public void updateReactionMessage(ReadableMap params, final Promise promise) {
        Map<String, Object> _params = MapUtil.readableMaptoMap(params);
        Log.e(TAG, "updateReactionMessage => " + _params);
        if (_params == null) {
            promise.reject("error", "params is not null!");
            return;
        }
        sessionService.updateReactionMessage(_params, promise);
    }

    @ReactMethod
    public void reactionMessage(String sessionId, String sessionType, String messageId, ReadableMap reaction, final Promise promise) {
        sessionService.reactionMessage(sessionId,sessionType,messageId,reaction, promise);
    }

    @ReactMethod
    public void sendTextMessageWithSession(String content, String sessionId, String sessionType, String sessionName, Integer messageSubType ,final Promise promise) {
        try {

            sessionService.sendTextMessageWithSession(content, sessionId, sessionType, sessionName, messageSubType, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });
            promise.resolve("success");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send text message: " + e.getMessage());
        }
    }

    /*************Session send message 聊天***********/
    /***sessionId,   聊天对象的 ID，如果是单聊，为用户帐号，如果是群聊，为群组 ID***/
    /***   sessionType,   聊天类型，单聊或群组***/
    /**
     * 发送文本消息
     *
     * @param content   文本内容
     * @param atUserIds
     * @param promise
     */
    @ReactMethod
    public void sendTextMessage(String content, ReadableArray atUserIds, Integer messageSubType,boolean isSkipFriendCheck, Boolean isSkipTipForStranger, final Promise promise) {
       try {
           LogUtil.w(TAG, "sendTextMessage" + content);

           List<String> atUserIdList = array2ListString(atUserIds);
           sessionService.sendTextMessage(content, atUserIdList, messageSubType, isSkipFriendCheck,isSkipTipForStranger, new SessionService.OnSendMessageListener() {
               @Override
               public int onResult(int code, IMMessage message) {
//                promise.resolve(ReactCache.createMessage(message,null));
                   return 0;
               }
           });
           promise.resolve("success");
       } catch (Exception e) {
           promise.reject("SEND_ERROR", "Failed to send text message: " + e.getMessage());
       }
    }

    public void sendTextMessage(String content,Integer messageSubType, boolean isSkipFriendCheck, boolean isSkipTipForStranger,final Promise promise) {
       try {
           LogUtil.w(TAG, "sendTextMessage" + content);
           sessionService.sendTextMessage(content, null, messageSubType, isSkipFriendCheck,isSkipTipForStranger, new SessionService.OnSendMessageListener() {
               @Override
               public int onResult(int code, IMMessage message) {
//                promise.resolve(ReactCache.createMessage(message,null));
                   return 0;
               }
           });
           promise.resolve("success");
       } catch (Exception e) {
           promise.reject("SEND_ERROR", "Failed to send text message: " + e.getMessage());
       }
    }

    @ReactMethod
    public void sendGifMessageWithSession(String url,String aspectRatio, String sessionId, String typeStr, String sessionName, final Promise promise) {
        try {
            sessionService.sendGifMessageWithSession(url, aspectRatio, sessionId, typeStr, sessionName, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });
            promise.resolve("success");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send gif message: " + e.getMessage());
        }
    }

    @ReactMethod
    public void sendGifMessage(String url, String aspectRatio, ReadableArray atUserIds, boolean isSkipFriendCheck, boolean isSkipTipForStranger, final Promise promise) {
        try {
            sessionService.sendGifMessage(url, aspectRatio, null, isSkipFriendCheck,isSkipTipForStranger, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
//                promise.resolve(ReactCache.createMessage(message,null));
                    return 0;
                }
            });
            promise.resolve("success");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send gif message: " + e.getMessage());
        }
    }

    @ReactMethod
    public void sendImageMessageWithSession(String file, String fileName, String sessionId, String sessionType, String sessionName, final Promise promise) {
        try {
            sessionService.sendImageMessageWithSession(file, fileName, sessionId, sessionType, sessionName, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });
            promise.resolve("success");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send image message: " + e.getMessage());
        }
    }

    //2.发送图片消息
//    file, // 图片文件对象
//    displayName // 文件显示名字，如果第三方 APP 不关注，可以为 null
    @ReactMethod
    public void sendImageMessage(String file, String displayName, boolean isHighQuality,boolean isSkipFriendCheck, boolean isSkipTipForStranger,final Promise promise) {
        try {
            sessionService.sendImageMessage(file, displayName, isHighQuality,isSkipFriendCheck, isSkipTipForStranger,null, null, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });
            promise.resolve("success");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send image message: " + e.getMessage());
        }
    }

    @ReactMethod
    public void sendFileMessageWithSession(String filePath, String fileName, String fileType, String sessionId, String sessionType, String sessionName, final Promise promise) {
        try {
            sessionService.sendFileMessageWitSession(filePath, fileName, fileType, sessionId, sessionType, sessionName, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });
            promise.resolve("200");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send file message: " + e.getMessage());
        }
    }

    @ReactMethod
    public void sendFileMessage(String filePath, String fileName, String fileType, final Promise promise) {
        try {
            sessionService.sendFileMessage(filePath, fileName, fileType, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });
            promise.resolve("200");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send file message: " + e.getMessage());
        }
    }

    @ReactMethod
    public void sendMessageTeamNotificationRequestJoin(ReadableMap sourceId, ReadableArray targets, Integer type, final Promise promise) {
        try {
            sessionService.sendMessageTeamNotificationRequestJoin(sourceId, targets,type, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });
            promise.resolve("200");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send sendMessageTeamNotificationRequestJoin message: " + e.getMessage());
        }
    }

    //3.发送音频消息
//    file, // 音频文件
//    duration // 音频持续时间，单位是ms
    @ReactMethod
    public void sendAudioMessage(String file, String duration, boolean isSkipFriendCheck, boolean isSkipTipForStranger,final Promise promise) {
        long durationL = 0;
        try {
            durationL = Long.parseLong(duration);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            sessionService.sendAudioMessage(file, durationL, isSkipFriendCheck,isSkipTipForStranger, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });
            promise.resolve("success");
        } catch ( Exception e) {
            promise.reject("SEND_ERROR", "Failed to send audio message: " + e.getMessage());
        }
    }

    //4.发送视频消息
//    file, // 视频文件
//    duration, // 视频持续时间
//    width, // 视频宽度
//    height, // 视频高度
//    displayName // 视频显示名，可为空
    @ReactMethod
    public void sendVideoMessage(String file, String duration, int width, int height, String displayName, boolean isSkipFriendCheck,boolean isSkipTipForStranger, final Promise promise) {
        try {
            sessionService.sendVideoMessage(file, duration, width, height, displayName, isSkipFriendCheck,isSkipTipForStranger, null, null, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });
            promise.resolve("success");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send video message: " + e.getMessage());
        }
    }

    @ReactMethod
    public void sendVideoMessageWithSession(String file, String sessionId, String sessionType, String sessionName, final Promise promise) {
        try {
            sessionService.sendVideoMessageWithSession(file, sessionId, sessionType, sessionName, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });
            promise.resolve("success");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send video message: " + e.getMessage());
        }
    }

    @ReactMethod
    public void sendDefaultMessage(String type, String digst, String content, final Promise promise) {
        sessionService.sendDefaultMessage(type, digst, content, new SessionService.OnSendMessageListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                return 0;
            }
        });
    }

    @ReactMethod
    public void sendRedPacketOpenMessage(String sendId, String hasRedPacket, String serialNo, final Promise promise) {
        sessionService.sendRedPacketOpenMessage(sendId, LoginService.getInstance().getAccount(), hasRedPacket, serialNo, new SessionService.OnSendMessageListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                return 0;
            }
        });
    }

    @ReactMethod
    public void sendCardMessage(String toSessionType, String toSessionId, String name, String imgPath, String cardSessionId, String cardSessionType, final Promise promise) {
        try {
            sessionService.sendCardMessage(toSessionType, toSessionId, name, imgPath, cardSessionId, cardSessionType, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    return 0;
                }
            });

            promise.resolve("200");
        } catch (Exception e) {
            promise.reject("SEND_ERROR", "Failed to send card message: " + e.getMessage());
        }

    }

    //5.发送自定义消息
//    attachment, // 自定义消息附件
//    config // 自定义消息的参数配置选项
    @ReactMethod
    public void sendRedPacketMessage(String type, String comments, String serialNo, final Promise promise) {
        sessionService.sendRedPacketMessage(type, comments, serialNo, new SessionService.OnSendMessageListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                return 0;
            }
        });
    }

    //5.发送自定义消息
//    attachment, // 自定义消息附件
//    config // 自定义消息的参数配置选项
    @ReactMethod
    public void sendBankTransferMessage(String amount, String comments, String serialNo, final Promise promise) {
        sessionService.sendBankTransferMessage(amount, comments, serialNo, new SessionService.OnSendMessageListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                return 0;
            }
        });
    }

    //6.发送地理位置消息
//    latitude, // 纬度
//    longitude, // 经度
//    address // 地址信息描述
    @ReactMethod
    public void sendLocationMessage(String sessionId, String sessionType, String latitude, String longitude, String address, final Promise promise) {
        try {
            sessionService.sendLocationMessage(sessionId, sessionType, latitude, longitude, address, new SessionService.OnSendMessageListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    if (code == ResponseCode.RES_SUCCESS) {
                        promise.resolve(code);
                    } else {
                        promise.reject("", "" + code);
                    }

                    return 0;
                }
            });
            promise.resolve("success");
        } catch ( Exception e) {
            promise.reject("SEND_ERROR", "Failed to send location message: " + e.getMessage());
        }
    }

    //7.发送提醒消息
//    content   //提醒内容
    @ReactMethod
    public void sendTipMessage(String content, final Promise promise) {
        sessionService.sendTipMessage(content, new SessionService.OnSendMessageListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                return 0;
            }
        });
    }

    /**
     * 下载文件附件
     *
     * @param messageId
     * @param promise
     */
//    @ReactMethod
//    public void downloadAttachment(String messageId, final String isThumb, final Promise promise) {
//        sessionService.queryMessage(messageId, new SessionService.OnMessageQueryListener() {
//            @Override
//            public int onResult(int code, IMMessage message) {
//                if (message != null) {
//                    sessionService.downloadAttachment(message, string2Boolean(isThumb));
//                    promise.resolve("开始下载");
//                } else {
//                    promise.resolve("开始下载");
//                }
//                return 0;
//            }
//        });
//
//    }

    /**
     * 转发消息操作
     *
     * @param dataDict
     * @param sessionId
     * @param sessionType
     * @param content
     */
    @ReactMethod
    public void forwardMultipleTextMessage(ReadableMap dataDict, final String sessionId, final String sessionType, final String content) {
        sessionService.forwardMultipleTextMessage(dataDict, sessionId, sessionType, content, (code, message) -> {
            return code;
        });
    }

    @ReactMethod
    public void forwardMultiTextMessageToMultipleRecipients(ReadableMap params, final Promise promise) {
        Map<String, Object> data = MapUtil.readableMaptoMap(params);
        List<Map<String, Object>> recipients = (List<Map<String, Object>>) data.get("recipients");
        String messageText = (String) data.get("messageText");
        String content = (String) data.get("content");
        if (recipients == null || recipients.isEmpty()) {
            promise.reject("recipients is required", "error");
            return;
        }
        if (messageText == null || messageText.isEmpty()) {
            promise.reject("messageText is required", "error");
            return;
        }

        for(Map<String, Object> recipient : recipients) {
            String sessionId = (String) recipient.get("sessionId");
            String sessionType = (String) recipient.get("sessionType");
            Boolean isSkipFriendCheck = (Boolean) recipient.get("isSkipFriendCheck");
            Boolean isSkipTipForStranger = (Boolean) recipient.get("isSkipTipForStranger");
            if (sessionId == null || sessionType == null || sessionId.isEmpty() || sessionType.isEmpty()) {
                continue;
            }

            SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
            Log.e(TAG, "test =>>>>>>>>>>>> " + sessionId + " " + sessionType);

            new Thread(() -> sessionService.handleForwardMultiTextMessageToRecipient(sessionId, sessionTypeEnum, messageText, content,isSkipFriendCheck, isSkipTipForStranger)).start();
        }

        promise.resolve("200");
    }

    @ReactMethod
    public void forwardMessagesToMultipleRecipients(ReadableMap params, final Promise promise) {
        Map<String, Object> data = MapUtil.readableMaptoMap(params);
        List<Map<String, Object>> recipients = (List<Map<String, Object>>) data.get("recipients");
        List<String> messageIds = (List<String>) data.get("messageIds");
        String parentId = (String) data.get("parentId");
        Boolean isHaveMultiMedia = (Boolean) data.get("isHaveMultiMedia");
        String content = (String) data.get("content");
        if (recipients == null || recipients.isEmpty()) {
            promise.reject("recipients is required", "error");
            return;
        }
        if (messageIds == null || messageIds.isEmpty()) {
            promise.reject("messageIds is required", "error");
            return;
        }

        for(Map<String, Object> recipient : recipients) {
            String sessionId = (String) recipient.get("sessionId");
            String sessionType = (String) recipient.get("sessionType");
            Boolean isSkipFriendCheck = (Boolean) recipient.get("isSkipFriendCheck");
            Boolean isSkipTipForStranger = (Boolean) recipient.get("isSkipTipForStranger");
            if (sessionId == null || sessionType == null || sessionId.isEmpty() || sessionType.isEmpty()) {
                continue;
            }

            SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);

            new Thread(() -> sessionService.handleForwardMessageToRecipient(messageIds, sessionId, sessionTypeEnum, content, parentId, isHaveMultiMedia, isSkipFriendCheck, isSkipTipForStranger)).start();
        }



        promise.resolve("已发送");
    }

    /**
     * 转发消息操作
     *
     * @param messageIds
     * @param sessionId
     * @param sessionType
     * @param content
     * @param promise
     */
    @ReactMethod
    public void sendForwardMessage(ReadableArray messageIds, final String sessionId, final String sessionType, final String content, final String parentId, final boolean isHaveMultiMedia, final Promise promise) {

        ArrayList<String> msgIds = (ArrayList<String>) (ArrayList<?>) (messageIds.toArrayList());
        getMsgService().queryMessageListByUuid(msgIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> messageList, Throwable throwable) {

                if (messageList == null || messageList.isEmpty()) {
                    promise.reject("" + code, "");
                    return;
                }

                int result = sessionService.sendForwardMessage(messageList, sessionId, sessionType, content, parentId, isHaveMultiMedia, new SessionService.OnSendMessageListener() {
                    @Override
                    public int onResult(int code, IMMessage message) {
                        return 0;
                    }
                });
                if (result == 0) {
                    showTip("请选择消息");
                } else if (result == 1) {
                    showTip("该类型消息不支持转发");
                } else {
                    promise.resolve(ResponseCode.RES_SUCCESS + "");
                }
            }
        });
    }

    @ReactMethod
    public void sendMultiMediaMessage(ReadableArray listMedia, Boolean isSkipFriendCheck, Boolean isSkipTipForStranger,  final Promise promise) {
        sessionService.sendMultiMediaMessage(listMedia, isSkipFriendCheck, isSkipTipForStranger, promise);
    }

    /**
     * 消息撤回
     *
     * @param messageId
     * @param promise
     */
    @ReactMethod
    public void revokeMessage(String messageId, final Promise promise) {
        LogUtil.w(TAG, "revokeMessage" + messageId);
        sessionService.queryMessage(messageId, new SessionService.OnMessageQueryListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                sessionService.revokeMessage(message, new SessionService.OnSendMessageListener() {
                    @Override
                    public int onResult(int code, IMMessage message) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            promise.resolve("success");
                        } else if (code == ResponseCode.RES_OVERDUE) {
                            promise.reject("" + code, "expired");
                        } else {
                            promise.reject("" + code, "fail");
                        }
                        return 0;
                    }
                });
                return 0;
            }
        });

    }

    @ReactMethod
    public void sendCustomNotification(ReadableMap dataDict, String toSessionId,String toSessionType, final Promise promise) {
        sessionService.sendCustomNotification(dataDict, toSessionId, toSessionType, new SessionService.OnCustomNotificationListener() {
            @Override
            public int onResult(int code, CustomNotification customNotification) {
                if (code == ResponseCode.RES_SUCCESS) {
                    promise.resolve("success");
                }else {
                    promise.reject("" + code, "fail");
                }
                return 0;
            }
        });

    }

    @ReactMethod
    public void updateAudioMessagePlayStatus(String messageId, final Promise promise) {
        LogUtil.w(TAG, "updateAudioMessagePlayStatus" + messageId);
        sessionService.queryMessage(messageId, new SessionService.OnMessageQueryListener() {

            @Override
            public int onResult(int code, IMMessage message) {
                sessionService.updateMessage(message, MsgStatusEnum.read);
                return 0;
            }
        });
    }

    @ReactMethod
    public void removeMessage(String messageId, String sessionId, String sessionType, final  Promise promise) {
        List<String> messageIds = new ArrayList<String>();
        messageIds.add(messageId);
        NIMClient.getService(MsgService.class).queryMessageListByUuid(messageIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS) {
                    promise.reject("error: " + code, "error");
                    return;
                }

                if (result.isEmpty()) {
                    promise.resolve("200");
                    return;
                }

                IMMessage message = result.get(0);
                if (message == null) {
                    promise.resolve("200");
                    return;
                }

                NIMClient.getService(MsgService.class).deleteChattingHistory(message);
                promise.resolve("200u");
            }
        });
    }

    @ReactMethod
    public void downloadAttachment(String messageId, String sessionId, String sessionType) {
        sessionService.queryMessage(messageId, new SessionService.OnMessageQueryListener() {
                                @Override
            public int onResult(int code, IMMessage message) {
                if (message != null) {
                    ReactCache.DownloadCallback callback = new ReactCache.DownloadCallback() {
                        @Override
                        public void onSuccess(Void result) {
                            Map<String, Object> map = MapBuilder.newHashMap();
                            map.put("downloadStatus", "success");
                            setLocalExtension(message, map);
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
                    Map<String, Object> map = MapBuilder.newHashMap();
                    map.put("downloadStatus", "downloading");
                    setLocalExtension(message, map);

                    AbortableFuture future = getService(MsgService.class).downloadAttachment(message, false);
                    future.setCallback(callback);

//                    sessionService.downloadAttachment(message, string2Boolean(isThumb));
//                    promise.resolve("开始下载");
                } else {
//                    promise.resolve("开始下载");
                }
                return 0;
            }
        });
    }

    /**
     * 消息删除
     *
     * @param messageId
     * @param promise
     */
    @ReactMethod
    public void deleteMessage(String messageId, final Promise promise) {
        LogUtil.w(TAG, "deleteMessage" + messageId);
        sessionService.queryMessage(messageId, new SessionService.OnMessageQueryListener() {

            @Override
            public int onResult(int code, IMMessage message) {
                if (message != null) {
                    sessionService.deleteMessage(message, promise);
                } else {
                    showTip("请选择消息");
                }
                return 0;
            }
        });
    }

    /**
     * 清空聊天记录
     * 删除与某个聊天对象的全部消息记录
     *
     * @param promise
     */
    @ReactMethod
    public void clearMessage(String sessionId, String sessionType, final Promise promise) {
        try {
            SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
            NIMClient.getService(MsgService.class).clearChattingHistory(sessionId, sessionTypeEnum);
            NIMClient.getService(MsgService.class).clearServerHistory(sessionId, sessionTypeEnum, true, "");
            RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, sessionTypeEnum);
            if (recent != null) {
                Map<String, Object> localExt = recent.getExtension();
                if (localExt == null) {
                    localExt = new HashMap<>();
                }
                if (localExt.get("reactedUsers") != null) {
                    localExt.remove("reactedUsers");
                }

                recent.setExtension(localExt);
                NIMClient.getService(MsgService.class).updateRecent(recent);
            }
            promise.resolve("200");
        } catch (Exception e) {
            promise.reject("CLEAR_ERROR", "Failed to clear chat history: " + e.getMessage());
        }
    }

    /**
     * 更新用户资料
     *
     * @param newUserInfo
     * @param promise
     */
    @ReactMethod
    public void updateMyUserInfo(ReadableMap newUserInfo, final Promise promise) {
        String contactId = LoginService.getInstance().getAccount();
        NimUserInfoCache.getInstance().updateMyUserInfo(MapUtil.readableMaptoMap(newUserInfo), new RequestCallbackWrapper() {
            @Override
            public void onResult(int code, Object result, Throwable exception) {
                promise.resolve("200");
            }

            @Override
            public void onFailed(int code) {
                super.onFailed(code);
                promise.reject("" + code, "");
            }
        });
    }

    /**
     * 保存好友备注
     *
     * @param contactId
     * @param alias
     * @param promise
     */
    @ReactMethod
    public void updateUserInfo(String contactId, String alias, final Promise promise) {
        Map<FriendFieldEnum, Object> map = new HashMap<>();
        map.put(FriendFieldEnum.ALIAS, alias);
        NIMClient.getService(FriendService.class).updateFriendFields(contactId, map).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void aVoid, Throwable throwable) {
                if (code == ResponseCode.RES_SUCCESS) {
                    promise.resolve("" + code);
                } else {
                    promise.reject("" + code, "");
                }
            }
        });
    }

    /**
     * 重发消息到服务器
     *
     * @param messageId
     * @param promise
     */
    @ReactMethod
    public void resendMessage(String messageId, final Promise promise) {
        LogUtil.w(TAG, "resendMessage" + messageId);
        sessionService.queryMessage(messageId, new SessionService.OnMessageQueryListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                Map<String, Object> map = message.getLocalExtension();
                if (map != null) {
                    if (map.containsKey("resend")) {
                        return -1;
                    }
                }
                promise.resolve("200");
                sessionService.resendMessage(message);

                return 0;
            }
        });

    }

    /**
     * 删除最近会话
     *
     * @param rContactId
     * @param promise
     */
    @ReactMethod
    public void deleteRecentContact(String rContactId, Promise promise) {
        LogUtil.w(TAG, "deleteRecentContact" + rContactId);
        boolean result = LoginService.getInstance().deleteRecentContact(rContactId);
        if (result) {
            promise.resolve("" + ResponseCode.RES_SUCCESS);
        } else {
            promise.reject("-1", "");
        }
    }

    @ReactMethod
    public void removeSession(String sessionId, String sessionType, final Promise promise) {
        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        RecentContact recentContact = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, sessionTypeEnum);
        Log.e(TAG, "test =>>>>> remove session " + recentContact);
        NIMClient.getService(MsgService.class).deleteRecentContact(recentContact);
        NIMClient.getService(MsgService.class).clearChattingHistory(sessionId, sessionTypeEnum);
        promise.resolve("success");
    }

    @ReactMethod
    public void getRecentContactList(final Promise promise) {
        NIMClient.getService(MsgService.class).queryRecentContacts()
                .setCallback(new RequestCallbackWrapper<List<RecentContact>>() {

                    @Override
                    public void onResult(int code, List<RecentContact> recentContacts, Throwable throwable) {
                        if (recentContacts != null && recentContacts.size() > 0) {
                            promise.resolve(ReactCache.createRecentList(recentContacts, 0));
                        } else {
                            promise.reject("-1", "");
                        }
                    }
                });
    }

    @ReactMethod
    public void setStrangerRecentReplyed(String rContactId) {
        RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(rContactId, SessionTypeEnum.P2P);

        if (recent == null) return;

        Map<String, Object> extension = recent.getExtension();

        if (extension == null) {
            extension = new HashMap<String, Object>();
        }

        extension.put("isReplyStranger", true);

        recent.setExtension(extension);

        NIMSDK.getMsgService().updateRecent(recent);
    }

    @ReactMethod
    public void getTeamList(String keyword, final Promise promise) {
        TeamListService teamListService = TeamListService.getInstance();
        teamListService.setOnLoadListener(new TeamListService.OnLoadListener() {
            @Override
            public void updateData(ContactDataList datas) {
                promise.resolve(ReactCache.createTeamList(datas));
            }
        });
        teamListService.query(keyword);
    }

    @ReactMethod
    public void getFriendList(String keyword, final Promise promise) {

        friendListService.setOnLoadListener(new FriendListService.OnLoadListener() {
            @Override
            public void updateData(ContactDataList datas) {
                promise.resolve(ReactCache.createFriendSet(datas, true));
            }
        });
        friendListService.query(keyword);
    }

    /************************/

    SessionService sessionService = SessionService.getInstance();

    /**
     * 进入聊天会话
     *
     * @param sessionId
     * @param type
     * @param promise
     */
    @ReactMethod
    public void startSession(String sessionId, String type, final Promise promise) {
        LogUtil.w(TAG, "startSession" + sessionId);
        if (TextUtils.isEmpty(sessionId)) {

            return;
        }
        sessionService.startSession(handler, sessionId, type);
    }

    /**
     * 退出聊天会话
     *
     * @param promise
     */
    @ReactMethod
    public void stopSession(final Promise promise) {
        LogUtil.w(TAG, "stopSession");
        sessionService.stopSession();
    }

    @ReactMethod
    public void readAllMessageBySession(String sessionId, String type, final Promise promise) {
        SessionTypeEnum sessionType = SessionUtil.getSessionType(type);
        NIMClient.getService(MsgService.class).clearUnreadCount(sessionId, sessionType).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    promise.resolve("success");
                    return;
                }

                promise.reject("error", "");
            }
        });
    }

    /**
     * 查询聊天内容
     *
     * @param sessionId
     * @param sessionType
     * @param timeLong
     * @param direction   查询方向 old new 默认new
     * @param limit       查询结果的条数限制
     * @param asc         查询结果的排序规则，如果为 true，结果按照时间升级排列，如果为 false，按照时间降序排列
     * @param promise
     */
    @ReactMethod
    public void queryMessageListHistory(String sessionId, String sessionType, String
            timeLong, String direction, int limit, String asc, final Promise promise) {
        LogUtil.w(TAG, "queryMessageListHistory");
        long time = 0;
        try {
            time = Long.parseLong(timeLong);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        QueryDirectionEnum directionEnum = getQueryDirection(direction);
        IMMessage message = MessageBuilder.createEmptyMessage(sessionId, sessionTypeEnum, time);
        NIMClient.getService(MsgService.class).queryMessageListEx(message, directionEnum, limit, string2Boolean(asc))
                .setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
                    @Override
                    public void onResult(int code, List<IMMessage> result, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            if (result != null && result.size() > 0) {
                                Object a = ReactCache.createMessageList(result);
                                promise.resolve(a);
                                return;
                            }
                        }
                        promise.reject("" + code, "");
                    }
                });
    }

    @ReactMethod
    public void removeReactedUsers(String sessionId, String sessionType, final Promise promise) {
        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, sessionTypeEnum);
        if (recent == null || recent.getExtension() == null) {
            promise.resolve("200");
            return;
        }

        Map<String, Object> localExt = recent.getExtension();
        localExt.remove("reactedUsers");
        recent.setExtension(localExt);

        NIMClient.getService(MsgService.class).updateRecent(recent);
        promise.resolve("200");
    }

    @ReactMethod
    public void getMessageById(String sessionId, String sessionType, String messageId, final Promise promise) {
        List<String> fromIds = new ArrayList<String>();
        fromIds.add(messageId);

        getMsgService().queryMessageListByUuid(fromIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    if (result == null || result.isEmpty()) {
                        promise.reject("" + code, "not found");
                        return;
                    }

                    WritableArray messageArr = ReactCache.createMessageList(result);

                    ReadableMap msg = messageArr.getMap(0);
                    WritableMap message = Arguments.createMap();

                    ReadableMapKeySetIterator iterator = msg.keySetIterator();

                    while (iterator.hasNextKey()) {
                        String key = iterator.nextKey();

                        switch (msg.getType(key)) {
                            case Null:
                                message.putNull(key);
                                break;
                            case Boolean:
                                message.putBoolean(key, msg.getBoolean(key));
                                break;
                            case Number:
                                message.putDouble(key, msg.getDouble(key));
                                break;
                            case String:
                                message.putString(key, msg.getString(key));
                                break;
                            case Map:
                                message.putMap(key, msg.getMap(key));
                                break;
                            case Array:
                               message.putArray(key, msg.getArray(key));
                                break;
                            default:
                                break;
                        }
                    }

                    promise.resolve(message);
                    return;
                }

                promise.reject("" + code, "");
            }
        });
    }

    @ReactMethod
    public void deleteFiles(ReadableArray sessionIds, final Promise promise) {
        ArrayList<String> _sessionIds = (ArrayList<String>) (ArrayList<?>) (sessionIds.toArrayList());
    }

    @ReactMethod
    public void searchFileMessages(final Promise promise) {
        MsgSearchOption option = new MsgSearchOption();
        ArrayList<MsgTypeEnum> messageTypes = new ArrayList<MsgTypeEnum>();
        messageTypes.add(MsgTypeEnum.file);
        option.setSearchContent("");
        option.setMessageTypes(messageTypes);
        option.setOrder(SearchOrderEnum.DESC);

        NIMClient.getService(MsgService.class).searchAllMessage(option).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    if (result != null && result.size() > 0) {
                        WritableMap a = ReactCache.createMessageObjectList(result);

                        promise.resolve(a);
                        return;
                    }
                }
                promise.reject("" + code, "");
            }
        });
    }

    @ReactMethod
    public void searchTextMessages(String searchContent, final Promise promise) {
        MsgSearchOption option = new MsgSearchOption();
        ArrayList<MsgTypeEnum> messageTypes = new ArrayList<MsgTypeEnum>();
        messageTypes.add(MsgTypeEnum.text);
        List<Integer> messageSubTypes = new ArrayList<>();
        messageSubTypes.add(0);
        messageSubTypes.add(1);
        option.setSearchContent(searchContent);
        option.setMessageTypes(messageTypes);
        option.setOrder(SearchOrderEnum.DESC);
        option.setMessageSubTypes(messageSubTypes);

        NIMClient.getService(MsgService.class).searchAllMessage(option).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    if (result != null && result.size() > 0) {
                        WritableMap a = ReactCache.createMessageObjectList(result);

                        promise.resolve(a);
                        return;
                    } else {
                        WritableArray a = (WritableArray) Arguments.createArray();
                        promise.resolve(a);
                    }
                }
                promise.reject("" + code, "");
            }
        });
    }

    @ReactMethod
    public void searchMessages(String keyWords, final Promise promise) {
        MsgSearchOption option = new MsgSearchOption();
        option.setSearchContent(keyWords);
        option.setAllMessageTypes(true);

        NIMClient.getService(MsgService.class).searchAllMessage(option)
                .setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
                    @Override
                    public void onResult(int code, List<IMMessage> result, Throwable exception) {
                        if (code == ResponseCode.RES_SUCCESS) {
                            if (result != null && result.size() > 0) {
                                WritableMap a = ReactCache.createMessageObjectList(result);

                                promise.resolve(a);
                                return;
                            }
                        }
                        promise.reject("" + code, "");
                    }
                });
    }

    @ReactMethod
    public void searchMessagesinCurrentSession(String keyWords, String anchorId, int limit, ReadableArray messageTypes, int direction, ReadableArray messageSubTypes, Boolean isDisableDownloadMedia, final Promise promise) {
        ArrayList<String> _messageTypes = (ArrayList<String>) (ArrayList<?>) (messageTypes.toArrayList());

        MsgSearchOption option = new MsgSearchOption();
        option.setSearchContent(keyWords);
        option.setLimit(limit);
        option.setOrder(direction == 1 ? SearchOrderEnum.ASC : SearchOrderEnum.DESC);

        if (messageSubTypes != null && messageSubTypes.size() > 0) {
            List<Object> arrObject =  MapUtil.readableArrayToArray(messageSubTypes);
            List<Integer> _messageSubTypes = arrObject.stream()
                    .map(ob->(Double)ob)
                    .map(Double::intValue)
                    .collect(Collectors.toList());

            option.setMessageSubTypes(_messageSubTypes);
        }

        if (messageTypes != null && _messageTypes.size() > 0) {
            Map<String, MsgTypeEnum> mockUpKeys = new HashMap();
            mockUpKeys.put("text", MsgTypeEnum.text);
            mockUpKeys.put("voice", MsgTypeEnum.audio);
            mockUpKeys.put("file", MsgTypeEnum.file);
            mockUpKeys.put("image", MsgTypeEnum.image);
            mockUpKeys.put("video", MsgTypeEnum.video);
            mockUpKeys.put("notification", MsgTypeEnum.notification);
            mockUpKeys.put("custom", MsgTypeEnum.custom);
            mockUpKeys.put("tip", MsgTypeEnum.tip);

            ArrayList<MsgTypeEnum> arrayListMessageTypes = new ArrayList<>();


            for (int i = 0; i < _messageTypes.size(); i++) {
                String type = _messageTypes.get(i);
                arrayListMessageTypes.add(mockUpKeys.get(type));
            }

            option.setMessageTypes(arrayListMessageTypes);
        }

        if ( anchorId == null || anchorId.isEmpty()) {
            IMMessage anchor;
            anchor = MessageBuilder.createEmptyMessage(sessionService.getSessionId(), sessionService.getSessionTypeEnum(), 0);
            option.setStartTime(direction == 1 ? anchor.getTime() : 0);
            option.setEndTime(direction == 0 ? anchor.getTime() : 0);

            NIMClient.getService(MsgService.class).searchMessage(sessionService.getSessionTypeEnum(), sessionService.getSessionId(), option)
                    .setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
                        @Override
                        public void onResult(int code, List<IMMessage> result, Throwable exception) {
                            Log.e(TAG, "search message hihi => " + result + " " + code);
                            if (code == ResponseCode.RES_SUCCESS) {
                                if (result != null && result.size() > 0) {
                                    List<IMMessage> messages = result;
                                    if (direction == 0) {
                                        Collections.reverse(messages);
                                    }

                                    WritableMap messageObjectList = ReactCache.createMessageObjectList(result, isDisableDownloadMedia);
                                    promise.resolve(messageObjectList);
                                    return;
                                }

                                WritableMap _result = Arguments.createMap();
                                promise.resolve(_result);
                                return;
                            }

                            if (exception != null) {
                                Log.e(TAG, "searchMessage error " + exception.getMessage());
                            }
                            promise.reject("" + code, "");
                        }
                    });
        } else {
            sessionService.queryMessage(anchorId, new SessionService.OnMessageQueryListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    option.setStartTime(direction == 1 ? message.getTime() : 0);
                    option.setEndTime(direction == 0 ? message.getTime() : 0);

                    NIMClient.getService(MsgService.class).searchMessage(sessionService.getSessionTypeEnum(), sessionService.getSessionId(), option)
                            .setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
                                @Override
                                public void onResult(int code, List<IMMessage> result, Throwable exception) {
                                    if (code == ResponseCode.RES_SUCCESS) {
                                        if (result != null && result.size() > 0) {
                                            List<IMMessage> messages = result;
                                            if (direction == 0) {
                                                Collections.reverse(messages);
                                            }
                                     
                                            WritableMap messageObjectList = ReactCache.createMessageObjectList(result);
                                            promise.resolve(messageObjectList);
                                            return;
                                        }
                                    }
                                    promise.reject("" + code, "");
                                }
                            });
                    return code;
                }
            });
        }
    }

    @ReactMethod
    public void readAllMessageOnlineServiceByListSession(ReadableArray listSessionId) {
        ArrayList<String> _listSessionId = (ArrayList<String>) (ArrayList<?>) (listSessionId.toArrayList());
        SessionService.getInstance().readAllMessageOnlineServiceByListSession(_listSessionId);
    }

    private WritableMap updateLastUnreadMessage(String sessionId, String sessionType, Boolean isUpdate) {
        if (!isUpdate) return null;
        String _sessionId = sessionId;
        SessionTypeEnum _sessionType;
        if (sessionId == null || sessionId.equals("")) {
            _sessionId = sessionService.getSessionId();
        }
        if (sessionType == null || sessionType.equals("")) {
            SessionTypeEnum type = sessionService.getSessionTypeEnum();
            if (type == SessionTypeEnum.None) {
                _sessionType = SessionTypeEnum.P2P;
            } else {
                _sessionType = type;
            }
        } else {
            _sessionType = SessionUtil.getSessionType(sessionType);
        }

        RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(_sessionId, _sessionType);

        if (recent == null) return null;

        WritableMap result = Arguments.createMap();

        Integer unreadCount = recent.getUnreadCount();
        String lastMessageId = "";
        String latestMessageTime = "";
        String recentLastMessageId = recent.getRecentMessageId();
        String recentLatestMessageTime = Long.toString(recent.getTime());

        Map<String, Object> extension = recent.getExtension();

        if (extension == null) {
            extension = new HashMap<String, Object>();
        }

        if (extension.get("lastReadMessageId") != null) {
            lastMessageId = (String) extension.get("lastReadMessageId");
        }

        if (extension.get("latestMessageTime") != null) {
            latestMessageTime = (String) extension.get("latestMessageTime");
        }

        extension.put("lastReadMessageId", recentLastMessageId);
        extension.put("latestMessageTime", recentLatestMessageTime);

        recent.setExtension(extension);

        NIMSDK.getMsgService().updateRecent(recent);

        result.putInt("unreadCount", unreadCount);

        if (!lastMessageId.equals("")) {
            result.putString("lastMessageId", lastMessageId);
        }

        if (!latestMessageTime.equals("")) {
            result.putString("latestMessageTime", latestMessageTime);
        }

        return result;
    }
    
    @ReactMethod
    public void updateIsTransferMessage(String sessionId, String sessionType, String messageId, final Promise promise) {
        if (sessionId == null || sessionType == null || messageId == null) {
            promise.reject("error", "Session id or session type is not null!");
            return;
        }

        ArrayList<String> messageIds = new ArrayList<>();
        messageIds.add(messageId);
        NIMClient.getService(MsgService.class).queryMessageListByUuid(messageIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS || result == null || result.isEmpty()) {
                    promise.reject("updateIsTransferMessage", "error with code " + code);
                    return;
                }

                IMMessage message = result.get(0);
                Map<String, Object> localExt = message.getLocalExtension();
                if (localExt == null) {
                    localExt = new HashMap<>();
                }

                localExt.put("isTransferUpdated", true);

                message.setLocalExtension(localExt);
                NIMClient.getService(MsgService.class).updateIMMessage(message);

                promise.resolve("success");
            }
        });
    }

    @ReactMethod
    public void hasMultipleMessages(String sessionId, String sessionType, final Promise promise) {
        if (sessionId == null || sessionType == null) {
            promise.reject("error", "Session id or session type is not null!");
            return;
        }

        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        MsgSearchOption option = new MsgSearchOption();
        NIMClient.getService(MsgService.class).searchMessage(sessionTypeEnum, sessionId, option).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS || result == null || result.isEmpty()) {
                    promise.reject("-1", "get messages error");
                    return;
                }

                promise.resolve(result.size() >= 2);
            }
        });
    }

    /**
     * 获取最近聊天内容
     *
     * @param messageId 最旧的消息ID
     * @param limit     查询结果的条数限制
     * @param promise
     */
    @ReactMethod
    public void queryMessageListEx(String messageId, final
    int limit, int direction, String sessionId, String sessionType, final Promise promise) {
        Boolean isUpdate = messageId == null || messageId.isEmpty() || messageId.equals("");
        WritableMap data = updateLastUnreadMessage(sessionId, sessionType, isUpdate);
        MsgSearchOption option = new MsgSearchOption();
        option.setLimit(limit);
        option.setOrder(direction == 1 ? SearchOrderEnum.ASC : SearchOrderEnum.DESC);
        option.setAllMessageTypes(true);

        RequestCallbackWrapper callback = new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS) {
                    if (result != null && result.size() > 0) {
                        List<IMMessage> messages = result;
                        if (sessionService.getIsSeenMessage()) {
                            if (direction == 0) {
                                Collections.reverse(messages);
                                getMsgService().sendMessageReceipt(sessionId, messages.get(0));
                            } else {
                                getMsgService().sendMessageReceipt(sessionId, messages.get(messages.size() - 1));
                            }
                        }

                        WritableArray a = ReactCache.createMessageList(messages);

                        if (data != null && isUpdate) {
                            WritableMap map = Arguments.createMap();
                            map.putMap("data", data);
                            map.putArray("messages", a);
                            sessionService.sendMsgReceipt(result);
                            promise.resolve(map);
                            return;
                        }

                        promise.resolve(a);
                        return;
                    }
                }
                promise.reject("" + code, "");
            }
        };

        if (messageId.isEmpty() || messageId == null) {
            if (sessionId != null && sessionType != null) {
                SessionTypeEnum sessionTypeE = SessionUtil.getSessionType(sessionType);

                NIMClient.getService(MsgService.class).searchMessage(sessionTypeE, sessionId, option)
                        .setCallback(callback);
            } else {
                NIMClient.getService(MsgService.class).searchMessage(sessionService.getSessionTypeEnum(), sessionService.getSessionId(), option)
                        .setCallback(callback);
            }
        } else {
            sessionService.queryMessage(messageId, new SessionService.OnMessageQueryListener() {
                @Override
                public int onResult(int code, IMMessage message) {
                    if (message != null) {
                        option.setStartTime(direction == 1 ? message.getTime() : 0);
                        option.setEndTime(direction == 0 ? message.getTime() : 0);
                        Log.d("optionoption>>>", option.toString());
                        if (sessionId != null && sessionType != null) {
                            SessionTypeEnum sessionTypeE = SessionUtil.getSessionType(sessionType);

                            NIMClient.getService(MsgService.class).searchMessage(sessionTypeE, sessionId, option)
                                    .setCallback(callback);
                        } else {
                            NIMClient.getService(MsgService.class).searchMessage(sessionService.getSessionTypeEnum(), sessionService.getSessionId(), option)
                                    .setCallback(callback);
                        }
                    }

                    return code;
                }
            });
        }
    }

    private QueryDirectionEnum getQueryDirection(String direction) {
        QueryDirectionEnum directionEnum = QueryDirectionEnum.QUERY_NEW;
        if ("old".equals(direction)) {
            directionEnum = QueryDirectionEnum.QUERY_OLD;
        }
        return directionEnum;
    }

    private WritableMap updateLocalExt(IMMessage message, String chatBotType) {
        String key = "chatBotType";
        Map<String, Object> newLocalExt = MapBuilder.newHashMap();
        newLocalExt.put("key", "chatBotType");
        
        Map<String, Object> map = ReactCache.setLocalExtension(message, newLocalExt);
        String valueChatBotType = (String) map.get(key);

        if (valueChatBotType == null) return null;

        WritableMap result = Arguments.createMap();
        result.putString(key, chatBotType);

        return result;
    }

    @ReactMethod
    public void setCancelResendMessage(String messageId, String sessionId, String sessionType) {
        sessionService.queryMessage(messageId, new SessionService.OnMessageQueryListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                if (code == ResponseCode.RES_SUCCESS) {
                    Map<String, Object> newLocalExt = MapBuilder.newHashMap();
                    newLocalExt.put("isCancelResend", true);
                    ReactCache.setLocalExtension(message, newLocalExt);
                }

                return 0;
            }
        });
    }

    @ReactMethod
    public void updateMessageOfCsr(String messageId, String sessionId, final Promise promise) {
        sessionService.queryMessage(messageId, new SessionService.OnMessageQueryListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                if (message == null || code != ResponseCode.RES_SUCCESS) {
                    promise.reject("error", "message not found");
                    return 0;
                };

                Map<String, Object> localExt = message.getLocalExtension();
                if (localExt == null) {
                    localExt = new HashMap<String, Object>();
                }

                Boolean isMessageCsrUpdated = (Boolean) localExt.get("isMessageCsrUpdated");
                if (isMessageCsrUpdated != null) {
                    promise.resolve("success");
                    return 0;
                }

                localExt.put("isMessageCsrUpdated", true);

                message.setLocalExtension(localExt);

                NIMClient.getService(MsgService.class).updateRecentByMessage(message, false);

                return 0;
            }
        });
    }

    @ReactMethod
    public void updateMessageOfChatBot(String messageId, String sessionId, String chatBotType, ReadableMap chatBotInfo,final Promise promise) {
        sessionService.queryMessage(messageId, new SessionService.OnMessageQueryListener() {
            @Override
            public int onResult(int code, IMMessage message) {
                if (message == null || code != ResponseCode.RES_SUCCESS) {
                   promise.reject("error", "message not found");
                    return 0;
                };

                Map<String, Object> localExtension = message.getLocalExtension();
                if (localExtension == null) {
                    localExtension = new HashMap<String, Object>();
                }

                String chatBotTypeLocalExt = (String) localExtension.get("chatBotType");

                if (chatBotTypeLocalExt != null && !chatBotTypeLocalExt.isEmpty()) {
                    promise.resolve("success");
                    return 0;
                }

                localExtension.put("chatBotType", chatBotType);
                if (chatBotInfo != null) {
                    localExtension.put("chatBotInfo", MapUtil.readableMaptoMap(chatBotInfo));
                }

                message.setLocalExtension(localExtension);

                NIMClient.getService(MsgService.class).updateRecentByMessage(message, false);

                promise.resolve("success");
                return 0;
            }
        });
    }

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, getCurrentActivity(), BASIC_PERMISSIONS);
        MPermission.with(getCurrentActivity())
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        Toast.makeText(getCurrentActivity(), "授权成功", Toast.LENGTH_SHORT).show();
        MPermission.printMPermissionResult(false, getCurrentActivity(), BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        Toast.makeText(getCurrentActivity(), "未全部授权，部分功能可能无法正常运行！", Toast.LENGTH_SHORT).show();
        MPermission.printMPermissionResult(false, getCurrentActivity(), BASIC_PERMISSIONS);
    }

    /**
     * *****************************录音 播放 ******************************************
     **/

    @ReactMethod
    public void onTouchVoice(Promise promise) {
        requestBasicPermission();

    }

    @ReactMethod
    public void startAudioRecord(Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                getCurrentActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getCurrentActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                audioMessageService.startAudioRecord(reactContext);
            } else {
                requestBasicPermission();
            }
        } else {
            audioMessageService.startAudioRecord(reactContext);
        }
    }

    @ReactMethod
    public void endAudioRecord(Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                getCurrentActivity().getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
        audioMessageService.endAudioRecord(sessionService);


    }

    @ReactMethod
    public void cancelAudioRecord(Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                getCurrentActivity().getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
        audioMessageService.cancelAudioRecord();
    }

    /** *******************获取图片/视频 拍照/录像 获取定位 显示图片/视频 显示定位 ******/

    /**
     * 播放录音
     *
     * @param audioFile
     * @param promise
     */
    @ReactMethod
    public void play(String audioFile, Boolean isExternalSpeaker, Promise promise) {
        audioPlayService.play(handler, reactContext, audioFile, isExternalSpeaker);
    }

    @ReactMethod
    public void switchAudioOutputDevice(Boolean isExternalSpeaker) {
        audioPlayService.updateAudioStreamType(isExternalSpeaker ? AudioManager.STREAM_MUSIC : AudioManager.STREAM_VOICE_CALL);
    }

    @ReactMethod
    public void getIsPlayingRecord(Callback callBack) {
        Boolean isPlaying = audioPlayService.isPlayingAudio();

        callBack.invoke(isPlaying);
    }

    @ReactMethod
    public void playLocal(String resourceFile, String type, Promise promise) {

        Uri uri = Uri.parse(resourceFile);
        LogUtil.w(TAG, "scheme:" + uri.getScheme());
        String filePath = uri.getPath();
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
            if (filePath.indexOf(".") == -1) {
                filePath = filePath + "." + type;
            }
        }
        LogUtil.w(TAG, "path:" + filePath);
        audioPlayService.playAudio(handler, reactContext, AudioManager.STREAM_RING, uri.getScheme(), filePath);
    }

    /**
     * 停止播放录音
     *
     * @param promise
     */
    @ReactMethod
    public void stopPlay(Promise promise) {
        audioPlayService.stopPlay(handler, reactContext);
    }

    /**
     * *****************************systemMsg 系统通知******************************************
     **/

    SysMessageObserver sysMessageObserver = SysMessageObserver.getInstance();

    /**
     * 进入系统通知消息
     *
     * @param promise
     */
    @ReactMethod
    public void startSystemMsg(Promise promise) {
        sysMessageObserver = SysMessageObserver.getInstance();
        sysMessageObserver.startSystemMsg();
    }

    /**
     * 退出系统通知消息
     *
     * @param promise
     */
    @ReactMethod
    public void stopSystemMsg(Promise promise) {
        if (sysMessageObserver != null)
            sysMessageObserver.stopSystemMsg();

    }

    /**
     * 开始系统通知计数监听
     *
     * @param promise
     */
    @ReactMethod
    public void startSystemMsgUnreadCount(Promise promise) {
        LoginService.getInstance().startSystemMsgUnreadCount();
    }


    /**
     * 停止系统通知计数监听
     *
     * @param promise
     */
    @ReactMethod
    public void stopSystemMsgUnreadCount(Promise promise) {
        LoginService.getInstance().registerSystemMsgUnreadCount(false);
    }

    /**
     * 查询系统通知列表
     *
     * @param offset
     * @param limit
     * @param promise
     */
    @ReactMethod
    public void querySystemMessagesBlock(String offset, String limit,
                                         final Promise promise) {
        int offsetInt = 0;
        int limitInt = 10;
        try {
            limitInt = Integer.parseInt(limit);
            offsetInt = Integer.parseInt(offset);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        List<SystemMessage> systemMessageList = NIMClient.getService(SystemMessageService.class)
                .querySystemMessagesBlock(offsetInt, limitInt);
        promise.resolve(ReactCache.createSystemMsg(systemMessageList));
    }


    /**
     * 同意/拒绝群邀请(仅限高级群)
     *
     * @param messageId
     * @param targetId
     * @param fromAccount
     * @param pass        同意/拒绝
     * @param promise
     */
    @ReactMethod
    public void acceptInvite(String messageId, String targetId, String fromAccount, String pass, String timestamp, final Promise promise) {
        long messageIdLong = 0L;
        try {
            messageIdLong = Long.parseLong(messageId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (sysMessageObserver != null)
            sysMessageObserver.acceptInvite(messageIdLong, targetId, fromAccount, string2Boolean(pass), timestamp, new RequestCallbackWrapper<Void>() {
                @Override
                public void onResult(int code, Void aVoid, Throwable throwable) {
                    if (code == ResponseCode.RES_SUCCESS) {
                        promise.resolve("" + code);
                    } else {
                        promise.reject("" + code, "");
                    }
                }
            });
    }

    /**
     * 通过/拒绝申请(仅限高级群)
     *
     * @param messageId
     * @param targetId
     * @param fromAccount
     * @param pass        通过/拒绝
     * @param promise
     */
    @ReactMethod
    public void passApply(String messageId, String targetId, String fromAccount, String pass, String timestamp, final Promise promise) {
        long messageIdLong = 0L;
        try {
            messageIdLong = Long.parseLong(messageId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (sysMessageObserver != null)
            sysMessageObserver.passApply(messageIdLong, targetId, fromAccount, string2Boolean(pass), timestamp, new RequestCallbackWrapper<Void>() {
                @Override
                public void onResult(int code, Void aVoid, Throwable throwable) {
                    if (code == ResponseCode.RES_SUCCESS) {
                        promise.resolve("" + code);
                    } else {
                        promise.reject("" + code, "");
                    }
                }
            });
    }

    /**
     * 通过/拒绝对方好友请求
     *
     * @param contactId
     * @param pass
     * @param timestamp
     * @param promise
     */
    @ReactMethod
    public void ackAddFriendRequest(String messageId, final String contactId, String pass, String timestamp, final Promise promise) {
        LogUtil.w(TAG, "ackAddFriendRequest" + contactId);
        long messageIdLong = 0L;
        try {
            messageIdLong = Long.parseLong(messageId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        final boolean toPass = string2Boolean(pass);
        if (sysMessageObserver != null)
            sysMessageObserver.ackAddFriendRequest(messageIdLong, contactId, string2Boolean(pass), timestamp, new RequestCallbackWrapper<Void>() {
                @Override
                public void onResult(int code, Void aVoid, Throwable throwable) {
                    if (code == ResponseCode.RES_SUCCESS) {
                        if (toPass) {
                            try {
                                IMMessage message = MessageBuilder.createTextMessage(contactId, SessionTypeEnum.P2P, "AGREE_FRIEND_REQUEST");
                                TimeUnit.MILLISECONDS.sleep(1500);
                                NIMClient.getService(MsgService.class).sendMessage(message, false);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        promise.resolve("" + code);
                    } else {
                        promise.reject("" + code, "");
                    }
                }
            });
    }

    /**
     * 删除系统通知
     *
     * @param fromAccount
     * @param timestamp
     * @param promise
     */
    @ReactMethod
    public void deleteSystemMessage(String fromAccount, String timestamp, final Promise promise) {
        if (sysMessageObserver != null)
            sysMessageObserver.deleteSystemMessageById(fromAccount, true);
    }

    /**
     * 删除所有系统通知
     *
     * @param promise
     */
    @ReactMethod
    public void clearSystemMessages(final Promise promise) {
        if (sysMessageObserver != null)
            sysMessageObserver.clearSystemMessages();

    }

    /**
     * 将所有系统通知设为已读
     *
     * @param promise
     */
    @ReactMethod
    public void resetSystemMessageUnreadCount(final Promise promise) {
        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
    }


    @ReactMethod
    public void getListSessionsCacheSize(ReadableArray sessionIds, final Promise promise) {
       try {
            ArrayList<String> _sessionIds = (ArrayList<String>) (ArrayList<?>) (sessionIds.toArrayList());
            WritableMap result = FileCacheUtil.getSessionsCacheSie(_sessionIds);
            promise.resolve(result);
        } catch (Error error){
            promise.reject(error);
        }
    }

    @ReactMethod
    public void cleanListSessionsCache(ReadableArray sessionIds, final Promise promise) {
        ArrayList<String> _sessionIds = (ArrayList<String>)(ArrayList<?>)(sessionIds.toArrayList());

        String result = FileCacheUtil.cleanSessionCache(_sessionIds);

        promise.resolve(result);
    }

    @ReactMethod
    public void getSessionCacheSize(String sessionId, final Promise promise) {
        long result = FileCacheUtil.getSessionCacheSie(sessionId);

        promise.resolve("" + FileUtil.formatFileSize(result));
//        promise.reject("" + code, "");
    }

    @ReactMethod
    public void cleanSessionCache(String sessionId, final Promise promise) {
        String result = FileCacheUtil.cleanSessionCache(sessionId);

        promise.resolve(result);
//        promise.reject("" + code, "");
    }

    void showTip(final String tip) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(reactContext.getCurrentActivity(), tip, Toast.LENGTH_SHORT).show();
            }
        });

    }

    void showTip(final int tipId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(reactContext.getCurrentActivity(), reactContext.getString(tipId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        LogUtil.w(TAG, "onActivityResult:" + requestCode + "-result:" + resultCode);
    }

    @Override
    public void onNewIntent(Intent intent) {

        LogUtil.w(TAG, "onNewIntent:" + intent.getExtras());
//        ReceiverMsgParser.openIntent(intent);
        if (reactContext.getCurrentActivity() != null && ReceiverMsgParser.checkOpen(intent)) {
            intent.putExtras(getIntent());
            reactContext.getCurrentActivity().setIntent(intent);
            ReactCache.emit(ReactCache.observeBackgroundPushEvent, ReceiverMsgParser.getWritableMap(intent));
            launch = null;
        }

    }

    public static String status = "";
    public static Intent launch = null;

    @ReactMethod
    public void getLaunch(Promise promise) {
        if (launch == null) {
            promise.resolve(null);
        } else {
            promise.resolve(ReceiverMsgParser.getWritableMap(launch));
            launch = null;
        }
    }

    @ReactMethod
    public void fetchNetInfo(Promise promise) {
        int networkType = NetworkUtil.getNetworkClass(reactContext);
        String networkString = "";
        switch (networkType) {
            case NetworkUtil.NETWORK_CLASS_2_G:
                networkString = "2g";
                break;
            case NetworkUtil.NETWORK_CLASS_3_G:
                networkString = "3g";
                break;
            case NetworkUtil.NETWORK_CLASS_4_G:
                networkString = "4g";
                break;
            case NetworkUtil.NETWORK_CLASS_WIFI:
                networkString = "wifi";
                break;
            case NetworkUtil.NETWORK_CLASS_UNKNOWN:
                networkString = "unknown";
                break;
        }
        promise.resolve(networkString);
    }

    @ReactMethod
    public void updateRecentToTemporarySession(String sessionId, String messageId, ReadableMap data, final Promise promise) {
        Map<String, Object> temporarySessionRef = MapUtil.readableMaptoMap(data);
        RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, SessionTypeEnum.P2P);
        if (recent == null) {
            promise.resolve("success");
            return;
        };

        Map<String, Object> localExt = recent.getExtension();
        if (localExt == null) {
            localExt = new HashMap<String, Object>();
        }

        localExt.put("temporarySessionRef", temporarySessionRef);

        recent.setExtension(localExt);

        NIMClient.getService(MsgService.class).updateRecentAndNotify(recent);

        List<String> messageIds = new ArrayList<String>();
        messageIds.add(messageId);
        NIMClient.getService(MsgService.class).queryMessageListByUuid(messageIds).setCallback(new RequestCallbackWrapper<List<IMMessage>>() {
            @Override
            public void onResult(int code, List<IMMessage> result, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS || result == null || result.isEmpty())  {
                    promise.resolve("success");
                    return;
                }

                IMMessage message = result.get(0);

                NIMClient.getService(MsgService.class).deleteChattingHistory(message);

                promise.resolve("success");
            }
        });
    }

    @ReactMethod
    public void removeTemporarySessionRef(String sessionId, final Promise promise) {
        RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, SessionTypeEnum.P2P);
        if (recent == null) {
            promise.resolve("success");
            return;
        }

        Map<String, Object> localExt = recent.getExtension();
        if (localExt == null) {
            localExt = new HashMap<>();
        }
        if (localExt.get("temporarySessionRef") == null) {
            promise.resolve("success");
            return;
        }

        localExt.remove("temporarySessionRef");

        recent.setExtension(localExt);

        NIMClient.getService(MsgService.class).updateRecent(recent);

        promise.resolve("success");
    }

    @ReactMethod
    public void addEmptyTemporarySession(String sessionId, ReadableMap data, final Promise promise) {
        Map<String, Object> temporarySessionRef = MapUtil.readableMaptoMap(data);
        String temporarySessionId = (String) temporarySessionRef.get("sessionId");
        if (temporarySessionId == null) {
            promise.resolve("success");
            return;
        };

        RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, SessionTypeEnum.P2P);
        if (recent != null) {
            Map<String, Object> localExt = recent.getExtension();
            if (localExt == null) {
                localExt = new HashMap<String, Object>();
            }

            Map<String, Object> tempSessionRef = (Map<String, Object>) localExt.get("temporarySessionRef");
            if (tempSessionRef != null && tempSessionRef.get("sessionId") != null && tempSessionRef.get("sessionId").equals(temporarySessionId)) {
                promise.resolve("success");
                return;
            };

            localExt.put("temporarySessionRef", temporarySessionRef);

            recent.setExtension(localExt);

            NIMClient.getService(MsgService.class).updateRecentAndNotify(recent);

            sessionService.sendMessageUpdateTemporarySession(sessionId, temporarySessionRef);

            promise.resolve("success");
            return;
        }

        recent = NIMClient.getService(MsgService.class).createEmptyRecentContact(sessionId, SessionTypeEnum.P2P, 0, 0 ,true);
        if (recent == null) {
            promise.resolve("success");
            return;
        }

        Map<String, Object> localExt = new HashMap<String, Object>();
        localExt.put("temporarySessionRef", temporarySessionRef);

        recent.setExtension(localExt);

        NIMClient.getService(MsgService.class).updateRecentAndNotify(recent);


        sessionService.sendMessageUpdateTemporarySession(sessionId, temporarySessionRef);

        promise.resolve("success");
    }

    @ReactMethod
    public void addEmptyRecentSession(String sessionId, String sessionType) {
        SessionTypeEnum type = SessionUtil.getSessionType(sessionType);
        NIMSDK.getMsgService().createEmptyRecentContact(sessionId, type, 0, System.currentTimeMillis(), true);
    }

    @ReactMethod
    public void addEmptyPinRecentSession(String sessionId, String sessionType) {
        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, sessionTypeEnum);
        if (recent != null) return;

        recent = NIMClient.getService(MsgService.class).createEmptyRecentContact(sessionId, sessionTypeEnum, 0, System.currentTimeMillis(), true, false);

        if (recent == null) return;

        Map<String, Object> localExt = recent.getExtension();
        if (localExt == null) {
            localExt = new HashMap<String, Object>();
        }

        Boolean isPinSessionWithEmpty = true;

        localExt.put("isPinSessionWithEmpty", isPinSessionWithEmpty);

        recent.setExtension(localExt);

        NIMClient.getService(MsgService.class).updateRecent(recent);
    }

    private Map<String, Object> handleRecentLocalExtWithActionHide(RecentContact recent, String latestMsgId, Boolean isHideSession, Boolean isPinCode) {
        Map<String, Object> extension = recent.getExtension();
        if (extension == null) {
            extension = new HashMap<String, Object>();
        };

        if (isHideSession) {
            extension.put("isHideSession", true);
            extension.put("isPinCode", isPinCode);
            if (latestMsgId != null) {
                extension.put("latestMsgIdWithHideSession", latestMsgId);
            }
        } else {
            extension.put("isHideSession", false);
            extension.put("isPinCode", false);
            extension.put("latestMsgIdWithHideSession", "");
        }

        return extension;
    }

    @ReactMethod
    public  void  updateActionHideRecentSession(String sessionId, String type, Boolean isHideSession, Boolean isPinCode, final Promise promise) {
        Log.e(TAG, "updateActionHideRecentSession");
        SessionTypeEnum sessionType = SessionUtil.getSessionType(type);
        RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, sessionType);
        if (recent == null) {
            NIMSDK.getMsgService().createEmptyRecentContact(sessionId, sessionType, 0, System.currentTimeMillis(), true, false);
        }

        recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, sessionType);

        String latestMsgId = recent.getRecentMessageId();

        recent.setExtension(handleRecentLocalExtWithActionHide(recent, latestMsgId, isHideSession, isPinCode));
        NIMSDK.getMsgService().updateRecentAndNotify(recent);
        promise.resolve("success");
    }

    private Map<String, Object> getExtensionByRecentContact(RecentContact recent) {
        Map<String, Object> extension = recent.getExtension();
        if (extension == null) {
            return new HashMap<String, Object>();
        }

        return extension;
    }

    @ReactMethod
    public void addEmptyRecentSessionWithoutMessage(String sessionId, String sessionType, final Promise promise) {
        SessionTypeEnum sessionTypeEnum = SessionUtil.getSessionType(sessionType);
        RecentContact recent =  NIMClient.getService(MsgService.class).queryRecentContact(sessionId, sessionTypeEnum);
        if (recent != null) {
            promise.resolve(ReactCache.createRecent(recent));
            return;
        }

        recent =  NIMClient.getService(MsgService.class).createEmptyRecentContact(sessionId, sessionTypeEnum, 0, System.currentTimeMillis(), true, false);

        if (recent == null) {
            promise.reject("addEmptyRecentSessionWithoutMessage", "Error: Create empty session failed");
            return;
        }

        promise.resolve(ReactCache.createRecent(recent));
    }

    @ReactMethod
    public void sendCustomMessageOfChatbot(String sessionId, String customerServiceType, final Promise promise) {
        CustomMessageChatBotAttachment attachment = new CustomMessageChatBotAttachment();
        attachment.setCustomServiceType(customerServiceType);

        CustomMessageConfig config = new CustomMessageConfig();
        config.enablePush = false;
        config.enableUnreadCount = false;

        IMMessage message = MessageBuilder.createCustomMessage(sessionId, SessionTypeEnum.P2P,"", attachment, config);

        Map<String, Object> localExt = new HashMap<>();
        localExt.put("isHideMessage", true);

        message.setLocalExtension(localExt);

        NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                if (code != ResponseCode.RES_SUCCESS) {
                    promise.reject("error", "send message failed");
                } else {
                    promise.resolve("success");
                }
            }
        });
    }

    @ReactMethod
    public void addEmptyRecentSessionCustomerService(ReadableArray data, final Promise promise) {
        Log.e(TAG, "addEmptyRecentSessionCustomerService =>>>>>> " + data);
        List<Object> arr = MapUtil.readableArrayToArray(data);

        for(Object item : arr) {
            Map<String, Object> map = (Map<String, Object>) item;
            Log.e(TAG, "addEmptyRecentSessionCustomerService =>>>>>> " + map);
            if (map == null) continue;

            String sessionId = (String) map.get("sessionId");
            String onlineServiceType = (String) map.get("onlineServiceType");
            String nickname = (String) map.get("nickname");
            if (sessionId == null || onlineServiceType == null) continue;;

            RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, SessionTypeEnum.P2P);
            if (recent == null) {
                NIMClient.getService(MsgService.class).createEmptyRecentContact(sessionId, SessionTypeEnum.P2P,0, System.currentTimeMillis(), true, false);
                recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, SessionTypeEnum.P2P);
            }
            if (recent == null) continue;

            Map<String, Object> extension = getExtensionByRecentContact(recent);

            Boolean isChatBot = false;
            Boolean isCsr = false;

            if (onlineServiceType.equals("chatbot")) {
                isChatBot = true;
            }

            if (onlineServiceType.equals("csr")) {
                isCsr = true;
            }

            if (nickname != null && !nickname.isEmpty()) {
                extension.put("name", nickname);
            }

            extension.put("isChatBot", isChatBot);
            extension.put("isCsr", isCsr);
            extension.put("isUpdated", true);

            recent.setExtension(extension);

            NIMClient.getService(MsgService.class).updateRecentAndNotify(recent);
         }

        promise.resolve("200");
    }

    @ReactMethod
    public  void updateRecentSessionIsCsrOrChatbot(String sessionId,String type, String name ) {
        Boolean isUpdated = type.equals("chatbot") || type.equals("csr");
        if (!isUpdated) return;

        RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(sessionId, SessionTypeEnum.P2P);

        Map<String, Object> extension = recent.getExtension();

        if (extension == null) {
            extension = new HashMap<String, Object>();
        }

        Boolean isCsr = false;
        Boolean isChatBot = false;

        if (type.equals("chatbot")) {
            isChatBot = true;
        }

        if (type.equals("csr")) {
            isCsr = true;
        }

        if (!name.equals("")) {
            extension.put("name", name);
        }

        extension.put("isCsr", isCsr);
        extension.put("isChatBot", isChatBot);
        extension.put("isUpdated", true);

        recent.setExtension(extension);

        NIMSDK.getMsgService().updateRecent(recent);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String getDeviceLanguage(){
        Log.d("Locale.getDefault() -->",Locale.getDefault().toString());
        Log.d("Locale.getDefault().getLanguage() -->",Locale.getDefault().getLanguage());
        return Locale.getDefault().getLanguage();
    }

    @Override
    public void onHostResume() {

        LogUtil.w(TAG, "onHostResume:" + status);

        if (!TextUtils.isEmpty(status) && !"onHostPause".equals(status)) {
            if (NIMClient.getStatus().wontAutoLogin()) {
                WritableMap r = Arguments.createMap();
                r.putString("status", status);
                ReactCache.emit(ReactCache.observeOnKick, r);
            }
        }
//        if (NIMClient.getStatus().wontAutoLogin()) {
//            Toast.makeText(IMApplication.getContext(), "您的帐号已在别的设备登录，请重新登陆", Toast.LENGTH_SHORT).show();
//        }
        status = "";
    }

    @Override
    public void onHostPause() {
        if (TextUtils.isEmpty(status)) {
            status = "onHostPause";
        }
        LogUtil.w(TAG, "onHostPause");
    }

    @Override
    public void onHostDestroy() {
        LogUtil.w(TAG, "onHostDestroy");
    }
}

class LocalExt {
    public Boolean isUpdated = true;
    public Boolean isCsr;
    public  Boolean isChatBot;

    public  LocalExt(Boolean csr, Boolean chatBot) {
        isCsr = csr;
        isChatBot = chatBot;
    }
}