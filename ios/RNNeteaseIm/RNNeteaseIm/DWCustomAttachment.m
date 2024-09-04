//
//  DWCustomAttachment.m
//  RNNeteaseIm
//
//  Created by Dowin on 2017/6/13.
//  Copyright © 2017年 Dowin. All rights reserved.
//

#import "DWCustomAttachment.h"

@implementation DWCustomAttachment

- (NSString *)encodeAttachment{
    if (self.custType == CustomMessageChatbotTypeCustomerService) {
        NSData *data = [NSJSONSerialization dataWithJSONObject:self.dataDict options:0 error:nil];
        NSString *content = nil;
        if (data) {
            content = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        }
        
        NSLog(@"content => %@", content);
        
        return content;
    }
    
    NSString *strType = @"";
    switch (self.custType) {
        case CustomMessgeTypeRedpacket:
            strType = @"redpacket";
            break;
        case CustomMessgeTypeBankTransfer:
            strType = @"transfer";
            break;
        case CustomMessgeTypeUrl:
            strType = @"url";
            break;
        case CustomMessgeTypeAccountNotice:
            strType = @"account_notice";
            break;
        case CustomMessgeTypeRedPacketOpenMessage:
            strType = @"redpacketOpen";
            break;
        case CustomMessageTypeFowardMultipleText:
            strType = @"forwardMultipleText";
            break;
        case CustomMessgeTypeBusinessCard:
            strType = @"card";
            break;
        case CustomMessgeTypeCustom:
            strType = @"custom";
            break;
        default:
            strType = @"unknown";
            break;
    }
    NSLog(@"custType msgtype %ld,%@", (long)self.custType ,strType);
    NSDictionary *dict = @{@"msgtype" : strType, @"data": self.dataDict};
    NSData *data = [NSJSONSerialization dataWithJSONObject:dict
                                                   options:0
                                                     error:nil];
    NSString *content = nil;
    if (data) {
        content = [[NSString alloc] initWithData:data
                                        encoding:NSUTF8StringEncoding];
    }
    return content;
}

@end
