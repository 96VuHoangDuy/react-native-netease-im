//
//  TeamViewController.m
//  NIM
//
//  Created by Dowin on 2017/5/4.
//  Copyright © 2017年 Dowin. All rights reserved.
//

#import "TeamViewController.h"
#import "ImConfig.h"
#import "UserStrangers.h"
#import "CacheUsers.h"

@interface TeamViewController ()<NIMTeamManagerDelegate>
{
  NSMutableArray *_myTeams;
}

@property (nonatomic, strong) NSDictionary *teamMemberStranger;

@end

@implementation TeamViewController


+(instancetype)initWithTeamViewController{
    static TeamViewController *teamVC = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        teamVC = [[TeamViewController alloc]init];
        
    });
    return teamVC;
}
-(void)initWithDelegate{
       [[NIMSDK sharedSDK].teamManager addDelegate:self];
       _myTeams = [self fetchTeams];
    NSMutableArray *teamArr = [NSMutableArray array];
    for (NIMTeam *team in _myTeams) {
        NSMutableDictionary *dic = [NSMutableDictionary dictionary];
        [dic setObject:[NSString stringWithFormat:@"%@",team.teamId] forKey:@"teamId"];
        [dic setObject:[NSString stringWithFormat:@"%@", team.teamName] forKey:@"name"];
        [dic setObject:[NSString stringWithFormat:@"%@", team.avatarUrl ] forKey:@"avatar"];
        [dic setObject:[NSString stringWithFormat:@"%ld", team.type] forKey:@"type"];
        NSArray *keys = [dic allKeys];
        for (NSString *tem  in keys) {
            if ([[dic objectForKey:tem] isEqualToString:@"(null)"]) {
                [dic setObject:@"" forKey:tem];
            }
        }

        [teamArr addObject:dic];
    }
    NIMModel *model = [NIMModel initShareMD];
    model.teamArr = teamArr;
}
-(void)getTeamList:(Success)succ Err:(Errors)err{
    _myTeams = [self fetchTeams];
    NSMutableArray *teamArr = [NSMutableArray array];
    for (NIMTeam *team in _myTeams) {
        NSMutableDictionary *dic = [NSMutableDictionary dictionary];
        [dic setObject:team.teamId forKey:@"teamId"];
        [dic setObject:[NSString stringWithFormat:@"%@", team.teamName] forKey:@"name"];
        [dic setObject:[NSString stringWithFormat:@"%@", team.avatarUrl ] forKey:@"avatar"];
        [dic setObject:[NSString stringWithFormat:@"%ld", team.type] forKey:@"type"];
        NSArray *keys = [dic allKeys];
        for (NSString *tem  in keys) {
            if ([[dic objectForKey:tem] isEqualToString:@"(null)"]) {
                [dic setObject:@"" forKey:tem];
            }
        }
        [teamArr addObject:dic];
    }
    if (teamArr) {
        succ(teamArr);
    }else{
        err(@"网络异常");
    }
}
//创建群组
-(void)createTeam:(NSDictionary *)fields type:(NSString *)type accounts:(NSArray *)accounts Succ:(Success)succ Err:(Errors)err{
    NIMCreateTeamOption *option = [[NIMCreateTeamOption alloc] init];
    option.joinMode   = NIMTeamJoinModeNoAuth;
     option.type       = NIMTeamTypeNormal;
    option.postscript = @"邀请你加入群组";
    option.name  = [fields objectForKey:@"name"]?[fields objectForKey:@"name"]:@"";
    option.intro  = [fields objectForKey:@"introduce"]?[fields objectForKey:@"introduce"]:@"";
    option.joinMode =  [[fields objectForKey:@"verifyType"]?[fields objectForKey:@"verifyType"]:@"0"  integerValue];
    option.inviteMode  = [[fields objectForKey:@"inviteMode"]?[fields objectForKey:@"inviteMode"]:@"1" integerValue];
    option.beInviteMode  = [[fields objectForKey:@"beInviteMode"]?[fields objectForKey:@"beInviteMode"]:@"1" integerValue];
    option.updateInfoMode  = [[fields objectForKey:@"teamUpdateMode"]?[fields objectForKey:@"teamUpdateMode"]:@"1" integerValue];

    if ([type isEqualToString:@"0"]) {
        option.type = NIMTeamTypeNormal;
    }
    if ([type isEqualToString:@"1"]){
        option.type = NIMTeamTypeAdvanced;
    }
    [[NIMSDK sharedSDK].teamManager createTeam:option users:accounts completion:^(NSError * _Nullable error, NSString * _Nullable teamId, NSArray<NSString *> * _Nullable failedUserIds) {
        if (!error) {
            NSDictionary *dic = @{@"teamId":teamId};
            succ(dic);
        }else{
            err(@"创建失败");
        }
    }];
}

//更新群资料
- (void)updateTeam:(NSString *)teamId fieldType:(NSString *)fieldType value:(NSString *)value Succ:(Success)succ Err:(Errors)err{
    if ([fieldType isEqualToString:@"name"]) {//群组名称
        [[NIMSDK sharedSDK].teamManager updateTeamName:value teamId:teamId completion:^(NSError * _Nullable error) {
            if (!error) {
                succ(@"200");
            }else{
                err(error);
            }
        }];
    }else if ([fieldType isEqualToString:@"icon"]) {//头像
        [[NIMSDK sharedSDK].teamManager updateTeamAvatar:value teamId:teamId completion:^(NSError * _Nullable error) {
            if (!error) {
                succ(@"200");
            }else{
                err(error);
            }
        }];
    }else if ([fieldType isEqualToString:@"introduce"]) {//群组介绍
        [[NIMSDK sharedSDK].teamManager updateTeamIntro:value teamId:teamId completion:^(NSError * _Nullable error) {
            if (!error) {
                succ(@"200");
            }else{
                err(error);
            }
        }];
    }else if ([fieldType isEqualToString:@"announcement"]) {//群组公告
        [[NIMSDK sharedSDK].teamManager updateTeamAnnouncement:value teamId:teamId completion:^(NSError * _Nullable error) {
            if (!error) {
                succ(@"200");
            }else{
                err(error);
            }
        }];
    }else if ([fieldType isEqualToString:@"verifyType"]) {//验证类型
        [[NIMSDK sharedSDK].teamManager updateTeamJoinMode:[value integerValue] teamId:teamId completion:^(NSError * _Nullable error) {
            if (!error) {
                succ(@"200");
            }else{
                err(error);
            }
        }];
    }else if ([fieldType isEqualToString:@"inviteMode"]) {//邀请他人类型
        [[NIMSDK sharedSDK].teamManager updateTeamInviteMode:[value integerValue] teamId:teamId completion:^(NSError * _Nullable error) {
            if (!error) {
                succ(@"200");
            }else{
                err(error);
            }
        }];
    }else if ([fieldType isEqualToString:@"beInviteMode"]) {//被邀请人权限
        [[NIMSDK sharedSDK].teamManager updateTeamBeInviteMode:[value integerValue] teamId:teamId completion:^(NSError * _Nullable error) {
            if (!error) {
                succ(@"200");
            }else{
                err(error);
            }
        }];
    }else if ([fieldType isEqualToString:@"teamUpdateMode"]) {//群资料修改权限
        [[NIMSDK sharedSDK].teamManager updateTeamUpdateInfoMode:[value integerValue] teamId:teamId completion:^(NSError * _Nullable error) {
            if (!error) {
                succ(@"200");
            }else{
                err(error);
            }
        }];
    }
}

//申请加入群组
-(void)applyJoinTeam:(NSString *)teamId message:(NSString *)message Succ:(Success)succ Err:(Errors)err{
    [[NIMSDK sharedSDK].teamManager applyToTeam:teamId message:message completion:^(NSError * _Nullable error, NIMTeamApplyStatus applyStatus) {
        if (!error) {
            switch (applyStatus) {
                case NIMTeamApplyStatusAlreadyInTeam:
                    succ(@"200");
                    break;
                case NIMTeamApplyStatusWaitForPass:
                    succ(@"申请成功，等待验证");
                default:
                    break;
            }
        }
        else{
            
            switch (error.code) {
                case NIMRemoteErrorCodeTeamAlreadyIn:
                    err(@"已经在群里");
                    break;
                default:
                    err(@"群申请失败");
                    break;
            }
        }
        
    
    }];
}

// -(void)getListTeamInfo:(NSArray *)teamIds Succ:(Success)succ Err:(Errors)err{
// //  NIMTeam *team =   [[NIMSDK sharedSDK].teamManager teamById:teamId];
//         /// completion 完成后的回调
//         NIMTeamFetchTeamInfoListHandler completion = ^(NSError * __nullable error,
//                                                        NSArray<NIMTeam *> * __nullable teams,
//                                                        NSArray<NSString *> * __nullable failedTeamIds)
//         {
//             if (error == nil) {
//                 /// 获取指定群ID信息 成功
//                 NSLog(@"[Fetch %lu teams succeeded, %lu failed in total.]", [teams count], [failedTeamIds count]);
//                 NSMutableArray *teamList = [NSMutableArray array];
                
//                 for (NIMTeam *team in teams) {
//                     NSDictionary *dic = [self convertTeamInfo:team];
//                     [teamList addObject:dic];
//                 }
                
//                 succ(teamList);
            
//             } else {
//                 /// 获取指定群ID信息 失败
//                 NSLog(@"[NSError message: %@]", error);
//             }
//         };
//         /// 获取指定群ID信息
//         [[[NIMSDK sharedSDK] teamManager] fetchTeamInfoList:teamIds
//                                                  completion:completion];
// }

- (NSDictionary *) convertTeamInfo:(NIMTeam *)team {
    NIMUser *creatorInfo = [[NIMSDK sharedSDK].userManager userInfo:team.owner];
    
    NSMutableDictionary *teamDic = [NSMutableDictionary dictionary];
    [teamDic setObject:[NSString stringWithFormat:@"%@",team.teamId] forKey:@"teamId"];
    [teamDic setObject:[NSString stringWithFormat:@"%@",team.teamName] forKey:@"name"];
    [teamDic setObject:[NSString stringWithFormat:@"%ld", team.type] forKey:@"type"];
    [teamDic setObject:[NSString stringWithFormat:@"%@",team.announcement]forKey:@"announcement"];
    [teamDic setObject:[NSString stringWithFormat:@"%@",team.owner] forKey:@"creator"];
    [teamDic setObject:[NSString stringWithFormat:@"%ld", team.memberNumber ] forKey:@"memberCount"];
    [teamDic setObject:[NSString stringWithFormat:@"%ld",team.level] forKey:@"memberLimit"];
    [teamDic setObject:[NSString stringWithFormat:@"%f", team.createTime ] forKey:@"createTime"];
    NSString *strMute = team.notifyStateForNewMsg == NIMTeamNotifyStateAll ? @"1" : @"0";
    [teamDic setObject:[NSString stringWithFormat:@"%@", strMute ] forKey:@"mute"];
    [teamDic setObject:[NSString stringWithFormat:@"%ld",team.joinMode] forKey:@"verifyType"];
    [teamDic setObject:[NSString stringWithFormat:@"%ld",team.beInviteMode] forKey:@"teamBeInviteMode"];

    if(creatorInfo != nil){
        [teamDic setObject:[NSString stringWithFormat:@"%@", creatorInfo.userInfo.nickName] forKey:@"creatorName"];
    }
    if (team.intro == nil || [team.intro isEqual:@"(null)"]) {
        [teamDic setObject:@"" forKey:@"introduce"];
    } else {
        [teamDic setObject:[NSString stringWithFormat:@"%@",team.intro] forKey:@"introduce"];
    }
    if (team.avatarUrl == nil || [team.avatarUrl isEqual:@"(null)"]) {
        [teamDic setObject:@"" forKey:@"avatar"];
    } else {
        [teamDic setObject:[NSString stringWithFormat:@"%@", team.avatarUrl] forKey:@"avatar"];
    }
    NSArray *keys = [teamDic allKeys];
    for (NSString *tem  in keys) {
        if ([[teamDic objectForKey:tem] isEqualToString:@"(null)"]) {
            [teamDic setObject:@"" forKey:tem];
        }
    }
    
    
    return teamDic;
}

//获取本地群资料
-(void)getTeamInfo:(NSString *)teamId Succ:(Success)succ Err:(Errors)err{
  NIMTeam *team = [[NIMSDK sharedSDK].teamManager teamById:teamId];
    if (team) {
        succ([self convertTeamInfo:team]);
    }

    else{
        err(@"获取群资料失败，请重新获取");
    }
}
//群成员禁言
-(void)setTeamMemberMute:(NSString *)teamId contactId:(NSString *)contactId mute:(NSString *)mute Succ:(Success)succ Err:(Errors)err{
    BOOL isMute = YES;
    if ([mute isEqualToString:@"1"]) {//禁言
        isMute = YES;
    }else{
        isMute = NO;
    }
    [[NIMSDK sharedSDK].teamManager updateMuteState:isMute userId:contactId inTeam:teamId completion:^(NSError * _Nullable error) {
        if (!error) {
            succ(@"200");
        }else{
            err(error);
        }
    }];
}
//更新群成员名片
- (void)updateMemberNick:(nonnull NSString *)teamId contactId:(nonnull NSString *)contactId nick:(nonnull NSString*)nick Succ:(Success)succ Err:(Errors)err{
    [[NIMSDK sharedSDK].teamManager updateUserNick:contactId newNick:nick inTeam:teamId completion:^(NSError * _Nullable error) {
        if (!error) {
            succ(@"200");
        }else{
            err(error);
        }
    }];
}

//获取远程资料
-(void)fetchTeamInfo:(NSString *)teamId Succ:(Success)succ Err:(Errors)err{
    [[NIMSDK sharedSDK].teamManager fetchTeamInfo:teamId completion:^(NSError * _Nullable error, NIMTeam * _Nullable team) {
        if (!error) {
            NSMutableDictionary *teamDic = [NSMutableDictionary dictionary];
            [teamDic setObject:[NSString stringWithFormat:@"%@",team.teamId] forKey:@"teamId"];
            [teamDic setObject:[NSString stringWithFormat:@"%@",team.teamName] forKey:@"name"];
            [teamDic setObject:[NSString stringWithFormat:@"%ld", team.type] forKey:@"type"];
            [teamDic setObject:[NSString stringWithFormat:@"%@",team.announcement]forKey:@"announcement"];
            [teamDic setObject:[NSString stringWithFormat:@"%@",team.owner] forKey:@"creator"];
            [teamDic setObject:[NSString stringWithFormat:@"%ld", team.memberNumber ] forKey:@"memberCount"];
            [teamDic setObject:[NSString stringWithFormat:@"%ld",team.level] forKey:@"memberLimit"];
            [teamDic setObject:[NSString stringWithFormat:@"%f", team.createTime ] forKey:@"createTime"];
            NSString *strMute = team.notifyStateForNewMsg == NIMTeamNotifyStateAll ? @"1" : @"0";
            [teamDic setObject:[NSString stringWithFormat:@"%@", strMute ] forKey:@"mute"];
            [teamDic setObject:[NSString stringWithFormat:@"%ld",team.joinMode] forKey:@"verifyType"];
            [teamDic setObject:[NSString stringWithFormat:@"%ld",team.beInviteMode] forKey:@"teamBeInviteMode"];
            [teamDic setObject:[NSString stringWithFormat:@"%ld",team.inviteMode] forKey:@"teamInviteMode"];
            [teamDic setObject:[NSString stringWithFormat:@"%ld",team.updateInfoMode] forKey:@"teamUpdateMode"];
            if (team.intro == nil || [team.intro isEqual:@"(null)"]) {
                [teamDic setObject:@"" forKey:@"introduce"];
            } else {
                [teamDic setObject:[NSString stringWithFormat:@"%@",team.intro] forKey:@"introduce"];
            }
            if (team.avatarUrl == nil || [team.avatarUrl isEqual:@"(null)"]) {
                [teamDic setObject:@"" forKey:@"avatar"];
            } else {
                [teamDic setObject:[NSString stringWithFormat:@"%@", team.avatarUrl] forKey:@"avatar"];
            }
            NSArray *keys = [teamDic allKeys];
            for (NSString *tem  in keys) {
                if ([[teamDic objectForKey:tem] isEqualToString:@"(null)"]) {
                    [teamDic setObject:@"" forKey:tem];
                }
            }
            succ(teamDic);
        }else{
            err(error);
        }
    }];
}

-(NSString *)getTeamNameDefault:(NSString *)teamId {
    NSMutableString *result = nil;
    NSString *nameCreator = @"";
    
    __block NSArray<NIMTeamMember *> *members = nil;
    
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    
    [[NIMSDK sharedSDK].teamManager fetchTeamMembers:teamId completion:^(NSError *error, NSArray<NIMTeamMember *> *m) {
        if (error != nil) {
            NSLog(@"getTeamNameDefault error: %@", error);
            dispatch_semaphore_signal(semaphore);
            return;
        }
        
        if (m == nil) {
            dispatch_semaphore_signal(semaphore);
            return;
        };
        
        members = m;
        dispatch_semaphore_signal(semaphore);
    }];
    
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);

    
    if (members != nil) {
        BOOL isFirstMember = YES;
        for(NIMTeamMember *member in members) {
            if (member != nil) {
                NIMUser *user = [[NIMSDK sharedSDK].userManager userInfo:member.userId];
                if (member.nickname == nil && (user == nil || user.userInfo.nickName == nil)) continue;
                NSString *memberName = member.nickname != nil ? member.nickname : user.userInfo.nickName;
                if (member.type == NIMTeamMemberTypeOwner) {
                    nameCreator = memberName;
                    continue;
                }
                
                if (isFirstMember) {
                    result = [NSMutableString stringWithString:[NSString stringWithFormat:@"%@", memberName]];
                    isFirstMember = NO;
                    continue;
                }
                
                [result appendString:[NSString stringWithFormat:@", %@", memberName]];
            }
        }
    }
    
    return [NSString stringWithFormat:@"%@, %@", nameCreator, result];
}

//获取群成员
-(void)getTeamMemberList:(NSString *)teamId Succ:(Success)succ Err:(Errors)err{

    [[NIMSDK sharedSDK].teamManager fetchTeamMembers:teamId completion:^(NSError * _Nullable error, NSArray<NIMTeamMember *> * _Nullable members) {
        if (!error) {
            NSMutableArray *arr = [NSMutableArray array];
            for (NIMTeamMember *member in members) {
                NSMutableDictionary *memb = [NSMutableDictionary dictionary];
                [memb setObject:[NSString stringWithFormat:@"%@", member.teamId] forKey:@"teamId"];
                [memb setObject:[NSString stringWithFormat:@"%@", member.userId] forKey:@"userId"];
                [memb setObject:[NSString stringWithFormat:@"%ld", member.type ] forKey:@"type"];
                [memb setObject:[NSString stringWithFormat:@"%@", member.nickname]  forKey:@"nickname"];
                [memb setObject:[NSString stringWithFormat:@"%d", member.isMuted]  forKey:@"isMute"];
                [memb setObject:[NSString stringWithFormat:@"%f", member.createTime]  forKey:@"createTime"];
                [memb setObject:[NSString stringWithFormat:@"%@", member.customInfo]  forKey:@"customInfo"];
                NIMUser   *user = [[NIMSDK sharedSDK].userManager userInfo:member.userId];
                BOOL isMe          = [member.userId isEqualToString:[NIMSDK sharedSDK].loginManager.currentAccount];
                BOOL isMyFriend    = [[NIMSDK sharedSDK].userManager isMyFriend:member.userId];
                BOOL isInBlackList = [[NIMSDK sharedSDK].userManager isUserInBlackList:member.userId];
                BOOL needNotify    = [[NIMSDK sharedSDK].userManager notifyForNewMsg:member.userId];
                [memb setObject:[NSString stringWithFormat:@"%@", user.userId] forKey:@"contactId"];
                [memb setObject:[NSString stringWithFormat:@"%@", user.alias] forKey:@"alias"];
                [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.nickName] forKey:@"name"];
                [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.avatarUrl] forKey:@"avatar"];
                [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.sign] forKey:@"signature"];
                [memb setObject:[NSString stringWithFormat:@"%ld", user.userInfo.gender ] forKey:@"gender"];
                [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.email] forKey:@"email"];
                [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.birth] forKey:@"birthday"];
                [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.mobile] forKey:@"mobile"];
                [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.ext] forKey:@"extension"];
                [memb setObject:[NSString stringWithFormat:@"%d",isMe] forKey:@"isMe"];
                [memb setObject:[NSString stringWithFormat:@"%d",isMyFriend] forKey:@"isMyFriend"];
                [memb setObject:[NSString stringWithFormat:@"%d",isInBlackList] forKey:@"isInBlackList"];
                [memb setObject:[NSString stringWithFormat:@"%d",needNotify] forKey:@"mute"];
                [memb setObject:@"" forKey:@"extensionMap"];
                NSArray *keys = [memb allKeys];
                for (NSString *tem  in keys) {
                    if ([[memb objectForKey:tem] isEqualToString:@"(null)"]) {
                        [memb setObject:@"" forKey:tem];
                    }
                }
                
//                if ([memb objectForKey:@"name"] != nil && [[memb objectForKey:@"name"] isEqual:@""] && [memb objectForKey:@"nickname"] != nil && [[memb objectForKey:@"nickname"] isEqual:@""]) {
                    NSDictionary *userWithCache = [[CacheUsers initWithCacheUsers] getUser:member.userId];
                    
                    if (userWithCache != nil) {
                        NSString *nameWithCache = [userWithCache objectForKey:@"nickname"];
                        NSString *avatarWithCache = [userWithCache objectForKey:@"avatar"];
                        NSString *genderWithCache = [userWithCache objectForKey:@"gender"];
                        if (nameWithCache != nil) {
                            [memb setObject:nameWithCache forKey:@"name"];
                        }
                        
                        if (avatarWithCache != nil && ![avatarWithCache isEqual:@"(null)"]) {
                            [memb setObject:avatarWithCache forKey:@"avatar"];
                        }
                        
                        NSString *gender = @"0";
                        if (gender != nil && [gender isEqual:@"male"]) {
                            gender = @"1";
                        }
                        
                        if (gender != nil && [gender isEqual:@"female"]) {
                            gender = @"2";
                        }
                        
                        [memb setObject:gender forKey:@"gender"];
                    } else {
                        [[UserStrangers initWithUserStrangers] setStranger:member.userId];
                    }
//                }
                
                [arr addObject:memb];
            }
        succ(arr);
        }else{
            err(error);
        }
    }];
}
//获取群成员资料及设置
- (void)fetchTeamMemberInfo:(NSString *)teamId contactId:(NSString *)contactId Succ:(Success)succ Err:(Errors)err{
    NIMTeamMember *member = [[NIMSDK sharedSDK].teamManager teamMember:contactId inTeam:teamId];
    NSMutableDictionary *memb = [NSMutableDictionary dictionary];
    [memb setObject:[NSString stringWithFormat:@"%@", member.teamId] forKey:@"teamId"];
    [memb setObject:[NSString stringWithFormat:@"%@", member.userId] forKey:@"userId"];
    [memb setObject:[NSString stringWithFormat:@"%ld", member.type ] forKey:@"type"];
    [memb setObject:[NSString stringWithFormat:@"%@", member.nickname]  forKey:@"nickname"];
    [memb setObject:[NSString stringWithFormat:@"%d", member.isMuted]  forKey:@"isMute"];
    [memb setObject:[NSString stringWithFormat:@"%f", member.createTime]  forKey:@"createTime"];
    [memb setObject:[NSString stringWithFormat:@"%@", member.customInfo]  forKey:@"customInfo"];
    NIMUser   *user = [[NIMSDK sharedSDK].userManager userInfo:member.userId];
    BOOL isMe          = [member.userId isEqualToString:[NIMSDK sharedSDK].loginManager.currentAccount];
    BOOL isMyFriend    = [[NIMSDK sharedSDK].userManager isMyFriend:member.userId];
    BOOL isInBlackList = [[NIMSDK sharedSDK].userManager isUserInBlackList:member.userId];
    BOOL needNotify    = [[NIMSDK sharedSDK].userManager notifyForNewMsg:member.userId];
    [memb setObject:[NSString stringWithFormat:@"%@", user.userId] forKey:@"contactId"];
    [memb setObject:[NSString stringWithFormat:@"%@", user.alias] forKey:@"alias"];
    [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.nickName] forKey:@"name"];
    [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.avatarUrl] forKey:@"avatar"];
    [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.sign] forKey:@"signature"];
    [memb setObject:[NSString stringWithFormat:@"%ld", user.userInfo.gender ] forKey:@"gender"];
    [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.email] forKey:@"email"];
    [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.birth] forKey:@"birthday"];
    [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.mobile] forKey:@"mobile"];
    [memb setObject:[NSString stringWithFormat:@"%@",user.userInfo.ext] forKey:@"extension"];
    [memb setObject:[NSString stringWithFormat:@"%d",isMe] forKey:@"isMe"];
    [memb setObject:[NSString stringWithFormat:@"%d",isMyFriend] forKey:@"isMyFriend"];
    [memb setObject:[NSString stringWithFormat:@"%d",isInBlackList] forKey:@"isInBlackList"];
    [memb setObject:[NSString stringWithFormat:@"%d",needNotify] forKey:@"mute"];
    [memb setObject:@"" forKey:@"extensionMap"];
    NSArray *keys = [memb allKeys];
    for (NSString *tem  in keys) {
        if ([[memb objectForKey:tem] isEqualToString:@"(null)"]) {
            [memb setObject:@"" forKey:tem];
        }
    }
    succ(memb);
}


//开启/关闭消息提醒
-(void)muteTeam:(NSString *)teamId mute:(NSString *)mute Succ:(Success)succ Err:(Errors)err{
    NSInteger notifyState = NIMTeamNotifyStateNone;//不接受任何群消息通知
    if ([mute isEqualToString:@"1"]) {
        notifyState = NIMTeamNotifyStateAll;
    }
    [[NIMSDK sharedSDK].teamManager updateNotifyState:notifyState inTeam:teamId completion:^(NSError * _Nullable error) {
         if (!error) {
             succ(@"200");
         }else{
             err(error);
         }
    }];

}
//解散群组
-(void)dismissTeams:(NSArray *)teamIds Succ:(Success)succ Err:(Errors)err{
    __block NSInteger completedCount = 0;
    __block BOOL hasErrorOccurred = NO;
    
    for (NSString *teamId in teamIds) {
        [[NIMSDK sharedSDK].teamManager dismissTeam:teamId completion:^(NSError *error) {
            if(hasErrorOccurred){
                return;
            }
            
            if(error){
                hasErrorOccurred = YES;
                err([NSString stringWithFormat:@"解散失败 code:%zd",error.code]);
                return;
            }
            
            completedCount++;
            if(completedCount == [teamIds count]){
                succ(@"200");
            }
        }];
    }
    
    

}
//拉人入群
-(void)addMembers:(NSString *)teamId accounts:(NSArray *)count type:(NSString *)type Succ:(Success)succ Err:(Errors)err{
    [[NIMSDK sharedSDK].teamManager addUsers:count toTeam:teamId postscript:@"" attach:type  completion:^(NSError *error, NSArray *members) {
        if (!error) {
            succ(@"200");
        }else{
            err([NSString stringWithFormat:@"邀请失败 code:%zd",error.code]);
        }
    }];

}
//踢人出群
-(void)removeMember:(NSString *)teamId accounts:(NSArray *)count Succ:(Success)succ Err:(Errors)err{
        [[NIMSDK sharedSDK].teamManager kickUsers:count fromTeam:teamId completion:^(NSError * _Nullable error) {
            if (!error) {
                succ(@"200");
            }else{
                err(@"移除失败");
            }
        }];
}
//主动退群
-(void)quitTeams:(NSArray *)teamIds Succ:(Success)succ Err:(Errors)err {
    __block NSInteger completedCount = 0;
    __block BOOL hasErrorOccurred = NO;

    for (NSString *teamId in teamIds) {
        [[NIMSDK sharedSDK].teamManager quitTeam:teamId completion:^(NSError * _Nullable error) {
            if (hasErrorOccurred) {
                return;
            }

            if (error) {
                hasErrorOccurred = YES;
                err(error);
                return;
            }

            completedCount++;
            if (completedCount == [teamIds count]) {
                succ(@"200");
            }
        }];
    }
}

//转让群组
-(void)transferManagerWithTeam:(NSString *)teamId
                    newOwnerId:(NSString *)newOwnerId quit:(NSString *)quit Succ:(Success)succ Err:(Errors)err{
    BOOL isLeave;
    if ([quit isEqualToString:@"1"]) {
        isLeave = true;
    }else{
        isLeave = false;
    }
    [[NIMSDK sharedSDK].teamManager transferManagerWithTeam:teamId newOwnerId:newOwnerId isLeave:isLeave completion:^(NSError * _Nullable error) {
        if (!error) {
            succ(@"200");
        }else{
            err(err);
        }
    }];
}

-(void)updateTeamAvatar:(NSString *)teamId avatarUrl:(NSString *)avatarUrl success:(Success)success error:(Errors)error {
    [[NIMSDK sharedSDK].teamManager updateTeamAvatar:avatarUrl teamId:teamId completion:^(NSError * _Nullable err) {
        if (err == nil) {
            success(@"200");
        }else{
            error(err);
        }
    }];
}

//修改群昵称
-(void)updateTeamName:(NSString *)teamId nick:(NSString *)nick Succ:(Success)succ Err:(Errors)err{
  [[NIMSDK sharedSDK].teamManager updateTeamName:nick teamId:teamId completion:^(NSError * _Nullable error) {
      if (!error) {
          succ(@"200");
      }else{
      err(err);
      }
  }];
}
-(void)stopTeamList{
     [[NIMSDK sharedSDK].teamManager removeDelegate:self];
}
- (NSMutableArray *)fetchTeams{
    NSMutableArray *myTeams = [[NSMutableArray alloc]init];
    for (NIMTeam *team in [NIMSDK sharedSDK].teamManager.allMyTeams) {
//        if (team.type == NIMTeamTypeNormal) {
            [myTeams addObject:team];
//        }
    }
    return myTeams;
}

@end
