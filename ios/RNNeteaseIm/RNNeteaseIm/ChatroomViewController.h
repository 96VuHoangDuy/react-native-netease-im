//
//  ChatroomViewController.h
//  RNNeteaseIm
//
//  Created by Rêu on 5/9/24.
//  Copyright © 2024 Kinooo. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "NIMModel.h"
#import "ImConfig.h"

typedef void(^Success)(id _Nullable params);
typedef void(^Errors)(id _Nullable error);
@interface ChatroomViewController : UIViewController
+(instancetype _Nonnull)initWithChatroomViewController;

-(void)loginChatroom:(NSDictionary *_Nonnull)params success:(Success _Nonnull )success err:(Errors _Nonnull )err;

-(void)logoutChatroom:(NSString *_Nonnull)roomId success:(Success _Nonnull )success err:(Errors _Nonnull )err;

-(NSDictionary * _Nullable)fetchUserInfo:(NSString * _Nonnull)accId;

-(BOOL)checkChatroomLoginStatus:(NSString *_Nonnull)roomId;

-(void)fetchChatroomInfo:(NSString *_Nonnull)roomId success:(Success _Nonnull)success err:(Errors _Nonnull)err;

-(NIMChatroom *_Nullable)getChatroomInfo:(NSString *_Nonnull)roomId;

-(void)fetchChatroomMember:(NSString *_Nonnull)roomId userId:(NSString *_Nonnull)userId success:(Success _Nonnull )success err:(Errors _Nonnull )err;

-(void)fetchChatroomMembers:(NSString *_Nonnull)roomId success:(Success _Nonnull )success err:(Errors _Nonnull )err;

-(void)fetchMessageHistory:(NSString *_Nonnull)roomId limit:(NSInteger)limit currentMessageId:(NSString *_Nullable)currentMessageId orderBy:(NSString *_Nullable)orderBy success:(Success _Nonnull)success err:(Errors _Nonnull)err;

@end
