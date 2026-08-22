//
//  ChatroomViewController.m
//  RNNeteaseIm
//
//  Created by Rêu on 5/9/24.
//  Copyright © 2024 Kinooo. All rights reserved.
//

/**
 * FLOW: chatroom-enter-message
 * ROLE: Implement bridge chatroom V2 cho iOS — mirror 1-1 với android ChatroomV2Service.java.
 * BREAKS: Lệch tên method/event/key với Android -> JS phải rẽ nhánh platform. Bỏ destroyInstance
 *         khi thoát -> listener phòng cũ vẫn bắn, tin nhắn nhân đôi khi vào lại phòng.
 */

#import "ChatroomViewController.h"
#import <React/RCTEventDispatcher.h>
#import "NIMSDK+ZYZJ.h"
#import "ImConfig.h"

// Tên event — phải trùng tuyệt đối ReactCache.java (Android) và NIMEventListenerEnum (JS).
static NSString *const kEventStatus            = @"observeChatroomStatus";
static NSString *const kEventKicked            = @"observeChatroomKicked";
static NSString *const kEventMessage           = @"observeChatroomMessage";
static NSString *const kEventSendMessage       = @"observeChatroomSendMessage";
static NSString *const kEventMessageRevoked    = @"observeChatroomMessageRevoked";
static NSString *const kEventMemberIn          = @"observeChatroomMemberIn";
static NSString *const kEventMemberOut         = @"observeChatroomMemberOut";
static NSString *const kEventMemberRoleUpdated = @"observeChatroomMemberRoleUpdated";
static NSString *const kEventMemberInfoUpdated = @"observeChatroomMemberInfoUpdated";
static NSString *const kEventSelfBanned        = @"observeChatroomSelfBanned";
static NSString *const kEventChatBanned        = @"observeChatroomChatBanned";
static NSString *const kEventInfoUpdated       = @"observeChatroomInfoUpdated";

#pragma mark -- serialize (mirror fromInfo/fromMember/fromMessage của Android) --

/// nil-safe: mọi getter chuỗi của SDK đều nullable, mà NSDictionary literal thì nil = crash.
static NSString *RNNIMStr(NSString *value) {
    return value == nil ? @"" : value;
}

/// Gói V2NIMError thành NSError để wrapper bên RNNeteaseIm reject đúng mã lỗi NIM (113404, 102302,
/// 102404...) thay vì "-1" chung chung — ma trận test đọc thẳng mã này.
static NSError *RNNIMErrorToNSError(V2NIMError *error) {
    if (error == nil) {
        return [NSError errorWithDomain:@"NIMChatroom"
                                   code:-1
                               userInfo:@{NSLocalizedDescriptionKey: @"unknown error"}];
    }
    return [NSError errorWithDomain:@"NIMChatroom"
                               code:error.code
                           userInfo:@{NSLocalizedDescriptionKey: RNNIMStr(error.desc)}];
}

/// SDK iOS trả NSTimeInterval GIÂY, Android trả long MILI-giây. JS/store đọc chung một key nên
/// quy về ms tại đây; sai chỗ này thì phân trang history iOS lệch 1000 lần.
static double RNNIMToMillis(NSTimeInterval seconds) {
    return seconds * 1000.0;
}

/// Enum iOS là số, enum Android là tên hằng — map tay để hai bên ra cùng chuỗi.
/// ⚠️ Android đặt tên TIPS, iOS header đặt TIP: lấy theo Android vì JS đã chạy với Android.
static NSString *RNNIMMessageTypeName(V2NIMMessageType type) {
    switch (type) {
        case V2NIM_MESSAGE_TYPE_INVALID:      return @"V2NIM_MESSAGE_TYPE_INVALID";
        case V2NIM_MESSAGE_TYPE_TEXT:         return @"V2NIM_MESSAGE_TYPE_TEXT";
        case V2NIM_MESSAGE_TYPE_IMAGE:        return @"V2NIM_MESSAGE_TYPE_IMAGE";
        case V2NIM_MESSAGE_TYPE_AUDIO:        return @"V2NIM_MESSAGE_TYPE_AUDIO";
        case V2NIM_MESSAGE_TYPE_VIDEO:        return @"V2NIM_MESSAGE_TYPE_VIDEO";
        case V2NIM_MESSAGE_TYPE_LOCATION:     return @"V2NIM_MESSAGE_TYPE_LOCATION";
        case V2NIM_MESSAGE_TYPE_NOTIFICATION: return @"V2NIM_MESSAGE_TYPE_NOTIFICATION";
        case V2NIM_MESSAGE_TYPE_FILE:         return @"V2NIM_MESSAGE_TYPE_FILE";
        case V2NIM_MESSAGE_TYPE_AVCHAT:       return @"V2NIM_MESSAGE_TYPE_AVCHAT";
        case V2NIM_MESSAGE_TYPE_TIP:          return @"V2NIM_MESSAGE_TYPE_TIPS";
        case V2NIM_MESSAGE_TYPE_ROBOT:        return @"V2NIM_MESSAGE_TYPE_ROBOT";
        case V2NIM_MESSAGE_TYPE_CALL:         return @"V2NIM_MESSAGE_TYPE_CALL";
        case V2NIM_MESSAGE_TYPE_CUSTOM:       return @"V2NIM_MESSAGE_TYPE_CUSTOM";
    }
    return @"";
}

static NSString *RNNIMSendingStateName(V2NIMMessageSendingState state) {
    switch (state) {
        case V2NIM_MESSAGE_SENDING_STATE_UNKNOWN:   return @"V2NIM_MESSAGE_SENDING_STATE_UNKNOWN";
        case V2NIM_MESSAGE_SENDING_STATE_SUCCEEDED: return @"V2NIM_MESSAGE_SENDING_STATE_SUCCEEDED";
        case V2NIM_MESSAGE_SENDING_STATE_FAILED:    return @"V2NIM_MESSAGE_SENDING_STATE_FAILED";
        case V2NIM_MESSAGE_SENDING_STATE_SENDING:   return @"V2NIM_MESSAGE_SENDING_STATE_SENDING";
    }
    return @"";
}

static NSString *RNNIMStatusName(V2NIMChatroomStatus status) {
    switch (status) {
        case V2NIM_CHATROOM_STATUS_DISCONNECTED: return @"V2NIM_CHATROOM_STATUS_DISCONNECTED";
        case V2NIM_CHATROOM_STATUS_WAITING:      return @"V2NIM_CHATROOM_STATUS_WAITING";
        case V2NIM_CHATROOM_STATUS_CONNECTING:   return @"V2NIM_CHATROOM_STATUS_CONNECTING";
        case V2NIM_CHATROOM_STATUS_CONNECTED:    return @"V2NIM_CHATROOM_STATUS_CONNECTED";
        case V2NIM_CHATROOM_STATUS_ENTERING:     return @"V2NIM_CHATROOM_STATUS_ENTERING";
        case V2NIM_CHATROOM_STATUS_ENTERED:      return @"V2NIM_CHATROOM_STATUS_ENTERED";
        case V2NIM_CHATROOM_STATUS_EXITED:       return @"V2NIM_CHATROOM_STATUS_EXITED";
    }
    return @"";
}

static NSString *RNNIMKickedReasonName(V2NIMChatroomKickedReason reason) {
    switch (reason) {
        case V2NIM_CHATROOM_KICKED_REASON_UNKNOWN:           return @"V2NIM_CHATROOM_KICKED_REASON_UNKNOWN";
        case V2NIM_CHATROOM_KICKED_REASON_CHATROOM_INVALID:  return @"V2NIM_CHATROOM_KICKED_REASON_CHATROOM_INVALID";
        case V2NIM_CHATROOM_KICKED_REASON_BY_MANAGER:        return @"V2NIM_CHATROOM_KICKED_REASON_BY_MANAGER";
        case V2NIM_CHATROOM_KICKED_REASON_BY_CONFLICT_LOGIN: return @"V2NIM_CHATROOM_KICKED_REASON_BY_CONFLICT_LOGIN";
        case V2NIM_CHATROOM_KICKED_REASON_SILENTLY:          return @"V2NIM_CHATROOM_KICKED_REASON_SILENTLY";
        case V2NIM_CHATROOM_KICKED_REASON_BE_BLOCKED:        return @"V2NIM_CHATROOM_KICKED_REASON_BE_BLOCKED";
    }
    return @"";
}

/// Android strip tiền tố V2NIM_CHATROOM_MEMBER_ROLE_ trước khi trả JS — iOS trả thẳng phần đuôi.
static NSString *RNNIMRoleName(V2NIMChatroomMemberRole role) {
    switch (role) {
        case V2NIM_CHATROOM_MEMBER_ROLE_NORMAL:          return @"NORMAL";
        case V2NIM_CHATROOM_MEMBER_ROLE_CREATOR:         return @"CREATOR";
        case V2NIM_CHATROOM_MEMBER_ROLE_MANAGER:         return @"MANAGER";
        case V2NIM_CHATROOM_MEMBER_ROLE_NORMAL_GUEST:    return @"NORMAL_GUEST";
        case V2NIM_CHATROOM_MEMBER_ROLE_ANONYMOUS_GUEST: return @"ANONYMOUS_GUEST";
        case V2NIM_CHATROOM_MEMBER_ROLE_VIRTUAL:         return @"VIRTUAL";
    }
    return @"";
}

/// info nil -> dict RỖNG (không có cả roomId), khớp Android fromInfo.
static NSMutableDictionary *RNNIMFromInfo(V2NIMChatroomInfo *info) {
    NSMutableDictionary *map = [NSMutableDictionary dictionary];
    if (info == nil) {
        return map;
    }
    map[@"roomId"]           = RNNIMStr(info.roomId);
    map[@"name"]             = RNNIMStr(info.roomName);
    map[@"announcement"]     = RNNIMStr(info.announcement);
    map[@"broadcastUrl"]     = RNNIMStr(info.liveUrl);
    map[@"creatorAccountId"] = RNNIMStr(info.creatorAccountId);
    map[@"serverExtension"]  = RNNIMStr(info.serverExtension);
    map[@"onlineUserCount"]  = @(info.onlineUserCount);
    map[@"isChatBanned"]     = @(info.chatBanned);
    map[@"isValidRoom"]      = @(info.isValidRoom);
    return map;
}

static NSDictionary *RNNIMFromMember(V2NIMChatroomMember *member) {
    if (member == nil) {
        return @{};
    }
    // Key giữ đúng type NIMChatroomMember phía JS — đổi tên là vỡ ChatroomMembersStore mobile.
    // avatar và avatarThumbnail cùng nguồn roomAvatar (V2 không có thumbnail riêng), giống Android.
    return @{
        @"roomId":           RNNIMStr(member.roomId),
        @"userId":           RNNIMStr(member.accountId),
        @"nickname":         RNNIMStr(member.roomNick),
        @"avatar":           RNNIMStr(member.roomAvatar),
        @"avatarThumbnail":  RNNIMStr(member.roomAvatar),
        @"type":             RNNIMRoleName(member.memberRole),
        @"isOnline":         @(member.isOnline),
        @"isBlocked":        @(member.blocked),
        @"isMuted":          @(member.chatBanned),
        @"isTempMuted":      @(member.tempChatBanned),
        @"tempMuteDuration": @(member.tempChatBannedDuration),  // giây ở cả 2 platform, không quy đổi
        @"enterTime":        @(RNNIMToMillis(member.enterTime)),
        @"updateTime":       @(RNNIMToMillis(member.updateTime)),
    };
}

static NSArray *RNNIMFromMembers(NSArray<V2NIMChatroomMember *> *members) {
    NSMutableArray *array = [NSMutableArray array];
    for (V2NIMChatroomMember *member in members) {
        [array addObject:RNNIMFromMember(member)];
    }
    return array;
}

static NSDictionary *RNNIMFromMessage(V2NIMChatroomMessage *message) {
    if (message == nil) {
        return @{};
    }
    return @{
        @"msgId":           RNNIMStr(message.messageClientId),
        @"roomId":          RNNIMStr(message.roomId),
        @"fromAccount":     RNNIMStr(message.senderId),
        @"text":            RNNIMStr(message.text),
        @"msgType":         RNNIMMessageTypeName(message.messageType),
        @"serverExtension": RNNIMStr(message.serverExtension),
        @"sendingState":    RNNIMSendingStateName(message.sendingState),
        @"subType":         @(message.subType),
        @"timestamp":       @(RNNIMToMillis(message.createTime)),
        @"isSelf":          @(message.isSelf),
    };
}

static NSArray *RNNIMFromMessages(NSArray<V2NIMChatroomMessage *> *messages) {
    NSMutableArray *array = [NSMutableArray array];
    for (V2NIMChatroomMessage *message in messages) {
        [array addObject:RNNIMFromMessage(message)];
    }
    return array;
}

#pragma mark -- provider + listener nội bộ --

/// Dynamic token BẮT BUỘC đi qua provider, không qua enterParams.token — sai đường là 102302 [D-020].
/// ⚠️ Provider trả token CỐ ĐỊNH của lần enter này: token có hạn nên reconnect sau khi hết hạn sẽ
/// fail. Giống hạn chế đang có bên Android, Phase 1 chưa gặp vì phiên chatroom ngắn.
@interface RNNIMChatroomTokenProvider : NSObject <V2NIMChatroomTokenProvider>
@property (nonatomic, copy) NSString *token;
@end

@implementation RNNIMChatroomTokenProvider
- (NSString *)getToken:(NSString *)roomId accountId:(NSString *)accountId {
    return self.token;
}
@end

/// addr từ backend `chatroom/addr`. Không truyền thì để nil và chỉ dựa vào LBS — docs NIM yêu cầu
/// enableLbs và linkProvider không được cùng rỗng.
@interface RNNIMChatroomLinkProvider : NSObject <V2NIMChatroomLinkProvider>
@property (nonatomic, copy) NSArray<NSString *> *addrs;
@end

@implementation RNNIMChatroomLinkProvider
- (NSArray<NSString *> *)getLinkAddress:(NSString *)roomId accountId:(NSString *)accountId {
    return self.addrs;
}
@end

@interface ChatroomViewController (Internal)
- (void)emit:(NSString *)event body:(NSDictionary *)body;
@end

/// Delegate V2 KHÔNG truyền roomId, nên mỗi phòng cần một listener riêng mang sẵn roomId
/// (Android giải quyết y hệt bằng listener factory theo roomId).
@interface RNNIMChatroomListener : NSObject <V2NIMChatroomClientListener, V2NIMChatroomListener>
@property (nonatomic, copy) NSString *roomId;
@property (nonatomic, weak) ChatroomViewController *owner;
@end

@implementation RNNIMChatroomListener

- (NSMutableDictionary *)base {
    return [@{@"roomId": RNNIMStr(self.roomId)} mutableCopy];
}

- (void)send:(NSString *)event body:(NSDictionary *)body {
    [self.owner emit:event body:body];
}

#pragma mark V2NIMChatroomClientListener

- (void)onChatroomStatus:(V2NIMChatroomStatus)status error:(V2NIMError *)error {
    NSMutableDictionary *map = [self base];
    map[@"status"] = RNNIMStatusName(status);
    if (error != nil) {
        map[@"code"] = @(error.code);
        map[@"message"] = RNNIMStr(error.desc);
    }
    [self send:kEventStatus body:map];
}

- (void)onChatroomEntered {
    NSMutableDictionary *map = [self base];
    map[@"status"] = @"ENTERED";
    [self send:kEventStatus body:map];
}

- (void)onChatroomExited:(V2NIMError *)error {
    NSMutableDictionary *map = [self base];
    map[@"status"] = @"EXITED";
    [self send:kEventStatus body:map];
}

- (void)onChatroomKicked:(V2NIMChatroomKickedInfo *)kickedInfo {
    NSMutableDictionary *map = [self base];
    map[@"reason"] = kickedInfo == nil ? @"" : RNNIMKickedReasonName(kickedInfo.kickedReason);
    [self send:kEventKicked body:map];
}

#pragma mark V2NIMChatroomListener

- (void)onReceiveMessages:(NSArray *)messages {
    NSMutableDictionary *map = [self base];
    map[@"messages"] = RNNIMFromMessages(messages);
    [self send:kEventMessage body:map];
}

- (void)onSendMessage:(V2NIMChatroomMessage *)message {
    NSMutableDictionary *map = [self base];
    map[@"message"] = RNNIMFromMessage(message);
    [self send:kEventSendMessage body:map];
}

- (void)onChatroomMemberEnter:(V2NIMChatroomMember *)member {
    NSMutableDictionary *map = [self base];
    map[@"member"] = RNNIMFromMember(member);
    [self send:kEventMemberIn body:map];
}

- (void)onChatroomMemberExit:(NSString *)accountId {
    NSMutableDictionary *map = [self base];
    map[@"userId"] = RNNIMStr(accountId);
    [self send:kEventMemberOut body:map];
}

- (void)onChatroomMemberRoleUpdated:(V2NIMChatroomMemberRole)previousRole member:(V2NIMChatroomMember *)member {
    NSMutableDictionary *map = [self base];
    map[@"previousType"] = RNNIMRoleName(previousRole);
    map[@"member"] = RNNIMFromMember(member);
    [self send:kEventMemberRoleUpdated body:map];
}

- (void)onChatroomMemberInfoUpdated:(V2NIMChatroomMember *)member {
    NSMutableDictionary *map = [self base];
    map[@"member"] = RNNIMFromMember(member);
    [self send:kEventMemberInfoUpdated body:map];
}

// Cấm chat vĩnh viễn và cấm tạm thời dùng CHUNG một event JS, phân biệt bằng isTempMuted.
- (void)onSelfChatBannedUpdated:(BOOL)chatBanned {
    NSMutableDictionary *map = [self base];
    map[@"isMuted"] = @(chatBanned);
    map[@"isTempMuted"] = @(NO);
    [self send:kEventSelfBanned body:map];
}

- (void)onSelfTempChatBannedUpdated:(BOOL)tempChatBanned tempChatBannedDuration:(NSInteger)tempChatBannedDuration {
    NSMutableDictionary *map = [self base];
    map[@"isMuted"] = @(tempChatBanned);
    map[@"isTempMuted"] = @(tempChatBanned);
    map[@"tempMuteDuration"] = @(tempChatBannedDuration);
    [self send:kEventSelfBanned body:map];
}

// Không bọc roomId ở ngoài — cố ý giữ giống Android (onChatroomInfoUpdated emit thẳng fromInfo).
- (void)onChatroomInfoUpdated:(V2NIMChatroomInfo *)chatroomInfo {
    [self send:kEventInfoUpdated body:RNNIMFromInfo(chatroomInfo)];
}

- (void)onChatroomChatBannedUpdated:(BOOL)chatBanned {
    NSMutableDictionary *map = [self base];
    map[@"isChatBanned"] = @(chatBanned);
    [self send:kEventChatBanned body:map];
}

- (void)onMessageRevokedNotification:(NSString *)messageClientId messageTime:(NSTimeInterval)messageTime {
    NSMutableDictionary *map = [self base];
    map[@"msgId"] = RNNIMStr(messageClientId);
    map[@"revokeTime"] = @(RNNIMToMillis(messageTime));
    [self send:kEventMessageRevoked body:map];
}

// onChatroomTagsUpdated: KHÔNG implement — tag ngoài Phase 1, khớp Android (khỏi đẻ event thừa).

@end

#pragma mark -- controller --

@interface ChatroomViewController ()

/// roomId -> instance. V2 bind 1-1 instance <-> phòng; giữ map để exit/destroy đúng instance.
@property (nonatomic, strong) NSMutableDictionary<NSString *, V2NIMChatroomClient *> *clients;
/// Listener và provider phải được GIỮ STRONG: SDK không retain, thả ra là event im lặng biến mất.
@property (nonatomic, strong) NSMutableDictionary<NSString *, RNNIMChatroomListener *> *listeners;
@property (nonatomic, strong) NSMutableDictionary<NSString *, NSArray *> *providers;

@end

@implementation ChatroomViewController

+ (instancetype)initWithChatroomViewController {
    static ChatroomViewController *cvc = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        cvc = [[ChatroomViewController alloc] init];
    });
    return cvc;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _clients = [NSMutableDictionary dictionary];
        _listeners = [NSMutableDictionary dictionary];
        _providers = [NSMutableDictionary dictionary];
    }
    return self;
}

#pragma mark -- lifecycle --

// [FLOW:chatroom-enter-message #7] Enter phòng bằng DYNAMIC token; thứ tự các bước bên dưới không đổi được.
- (void)loginChatroom:(NSDictionary *)params success:(Success)success err:(Errors)err {
    NSString *roomId = RNNIMStr(params[@"roomId"]);
    if (roomId.length == 0) {
        err(@"roomId rỗng");
        return;
    }
    // Chặn sớm: thiếu accid/token thì NIM trả 102404/102302 rất khó lần ngược về nguyên nhân thật.
    NSString *accid = RNNIMStr(params[@"accid"]);
    NSString *token = RNNIMStr(params[@"token"]);
    if (accid.length == 0 || token.length == 0) {
        err(@"thiếu accid hoặc token");
        return;
    }

    @try {
        // Backend cấp appKey riêng, khác appKey app đăng ký lúc launch. Token chỉ hợp lệ dưới đúng
        // appKey đã cấp nó. No-op nếu đã khớp (login IM thường đã updateAppKey rồi).
        [self syncAppKey:RNNIMStr(params[@"appKey"])];

        // Vào lại phòng đang mở: dọn instance cũ TRƯỚC, nếu không listener cũ vẫn bắn -> tin nhân đôi.
        [self exitInternal:roomId];

        V2NIMChatroomClient *client = [V2NIMChatroomClient newInstance];
        self.clients[roomId] = client;

        RNNIMChatroomListener *listener = [[RNNIMChatroomListener alloc] init];
        listener.roomId = roomId;
        listener.owner = self;
        self.listeners[roomId] = listener;
        [client addChatroomClientListener:listener];
        [[client getChatroomService] addChatroomListener:listener];

        V2NIMChatroomEnterParams *enterParams = [[V2NIMChatroomEnterParams alloc] init];
        enterParams.accountId = accid;
        enterParams.roomNick = RNNIMStr(params[@"nickname"]);
        enterParams.roomAvatar = RNNIMStr(params[@"avatar"]);
        enterParams.enableLbs = YES;
        // Builder bên Android tự điền timeout mặc định 60s, còn iOS init thẳng struct nên field này
        // là 0. Set tay cho khớp mặc định trong docs, khỏi phụ thuộc SDK có coi 0 là "dùng default".
        enterParams.timeout = 60;

        NSMutableArray *retained = [NSMutableArray array];

        NSArray *addrs = [params[@"addrs"] isKindOfClass:NSArray.class] ? params[@"addrs"] : nil;
        if (addrs.count > 0) {
            RNNIMChatroomLinkProvider *linkProvider = [[RNNIMChatroomLinkProvider alloc] init];
            linkProvider.addrs = addrs;
            enterParams.linkProvider = linkProvider;
            [retained addObject:linkProvider];
        }

        // Backend cấp DYNAMIC token (JWT có hạn). Dynamic thì KHÔNG truyền qua enterParams.token —
        // phải khai authType + tokenProvider, nếu không NIM trả 102302 invalid token [D-020].
        // Nhánh static giữ lại để còn đối chứng nếu console đổi login policy; hiện KHÔNG dùng.
        if ([@"static" isEqualToString:RNNIMStr(params[@"authType"])]) {
            enterParams.token = token;
        } else {
            RNNIMChatroomTokenProvider *tokenProvider = [[RNNIMChatroomTokenProvider alloc] init];
            tokenProvider.token = token;
            [retained addObject:tokenProvider];

            V2NIMChatroomLoginOption *loginOption = [[V2NIMChatroomLoginOption alloc] init];
            loginOption.authType = V2NIM_LOGIN_AUTH_TYPE_DYNAMIC_TOKEN;
            loginOption.tokenProvider = tokenProvider;
            enterParams.loginOption = loginOption;
        }
        self.providers[roomId] = retained;

        __weak typeof(self) weakSelf = self;
        [client enter:roomId
          enterParams:enterParams
              success:^(V2NIMChatroomEnterResult *result) {
                  NSMutableDictionary *map = RNNIMFromInfo(result.chatroom);
                  map[@"isLoginSuccess"] = @(YES);
                  map[@"selfMember"] = RNNIMFromMember(result.selfMember);
                  success(map);
              }
              failure:^(V2NIMError *error) {
                  // enter fail -> instance vô dụng, không giữ lại (lần enter sau tạo mới).
                  [weakSelf exitInternal:roomId];
                  err(RNNIMErrorToNSError(error));
              }];
    } @catch (NSException *exception) {
        NSLog(@"ChatroomV2 enter lỗi: %@", exception.reason);
        err(RNNIMStr(exception.reason));
    }
}

/// Thoát phòng + huỷ instance. Gọi lại khi không ở trong phòng là no-op.
- (void)logoutChatroom:(NSString *)roomId success:(Success)success err:(Errors)err {
    @try {
        [self exitInternal:roomId];
        success(@(YES));
    } @catch (NSException *exception) {
        NSLog(@"ChatroomV2 exit lỗi: %@", exception.reason);
        err(RNNIMStr(exception.reason));
    }
}

/// Info phòng lấy từ cache của instance (không gọi mạng).
- (void)fetchChatroomInfo:(NSString *)roomId success:(Success)success err:(Errors)err {
    V2NIMChatroomClient *client = [self clientFor:roomId err:err];
    if (client == nil) {
        return;
    }
    V2NIMChatroomInfo *info = [client getChatroomInfo];
    NSMutableDictionary *map = RNNIMFromInfo(info);
    map[@"isLoginSuccess"] = @(info != nil);
    success(map);
}

#pragma mark -- message --

/// Gửi text. Thành công còn bắn thêm qua observeChatroomSendMessage.
- (void)sendTextMessage:(NSString *)roomId text:(NSString *)text success:(Success)success err:(Errors)err {
    V2NIMChatroomClient *client = [self clientFor:roomId err:err];
    if (client == nil) {
        return;
    }
    V2NIMChatroomMessage *message = [V2NIMChatroomMessageCreator createTextMessage:RNNIMStr(text)];
    [[client getChatroomService] sendMessage:message
                                      params:[[V2NIMSendChatroomMessageParams alloc] init]
                                     success:^(V2NIMSendChatroomMessageResult *result) {
                                         success(RNNIMFromMessage(result.message));
                                     }
                                     failure:^(V2NIMError *error) {
                                         err(RNNIMErrorToNSError(error));
                                     }
                                    progress:nil];
}

/**
 * Lịch sử tin nhắn (server giữ mặc định 10 ngày).
 * beginTime = 0 -> lấy từ hiện tại lùi về; phân trang bằng createTime của tin cũ nhất đang có.
 * V2 phân trang theo THỜI GIAN, không theo messageId như bản old-gen trước đây của iOS.
 */
- (void)fetchMessageHistory:(NSString *)roomId limit:(NSInteger)limit beginTime:(double)beginTimeMs orderBy:(NSString *)orderBy success:(Success)success err:(Errors)err {
    V2NIMChatroomClient *client = [self clientFor:roomId err:err];
    if (client == nil) {
        return;
    }
    V2NIMChatroomMessageListOption *option = [[V2NIMChatroomMessageListOption alloc] init];
    option.limit = limit <= 0 ? 20 : limit;
    // JS truyền ms (khớp Android), SDK iOS nhận giây.
    option.beginTime = beginTimeMs / 1000.0;
    option.direction = [@"ASC" caseInsensitiveCompare:RNNIMStr(orderBy)] == NSOrderedSame
        ? V2NIM_QUERY_DIRECTION_ASC
        : V2NIM_QUERY_DIRECTION_DESC;

    [[client getChatroomService] getMessageList:option
                                        success:^(NSArray<V2NIMChatroomMessage *> *messages) {
                                            success(RNNIMFromMessages(messages));
                                        }
                                        failure:^(V2NIMError *error) {
                                            err(RNNIMErrorToNSError(error));
                                        }];
}

#pragma mark -- member --

/// Member list phân trang. pageToken rỗng = trang đầu; kết quả trả kèm pageToken/finished.
- (void)fetchChatroomMembers:(NSString *)roomId limit:(NSInteger)limit pageToken:(NSString *)pageToken onlyOnline:(BOOL)onlyOnline success:(Success)success err:(Errors)err {
    V2NIMChatroomClient *client = [self clientFor:roomId err:err];
    if (client == nil) {
        return;
    }
    V2NIMChatroomMemberQueryOption *option = [[V2NIMChatroomMemberQueryOption alloc] init];
    option.limit = limit <= 0 ? 100 : limit;
    // Cờ do màn hình gọi quyết định, không hard-code ở đây (khớp Android).
    option.onlyOnline = onlyOnline;
    if (RNNIMStr(pageToken).length > 0) {
        option.pageToken = pageToken;
    }

    [[client getChatroomService] getMemberListByOption:option
                                               success:^(V2NIMChatroomMemberListResult *result) {
                                                   success(@{
                                                       @"pageToken": RNNIMStr(result.pageToken),
                                                       @"finished": @(result.finished),
                                                       @"members": RNNIMFromMembers(result.memberList),
                                                   });
                                               }
                                               failure:^(V2NIMError *error) {
                                                   err(RNNIMErrorToNSError(error));
                                               }];
}

/// Lấy member theo danh sách accountId (dùng cho profile tối thiểu khi bấm vào 1 người).
- (void)fetchChatroomMember:(NSString *)roomId accountIds:(NSArray<NSString *> *)accountIds success:(Success)success err:(Errors)err {
    V2NIMChatroomClient *client = [self clientFor:roomId err:err];
    if (client == nil) {
        return;
    }
    [[client getChatroomService] getMemberByIds:accountIds ?: @[]
                                        success:^(NSArray<V2NIMChatroomMember *> *members) {
                                            success(RNNIMFromMembers(members));
                                        }
                                        failure:^(V2NIMError *error) {
                                            err(RNNIMErrorToNSError(error));
                                        }];
}

#pragma mark -- legacy: recent-session list --

/// Giữ nguyên old-gen: NIMViewController cần info của phòng CHƯA vào (V2 chỉ có info của phòng đã
/// enter), nên không chuyển sang V2 được mà không đổi hành vi danh sách hội thoại.
- (NIMChatroom *)getChatroomInfo:(NSString *)roomId {
    __block NIMChatroom *chatroom = nil;
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);

    [[NIMSDK sharedSDK].chatroomManager fetchChatroomInfo:roomId completion:^(NSError *error, NIMChatroom *c) {
        if (error != nil) {
            NSLog(@"getChatroomInfo error: %@", error);
        } else {
            chatroom = c;
        }
        dispatch_semaphore_signal(semaphore);
    }];

    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
    return chatroom;
}

- (BOOL)checkChatroomLoginStatus:(NSString *)roomId {
    return self.clients[RNNIMStr(roomId)] != nil;
}

#pragma mark -- helpers --

- (V2NIMChatroomClient *)clientFor:(NSString *)roomId err:(Errors)err {
    V2NIMChatroomClient *client = self.clients[RNNIMStr(roomId)];
    if (client == nil) {
        err([NSString stringWithFormat:@"chưa vào phòng %@", RNNIMStr(roomId)]);
    }
    return client;
}

- (void)exitInternal:(NSString *)roomId {
    NSString *key = RNNIMStr(roomId);
    V2NIMChatroomClient *client = self.clients[key];
    if (client == nil) {
        return;
    }
    // Xoá khỏi clients ngay để mọi lời gọi sau đó rơi vào nhánh "chưa vào phòng".
    [self.clients removeObjectForKey:key];
    @try {
        [client exit];
    } @catch (NSException *exception) {
        NSLog(@"ChatroomV2 exit instance lỗi: %@", exception.reason);
    }
    // destroyInstance BẮT BUỘC sau exit: chỉ exit thì instance vẫn nằm trong getInstanceList() và
    // giữ listener -> rò bộ nhớ + event của phòng cũ vẫn bắn sau khi thoát.
    [V2NIMChatroomClient destroyInstance:[client getInstanceId]];
    // Chỉ nhả listener/provider SAU khi destroy. SDK có thể chỉ giữ listener yếu; nhả sớm thì object
    // chết ngay trước khi `exit` kịp bắn onChatroomExited -> JS mất event EXITED của lần thoát này.
    [self.listeners removeObjectForKey:key];
    [self.providers removeObjectForKey:key];
}

/// Đồng bộ appKey runtime với SDK. Chỉ so-và-đổi; KHÔNG dùng ensureRegisterV2WithAppKey: của
/// RNNeteaseIm vì hàm đó kèm setupCallKit, gọi với appKey chatroom sẽ khởi tạo CallKit sai key.
- (void)syncAppKey:(NSString *)appKey {
    @try {
        NSString *current = [[NIMSDK sharedSDK] appKey];
        if (appKey.length > 0 && ![appKey isEqualToString:current]) {
            V2NIMError *e = [[NIMSDK sharedSDK] updateAppKey:appKey];
            NSLog(@"IMTRACE_IOS chatroom updateAppKey(%@) prev=%@ -> %@", appKey, current,
                  e == nil ? @"OK" : ([NSString stringWithFormat:@"err %d %@", e.code, e.desc]));
        } else {
            NSLog(@"IMTRACE_IOS chatroom syncAppKey: appKey khớp SDK (%@), skip", current);
        }
    } @catch (NSException *exception) {
        NSLog(@"IMTRACE_IOS chatroom syncAppKey lỗi: %@", exception.reason);
    }
}

// [FLOW:chatroom-enter-message #9] Forward event realtime sang JS, cùng đường với các observe* khác.
- (void)emit:(NSString *)event body:(NSDictionary *)body {
    @try {
        [self.bridge.eventDispatcher sendDeviceEventWithName:event body:body];
    } @catch (NSException *exception) {
        NSLog(@"ChatroomV2 emit %@ lỗi: %@", event, exception.reason);
    }
}

@end
