//
//  CacheUsers.h
//  RNNeteaseIm
//
//  Created by Rêu on 25/9/24.
//  Copyright © 2024 Kinooo. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "NIMModel.h"
#import "ImConfig.h"

typedef void(^NIMFetchUsersHandle)(NSDictionary * _Nullable response,NSError * __nullable error);

@interface CacheUsers : UIViewController

+(instancetype _Nonnull) initWithCacheUsers;

-(void)fetchUsers:(NSArray<NSString *> * _Nonnull)accIds completion:(NIMFetchUsersHandle _Nullable)completion;

-(void)setListCustomerServiceAndChatbot:(NSDictionary *_Nonnull)listCustomerServiceAndChatbot;

-(NSDictionary * _Nullable)getUser:(NSString * _Nonnull)accId;

-(NSString *_Nullable)getCustomerServiceOrChatbot:(NSString *_Nonnull)accId;

@end
