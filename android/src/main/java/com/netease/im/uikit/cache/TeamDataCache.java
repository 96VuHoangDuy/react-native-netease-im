package com.netease.im.uikit.cache;

import android.text.TextUtils;

import com.facebook.react.bridge.WritableMap;
import com.netease.im.ReactCache;
import com.netease.im.login.LoginService;
import com.netease.im.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.TeamServiceObserver;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 群信息/群成员数据监听&缓存
 * <p/>
 * Created by huangjun on 2015/3/1.
 */
public class TeamDataCache {
    final static String TAG = "TeamDataCache";
    private static TeamDataCache instance;

    public static synchronized TeamDataCache getInstance() {
        if (instance == null) {
            instance = new TeamDataCache();
        }

        return instance;
    }

    public void buildCache() {
        final List<Team> teams = NIMClient.getService(TeamService.class).queryTeamListBlock();
        LogUtil.w(TAG, "start build TeamDataCache");

        addOrUpdateTeam(teams);

        LogUtil.w(TAG, "build TeamDataCache completed, team count = " + teams.size());
    }

    public void clear() {
        clearTeamCache();
        clearTeamMemberCache();
    }

    /**
     * *
     * ******************************************** 观察者 ********************************************
     */

    public interface TeamDataChangedObserver {
        void onUpdateTeams(List<Team> teams);

        void onRemoveTeam(Team team);
    }

    public interface TeamMemberDataChangedObserver {
        void onUpdateTeamMember(List<TeamMember> members);

        void onRemoveTeamMember(List<TeamMember> members);
    }

    private List<TeamDataChangedObserver> teamObservers = new ArrayList<>();
    private List<TeamMemberDataChangedObserver> memberObservers = new ArrayList<>();

    public void registerObservers(boolean register) {
        NIMClient.getService(TeamServiceObserver.class).observeTeamUpdate(teamUpdateObserver, register);
        NIMClient.getService(TeamServiceObserver.class).observeTeamRemove(teamRemoveObserver, register);
        NIMClient.getService(TeamServiceObserver.class).observeMemberUpdate(memberUpdateObserver, register);
        NIMClient.getService(TeamServiceObserver.class).observeMemberRemove(memberRemoveObserver, register);
    }

    // 群资料变动观察者通知。新建群和群更新的通知都通过该接口传递
    private Observer<List<Team>> teamUpdateObserver = new Observer<List<Team>>() {
        @Override
        public void onEvent(final List<Team> teams) {
            if (teams != null) {
                LogUtil.w(TAG, "team update size:" + teams.size());
            }
            addOrUpdateTeam(teams);
            notifyTeamDataUpdate(teams);
        }
    };

    // 移除群的观察者通知。自己退群，群被解散，自己被踢出群时，会收到该通知
    private Observer<Team> teamRemoveObserver = new Observer<Team>() {
        @Override
        public void onEvent(Team team) {
            // team的flag被更新，isMyTeam为false
            addOrUpdateTeam(team);
            notifyTeamDataRemove(team);
        }
    };

    // 群成员资料变化观察者通知。可通过此接口更新缓存。
    private Observer<List<TeamMember>> memberUpdateObserver = new Observer<List<TeamMember>>() {
        @Override
        public void onEvent(List<TeamMember> members) {
            addOrUpdateTeamMembers(members);
            notifyTeamMemberDataUpdate(members);
        }
    };

    // 移除群成员的观察者通知。
    private Observer<List<TeamMember>> memberRemoveObserver = new Observer<List<TeamMember>>() {
        @Override
        public void onEvent(List<TeamMember> member) {
            // member的validFlag被更新，isInTeam为false
            addOrUpdateTeamMembers(member);
            notifyTeamMemberRemove(member);
        }
    };

    public void registerTeamDataChangedObserver(TeamDataChangedObserver o) {
        if (teamObservers.contains(o)) {
            return;
        }

        teamObservers.add(o);
    }

    public void unregisterTeamDataChangedObserver(TeamDataChangedObserver o) {
        teamObservers.remove(o);
    }

    public void registerTeamMemberDataChangedObserver(TeamMemberDataChangedObserver o) {
        if (memberObservers.contains(o)) {
            return;
        }

        memberObservers.add(o);
    }

    public void unregisterTeamMemberDataChangedObserver(TeamMemberDataChangedObserver o) {
        memberObservers.remove(o);
    }

    private void notifyTeamDataUpdate(List<Team> teams) {
        for (TeamDataChangedObserver o : teamObservers) {
            o.onUpdateTeams(teams);
        }
    }

    private void notifyTeamDataRemove(Team team) {
        for (TeamDataChangedObserver o : teamObservers) {
            o.onRemoveTeam(team);
        }
    }

    private void notifyTeamMemberDataUpdate(List<TeamMember> members) {
        for (TeamMemberDataChangedObserver o : memberObservers) {
            o.onUpdateTeamMember(members);
        }
    }

    private void notifyTeamMemberRemove(List<TeamMember> members) {
        for (TeamMemberDataChangedObserver o : memberObservers) {
            o.onRemoveTeamMember(members);
        }
    }

    /**
     * *
     * ******************************************** 群资料缓存 ********************************************
     */

    private Map<String, Team> id2TeamMap = new ConcurrentHashMap<>();

    public void clearTeamCache() {
        id2TeamMap.clear();
    }

    /**
     * 异步获取Team（先从SDK DB中查询，如果不存在，则去服务器查询）
     */
    public void fetchTeamById(final String teamId, final SimpleCallback<Team> callback) {
        NIMClient.getService(TeamService.class).queryTeam(teamId).setCallback(new RequestCallbackWrapper<Team>() {
            @Override
            public void onResult(int code, Team t, Throwable exception) {
                boolean success = true;
                if (code == ResponseCode.RES_SUCCESS) {
                    addOrUpdateTeam(t);
                } else {
                    success = false;
                    LogUtil.e(TAG, "fetchTeamById failed, code=" + code);
                }

                if (exception != null) {
                    success = false;
                    LogUtil.e(TAG, "fetchTeamById throw exception, e=" + exception.getMessage());
                }

                if (callback != null) {
                    callback.onResult(success, t);
                }
            }
        });
    }

    public Object fetchSyncTeamById(String teamId) throws Exception {
        Team result = getTeamById(teamId);
        if (result != null) {
            return ReactCache.createTeamInfo(result);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final Team[] resultHolder = new Team[1];

        fetchTeamById(teamId, new SimpleCallback<Team>() {
            @Override
            public void onResult(boolean success, Team result) {
                if (success && result != null) {
                    resultHolder[0] = result;
                }

                latch.countDown();
            }
        });

        latch.await();

        if (resultHolder[0] == null) {
            return null;
        }

        return ReactCache.createTeamInfo(resultHolder[0]);
    }

    /**
     * 同步从本地获取Team（先从缓存中查询，如果不存在再从SDK DB中查询）
     */
    public Team getTeamById(String teamId) {
        Team team = id2TeamMap.get(teamId);

        if (team == null) {
            team = NIMClient.getService(TeamService.class).queryTeamBlock(teamId);
            addOrUpdateTeam(team);
        }

        return team;
    }

    public String getTeamName(String teamId) {
        Team team = getTeamById(teamId);
        return team == null ? teamId : TextUtils.isEmpty(team.getName()) ? team.getId() : team
                .getName();
    }

    public List<Team> getAllTeams() {
        List<Team> teams = new ArrayList<>();
        for (Team t : id2TeamMap.values()) {
            if (t.isMyTeam()) {
                teams.add(t);
            }
        }
        return teams;
    }

    public List<Team> getAllAdvancedTeams() {
        return getAllTeamsByType(TeamTypeEnum.Advanced);
    }

    public List<Team> getAllNormalTeams() {
        return getAllTeamsByType(TeamTypeEnum.Normal);
    }

    private List<Team> getAllTeamsByType(TeamTypeEnum type) {
        List<Team> teams = new ArrayList<>();
        for (Team t : id2TeamMap.values()) {
            if (t.isMyTeam() && t.getType() == type) {
                teams.add(t);
            }
        }

        return teams;
    }

    public void addOrUpdateTeam(Team team) {
        if (team == null) {
            return;
        }

        id2TeamMap.put(team.getId(), team);
    }

    private void addOrUpdateTeam(List<Team> teamList) {
        if (teamList == null || teamList.isEmpty()) {
            return;
        }

        for (Team t : teamList) {
            if (t == null) {
                continue;
            }

            id2TeamMap.put(t.getId(), t);
        }
    }

    /**
     * *
     * ************************************** 群成员缓存(由App主动添加缓存) ****************************************
     */

    private Map<String, Map<String, TeamMember>> teamMemberCache = new ConcurrentHashMap<>();

    public void clearTeamMemberCache() {
        teamMemberCache.clear();
    }

    public Object fetchSyncTeamMemberList(String teamId) throws Exception {
        List<TeamMember> result = getTeamMemberList(teamId);
        if (result != null && !result.isEmpty()) {
            return ReactCache.createTeamMemberList(result);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final List<TeamMember>[] resultHolder = new List[1];

        fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> result) {
                if (success && result != null) {
                    resultHolder[0] = result;
                }

                latch.countDown();
            }
        });

        latch.await();

        if (resultHolder[0] == null) {
            return null;
        }

        return ReactCache.createTeamMemberList(resultHolder[0]);
    }

    /**
     * （异步）查询群成员资料列表（先从SDK DB中查询，如果本地群成员资料已过期会去服务器获取最新的。）
     */
    public void fetchTeamMemberList(final String teamId, final SimpleCallback<List<TeamMember>> callback) {
        NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {
                boolean success = true;
                if (code == ResponseCode.RES_SUCCESS) {
                    replaceTeamMemberList(teamId, members);
                } else {
                    success = false;
                    LogUtil.e(TAG, "fetchTeamMemberList failed, code=" + code);
                }

                if (exception != null) {
                    success = false;
                    LogUtil.e(TAG, "fetchTeamMemberList throw exception, e=" + exception.getMessage());
                }

                if (callback != null) {
                    callback.onResult(success, members);
                }
            }
        });
    }

    /**
     * 在缓存中查询群成员列表
     */
    public List<TeamMember> getTeamMemberList(String teamId) {
        List<TeamMember> members = new ArrayList<>();
        Map<String, TeamMember> map = teamMemberCache.get(teamId);
        if (map != null && !map.values().isEmpty()) {
            for (TeamMember m : map.values()) {
                if (m.isInTeam()) {
                    members.add(m);
                }
            }
        }

        return members;
    }

    /**
     * （异步）查询群成员资料（先从SDK DB中查询，如果本地群成员资料已过期会去服务器获取最新的。）
     */
    public void fetchTeamMember(final String teamId, final String account, final SimpleCallback<TeamMember> callback) {
        NIMClient.getService(TeamService.class).queryTeamMember(teamId, account).setCallback(new RequestCallbackWrapper<TeamMember>() {
            @Override
            public void onResult(int code, TeamMember member, Throwable exception) {
                boolean success = true;
                if (code == ResponseCode.RES_SUCCESS) {
                    addOrUpdateTeamMember(member);
                } else {
                    success = false;
                    LogUtil.e(TAG, "fetchTeamMember failed, code=" + code);
                }

                if (exception != null) {
                    success = false;
                    LogUtil.e(TAG, "fetchTeamMember throw exception, e=" + exception.getMessage());
                }

                if (callback != null) {
                    callback.onResult(success, member);
                }
            }
        });
    }

    /**
     * 查询群成员资料（先从缓存中查，如果没有则从SDK DB中查询）
     */
    public TeamMember getTeamMember(String teamId, String account) {
        Map<String, TeamMember> map = teamMemberCache.get(teamId);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            teamMemberCache.put(teamId, map);
        }

        if (!map.containsKey(account)) {
            TeamMember member = NIMClient.getService(TeamService.class).queryTeamMemberBlock(teamId, account);
            if (member != null) {
                map.put(account, member);
            }
        }

        return map.get(account);
    }

    /**
     * 获取显示名称。用户本人显示“我”
     *
     * @param tid
     * @param account
     * @return
     */
    public String getTeamMemberDisplayName(String tid, String account) {
//        if (TextUtils.equals(account, LoginService.getInstance().getAccount())) {
//            return "我";
//        }

        return getDisplayNameWithoutMe(tid, account);
    }

    /**
     * 获取显示名称。用户本人显示“你”
     *
     * @param tid
     * @param account
     * @return
     */
    public String getTeamMemberDisplayNameYou(String tid, String account) {
//        if (TextUtils.equals(account, LoginService.getInstance().getAccount())) {
//            return "你";
//        }

        return getDisplayNameWithoutMe(tid, account);
    }

    /**
     * 获取显示名称。用户本人也显示昵称
     * 高级群：首先返回群昵称。没有群昵称，则返回备注名。没有设置备注名，则返回用户昵称。
     * 讨论组：首先返回备注名。没有设置备注名，则返回用户昵称。
     */
    public String getDisplayNameWithoutMe(String tid, String account) {
        String memberNick = getTeamNick(tid, account);
        if (!TextUtils.isEmpty(memberNick)) {
            return memberNick;
        }

        String alias = NimUserInfoCache.getInstance().getAlias(account);
        if (!TextUtils.isEmpty(alias)) {
            return alias;
        }

        return NimUserInfoCache.getInstance().getUserName(account);
    }

    public String getTeamNick(String tid, String account) {
        Team team = getTeamById(tid);
        if (team != null && team.getType() == TeamTypeEnum.Advanced) {
            TeamMember member = getTeamMember(tid, account);
            if (member != null && !TextUtils.isEmpty(member.getTeamNick())) {
                return member.getTeamNick();
            }
        }
        return null;
    }

    private void replaceTeamMemberList(String tid, List<TeamMember> members) {
        if (members == null || members.isEmpty() || TextUtils.isEmpty(tid)) {
            return;
        }

        Map<String, TeamMember> map = teamMemberCache.get(tid);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            teamMemberCache.put(tid, map);
        } else {
            map.clear();
        }

        for (TeamMember m : members) {
            map.put(m.getAccount(), m);
        }
    }

    private void addOrUpdateTeamMember(TeamMember member) {
        if (member == null) {
            return;
        }

        Map<String, TeamMember> map = teamMemberCache.get(member.getTid());
        if (map == null) {
            map = new ConcurrentHashMap<>();
            teamMemberCache.put(member.getTid(), map);
        }

        map.put(member.getAccount(), member);
    }

    private void addOrUpdateTeamMembers(List<TeamMember> members) {
        for (TeamMember m : members) {
            addOrUpdateTeamMember(m);
        }
    }
}
