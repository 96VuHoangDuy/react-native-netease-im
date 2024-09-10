//
//  ChatroomViewController.m
//  RNNeteaseIm
//
//  Created by Rêu on 5/9/24.
//  Copyright © 2024 Kinooo. All rights reserved.
//

#import "ChatroomViewController.h"
#import "ImConfig.h"
#import "NIMViewController.h"

@interface ChatroomViewController ()<NIMChatroomManagerDelegate> {
    NSDictionary *chatroomLoginStatus;
}

@property (nonatomic, strong) NSDictionary *charoomLoginStatus;

@end

@implementation ChatroomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
}

+ (instancetype)initWithChatroomViewController {
    static ChatroomViewController *cvc = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        cvc = [[ChatroomViewController alloc]init];
        
    });
    return cvc;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _charoomLoginStatus = [[NSMutableDictionary alloc] init];
    }
    
    return self;
}

- (void)updateChatroomLoginStatus:(NSString *)roomId isLogin:(BOOL)isLogin {
    NSMutableDictionary *chatroomLoginStatus = _charoomLoginStatus ? [_charoomLoginStatus mutableCopy] : [[NSMutableDictionary alloc] init];
    [chatroomLoginStatus setObject:roomId forKey:[NSNumber numberWithBool:isLogin]];
    _charoomLoginStatus = chatroomLoginStatus;
}

-(BOOL)checkChatroomLoginStatus:(NSString *)roomId {
    NSNumber *loginStatus = [_charoomLoginStatus objectForKey:roomId];
    if (loginStatus == nil) return NO;
    
    return [loginStatus boolValue];
}

- (void)loginChatroom:(NSDictionary *)params success:(Success)success err:(Errors)err {
    NSString *roomId = [params objectForKey:@"roomId"];
    NSString *nickname = [params objectForKey:@"nickname"];
    NSString *avatar = [params objectForKey:@"avatar"];
    if (roomId == nil || nickname == nil || avatar == nil) {
        err(@"missing params");
        return;
    }
    
    NIMChatroomEnterRequest *request = [[NIMChatroomEnterRequest alloc] init];
    request.roomAvatar = avatar;
    request.roomNickname = nickname;
    request.roomId = roomId;
    request.loginAuthType = NIMChatroomLoginAuthTypeDynamicToken;
    request.retryCount = 3;
    request.roomExt = @"";
    
    [[NIMSDK sharedSDK].chatroomManager enterChatroom:request completion:^(NSError *error, NIMChatroom *chatroom, NIMChatroomMember *member) {
        if (error != nil) {
            NSLog(@"login chat room error: %@", error);
            err(error);
            return;
        }
        
        NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
        [result setObject:chatroom.roomId forKey:@"roomId"];
        [result setObject:chatroom.name forKey:@"name"];
        [result setObject:[NSNumber numberWithLong:chatroom.onlineUserCount] forKey:@"onlineUserCount"];
        [result setObject:[NSNumber numberWithBool:YES] forKey:@"isLoginSuccess"];
        if (chatroom.announcement != nil) {
            [result setObject:chatroom.announcement forKey:@"announcement"];
        }
        if (chatroom.broadcastUrl) {
            [result setObject:chatroom.broadcastUrl forKey:@"broadcastUrl"];
        }
        
//        NIMSession *session = [NIMSession session:roomId type:NIMSessionTypeChatroom];
//        NSLog(@"loginChatroom session: %@",session);
//        NIMRecentSession *recent = [[NIMSDK sharedSDK].conversationManager recentSessionBySession:session];
//        NSLog(@"loginChatroom recent: %@", recent);
//        if (recent == nil) {
//            NIMAddEmptyRecentSessionBySessionOption *option = [[NIMAddEmptyRecentSessionBySessionOption alloc] init];
//            option.addEmptyMsgIfNoLastMsgExist = NO;
//            [[NIMSDK sharedSDK].conversationManager addEmptyRecentSessionBySession:session option:option];
//        }
        
        NSLog(@"loginChatroom result: %@", result);

        [self updateChatroomLoginStatus:roomId isLogin:YES];
        
        success(result);
    }];
}

- (void)logoutChatroom:(NSString *)roomId success:(Success)success err:(Errors)err {
    [[NIMSDK sharedSDK].chatroomManager exitChatroom:roomId completion:^(NSError *error) {
        if (error != nil) {
            NSLog(@"logoutChatroom error: %@", error);
            err(error);
        } else {
            [self updateChatroomLoginStatus:roomId isLogin:NO];
            
            success(@"success");
        }
    }];
}

- (NSDictionary *)fetchUserInfo:(NSString *)accId {
    __block NIMUser *user = nil;
    
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    
    [[NIMSDK sharedSDK].userManager fetchUserInfos:@[accId] completion:^(NSArray<NIMUser *> * __nullable users,NSError * __nullable error) {
        if (error != nil) {
            NSLog(@"fetchUserInfo error: %@", error);
            dispatch_semaphore_signal(semaphore);
            return;
        }
        
        if (users == nil || users.count == 0 || users.firstObject == nil) {
            dispatch_semaphore_signal(semaphore);
            return;
        }
        
        user = users.firstObject;
        dispatch_semaphore_signal(semaphore);
    }];
    
    if (user == nil) return nil;
    
    NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
    
    BOOL isMe          = [accId isEqualToString:[NIMSDK sharedSDK].loginManager.currentAccount];
    BOOL isMyFriend    = [[NIMSDK sharedSDK].userManager isMyFriend:accId];
    BOOL isInBlackList = [[NIMSDK sharedSDK].userManager isUserInBlackList:accId];
    BOOL needNotify    = [[NIMSDK sharedSDK].userManager notifyForNewMsg:accId];
    
    [result setObject:[NSString stringWithFormat:@"%d",isMe] forKey:@"isMe"];
    [result setObject:[NSString stringWithFormat:@"%d",isMyFriend] forKey:@"isMyFriend"];
    [result setObject:[NSString stringWithFormat:@"%d",isInBlackList] forKey:@"isInBlackList"];
    [result setObject:[NSString stringWithFormat:@"%d",!needNotify] forKey:@"mute"];
    [result setObject:[NSString stringWithFormat:@"%@", user.userId] forKey:@"contactId"];
    [result setObject:[NSString stringWithFormat:@"%@", user.alias] forKey:@"alias"];
    [result setObject:[NSString stringWithFormat:@"%@",user.userInfo.nickName] forKey:@"name"];
    [result setObject:[NSString stringWithFormat:@"%@",user.userInfo.avatarUrl] forKey:@"avatar"];
    [result setObject:[NSString stringWithFormat:@"%@",user.userInfo.sign] forKey:@"signature"];
    [result setObject:[NSString stringWithFormat:@"%ld", user.userInfo.gender ] forKey:@"gender"];
    [result setObject:[NSString stringWithFormat:@"%@",user.userInfo.email] forKey:@"email"];
    [result setObject:[NSString stringWithFormat:@"%@",user.userInfo.birth] forKey:@"birthday"];
    [result setObject:[NSString stringWithFormat:@"%@",user.userInfo.mobile] forKey:@"mobile"];
    [result setObject:[NSString stringWithFormat:@"%@",user.userInfo.ext] forKey:@"extension"];
    
    return result;
}

- (NIMChatroom *)getChatroomInfo:(NSString *)roomId  {
    __block NIMChatroom *chatroom = nil;
    
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    
    [[NIMSDK sharedSDK].chatroomManager fetchChatroomInfo:roomId completion:^(NSError *error, NIMChatroom *c) {
        if (error != nil) {
            NSLog(@"loginChatroom error: %@", error);
            dispatch_semaphore_signal(semaphore);
            return;
        }
        
        if (c == nil) {
            dispatch_semaphore_signal(semaphore);
            return;
        };
        
        chatroom = c;
        dispatch_semaphore_signal(semaphore);
    }];
    
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
    
    return chatroom;
}

- (void)fetchChatroomInfo:(NSString *)roomId success:(Success)success err:(Errors)err {
    [[NIMSDK sharedSDK].chatroomManager fetchChatroomInfo:roomId completion:^(NSError *error, NIMChatroom *chatroom) {
        if (error != nil) {
            err(error);
        } else {
            NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
            [result setObject:chatroom.roomId forKey:@"roomId"];
            [result setObject:chatroom.name forKey:@"name"];
            [result setObject:[NSNumber numberWithLong:chatroom.onlineUserCount] forKey:@"onlineUserCount"];
            if (chatroom.announcement != nil) {
                [result setObject:chatroom.announcement forKey:@"announcement"];
            }
            if (chatroom.broadcastUrl) {
                [result setObject:chatroom.broadcastUrl forKey:@"broadcastUrl"];
            }
            
            NSDictionary *creator = [self fetchUserInfo:chatroom.creator];
            if (creator != nil) {
                [result setObject:creator forKey:@"creator"];
            }
            
            success(result);
        }
    }];
}

-(void)fetchChatroomMember:(NSString *)roomId userId:(NSString *)userId success:(Success)success err:(Errors)err {
    NIMChatroomMembersByIdsRequest *request = [[NIMChatroomMembersByIdsRequest alloc] init];
    request.roomId = roomId;
    request.userIds = @[userId];
    
    [[NIMSDK sharedSDK].chatroomManager fetchChatroomMembersByIds:request completion:^(NSError *error, NSArray *members) {
        if (error != nil) {
            err(error);
            return;
        }
        
        if (members == nil || members.count != 1) {
            err(@"member not found");
            return;
        }
        
        NIMChatroomMember *member = members.firstObject;
        
        NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
        
        [result setObject:member.userId forKey:@"userId"];
        [result setObject:member.roomNickname forKey:@"nickname"];
        [result setObject:member.roomAvatar forKey:@"avatar"];
        [result setObject:member.roomAvatarThumbnail forKey:@"avatarThumbnail"];
        [result setObject:[self getMemberType:member.type] forKey:@"type"];
        [result setObject:[NSNumber numberWithBool:member.isMuted] forKey:@"isMuted"];
        [result setObject:[NSNumber numberWithBool:member.isTempMuted] forKey:@"isTempMuted"];
        [result setObject:[NSNumber numberWithLong:member.tempMuteDuration] forKey:@"tempMuteDuration"];
        [result setObject:[NSNumber numberWithBool:member.isOnline] forKey:@"isOnline"];
        
        success(result);
    }];
}

-(void)fetchChatroomMembers:(NSString *)roomId success:(Success)success err:(Errors)err {
    NIMChatroomMemberRequest *request = [[NIMChatroomMemberRequest alloc] init];
    request.roomId = roomId;
    request.type = NIMChatroomFetchMemberTypeTemp;
    request.limit = 100;
    
    [[NIMSDK sharedSDK].chatroomManager fetchChatroomMembers:request completion:^(NSError *error, NSArray<NIMChatroomMember *> *members) {
        if (error != nil) {
            NSLog(@"fetchChatroomMembers error: %@", error);
            err(error);
            return;
        }
        
        NSLog(@"member =>>>>> %@", members);
        
        if (members == nil || members.count == 0) {
            success(@[]);
            return;
        }

        NSMutableArray *result = [[NSMutableArray alloc] init];
        for(NIMChatroomMember *member in members) {
            if ([member.userId isEqual:[[NIMViewController initWithController] strAccount]]) continue;
            
            NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
            
            [dic setObject:member.userId forKey:@"userId"];
            [dic setObject:member.roomNickname forKey:@"nickname"];
            [dic setObject:member.roomAvatar forKey:@"avatar"];
            [dic setObject:member.roomAvatarThumbnail forKey:@"avatarThumbnail"];
            [dic setObject:[self getMemberType:member.type] forKey:@"type"];
            [dic setObject:[NSNumber numberWithBool:member.isMuted] forKey:@"isMuted"];
            [dic setObject:[NSNumber numberWithBool:member.isTempMuted] forKey:@"isTempMuted"];
            [dic setObject:[NSNumber numberWithLong:member.tempMuteDuration] forKey:@"tempMuteDuration"];
            [dic setObject:[NSNumber numberWithBool:member.isOnline] forKey:@"isOnline"];
            
            [result addObject:dic];
        }
        
        success(result);
    }];
}

-(NSString *)getMemberType:(NIMChatroomMemberType)memberType {
    switch (memberType) {
        case NIMChatroomMemberTypeGuest:
            return @"GUEST";
        
        case NIMChatroomMemberTypeLimit:
            return @"LIMIT";
        
        case NIMChatroomMemberTypeNormal:
            return @"NORMAL";
        
        case NIMChatroomMemberTypeCreator:
            return @"creator";
            
        case NIMChatroomMemberTypeManager:
            return @"MANAGER";
            
        default:
            return @"ANONYMOUS_GUEST";
    }
}

@end
