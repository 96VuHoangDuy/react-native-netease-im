//
//  NTESSDKConfig.m
//  NIM
//
//  Created by amao on 5/9/16.
//  Copyright © 2016 Netease. All rights reserved.
//

#import "NTESSDKConfigDelegate.h"


@implementation NTESSDKConfigDelegate
- (BOOL)shouldIgnoreNotification:(NIMNotificationObject *)notification
{
    BOOL ignore = NO;
    NIMNotificationContent *content = notification.content;
    if ([content isKindOfClass:[NIMTeamNotificationContent class]]) //这里做个示范如何忽略部分通知 (不在聊天界面显示)
    {
        NSArray *types = @[];
        NIMTeamOperationType type = [(NIMTeamNotificationContent *)content operationType];
        for (NSString *item in types)
        {
            if (type == [item integerValue])
            {
                ignore = YES;
                break;
            }
        }
    }
    return ignore;
}

- (nullable NSString *)dynamicChatRoomTokenForAccount:(NSString *)account room:(NSString *)roomId appKey:(NSString *)appKey
{
    return nil;
}
@end
