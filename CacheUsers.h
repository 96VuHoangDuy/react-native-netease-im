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

@interface CacheUsers : UIViewController

@property (nonatomic, readonly, strong) NSDictionary * _Nullable listUsers;

+(instancetype _Nonnull) initWithCacheUsers;

@end
