//
//  ChatroomViewController.h
//  RNNeteaseIm
//
//  Created by Rêu on 5/9/24.
//  Copyright © 2024 Kinooo. All rights reserved.
//

/**
 * FLOW: chatroom-enter-message
 * ROLE: Bridge NIM Chatroom V2 (V2NIMChatroomClient) cho iOS — enter/exit phòng, gửi/nhận tin,
 *       history, member list, và forward mọi event realtime của phòng sang JS.
 * BREAKS: Đổi tên event hoặc key payload ở đây mà không đổi phía JS -> màn chat đứng im (không có
 *         tin mới, member/ban không cập nhật) nhưng KHÔNG báo lỗi. Lệch với Android là JS phải rẽ
 *         nhánh platform.
 */

#import <UIKit/UIKit.h>
#import <React/RCTBridge.h>
#import "NIMModel.h"
#import "ImConfig.h"

typedef void(^Success)(id _Nullable params);
typedef void(^Errors)(id _Nullable error);

@interface ChatroomViewController : UIViewController

/// Đường emit event lên JS. RNNeteaseIm gán trước mỗi lần forward (nó giữ _bridge, class này thì không).
@property (nonatomic, weak) RCTBridge * _Nullable bridge;

+ (instancetype _Nonnull)initWithChatroomViewController;

- (void)loginChatroom:(NSDictionary *_Nonnull)params success:(Success _Nonnull)success err:(Errors _Nonnull)err;

- (void)logoutChatroom:(NSString *_Nonnull)roomId success:(Success _Nonnull)success err:(Errors _Nonnull)err;

- (void)fetchChatroomInfo:(NSString *_Nonnull)roomId success:(Success _Nonnull)success err:(Errors _Nonnull)err;

- (void)fetchChatroomMember:(NSString *_Nonnull)roomId accountIds:(NSArray<NSString *> *_Nullable)accountIds success:(Success _Nonnull)success err:(Errors _Nonnull)err;

- (void)fetchChatroomMembers:(NSString *_Nonnull)roomId limit:(NSInteger)limit pageToken:(NSString *_Nullable)pageToken onlyOnline:(BOOL)onlyOnline success:(Success _Nonnull)success err:(Errors _Nonnull)err;

/// beginTimeMs tính bằng MILI-giây cho khớp Android; SDK iOS nhận giây nên bên trong tự chia 1000.
- (void)fetchMessageHistory:(NSString *_Nonnull)roomId limit:(NSInteger)limit beginTime:(double)beginTimeMs orderBy:(NSString *_Nullable)orderBy success:(Success _Nonnull)success err:(Errors _Nonnull)err;

- (void)sendTextMessage:(NSString *_Nonnull)roomId text:(NSString *_Nonnull)text success:(Success _Nonnull)success err:(Errors _Nonnull)err;

#pragma mark -- legacy: recent-session list (NIMViewController), KHÔNG thuộc bridge V2 --

/// Đồng bộ (semaphore), old-gen. Chỉ NIMViewController.handleSessionChatroom dùng để dựng dòng
/// hội thoại chatroom trong danh sách recent session.
- (NIMChatroom *_Nullable)getChatroomInfo:(NSString *_Nonnull)roomId;

- (BOOL)checkChatroomLoginStatus:(NSString *_Nonnull)roomId;

@end
