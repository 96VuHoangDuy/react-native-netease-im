//
//  NNIMSetAllPayload.m
//  MI_PUSH_IOS
//
//  Created by Wangwei on 2025/12/17.
//

#import "NNIMSetAllPayload.h"

@implementation NNIMSetAllPayload

+ (void)builderPayload:(NSMutableDictionary *)payload {
    NSMutableDictionary *sessionBody = payload[@"sessionBody"];
    
    if(sessionBody &&
       ![sessionBody isEqual:[NSNull null]]  &&
       [sessionBody isKindOfClass:[NSDictionary class]]) {
        [self hwField:payload sessionBody:sessionBody];
        [self honorField:payload sessionBody:sessionBody];
        [self oppoField:payload sessionBody:sessionBody];
    }
}

// for HUAWEI
+ (void)hwField:(NSMutableDictionary *)payload sessionBody:(NSMutableDictionary *)sessionBody {
    NSMutableDictionary *hwField = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *data = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *androidConfig = [[NSMutableDictionary alloc] init];
    NSString *jsonString = @"{}";
    NSError *error = nil;
    
    [data setObject:sessionBody forKey:@"sessionBody"];
    
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data
                                                       options:0
                                                         error:&error];
    if (!error && jsonData) {
        jsonString = [[NSString alloc] initWithData:jsonData
                                           encoding:NSUTF8StringEncoding];
    }
    
    [androidConfig setObject:jsonString forKey:@"data"];
    [hwField setObject:androidConfig forKey:@"androidConfig"];
    [payload setObject:hwField forKey:@"hwField"];
}

// for Honor
+ (void)honorField:(NSMutableDictionary *)payload sessionBody:(NSMutableDictionary *)sessionBody {
    NSMutableDictionary *honorField = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *data = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *notification = [[NSMutableDictionary alloc] init];
    
    [data setObject:sessionBody forKey:@"sessionBody"];
    
    // to JSONString
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data
                                                       options:0
                                                         error:&error];
    NSString *jsonString = error ? @"" : [[NSString alloc] initWithData:jsonData
                                                               encoding:NSUTF8StringEncoding];
    
    [notification setObject:@"NORMAL" forKey:@"importance"];
    [honorField setObject:notification forKey:@"notification"];
    
    [honorField setObject:jsonString forKey:@"data"];
    [payload setObject:honorField forKey:@"honorField"];
}

// for OPPO
+ (void)oppoField:(NSMutableDictionary *)payload sessionBody:(NSMutableDictionary *)sessionBody {
    NSMutableDictionary *oppoField = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *androidConfig = [[NSMutableDictionary alloc] init];
    
    [androidConfig setObject:sessionBody forKey:@"sessionBody"];
    [oppoField setObject:androidConfig forKey:@"action_parameters"];
    [payload setObject:oppoField forKey:@"oppoField"];
}

@end

