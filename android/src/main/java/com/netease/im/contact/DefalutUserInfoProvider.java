package com.netease.im.contact;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.netease.im.R;
import com.netease.im.common.ImageLoaderKit;
import com.netease.im.uikit.cache.NimUserInfoCache;
import com.netease.im.uikit.cache.TeamDataCache;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

import static com.netease.im.MessageConstant.Card.sessionId;


/**
 * UIKit默认的用户信息提供者
 * <p>
 * Created by hzchenkang on 2016/12/19.
 */

public class DefalutUserInfoProvider implements UserInfoProvider {

    private Context context;

    public DefalutUserInfoProvider(Context context) {
        this.context = context;
    }

    @Override
    public UserInfo getUserInfo(String account) {
        UserInfo user = NimUserInfoCache.getInstance().getUserInfo(account);
        if (user == null) {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(account, null);
        }

        return user;
    }

    public int getDefaultIconResId() {
        return R.drawable.nim_avatar_default;
    }

    public Bitmap getAvatarForMessageNotifier(String account) {
        /**
         * 注意：这里最好从缓存里拿，如果读取本地头像可能导致UI进程阻塞，导致通知栏提醒延时弹出。
         */
        UserInfo user = getUserInfo(account);
        return (user != null) ? ImageLoaderKit.getNotificationBitmapFromCache(user.getAvatar()) : null;
    }

    @Override
    public String getDisplayNameForMessageNotifier(String account, String sessionId, SessionTypeEnum sessionType) {
        String nick = null;
        if (sessionType == SessionTypeEnum.P2P) {
            nick = NimUserInfoCache.getInstance().getAlias(account);
        } else if (sessionType == SessionTypeEnum.Team) {
            nick = TeamDataCache.getInstance().getTeamNick(sessionId, account);
            if (TextUtils.isEmpty(nick)) {
                nick = NimUserInfoCache.getInstance().getAlias(account);
            }
        }
        // 返回null，交给sdk处理。如果对方有设置nick，sdk会显示nick
        if (TextUtils.isEmpty(nick)) {
            return null;
        }

        return nick;
    }

    @Override
    public Bitmap getAvatarForMessageNotifier(SessionTypeEnum sessionType, String account) {
       /*
         * 注意：这里最好从缓存里拿，如果加载时间过长会导致通知栏延迟弹出！该函数在后台线程执行！
         */
        Bitmap bm = null;
        int defResId = R.drawable.nim_avatar_default;

        if (SessionTypeEnum.P2P == sessionType) {
            UserInfo user = getUserInfo(sessionId);
            bm = (user != null) ? ImageLoaderKit.getNotificationBitmapFromCache(user.getAvatar()) : null;
        } else if (SessionTypeEnum.Team == sessionType) {
            Team team = TeamDataCache.getInstance().getTeamById(account);
            bm = (team != null) ? ImageLoaderKit.getNotificationBitmapFromCache(team.getIcon()) : null;
            defResId = R.drawable.nim_avatar_group;
        }

        if (bm == null) {
            Drawable drawable = context.getResources().getDrawable(defResId);
            if (drawable instanceof BitmapDrawable) {
                bm = ((BitmapDrawable) drawable).getBitmap();
            }
        }
        return bm;
    }

    @Override
    public String getDisplayTitleForMessageNotifier(IMMessage message) {
        return null;
    }

    public Bitmap getTeamIcon(String teamId) {
        /**
         * 注意：这里最好从缓存里拿，如果读取本地头像可能导致UI进程阻塞，导致通知栏提醒延时弹出。
         */
        // 从内存缓存中查找群头像
        Team team = TeamDataCache.getInstance().getTeamById(teamId);
        if (team != null) {
            Bitmap bm = ImageLoaderKit.getNotificationBitmapFromCache(team.getIcon());
            if (bm != null) {
                return bm;
            }
        }

        // 默认图
        Drawable drawable = context.getResources().getDrawable(R.drawable.nim_avatar_group);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        return null;
    }
}
