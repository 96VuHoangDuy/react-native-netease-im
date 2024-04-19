package com.netease.im;

import static com.netease.im.IMApplication.getSdkStorageRooPath;
import static com.netease.nimlib.sdk.NIMClient.getService;
import static com.netease.nimlib.sdk.NIMSDK.getMsgService;

import android.content.Context;
import android.os.Environment;

import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.netease.im.common.ImageLoaderKit;
import com.netease.im.login.LoginService;
import com.netease.im.session.SessionService;
import com.netease.im.session.extension.AccountNoticeAttachment;
import com.netease.im.session.extension.BankTransferAttachment;
import com.netease.im.session.extension.CustomAttachment;
import com.netease.im.session.extension.CustomAttachmentType;
import com.netease.im.session.extension.DefaultCustomAttachment;
import com.netease.im.session.extension.LinkUrlAttachment;
import com.netease.im.session.extension.RedPacketAttachement;
import com.netease.im.session.extension.RedPacketOpenAttachement;
import com.netease.im.uikit.cache.FriendDataCache;
import com.netease.im.uikit.cache.NimUserInfoCache;
import com.netease.im.uikit.cache.TeamDataCache;
import com.netease.im.uikit.common.util.file.FileUtil;
import com.netease.im.uikit.common.util.log.LogUtil;
import com.netease.im.uikit.common.util.sys.TimeUtil;
import com.netease.im.uikit.contact.core.item.AbsContactItem;
import com.netease.im.uikit.contact.core.item.ContactItem;
import com.netease.im.uikit.contact.core.model.ContactDataList;
import com.netease.im.uikit.contact.core.model.IContact;
import com.netease.im.uikit.contact.core.model.TeamContact;
import com.netease.im.uikit.session.emoji.AitHelper;
import com.netease.im.uikit.session.helper.TeamNotificationHelper;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
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
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.model.MemberChangeAttachment;
import com.netease.nimlib.sdk.team.model.MuteMemberAttachment;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;


import org.apache.lucene.portmobile.file.Files;
import org.apache.lucene.portmobile.file.Paths;
import org.apache.lucene.portmobile.file.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dowin on 2017/4/28.
 */

public class ReactCache {
    public final static String observeRecentContact = "observeRecentContact";//'最近会话'
    public final static String observeOnlineStatus = "observeOnlineStatus";//'在线状态'
    public final static String observeFriend = "observeFriend";//'联系人'
    public final static String observeTeam = "observeTeam";//'群组'
    public final static String observeReceiveMessage = "observeReceiveMessage";//'接收消息'

    public final static String observeDeleteMessage = "observeDeleteMessage";//'撤销后删除消息'
    public final static String observeReceiveSystemMsg = "observeReceiveSystemMsg";//'系统通知'
    public final static String observeMsgStatus = "observeMsgStatus";//'发送消息状态变化'
    public final static String observeAudioRecord = "observeAudioRecord";//'录音状态'
    public final static String observeUnreadCountChange = "observeUnreadCountChange";//'未读数变化'
    public final static String observeBlackList = "observeBlackList";//'黑名单'
    public final static String observeAttachmentProgress = "observeAttachmentProgress";//'上传下载进度'
    public final static String observeOnKick = "observeOnKick";//'被踢出'
    public final static String observeAccountNotice = "observeAccountNotice";//'账户变动通知'
    public final static String observeLaunchPushEvent = "observeLaunchPushEvent";//''
    public final static String observeBackgroundPushEvent = "observeBackgroundPushEvent";//''

    final static String TAG = "ReactCache";
    private static ReactContext reactContext;

    public static void setReactContext(ReactContext reactContext) {
        ReactCache.reactContext = reactContext;
    }

    public static ReactContext getReactContext() {
        return reactContext;
    }

    public static void emit(String eventName, Object date) {
        try {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public  static  WritableMap convertReadableMap(ReadableMap map) {
//        WritableMap result = Arguments.createMap();
//        ReadableMapKeySetIterator iterator = map.keySetIterator();
//        while (iterator.hasNextKey()) {
//            String key = iterator.nextKey();
//            switch (map.getType(key)) {
//                case Null:
//                    result.putNull(key);
//                    break;
//                case Boolean:
//                    result.putBoolean(key, map.getBoolean(key));
//                    break;
//                case Number:
//                    result.putDouble(key, map.getDouble(key));
//                    break;
//                case String:
//                    result.putString(key, map.getString(key));
//                    break;
//                case Map:
//                    result.putMap(key, map.getMap(key));
//                    break;
//                case Array:
//                    // If the value is a ReadableArray, handle it similarly to ReadableMap
//                    // For simplicity, this example puts null in the WritableMap
//                    writableMap.putNull(key);
//                    break;
//                // Add more cases for other types if needed
//                default:
//                    // Handle other types or skip them as per your use case
//                    break;
//            }
//        }
//    }

    public static Object createRecentList(List<RecentContact> recents, int unreadNum) {
        LogUtil.w(TAG, "size:" + (recents == null ? 0 : recents.size()));
        // recents参数即为最近联系人列表（最近会话列表）
        WritableMap writableMap = Arguments.createMap();
        WritableArray array = Arguments.createArray();
        int unreadNumTotal = 0;
        if (recents != null && recents.size() > 0) {

            WritableMap map;
            for (RecentContact contact : recents) {
                map = Arguments.createMap();
                String contactId = contact.getContactId();
                map.putString("contactId", contactId);
                map.putString("unreadCount", String.valueOf(contact.getUnreadCount()));
                String name = "";
                SessionTypeEnum sessionType = contact.getSessionType();
                String imagePath = "";
                Map<String, Object> extension = contact.getExtension();
                Boolean isHideRecent = false;
                IMMessage lastMessage = NIMClient.getService(MsgService.class).queryLastMessage(contact.getContactId(), contact.getSessionType());

                if (extension != null) {
                    WritableMap localExt = Arguments.createMap();
                    Boolean isCsr = (Boolean) extension.get("isCsr");
                    Boolean isChatBot = (Boolean) extension.get("isChatBot");
                    Boolean isUpdated = (Boolean) extension.get("isUpdated");
                    String nameCsr = (String) extension.get("name");
                    Boolean isHideSession = (Boolean) extension.get("isHideSession");

                    if (isCsr != null) {
                        localExt.putBoolean("isCsr", isCsr);
                    }
                    if (isChatBot != null) {
                        localExt.putBoolean("isChatBot", isChatBot);
                    }
                    if (isUpdated != null) {
                        localExt.putBoolean("isUpdated", isUpdated);
                    }

                    if (nameCsr != null) {
                        localExt.putString("name", nameCsr);
                    }

                    if (isHideSession != null) {
                        isHideRecent = isHideSession;
                        localExt.putBoolean("isHideSession", isHideSession);
                    }

                    if (lastMessage != null) {
                        Map<String, Object> messageLocalExt = lastMessage.getLocalExtension();
                        if (messageLocalExt != null) {
                            String notificationType = (String) messageLocalExt.get("notificationType");

                            if (notificationType != null) {
                                localExt.putString("notificationType", notificationType);
                            }
                        }
                    }

                    map.putMap("localExt", localExt);
                 }
                Team team = null;
                if (sessionType == SessionTypeEnum.P2P) {
                    map.putString("teamType", "-1");
                    NimUserInfoCache nimUserInfoCache = NimUserInfoCache.getInstance();
                    imagePath = nimUserInfoCache.getAvatar(contactId);
                    Boolean isNeedMessageNotify = NIMClient.getService(FriendService.class).isNeedMessageNotify(contactId);
                    if (isNeedMessageNotify == true && !isHideRecent) {
                        unreadNumTotal += contact.getUnreadCount();
                    }
                    map.putString("mute", boolean2String(!isNeedMessageNotify));
                    map.putBoolean("isMyFriend", NIMClient.getService(FriendService.class).isMyFriend(contactId));

                    Boolean isReplyStranger = extension != null ? (Boolean) extension.get("isReplyStranger") : false;
                    map.putBoolean("isReplyStranger", isReplyStranger != null ? isReplyStranger : false);

                    name = nimUserInfoCache.getUserDisplayName(contactId);
                } else if (sessionType == SessionTypeEnum.Team) {
                    team = TeamDataCache.getInstance().getTeamById(contactId);
                    if (team != null) {
                        name = team.getName();
                        map.putString("teamType", Integer.toString(team.getType().getValue()));
                        imagePath = team.getIcon();
                        map.putString("memberCount", Integer.toString(team.getMemberCount()));
                        map.putString("mute", getMessageNotifyType(team.getMessageNotifyType()));
                        if (team.getMessageNotifyType() == TeamMessageNotifyTypeEnum.All && !isHideRecent) {
                            unreadNumTotal += contact.getUnreadCount();
                        }
                    }
                }
                map.putString("imagePath", imagePath);
                map.putString("imageLocal", ImageLoaderKit.getMemoryCachedAvatar(imagePath));
                map.putString("name", name);
                map.putString("sessionType", Integer.toString(contact.getSessionType().getValue()));

                String fromAccount = contact.getFromAccount();
                String content = contact.getContent();
                switch (contact.getMsgType()) {
                    case text:
                        if (contact.getContent() != null && contact.getContent().equals("[动图]") && !TextUtils.equals(LoginService.getInstance().getAccount(), fromAccount)) {
                            content = name + " : [动图]";
                            break;
                        }
                        content = contact.getContent();
                        break;
                    case image:
                        if (!TextUtils.equals(LoginService.getInstance().getAccount(), fromAccount)) {
                            content = name + " : [图片]";
                        } else {
                            content = "[图片]";
                        }
                        break;
                    case video:
                        if (!TextUtils.equals(LoginService.getInstance().getAccount(), fromAccount)) {
                            content = name + " : [视频]";
                        } else {
                            content = "[视频]";
                        }
                        break;
                    case audio:
                        if (!TextUtils.equals(LoginService.getInstance().getAccount(), fromAccount)) {
                            content = name + " : [语音消息]";
                        } else {
                            content = "[语音消息]";
                        }
                        break;
                    case location:
                        if (!TextUtils.equals(LoginService.getInstance().getAccount(), fromAccount)) {
                            content = name + " : [位置]";
                        } else {
                            content = "[位置]";
                        }
                        break;
                    case tip:
                        List<String> uuids = new ArrayList<>();
                        uuids.add(contact.getRecentMessageId());
                        List<IMMessage> messages = NIMClient.getService(MsgService.class).queryMessageListByUuidBlock(uuids);
                        if (messages != null && messages.size() > 0) {
                            content = messages.get(0).getContent();
                        }
                        break;
                    case notification:
                        if (sessionType == SessionTypeEnum.Team && team != null) {
                            content = TeamNotificationHelper.getTeamNotificationText(contact.getContactId(),
                                    contact.getFromAccount(),
                                    (NotificationAttachment) contact.getAttachment());
                            WritableMap notiObj = Arguments.createMap();

                            NotificationAttachment attachment = (NotificationAttachment) contact.getAttachment();
                            NotificationType operationType = attachment.getType();
                            String sourceId = contact.getFromAccount();

                            WritableMap sourceIdMap = Arguments.createMap();
                            sourceIdMap.putString("sourceName", getTeamUserDisplayName(contactId, sourceId));
                            sourceIdMap.putString("sourceId", sourceId);

                            notiObj.putInt("operationType", operationType.getValue());
                            notiObj.putMap("sourceId", sourceIdMap);

                            switch (operationType) {
                                case InviteMember:
                                case KickMember:
                                case PassTeamApply:
                                case TransferOwner:
                                case AddTeamManager:
                                case RemoveTeamManager:
                                case AcceptInvite:
                                case MuteTeamMember:
                                    MemberChangeAttachment memberAttachment = (MemberChangeAttachment) attachment;
                                    ArrayList<String> targets = memberAttachment.getTargets();

                                    WritableArray targetsWritableArray = Arguments.createArray();

                                    for (String targetId : targets) {
                                        String targetName = getTeamUserDisplayName(contactId, targetId);

                                        WritableMap target = Arguments.createMap();
                                        target.putString("targetName", targetName);
                                        target.putString("targetId", targetId);

                                        targetsWritableArray.pushMap(target);
                                    }

                                    if (operationType == NotificationType.MuteTeamMember) {
                                        MuteMemberAttachment muteMemberAttachment = (MuteMemberAttachment) attachment;
                                        notiObj.putString("isMute", muteMemberAttachment.isMute() ? "mute" : "unmute");
                                    }

                                    notiObj.putArray("targets", targetsWritableArray);
                                    break;
                                case LeaveTeam:
                                case DismissTeam:
                                    notiObj.putArray("targets", null);
                                    break;
                                case UpdateTeam:
                                    Map<TeamFieldEnum, String> mockUpKeys = new HashMap();
                                    mockUpKeys.put(TeamFieldEnum.Name, "NIMTeamUpdateTagName");
                                    mockUpKeys.put(TeamFieldEnum.Introduce, "NIMTeamUpdateTagIntro");
                                    mockUpKeys.put(TeamFieldEnum.Announcement, "NIMTeamUpdateTagAnouncement");
                                    mockUpKeys.put(TeamFieldEnum.VerifyType, "NIMTeamUpdateTagJoinMode");
                                    mockUpKeys.put(TeamFieldEnum.ICON, "NIMTeamUpdateTagAvatar");
                                    mockUpKeys.put(TeamFieldEnum.InviteMode, "NIMTeamUpdateTagInviteMode");
                                    mockUpKeys.put(TeamFieldEnum.BeInviteMode, "NIMTeamUpdateTagBeInviteMode");
                                    mockUpKeys.put(TeamFieldEnum.TeamUpdateMode, "NIMTeamUpdateTagUpdateInfoMode");
                                    mockUpKeys.put(TeamFieldEnum.AllMute, "NIMTeamUpdateTagMuteMode");

                                    UpdateTeamAttachment updateTeamAttachment = (UpdateTeamAttachment) attachment;
                                    Set<Map.Entry<TeamFieldEnum, Object>> updateTeamAttachmentDetail = updateTeamAttachment.getUpdatedFields().entrySet();
                                    WritableMap updateDetail = Arguments.createMap();

                                    for (Map.Entry<TeamFieldEnum, Object> field : updateTeamAttachmentDetail) {
                                        updateDetail.putString("type", mockUpKeys.get(field.getKey()));
                                        updateDetail.putString("value", field.getValue().toString());
                                    }
                                    notiObj.putMap("updateDetail", updateDetail);
                                    break;
                                default:
                                    break;
                            }

                            map.putMap(MESSAGE_EXTEND, notiObj);
                        }
                        break;
                    default:
                        break;
                }
//                map.putString("msgType", getMessageType(contact.getMsgType(),(CustomAttachment) contact.getAttachment()));
                if (contact.getMsgType() == MsgTypeEnum.custom) {
                    map.putString(MessageConstant.Message.MSG_TYPE, getMessageType(contact.getMsgType(), (CustomAttachment) contact.getAttachment()));
                } else {
                    if (lastMessage != null && lastMessage.getRemoteExtension() != null) {
                        Map<String, Object> extensionMsg = lastMessage.getRemoteExtension();

                        if (extensionMsg.containsKey("extendType")) {
                            String extendType = extensionMsg.get("extendType").toString();
                            if (extendType.equals("forwardMultipleText")) {
                                WritableMap extend = Arguments.createMap();


                                content = "[聊天记录]";
                                extend.putString("messages", contact.getContent());
                                map.putMap(MESSAGE_EXTEND, extend);
                                map.putString(MessageConstant.Message.MSG_TYPE, "forwardMultipleText");
                            }

                            if (extendType.equals("card")) {
                                WritableMap writableMapExtend = new WritableNativeMap();

                                for (Map.Entry<String, Object> entry : extensionMsg.entrySet()) {
                                    writableMapExtend.putString(entry.getKey(), entry.getValue().toString());
                                }

                                map.putMap(MESSAGE_EXTEND, writableMapExtend);
                                map.putString(MessageConstant.Message.MSG_TYPE, "card");
                            }

                            if (extendType.equals("revoked_success")) {
                                WritableMap writableMapExtend = new WritableNativeMap();
                                writableMapExtend.putString("tipMsg", contact.getContent());

                                map.putMap(MESSAGE_EXTEND, writableMapExtend);
                                map.putString(MessageConstant.Message.MSG_TYPE, "notification");
                            }

                            if (extendType.equals("TEAM_NOTIFICATION_MESSAGE")) {
                                map.putMap(MESSAGE_EXTEND, MapUtil.mapToReadableMap(extensionMsg));
                                map.putString(MessageConstant.Message.MSG_TYPE, "notification");
                            }
                        } else {
                            map.putString(MessageConstant.Message.MSG_TYPE, getMessageType(contact.getMsgType(), null));
                        }
                    } else {
                        map.putString(MessageConstant.Message.MSG_TYPE, getMessageType(contact.getMsgType(), null));
                    }

//                    if (contact.getExtension() != null) {
//                        WritableMap extend = Arguments.createMap();
//
//                        extend.putString("messages", contact.getContent());
//                        map.putMap(MESSAGE_EXTEND, extend);
//                        map.putString(MessageConstant.Message.MSG_TYPE, "forwardMultipleText");
//                    } else {
//                        map.putString(MessageConstant.Message.MSG_TYPE, getMessageType(contact.getMsgType(), null));
//                    }
                }
                map.putString("msgStatus", Integer.toString(contact.getMsgStatus().getValue()));
                map.putString("messageId", contact.getRecentMessageId());

                map.putString("fromAccount", fromAccount);
                map.putString("time", TimeUtil.getTimeShowString(contact.getTime(), true));


                String fromNick = "";
                String teamNick = "";
                if (!TextUtils.isEmpty(fromAccount)) {
                    try {
                        fromNick = contact.getFromNick();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    fromNick = TextUtils.isEmpty(fromNick) ? NimUserInfoCache.getInstance().getUserDisplayName(fromAccount) : fromNick;
                    map.putString("nick", fromNick);

                    if (contact.getSessionType() == SessionTypeEnum.Team && !TextUtils.equals(LoginService.getInstance().getAccount(), fromAccount)) {
                        String tid = contact.getContactId();
                        teamNick = TextUtils.isEmpty(fromAccount) ? "" : getTeamUserDisplayName(tid, fromAccount) + ": ";
                        if ((contact.getAttachment() instanceof NotificationAttachment)) {
                            if (AitHelper.hasAitExtention(contact)) {
                                if (contact.getUnreadCount() == 0) {
                                    AitHelper.clearRecentContactAited(contact);
                                } else {
                                    content = AitHelper.getAitAlertString(content);
                                }
                            }
                        }
                    }
                }
                CustomAttachment attachment = null;
                try {
                    if (contact.getMsgType() == MsgTypeEnum.custom) {
                        attachment = (CustomAttachment) contact.getAttachment();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (attachment != null) {
                    map.putString("custType", attachment.getType());
                    switch (attachment.getType()) {
                        case CustomAttachmentType.RedPacket:
                            if (attachment instanceof RedPacketAttachement) {
                                content = "[红包] " + ((RedPacketAttachement) attachment).getComments();
                            }
                            break;
                        case CustomAttachmentType.BankTransfer:
                            if (attachment instanceof BankTransferAttachment) {
                                content = "[转账] " + ((BankTransferAttachment) attachment).getComments();
                            }
                            break;
                        case CustomAttachmentType.LinkUrl:
                            if (attachment instanceof LinkUrlAttachment) {
                                content = ((LinkUrlAttachment) attachment).getTitle();
                            }
                            break;
                        case CustomAttachmentType.AccountNotice:
                            if (attachment instanceof AccountNoticeAttachment) {
                                content = ((AccountNoticeAttachment) attachment).getTitle();
                            }
                            break;
                        case CustomAttachmentType.RedPacketOpen:
                            if (attachment instanceof RedPacketOpenAttachement) {
                                teamNick = "";
                                RedPacketOpenAttachement rpOpen = (RedPacketOpenAttachement) attachment;
                                if (sessionType == SessionTypeEnum.Team && !rpOpen.isSelf()) {
                                    content = "";
                                } else {
                                    content = rpOpen.getTipMsg(false);
                                }
                            }
                            break;
//                        case CustomAttachmentType.Card:
//                            if (attachment instanceof CardAttachment) {
//                                String str;
//                                if (fromAccount.equals(LoginService.getInstance().getAccount())) {
//                                    str = "推荐了";
//                                } else {
//                                    str = "向你推荐了";
//                                }
//                                content = str + ((CardAttachment) attachment).getName();
//                            }
//                            break;
                        default:
                            if (attachment instanceof DefaultCustomAttachment) {
                                content = ((DefaultCustomAttachment) attachment).getDigst();
                                if (TextUtils.isEmpty(content)) {
                                    content = "[未知消息]";
                                }
                            }
                            break;
                    }
                }
                content = teamNick + content;
                map.putString("content", content);
                array.pushMap(map);
            }
//            LogUtil.w(TAG, array + "");
        }
        writableMap.putArray("recents", array);
        writableMap.putString("unreadCount", Integer.toString(unreadNumTotal));
        return writableMap;
    }

    private static String getTeamUserDisplayName(String tid, String account) {
        return TeamDataCache.getInstance().getTeamMemberDisplayName(tid, account);
    }

    static Pattern pattern = Pattern.compile("\\d{5}");

    static boolean hasFilterFriend(String contactId) {
        if (contactId != null) {
            if (contactId.equals(LoginService.getInstance().getAccount())) {
                return true;
            }
            if (contactId.length() == 5) {
                Matcher matcher = pattern.matcher(contactId);
                if (matcher.matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 过滤自己 和 \d{5}账号
     *
     * @param dataList
     * @return
     */
    public static Object createFriendList(ContactDataList dataList, boolean hasFilter) {
        LogUtil.w(TAG, dataList.getCount() + "");
        WritableArray array = Arguments.createArray();
        if (dataList != null) {
            int count = dataList.getCount();
            for (int i = 0; i < count; i++) {
                AbsContactItem item = dataList.getItem(i);

                if (item instanceof ContactItem) {

                    ContactItem contactItem = (ContactItem) item;
                    IContact contact = contactItem.getContact();
                    String contactId = contact.getContactId();
                    if (hasFilter && hasFilterFriend(contactId)) {
                        continue;
                    }
                    String belongs = contactItem.belongsGroup();
                    WritableMap map = Arguments.createMap();

                    map.putString("itemType", Integer.toString(contactItem.getItemType()));
                    map.putString("belong", belongs);
                    map.putString("contactId", contactId);

                    map.putString("alias", contact.getDisplayName());
                    map.putString("type", Integer.toString(contact.getContactType()));
                    map.putString("name", NimUserInfoCache.getInstance().getUserName(contactId));
                    String avatar = NimUserInfoCache.getInstance().getAvatar(contactId);
                    map.putString("avatar", avatar);
                    map.putString("avatarLocal", ImageLoaderKit.getMemoryCachedAvatar(avatar));
                    array.pushMap(map);
//                } else {
//                map.putString("itemType", Integer.toString(item.getItemType()));
//                map.putString("belong", item.belongsGroup());
                }
            }
        }
        LogUtil.w(TAG, array + "");
        return array;
    }


    public static Object createFriendSet(ContactDataList datas, boolean hasFilter) {

        WritableMap writableMap = Arguments.createMap();
        if (datas != null) {
            Map<String, WritableArray> listHashMap = new HashMap<>();
            int count = datas.getCount();
            for (int i = 0; i < count; i++) {
                AbsContactItem item = datas.getItem(i);

                if (item instanceof ContactItem) {
                    ContactItem contactItem = (ContactItem) item;
                    IContact contact = contactItem.getContact();
                    String contactId = contact.getContactId();
                    if (hasFilter && hasFilterFriend(contactId)) {
                        continue;
                    }
                    String belongs = contactItem.belongsGroup();
                    WritableMap map = Arguments.createMap();

                    map.putString("itemType", Integer.toString(contactItem.getItemType()));
                    map.putString("belong", belongs);
                    map.putString("contactId", contact.getContactId());

                    map.putString("alias", contact.getDisplayName());
                    map.putString("type", Integer.toString(contact.getContactType()));
                    map.putString("name", NimUserInfoCache.getInstance().getUserDisplayName(contact.getContactId()));
                    String avatar = NimUserInfoCache.getInstance().getAvatar(contact.getContactId());
                    map.putString("avatar", avatar);
                    map.putString("avatarLocal", ImageLoaderKit.getMemoryCachedAvatar(avatar));
                    WritableArray array = listHashMap.get(belongs);
                    if (array == null) {
                        array = Arguments.createArray();
                    }
                    array.pushMap(map);

                    listHashMap.put(belongs, array);
//                } else {
//                map.putString("itemType", Integer.toString(item.getItemType()));
//                map.putString("belong", item.belongsGroup());
                }
            }
            if (listHashMap.size() > 0) {
                for (Map.Entry<String, WritableArray> entry : listHashMap.entrySet()) {
                    writableMap.putArray(entry.getKey(), entry.getValue());
                }
            }
            listHashMap.clear();
        }

        LogUtil.w(TAG, writableMap + "");
        return writableMap;
    }

    public static Object createTeamList(ContactDataList datas) {

        WritableArray writableArray = Arguments.createArray();
        if (datas != null) {
            int count = datas.getCount();
            for (int i = 0; i < count; i++) {
                AbsContactItem item = datas.getItem(i);

                if (item instanceof ContactItem) {
                    ContactItem contactItem = (ContactItem) item;
                    if (contactItem.getContact() instanceof TeamContact) {
                        String belongs = contactItem.belongsGroup();
                        WritableMap map = Arguments.createMap();

                        map.putString("itemType", Integer.toString(contactItem.getItemType()));
                        map.putString("belong", belongs);

                        TeamContact teamContact = (TeamContact) contactItem.getContact();
                        map.putString("teamId", teamContact.getContactId());
                        if (teamContact.getTeam() != null) {
                            map.putString("teamType", Integer.toString(teamContact.getTeam().getType().getValue()));
                        }
                        map.putString("name", teamContact.getDisplayName());
                        map.putString("type", Integer.toString(teamContact.getContactType()));
                        String avatar = NimUserInfoCache.getInstance().getAvatar(teamContact.getContactId());
                        map.putString("avatar", avatar);
                        map.putString("avatarLocal", ImageLoaderKit.getMemoryCachedAvatar(avatar));
                        writableArray.pushMap(map);
                    }
//                } else {
//                map.putString("itemType", Integer.toString(item.getItemType()));
//                map.putString("belong", item.belongsGroup());
                }
            }
        }

        LogUtil.w(TAG, writableArray + "");
        return writableArray;
    }

    /**
     * account 账号
     * name 用户名
     * avatar 头像
     * signature 签名
     * gender 性别
     * email
     * birthday
     * mobile
     * extension扩展
     * extensionMap扩展map
     */
    public static Object createUserInfo(NimUserInfo userInfo) {
        WritableMap writableMap = Arguments.createMap();
        if (userInfo != null) {

            writableMap.putString("isMyFriend", boolean2String(FriendDataCache.getInstance().isMyFriend(userInfo.getAccount())));
//            writableMap.putString("isMyFriend", boolean2String(NIMClient.getService(FriendService.class).isMyFriend(userInfo.getAccount())));
            writableMap.putString("isMe", boolean2String(userInfo.getAccount() != null && userInfo.getAccount().equals(LoginService.getInstance().getAccount())));
            writableMap.putString("isInBlackList", boolean2String(NIMClient.getService(FriendService.class).isInBlackList(userInfo.getAccount())));
            writableMap.putString("mute", boolean2String(!NIMClient.getService(FriendService.class).isNeedMessageNotify(userInfo.getAccount())));

            writableMap.putString("contactId", userInfo.getAccount());
            writableMap.putString("name", userInfo.getName());
            writableMap.putString("alias", NimUserInfoCache.getInstance().getUserDisplayName(userInfo.getAccount()));
            writableMap.putString("avatar", userInfo.getAvatar());
            writableMap.putString("avatarLocal", ImageLoaderKit.getMemoryCachedAvatar(userInfo.getAvatar()));
            writableMap.putString("signature", userInfo.getSignature());
            writableMap.putString("gender", Integer.toString(userInfo.getGenderEnum().getValue()));
            writableMap.putString("email", userInfo.getEmail());
            writableMap.putString("birthday", userInfo.getBirthday());
            writableMap.putString("mobile", userInfo.getMobile());
        }
        return writableMap;
    }

    public static Object createSystemMsg(List<SystemMessage> sysItems) {
        WritableArray writableArray = Arguments.createArray();

        if (sysItems != null && sysItems.size() > 0) {
            NimUserInfoCache nimUserInfoCache = NimUserInfoCache.getInstance();
            for (SystemMessage message : sysItems) {
                WritableMap map = Arguments.createMap();
                boolean verify = isVerifyMessageNeedDeal(message);
                map.putString("messageId", Long.toString(message.getMessageId()));
                map.putString("type", Integer.toString(message.getType().getValue()));
                map.putString("targetId", message.getTargetId());
                map.putString("fromAccount", message.getFromAccount());
                String avatar = nimUserInfoCache.getAvatar(message.getFromAccount());
                map.putString("avatar", avatar);
                map.putString("avatarLocal", ImageLoaderKit.getMemoryCachedAvatar(avatar));
                map.putString("name", nimUserInfoCache.getUserDisplayNameEx(message.getFromAccount()));//alias
                map.putString("time", Long.toString(message.getTime() / 1000));
                map.putString("isVerify", boolean2String(verify));
                map.putString("status", Integer.toString(message.getStatus().getValue()));
                map.putString("verifyText", getVerifyNotificationText(message));
                map.putString("verifyResult", "");
                if (verify) {
                    if (message.getStatus() != SystemMessageStatus.init) {
                        map.putString("verifyResult", getVerifyNotificationDealResult(message));
                    }
                }
                writableArray.pushMap(map);
            }
        }
        return writableArray;
    }

    private static String getVerifyNotificationText(SystemMessage message) {
        StringBuilder sb = new StringBuilder();
        String fromAccount = NimUserInfoCache.getInstance().getUserDisplayNameYou(message.getFromAccount());
        Team team = TeamDataCache.getInstance().getTeamById(message.getTargetId());
        if (team == null && message.getAttachObject() instanceof Team) {
            team = (Team) message.getAttachObject();
        }
        String teamName = team == null ? message.getTargetId() : team.getName();

        if (message.getType() == SystemMessageType.TeamInvite) {
            sb.append("邀请").append("你").append("加入群 ").append(teamName);
        } else if (message.getType() == SystemMessageType.DeclineTeamInvite) {
            sb.append(fromAccount).append("拒绝了群 ").append(teamName).append(" 邀请");
        } else if (message.getType() == SystemMessageType.ApplyJoinTeam) {
            sb.append("申请加入群 ").append(teamName);
        } else if (message.getType() == SystemMessageType.RejectTeamApply) {
            sb.append(fromAccount).append("拒绝了你加入群 ").append(teamName).append("的申请");
        } else if (message.getType() == SystemMessageType.AddFriend) {
            AddFriendNotify attachData = (AddFriendNotify) message.getAttachObject();
            if (attachData != null) {
                if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_DIRECT) {
                    sb.append("已添加你为好友");
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_AGREE_ADD_FRIEND) {
                    sb.append("通过了你的好友请求");
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_REJECT_ADD_FRIEND) {
                    sb.append("拒绝了你的好友请求");
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                    sb.append(TextUtils.isEmpty(message.getContent()) ? "请求添加好友" : message.getContent());
                }
            }
        }

        return sb.toString();
    }

    /**
     * 是否验证消息需要处理（需要有同意拒绝的操作栏）
     */
    private static boolean isVerifyMessageNeedDeal(SystemMessage message) {
        if (message.getType() == SystemMessageType.AddFriend) {
            if (message.getAttachObject() != null) {
                AddFriendNotify attachData = (AddFriendNotify) message.getAttachObject();
                if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_DIRECT ||
                        attachData.getEvent() == AddFriendNotify.Event.RECV_AGREE_ADD_FRIEND ||
                        attachData.getEvent() == AddFriendNotify.Event.RECV_REJECT_ADD_FRIEND) {
                    return false; // 对方直接加你为好友，对方通过你的好友请求，对方拒绝你的好友请求
                } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                    return true; // 好友验证请求
                }
            }
            return false;
        } else if (message.getType() == SystemMessageType.TeamInvite || message.getType() == SystemMessageType.ApplyJoinTeam) {
            return true;
        } else {
            return false;
        }
    }

    private static String getVerifyNotificationDealResult(SystemMessage message) {
        if (message.getStatus() == SystemMessageStatus.passed) {
            return "已同意";
        } else if (message.getStatus() == SystemMessageStatus.declined) {
            return "已拒绝";
        } else if (message.getStatus() == SystemMessageStatus.ignored) {
            return "已忽略";
        } else if (message.getStatus() == SystemMessageStatus.expired) {
            return "已过期";
        } else {
            return "未处理";
        }
    }

    public static Object createBlackList(List<UserInfo> data) {
        WritableArray array = Arguments.createArray();
        if (data != null) {
            for (UserInfo userInfo : data) {
                if (userInfo != null) {
                    WritableMap writableMap = Arguments.createMap();
                    writableMap.putString("contactId", userInfo.getAccount());
                    writableMap.putString("name", userInfo.getName());
                    writableMap.putString("avatar", userInfo.getAvatar());
                    writableMap.putString("avatarLocal", ImageLoaderKit.getMemoryCachedAvatar(userInfo.getAvatar()));
                    array.pushMap(writableMap);
                }
            }
        }
        return array;
    }

    private static boolean needShowTime(Set<String> timedItems, IMMessage message) {
        return timedItems != null && timedItems.contains(message.getUuid());
    }

    /**
     * @param messageList
     * @return Object
     */
    public static WritableArray createMessageList(List<IMMessage> messageList) {
        WritableArray writableArray = Arguments.createArray();

        if (messageList != null) {
            int size = messageList.size();
            for (int i = 0; i < size; i++) {

                IMMessage item = messageList.get(i);
                if (item != null) {
                    WritableMap itemMap = createMessage(item, false);
                    if (itemMap != null) {
                        writableArray.pushMap(itemMap);
                    }
                }
            }
        }
        return writableArray;
    }

      /**
     * @param messageList
     * @return Object
     */
    public static WritableMap createMessageObjectList(List<IMMessage> messageList) {
        WritableMap objectGroupMessages = Arguments.createMap();
        Map<String, WritableArray> listHashMap = new HashMap<>();

        if (messageList != null) {
            for (IMMessage item : messageList) {
                if (item != null) {
                    WritableMap itemMap = createMessage(item, false);
                    String sessionId = item.getSessionId();
                    WritableArray array = listHashMap.get(sessionId);
                    if (array == null) {
                        array = Arguments.createArray();
                    }

                    array.pushMap(itemMap);
                    listHashMap.put(sessionId, array);
                }
            }
        }

        if (listHashMap.size() > 0) {
            for (Map.Entry<String, WritableArray> entry : listHashMap.entrySet()) {
                objectGroupMessages.putArray(entry.getKey(), entry.getValue());
            }
        }


        return objectGroupMessages;
    }

    static String getMessageNotifyType(TeamMessageNotifyTypeEnum notifyTypeEnum) {
        String notify = "1";
        if (notifyTypeEnum == TeamMessageNotifyTypeEnum.All) {
            notify = "1";
        } else if (notifyTypeEnum == TeamMessageNotifyTypeEnum.Manager) {
            notify = "0";
        } else if (notifyTypeEnum == TeamMessageNotifyTypeEnum.Mute) {
            notify = "0";
        }
        return notify;
    }

    public static Object createTeamInfo(Team team) {
        WritableMap writableMap = Arguments.createMap();
        if (team != null) {
            writableMap.putString("teamId", team.getId());
            writableMap.putString("name", team.getName());
            writableMap.putString("avatar", team.getIcon());
            writableMap.putString("avatarLocal", ImageLoaderKit.getMemoryCachedAvatar(team.getIcon()));
            writableMap.putString("type", Integer.toString(team.getType().getValue()));
            writableMap.putString("introduce", team.getIntroduce());
            writableMap.putString("createTime", TimeUtil.getTimeShowString(team.getCreateTime(), true));
            writableMap.putString("creator", team.getCreator());
            writableMap.putString("mute", getMessageNotifyType(team.getMessageNotifyType()));
            writableMap.putString("memberCount", Integer.toString(team.getMemberCount()));
            writableMap.putString("memberLimit", Integer.toString(team.getMemberLimit()));
        }
        return writableMap;
    }

    // userId
// contactId 群成员ID
// type 类型：0普通成员 1拥有者 2管理员 3申请者
// teamNick  群名片
// isMute 是否禁言
// joinTime 加入时间
// isInTeam 是否在群
// isMe
    public static WritableMap createTeamMemberInfo(TeamMember teamMember) {
        WritableMap writableMap = Arguments.createMap();
        if (teamMember != null) {
            writableMap.putString("contactId", teamMember.getAccount());
            writableMap.putString("type", Integer.toString(teamMember.getType().getValue()));
            writableMap.putString("alias", NimUserInfoCache.getInstance().getUserDisplayName(teamMember.getAccount()));
            //            writableMap.putString("name", TeamDataCache.getInstance().getTeamMemberDisplayName(teamMember.getTid(), teamMember.getAccount()));
            writableMap.putString("name", NimUserInfoCache.getInstance().getUserDisplayName(teamMember.getAccount()));
            writableMap.putString("nickname", teamMember.getTeamNick());
            writableMap.putString("joinTime", TimeUtil.getTimeShowString(teamMember.getJoinTime(), true));
            String avatar = NimUserInfoCache.getInstance().getAvatar(teamMember.getAccount());
            writableMap.putString("avatar", avatar);
            writableMap.putString("avatarLocal", ImageLoaderKit.getMemoryCachedAvatar(avatar));
            writableMap.putString("isInTeam", boolean2String(teamMember.isInTeam()));
            writableMap.putString("isMute", boolean2String(teamMember.isMute()));
            writableMap.putString("teamId", teamMember.getTid());
            writableMap.putString("isMe", boolean2String(TextUtils.equals(teamMember.getAccount(), LoginService.getInstance().getAccount())));

            NimUserInfo memberInfo = NIMClient.getService(UserService.class).getUserInfo(teamMember.getAccount());
            if (memberInfo != null) {
                String birthday = memberInfo.getBirthday();

                if (birthday != null && !birthday.isEmpty()) {
                    writableMap.putString("birthday", birthday);
                } else {
                    writableMap.putString("birthday", "");
                }
            } else {
                writableMap.putString("birthday", "");
            }
        }
        return writableMap;
    }

    public static Object createTeamMemberList(List<TeamMember> teamMemberList) {

        WritableArray array = Arguments.createArray();
        int size = teamMemberList.size();
        if (teamMemberList != null && size > 0) {
            for (int i = 0; i < size; i++) {
                TeamMember teamMember = teamMemberList.get(i);

                WritableMap writableMap = createTeamMemberInfo(teamMember);

                array.pushMap(writableMap);
            }
        }
        return array;
    }

    private static boolean receiveReceiptCheck(final IMMessage msg) {
        if (msg != null) {
            if (msg.getSessionType() == SessionTypeEnum.P2P
                    && msg.getDirect() == MsgDirectionEnum.Out
                    && msg.getMsgType() != MsgTypeEnum.tip
                    && msg.getMsgType() != MsgTypeEnum.notification
                    && msg.isRemoteRead()) {
                return true;
            } else {
                return msg.isRemoteRead();
            }
        }
        return false;
    }

    static String boolean2String(boolean bool) {
        return bool ? Integer.toString(1) : Integer.toString(0);
    }


    /**
     * case text
     * case image
     * case voice
     * case video
     * case location
     * case notification
     * case redpacket
     * case transfer
     * case url
     * case account_notice
     * case redpacketOpen
     *
     * @return
     */
    static String getMessageType(MsgTypeEnum msgType, CustomAttachment attachment) {
        String type = MessageConstant.MsgType.CUSTON;
        switch (msgType) {
            case text:
                type = MessageConstant.MsgType.TEXT;
                break;
            case image:
                type = MessageConstant.MsgType.IMAGE;
                break;
            case audio:
                type = MessageConstant.MsgType.VOICE;
                break;
            case video:
                type = MessageConstant.MsgType.VIDEO;
                break;
            case location:
                type = MessageConstant.MsgType.LOCATION;
                break;
            case file:
                type = MessageConstant.MsgType.FILE;
                break;
            case notification:
                type = MessageConstant.MsgType.NOTIFICATION;
                break;
            case tip:
                type = MessageConstant.MsgType.TIP;
                break;
            case robot:
                type = MessageConstant.MsgType.ROBOT;
                break;
            case custom:
                if (attachment != null) {
                    switch (attachment.getType()) {
                        case CustomAttachmentType.ForwardMultipleText:
                            type = MessageConstant.MsgType.ForwardMultipleText;
                            break;

                        case CustomAttachmentType.RedPacket:
                            type = MessageConstant.MsgType.RED_PACKET;
                            break;

                        case CustomAttachmentType.BankTransfer:
                            type = MessageConstant.MsgType.BANK_TRANSFER;
                            break;
                        case CustomAttachmentType.AccountNotice:
                            type = MessageConstant.MsgType.ACCOUNT_NOTICE;
                            break;
                        case CustomAttachmentType.LinkUrl:
                            type = MessageConstant.MsgType.LINK;
                            break;
                        case CustomAttachmentType.RedPacketOpen:
                            type = MessageConstant.MsgType.RED_PACKET_OPEN;
                            break;
                        case CustomAttachmentType.Card:
                            type = MessageConstant.MsgType.CARD;
                            break;
                        default:
                            type = MessageConstant.MsgType.CUSTON;
                            break;
                    }
                } else {
                    type = MessageConstant.MsgType.CUSTON;
                }
                break;
            default:
                type = MessageConstant.MsgType.CUSTON;
                break;
        }

        return type;
    }

    static String getMessageStatus(MsgStatusEnum statusEnum) {
        switch (statusEnum) {
            case draft:
                return MessageConstant.MsgStatus.SEND_DRAFT;
            case sending:
                return MessageConstant.MsgStatus.SEND_SENDING;
            case success:
                return MessageConstant.MsgStatus.SEND_SUCCESS;
            case fail:
                return MessageConstant.MsgStatus.SEND_FAILE;
            case read:
                return MessageConstant.MsgStatus.RECEIVE_READ;
            case unread:
                return MessageConstant.MsgStatus.RECEIVE_UNREAD;
            default:
                return MessageConstant.MsgStatus.SEND_DRAFT;
        }

    }

    final static String MESSAGE_EXTEND = MessageConstant.Message.EXTEND;

    public static boolean isOriginVideoHasDownloaded(final IMMessage message) {
        if (message.getAttachStatus() == AttachStatusEnum.transferred &&
                !TextUtils.isEmpty(((VideoAttachment) message.getAttachment()).getPath())) {
            return true;
        }
        return false;
    }

    public interface DownloadCallback extends RequestCallback<Void> {
        @Override
        void onSuccess(Void result);

        @Override
        void onFailed(int code);

        @Override
        void onException(Throwable exception);
    }

    private static boolean filemovetoanotherfolder(File afile, File bfile) throws IOException {
        boolean ismove = false;
        InputStream inStream = null;
        OutputStream outStream = null;
        final int BUFFERSIZE = 4 * 1024;

        try {
            FileInputStream fin = new FileInputStream(afile);
            FileOutputStream fout = new FileOutputStream(bfile);

            byte[] buffer = new byte[BUFFERSIZE];

            while (fin.available() != 0) {
                int bytesRead = fin.read(buffer);
                fout.write(buffer, 0, bytesRead);
            }
            // delete the original file
            afile.delete();
            ismove = true;
            System.out.println("File is copied successful!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inStream != null) {
                inStream.close();
            }

            if (outStream != null) {
                outStream.close();
            }
        }
        return ismove;
    }

    private static File replaceVideoPath(String path, String sessionId, String type, String extension) {
        String currentFileName = path.substring(path.lastIndexOf("/"), path.length());
        int extensionIndex = currentFileName.lastIndexOf(".");

        if (extensionIndex > 0) {
            currentFileName = currentFileName.substring(1, extensionIndex);
        } else {
            currentFileName = currentFileName.substring(1);
        }

        File from = new File(path);
        boolean isFromPathExits = from.exists();
        if (!isFromPathExits) return null;

        Context context = IMApplication.getContext();
        String directory = context.getCacheDir().getAbsolutePath();

        String typeDir = "";
        switch (type) {
            case "video":
                typeDir = "/video/";
                break;
            case "image":
                typeDir = "/image/";
                break;
            case "audio":
                typeDir = "/audio/";
                break;
            case "file":
                typeDir = "/file/";
                break;
        }
        Log.d("directory", directory + "......" + directory + "/nim" + typeDir + sessionId + "......." + currentFileName.trim() + extension);

        File to = new File(directory + "/nim" + typeDir + sessionId, currentFileName.trim() + extension);
        File toMkdir = new File(directory + "/nim" + typeDir + sessionId);

        if (!toMkdir.exists()) {
            toMkdir.mkdir();
        }

        try {
            boolean isMoveSuccess = filemovetoanotherfolder(from, to);

            if (isMoveSuccess) {
                return to;
            }
            return null;
        } catch (IOException e) {
                return null;
        }
    }

    public static Map<String, Object> setLocalExtension(IMMessage item, String key, Object value) {
        Map<String, Object> localExtension = item.getLocalExtension();

        Map<String, Object> map = MapBuilder.newHashMap();

        if (localExtension != null) {
            map.putAll(localExtension);
        }

        if (map.containsKey(key)) {
            map.replace(key, value);
        } else {
            map.put(key, value);
        }

        item.setLocalExtension(map);
        getMsgService().updateIMMessage(item);
        Log.d("mapmap", map.toString());
        return map;
    }

    public static WritableMap generateVideoExtend(IMMessage item) {
        Log.d("getSdkStorageRooPath", IMApplication.getContext().getCacheDir().getAbsolutePath());

        WritableMap videoDic = Arguments.createMap();
        MsgAttachment attachment = item.getAttachment();
        Map<String, Object> localExtension = item.getLocalExtension();

        if (attachment instanceof VideoAttachment) {
            VideoAttachment videoAttachment = (VideoAttachment) attachment;
            videoDic.putString(MessageConstant.MediaFile.URL, videoAttachment.getUrl());
            videoDic.putBoolean("isReplacePathSuccess", (Boolean) localExtension.get("isReplacePathSuccess"));
            videoDic.putBoolean("needRefreshMessage", false);

            Boolean isFilePathDeleted = false;
            Log.d("videoAttachment.", videoAttachment.getPath() + "");

            if (localExtension.get("isReplacePathSuccess").equals(true) && videoAttachment.getPath() == null) {
                videoDic.putBoolean("isFilePathDeleted", true);
                isFilePathDeleted = true;
            }

            videoDic.putBoolean("isFilePathDeleted", isFilePathDeleted);

            if (!isFilePathDeleted) {
                if (videoAttachment.getPath() != null) {
                    if ((!videoAttachment.getPath().contains(".mp4")
                            || !videoAttachment.getPath().contains(item.getSessionId()))
                                && item.getStatus() == MsgStatusEnum.success) {
                        File newFile = replaceVideoPath(videoAttachment.getPath(), item.getSessionId(), "video", ".mp4");
                        if (newFile != null) {
                            videoAttachment.setPath(newFile.getPath());
                            item.setAttachment(videoAttachment);
                            getMsgService().updateIMMessageStatus(item);
                            videoDic.putString(MessageConstant.MediaFile.PATH, newFile.getPath());
                            videoDic.putString(MessageConstant.MediaFile.THUMB_PATH, newFile.getPath());

                            setLocalExtension(item, "isReplacePathSuccess", true);
                            videoDic.putBoolean("isReplacePathSuccess", true);

                            videoDic.putBoolean("needRefreshMessage", true);
                        }
                    } else {
                        videoDic.putString(MessageConstant.MediaFile.THUMB_PATH, videoAttachment.getPath());
                        videoDic.putString(MessageConstant.MediaFile.PATH, videoAttachment.getPath());
                    }
                } else {
                    videoDic.putString(MessageConstant.MediaFile.THUMB_PATH, videoAttachment.getPath());
                    videoDic.putString(MessageConstant.MediaFile.PATH, videoAttachment.getPath());
                }

                if (!isOriginVideoHasDownloaded(item)) {
                    DownloadCallback callback = new DownloadCallback() {
                        @Override
                        public void onSuccess(Void result) {
                            setLocalExtension(item, "downloadStatus", "success");
                            ReactCache.createMessage(item, true);
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
                    setLocalExtension(item, "downloadStatus", "downloading");

                    AbortableFuture future = getService(MsgService.class).downloadAttachment(item, videoAttachment.getThumbPath() == null);
                    future.setCallback(callback);
                }
                videoDic.putString(MessageConstant.MediaFile.DISPLAY_NAME, videoAttachment.getDisplayName());
                videoDic.putString(MessageConstant.MediaFile.HEIGHT, Integer.toString(videoAttachment.getHeight()));
                videoDic.putString(MessageConstant.MediaFile.WIDTH, Integer.toString(videoAttachment.getWidth()));
                videoDic.putString(MessageConstant.MediaFile.DURATION, Long.toString(videoAttachment.getDuration()));
                videoDic.putString(MessageConstant.MediaFile.SIZE, Long.toString(videoAttachment.getSize()));
            }
        }
//        }

        return videoDic;
    }

    public static WritableMap generateImageExtend(IMMessage item) {
        MsgAttachment attachment = item.getAttachment();
        WritableMap imageObj = Arguments.createMap();

        if (attachment instanceof ImageAttachment) {
            ImageAttachment imageAttachment = (ImageAttachment) attachment;

            Map<String, Object> localExtension = item.getLocalExtension();
            imageObj.putBoolean("isReplacePathSuccess", (Boolean) localExtension.get("isReplacePathSuccess"));
            imageObj.putBoolean("needRefreshMessage", false);

            Boolean isFilePathDeleted = false;
            Boolean isFileDownloading = true;
            Log.d("imageAttachment", imageAttachment.getPath() + "");
            Log.d("localExtension.get()", localExtension.get("isReplacePathSuccess") + "");

            if (localExtension.get("isReplacePathSuccess").equals(true) && imageAttachment.getPath() == null) {
                    imageObj.putBoolean("isFilePathDeleted", true);
                    isFilePathDeleted = true;
            }

            imageObj.putBoolean("isFilePathDeleted", isFilePathDeleted);

            if (!isFilePathDeleted) {
                if (imageAttachment.getPath() != null
                        && !imageAttachment.getPath().contains(item.getSessionId())
                            && item.getStatus() == MsgStatusEnum.success) {
                    File newFile = replaceVideoPath(imageAttachment.getPath(), item.getSessionId(), "image", "." + imageAttachment.getExtension());
                    if (newFile != null) {
                        imageAttachment.setPath(newFile.getPath());
                        item.setAttachment(imageAttachment);
                        getMsgService().updateIMMessageStatus(item);
                        imageObj.putString(MessageConstant.MediaFile.PATH, newFile.getPath());
                        imageObj.putString(MessageConstant.MediaFile.THUMB_PATH, newFile.getPath());

                        setLocalExtension(item, "isReplacePathSuccess", true);
                        imageObj.putBoolean("isReplacePathSuccess", true);
                        imageObj.putBoolean("needRefreshMessage", true);
                    }
                } else {
                    if (item.getDirect() == MsgDirectionEnum.Out) {
                        imageObj.putString(MessageConstant.MediaFile.THUMB_PATH, imageAttachment.getPath());
                    } else {
                        imageObj.putString(MessageConstant.MediaFile.THUMB_PATH, imageAttachment.getThumbPath());
                    }
                    imageObj.putString(MessageConstant.MediaFile.PATH, imageAttachment.getPath());
                    imageObj.putString(MessageConstant.MediaFile.URL, imageAttachment.getUrl());
                    imageObj.putString(MessageConstant.MediaFile.DISPLAY_NAME, imageAttachment.getDisplayName());
                    imageObj.putString(MessageConstant.MediaFile.HEIGHT, Integer.toString(imageAttachment.getHeight()));
                    imageObj.putString(MessageConstant.MediaFile.WIDTH, Integer.toString(imageAttachment.getWidth()));
                }

                SessionService.getInstance().downloadAttachment(item, imageAttachment.getThumbPath() == null);
            }
        }

        return imageObj;
    }

    public static WritableMap generateFileExtend(IMMessage item) {
        MsgAttachment attachment = item.getAttachment();
        WritableMap fileObj = Arguments.createMap();

        if (attachment instanceof FileAttachment) {
            FileAttachment fileAttachment = (FileAttachment) attachment;

            Map<String, Object> localExtension = item.getLocalExtension();
            fileObj.putBoolean("isReplacePathSuccess", (Boolean) localExtension.get("isReplacePathSuccess"));
            fileObj.putBoolean("needRefreshMessage", false);

            Boolean isFilePathDeleted = false;
            Boolean isFileDownloading = true;

            if (localExtension.get("isReplacePathSuccess").equals(true) && fileAttachment.getPath() == null) {
                fileObj.putBoolean("isFilePathDeleted", true);
                isFilePathDeleted = true;
            }

            fileObj.putBoolean("isFilePathDeleted", isFilePathDeleted);

            if (!isFilePathDeleted) {
                if (fileAttachment.getPath() != null
                        && !fileAttachment.getPath().contains(item.getSessionId())
                        && item.getStatus() == MsgStatusEnum.success) {
                    File newFile = replaceVideoPath(fileAttachment.getPath(), item.getSessionId(), "file", "." + fileAttachment.getExtension());
                    if (newFile != null) {
                        fileAttachment.setPath(newFile.getPath());
                        item.setAttachment(fileAttachment);
                        getMsgService().updateIMMessageStatus(item);
                        fileObj.putString("filePath", fileAttachment.getPath());
                        fileObj.putString("fileUrl", fileAttachment.getUrl());
                        fileObj.putString("fileName", item.getContent());
                        fileObj.putString("fileMd5", fileAttachment.getMd5());
                        fileObj.putString("fileSize", FileUtil.formatFileSize(fileAttachment.getSize()));
                        fileObj.putString("fileType", fileAttachment.getExtension());

                        setLocalExtension(item, "isReplacePathSuccess", true);
                        fileObj.putBoolean("isReplacePathSuccess", true);
                        fileObj.putBoolean("needRefreshMessage", true);
                    }
                } else {
                    if (item.getDirect() == MsgDirectionEnum.Out) {
                        fileObj.putString(MessageConstant.MediaFile.PATH, fileAttachment.getPath());
                    } else {
                        fileObj.putString(MessageConstant.MediaFile.PATH, fileAttachment.getPath());
                    }
                    fileObj.putString("filePath", fileAttachment.getPath());
                    fileObj.putString("fileUrl", fileAttachment.getUrl());
                    fileObj.putString("fileName", item.getContent());
                    fileObj.putString("fileMd5", fileAttachment.getMd5());
                    fileObj.putString("fileSize", FileUtil.formatFileSize(fileAttachment.getSize()));
                    fileObj.putString("fileType", fileAttachment.getExtension());
                }

                SessionService.getInstance().downloadAttachment(item, fileAttachment.getThumbPath() == null);
            }
        }

        return fileObj;
    }

    public static WritableMap generateRecordExtend(IMMessage item) {
        MsgAttachment attachment = item.getAttachment();
        WritableMap audioObj = Arguments.createMap();

        if (attachment instanceof AudioAttachment) {
            AudioAttachment audioAttachment = (AudioAttachment) attachment;

            Map<String, Object> localExtension = item.getLocalExtension();
            audioObj.putBoolean("isReplacePathSuccess", (Boolean) localExtension.get("isReplacePathSuccess"));
            audioObj.putBoolean("needRefreshMessage", false);
            Log.d("audioAttachment.ge", audioAttachment.getPath() + "");
            Boolean isFilePathDeleted = false;

            if (localExtension.get("isReplacePathSuccess").equals(true) && audioAttachment.getPath() == null) {
                audioObj.putBoolean("isFilePathDeleted", true);
                isFilePathDeleted = true;
            }

            audioObj.putBoolean("isFilePathDeleted", isFilePathDeleted);

            if (!isFilePathDeleted) {
                if (audioAttachment.getPath() != null
                        && !audioAttachment.getPath().contains(item.getSessionId())
                         && item.getStatus() == MsgStatusEnum.success) {
                        File newFile = replaceVideoPath(audioAttachment.getPath(), item.getSessionId(), "audio", "." + audioAttachment.getExtension());
                    if (newFile != null) {
                        audioAttachment.setPath(newFile.getPath());
                        item.setAttachment(audioAttachment);
                        getMsgService().updateIMMessageStatus(item);
                        audioObj.putString(MessageConstant.MediaFile.PATH, newFile.getPath());
                        audioObj.putString(MessageConstant.MediaFile.THUMB_PATH, newFile.getPath());
                        audioObj.putString(MessageConstant.MediaFile.DURATION, Long.toString(audioAttachment.getDuration()));

                        setLocalExtension(item, "isReplacePathSuccess", true);
                        audioObj.putBoolean("isReplacePathSuccess", true);
                        audioObj.putBoolean("needRefreshMessage", true);
                    }
                } else {
                    audioObj.putString(MessageConstant.MediaFile.PATH, audioAttachment.getPath());
                    audioObj.putString(MessageConstant.MediaFile.THUMB_PATH, audioAttachment.getThumbPath());
                    audioObj.putString(MessageConstant.MediaFile.URL, audioAttachment.getUrl());
                    audioObj.putString(MessageConstant.MediaFile.DURATION, Long.toString(audioAttachment.getDuration()));
                }
                if (item.getStatus() == MsgStatusEnum.read) {
                    audioObj.putBoolean(MessageConstant.MediaFile.IS_PLAYED, true);
                } else {
                    audioObj.putBoolean(MessageConstant.MediaFile.IS_PLAYED, false);
                }
                SessionService.getInstance().downloadAttachment(item, false);
            }
        }

       return audioObj;
    }

    /**
     * <br/>uuid 消息ID
     * <br/>sessionId 会话id
     * <br/>sessionType  会话类型
     * <br/>fromNick 发送人昵称
     * <br/>msgType  消息类型
     * <br/>status 消息状态
     * <br/>direct 发送或接收
     * <br/>content 发送内容
     * <br/>time 发送时间
     * <br/>fromAccount 发送人账号
     *
     * @param item
     * @return
     */
    public static WritableMap createMessage(IMMessage item, boolean isNoti) {
        WritableMap itemMap = Arguments.createMap();
        itemMap.putString(MessageConstant.Message.MSG_ID, item.getUuid());
        RecentContact recent = NIMClient.getService(MsgService.class).queryRecentContact(item.getSessionId(), item.getSessionType());
        Map<String, Object> messageLocalExt = item.getLocalExtension();

        WritableMap localExt = Arguments.createMap();

        if (messageLocalExt != null) {
            String chatBotType = (String) messageLocalExt.get("chatBotType");
            Boolean isCancelResend = (Boolean) messageLocalExt.get("isCancelResend");
            Boolean isSentBirthday = (Boolean) messageLocalExt.get("isSentBirthday");
            String notificationType = (String) messageLocalExt.get("notificationType");
            String birthdayMemberContactId = (String) messageLocalExt.get("birthdayMemberContactId");
            String birthdayMemberName = (String) messageLocalExt.get("birthdayMemberName");

            if (chatBotType != null) {
                localExt.putString("chatBotType", chatBotType);
            }

            if (isCancelResend != null) {
                localExt.putBoolean("isCancelResend", isCancelResend);
            }

            if (isSentBirthday != null) {
                localExt.putBoolean("isSentBirthday", isSentBirthday);
            }

            if (notificationType != null) {
                localExt.putString("notificationType", notificationType);
            }

            if (birthdayMemberName != null) {
                localExt.putString("birthdayMemberName", birthdayMemberName);
            }

            if (birthdayMemberContactId != null) {
                localExt.putString("birthdayMemberContactId", birthdayMemberContactId);
            }
        }

        Boolean isCsr = false;
        Boolean isChatBot = false;


        if (recent != null) {
            Map<String, Object> extension = recent.getExtension();
            if (extension != null) {
                Boolean extensionIsCsr = (Boolean) extension.get("isCsr");
                Boolean extensionIsChatBot = (Boolean) extension.get("isChatBot");

                if (extensionIsChatBot != null) {
                    isChatBot = extensionIsChatBot;
                }
                if (extensionIsCsr != null) {
                    isCsr = extensionIsCsr;
                }
            }
        }

        itemMap.putMap("localExt", localExt);

        if (item.getMsgType() == MsgTypeEnum.custom) {
            itemMap.putString(MessageConstant.Message.MSG_TYPE, getMessageType(item.getMsgType(), (CustomAttachment) item.getAttachment()));
        } else {
            if (item.getRemoteExtension() != null) {
                Map<String, Object> extensionMsg = item.getRemoteExtension();

                if (extensionMsg.containsKey("extendType")) {
                    String extendType = extensionMsg.get("extendType").toString();
                    if (extendType.equals("forwardMultipleText")) {
                        WritableMap extend = Arguments.createMap();

                        extend.putString("messages", item.getContent());
                        itemMap.putMap(MESSAGE_EXTEND, extend);
                        itemMap.putString(MessageConstant.Message.MSG_TYPE, "forwardMultipleText");
                    }

                    if (extendType.equals("card")) {
                        WritableMap writableMapExtend = new WritableNativeMap();
                        for (Map.Entry<String, Object> entry : extensionMsg.entrySet()) {
                            writableMapExtend.putString(entry.getKey(), entry.getValue().toString());
                        }

                        itemMap.putMap(MESSAGE_EXTEND, writableMapExtend);
                        itemMap.putString(MessageConstant.Message.MSG_TYPE, "card");
                    }

                    if (extendType.equals("revoked_success")) {
                        WritableMap writableMapExtend = new WritableNativeMap();
                        writableMapExtend.putString("tipMsg", item.getContent());

                        itemMap.putMap(MESSAGE_EXTEND, writableMapExtend);
                        itemMap.putString(MessageConstant.Message.MSG_TYPE, "notification");
                    }

                    if (extendType.equals("gif")) {
                        WritableMap extend = new WritableNativeMap();
                        String pathGif = extensionMsg.get("path").toString();
                        String aspectRatioGif = extensionMsg.get("aspectRatio").toString();
                        Float aspectRatio = Float.parseFloat(aspectRatioGif);

                        extend.putString("extendType", extendType);
                        extend.putString("path", pathGif);
                        extend.putDouble("aspectRatio", aspectRatio);

                        itemMap.putMap(MESSAGE_EXTEND, extend);
                        itemMap.putString(MessageConstant.Message.MSG_TYPE, "image");
                    }

                    if (extendType.equals("TEAM_NOTIFICATION_MESSAGE")) {
                        WritableMap extend = new WritableNativeMap();

                        itemMap.putMap(MESSAGE_EXTEND, MapUtil.mapToReadableMap(extensionMsg));
                        itemMap.putString(MessageConstant.Message.MSG_TYPE, "notification");
                    }
                }


            } else {
                itemMap.putString(MessageConstant.Message.MSG_TYPE, getMessageType(item.getMsgType(), null));
            }
        }

        itemMap.putString(MessageConstant.Message.TIME_STRING, Long.toString(item.getTime() / 1000));
        itemMap.putString(MessageConstant.Message.SESSION_ID, item.getSessionId());
        itemMap.putString(MessageConstant.Message.SESSION_TYPE, Integer.toString(item.getSessionType().getValue()));

        itemMap.putBoolean(MessageConstant.Message.IS_OUTGOING, item.getDirect().getValue() == 0);
        itemMap.putString(MessageConstant.Message.STATUS, getMessageStatus(item.getStatus()));
        itemMap.putString(MessageConstant.Message.ATTACH_STATUS, Integer.toString(item.getAttachStatus().getValue()));
        itemMap.putString(MessageConstant.Message.IS_REMOTE_READ, boolean2String(receiveReceiptCheck(item)));

        WritableMap user = Arguments.createMap();
        String fromAccount = item.getFromAccount();
        String avatar = null;

        String fromNick = null;
        String displayName = null;
        try {
            fromNick = item.getFromNick();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(fromAccount)) {
            if (item.getSessionType() == SessionTypeEnum.Team && !TextUtils.equals(LoginService.getInstance().getAccount(), fromAccount)) {
                displayName = getTeamUserDisplayName(item.getSessionId(), fromAccount);
            } else {
                if (isCsr && recent != null) {
                    Map<String, Object> extension = recent.getExtension();
                    if (extension != null) {
                        String csrName = (String) extension.get("name");

                        displayName = csrName != null ? csrName : "CSR";
                    } else {
                        displayName = "CSR";
                    }
                } else {
                    displayName = !TextUtils.isEmpty(fromNick) ? fromNick : NimUserInfoCache.getInstance().getUserDisplayName(fromAccount);
                }
            }
            avatar = NimUserInfoCache.getInstance().getAvatar(fromAccount);
        }
        user.putString(MessageConstant.User.DISPLAY_NAME, displayName);
        user.putString(MessageConstant.User.USER_ID, fromAccount);
        user.putString(MessageConstant.User.AVATAR_PATH, avatar);

        user.putBoolean("isChatBot", isChatBot);

        if (isCsr) {
            user.putBoolean("isCsr", true);
        }

        itemMap.putMap(MessageConstant.Message.FROM_USER, user);

        MsgAttachment attachment = item.getAttachment();
        String text = "";


        if (attachment != null) {
            Map<String, Object> localExtension = item.getLocalExtension();

            if (localExtension == null || !localExtension.containsKey("isReplacePathSuccess")) {
                Map<String, Object> newLocalExtension = setLocalExtension(item, "isReplacePathSuccess", false);
                Log.d("newLocalExtension", newLocalExtension.toString());

//                localExtension.putAll(newLocalExtension);
            }

            if (item.getMsgType() == MsgTypeEnum.image) {
                WritableMap imageObj = generateImageExtend(item);

                itemMap.putMap(MESSAGE_EXTEND, imageObj);
            } else if (item.getMsgType() == MsgTypeEnum.audio) {
                WritableMap audioObj = generateRecordExtend(item);

                itemMap.putMap(MESSAGE_EXTEND, audioObj);
            } else if (item.getMsgType() == MsgTypeEnum.video) {
                WritableMap videoDic = generateVideoExtend(item);

                itemMap.putMap(MESSAGE_EXTEND, videoDic);
            } else if (item.getMsgType() == MsgTypeEnum.file) {
                WritableMap fileObj = generateFileExtend(item);

                itemMap.putMap(MESSAGE_EXTEND, fileObj);
            } else if (item.getMsgType() == MsgTypeEnum.location) {
                WritableMap locationObj = Arguments.createMap();

                LocationAttachment locationAttachment = (LocationAttachment) item.getAttachment();
                if (locationAttachment instanceof LocationAttachment) {
                    locationObj.putString("latitude", String.valueOf(locationAttachment.getLatitude()));
                    locationObj.putString("longitude", String.valueOf(locationAttachment.getLongitude()));
                    locationObj.putString("title", locationAttachment.getAddress());

                    itemMap.putMap(MESSAGE_EXTEND, locationObj);
                }
            } else if (item.getMsgType() == MsgTypeEnum.notification) {
                if (item.getSessionType() == SessionTypeEnum.Team) {
                    WritableMap notiObj = Arguments.createMap();
//                    text = TeamNotificationHelper.getTeamNotificationText(item, item.getSessionId());

                    NotificationAttachment notiAttachment = (NotificationAttachment) attachment;
                    NotificationType operationType = notiAttachment.getType();
                    String sourceId = item.getFromAccount();

                    WritableMap sourceIdMap = Arguments.createMap();
                    sourceIdMap.putString("sourceName", getTeamUserDisplayName(item.getSessionId(), sourceId));
                    sourceIdMap.putString("sourceId", sourceId);

                    notiObj.putInt("operationType", operationType.getValue());
                    notiObj.putMap("sourceId", sourceIdMap);

                    switch (operationType) {
                        case InviteMember:
                        case KickMember:
                        case PassTeamApply:
                        case TransferOwner:
                        case AddTeamManager:
                        case RemoveTeamManager:
                        case AcceptInvite:
                        case MuteTeamMember:
                            MemberChangeAttachment memberAttachment = (MemberChangeAttachment) notiAttachment;
                            ArrayList<String> targets = memberAttachment.getTargets();

                            WritableArray targetsWritableArray = Arguments.createArray();

                            for (String targetId : targets) {
                                String targetName = getTeamUserDisplayName(item.getSessionId(), targetId);

                                WritableMap target = Arguments.createMap();
                                target.putString("targetName", targetName);
                                target.putString("targetId", targetId);

                                targetsWritableArray.pushMap(target);
                            }

                            if (operationType == NotificationType.MuteTeamMember) {
                                MuteMemberAttachment muteMemberAttachment = (MuteMemberAttachment) attachment;
                                notiObj.putString("isMute", muteMemberAttachment.isMute() ? "mute" : "unmute");
                            }

                            notiObj.putArray("targets", targetsWritableArray);
                            break;
                        case LeaveTeam:
                        case DismissTeam:
                            notiObj.putArray("targets", null);
                            break;
                        case UpdateTeam:
                            Map<TeamFieldEnum, String> mockUpKeys = new HashMap();
                            mockUpKeys.put(TeamFieldEnum.Name, "NIMTeamUpdateTagName");
                            mockUpKeys.put(TeamFieldEnum.Introduce, "NIMTeamUpdateTagIntro");
                            mockUpKeys.put(TeamFieldEnum.Announcement, "NIMTeamUpdateTagAnouncement");
                            mockUpKeys.put(TeamFieldEnum.VerifyType, "NIMTeamUpdateTagJoinMode");
                            mockUpKeys.put(TeamFieldEnum.ICON, "NIMTeamUpdateTagAvatar");
                            mockUpKeys.put(TeamFieldEnum.InviteMode, "NIMTeamUpdateTagInviteMode");
                            mockUpKeys.put(TeamFieldEnum.BeInviteMode, "NIMTeamUpdateTagBeInviteMode");
                            mockUpKeys.put(TeamFieldEnum.TeamUpdateMode, "NIMTeamUpdateTagUpdateInfoMode");
                            mockUpKeys.put(TeamFieldEnum.AllMute, "NIMTeamUpdateTagMuteMode");

                            UpdateTeamAttachment updateTeamAttachment = (UpdateTeamAttachment) attachment;
                            Set<Map.Entry<TeamFieldEnum, Object>> updateTeamAttachmentDetail = updateTeamAttachment.getUpdatedFields().entrySet();
                            WritableMap updateDetail = Arguments.createMap();

                            for (Map.Entry<TeamFieldEnum, Object> field : updateTeamAttachmentDetail) {
                                updateDetail.putString("type", mockUpKeys.get(field.getKey()));
                                updateDetail.putString("value", field.getValue().toString());

                                // Log.d("tét field.toString", field.toString() );
                                // Log.d("tét field.getKey()", field.getKey().toString() );
                                // Log.d("tét field.getValue()", field.getValue().toString() );
                            }
                            notiObj.putMap("updateDetail", updateDetail);
                            break;
                        default:
                            break;
                    }

                    itemMap.putMap(MESSAGE_EXTEND, notiObj);
                } else {
                    text = item.getContent();
                }
            }
        } else {
            text = item.getContent();
        }

        if (item.getMsgType() == MsgTypeEnum.text) {
            text = item.getContent();

        } else if (item.getMsgType() == MsgTypeEnum.tip) {
            if (TextUtils.isEmpty(item.getContent())) {
                Map<String, Object> content = item.getRemoteExtension();
                if (content != null && !content.isEmpty()) {
                    text = (String) content.get("content");
                }
                content = item.getLocalExtension();
                if (content != null && !content.isEmpty()) {
                    text = (String) content.get("content");
                }
                if (TextUtils.isEmpty(text)) {
                    text = "未知通知提醒";
                }
            } else {
                text = item.getContent();
            }
        }
        itemMap.putString(MessageConstant.Message.MSG_TEXT, text);

//        if (item.getDirect() == MsgDirectionEnum.In && itemMap.getMap(MESSAGE_EXTEND) != null && itemMap.getMap(MESSAGE_EXTEND).toHashMap().containsKey("needRefreshMessage") && itemMap.getMap(MESSAGE_EXTEND).getBoolean("needRefreshMessage")) {
//            WritableArray writableArray = Arguments.createArray();
//            writableArray.pushMap(itemMap);
//            ReactCache.emit(ReactCache.observeMsgStatus, writableArray);
//        }

        if (isNoti) {
            WritableArray a = Arguments.createArray();
            a.pushMap(itemMap);

            ReactCache.emit(ReactCache.observeMsgStatus, a);
        }

        return itemMap;
    }

    public static Object createAudioPlay(String type, long position) {
        WritableMap result = Arguments.createMap();
        result.putString("type", "play");
        result.putString("status", type);
        result.putString("playEnd", Long.toString(position));
        return result;
    }

    public static Object createAudioRecord(int recordPower, long currentTime) {
        WritableMap result = Arguments.createMap();

        result.putString("currentTime", Long.toString(currentTime));
        result.putString("recordPower", Integer.toString(recordPower));
        return result;
    }

    public static Object createAttachmentProgress(AttachmentProgress attachmentProgress) {
        WritableMap result = Arguments.createMap();
        result.putString("_id", attachmentProgress.getUuid());
        result.putString("total", Long.toString(attachmentProgress.getTotal()));
        result.putString("transferred", Long.toString(attachmentProgress.getTransferred()));

        return result;
    }
}
