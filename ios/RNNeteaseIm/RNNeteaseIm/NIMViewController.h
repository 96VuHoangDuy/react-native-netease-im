//
//  NIMViewController.h
//  NIM
//
//  Created by Dowin on 2017/5/8.
//  Copyright © 2017年 Dowin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "NIMModel.h"
#import "NTESClientUtil.h"
typedef void(^SUCCESS) (id param);
typedef void(^ERROR)(NSString *error);
@interface NIMViewController : UIViewController

@property (copy, nonatomic) NSString *strAccount;
@property (copy, nonatomic) NSString *strToken;
@property (copy, nonatomic) NSString *lastMessageId;
@property (assign, nonatomic) BOOL isUpdated;

@property (nonatomic, strong) NSDictionary *listUserInfo;
@property (nonatomic, strong) NSDictionary *listCsrOrChatbot;
@property (assign, nonatomic) BOOL isFetchCsrAndChatbot;

+(instancetype)initWithController;
-(instancetype)initWithNIMController;
-(void)deleteCurrentSession:(NSString *)recentContactId andback:(ERROR)error;
//获取最近聊天列表回调
-(void)getRecentContactListsuccess:(SUCCESS)suc andError:(ERROR)err;
-(void)removeSession:(NSString *)sessionId sessionType:(NSString *)sessionType;
-(void)addDelegate;
- (void)getResouces;

- (NSString *)messageContent:(NIMMessage*)lastMessage;
@end
