//
//  NIMObject.m
//  RNNeteaseIm
//
//  Created by Dowin on 2017/5/17.
//  Copyright © 2017年 Dowin. All rights reserved.
//

#import "NIMObject.h"

@implementation NIMObject
+(instancetype)initNIMObject{
    static NIMObject *nimObj  = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        nimObj = [[NIMObject alloc]init];
    });
    return nimObj;
}
- (void)downLoadAttachment:(NSString *)path filePath:(NSString *)filePath Error:(void(^)(NSError *error))handler progress:(void(^)(float progress))succ {
    __weak typeof(self) wself = self;
    [[NIMSDK sharedSDK].resourceManager download:path filepath:filePath progress:^(float progress) {
        succ(progress);
    } completion:^(NSError *error) {
        if (wself) {
            if (handler) {
                handler(error);
            }
        }
    }];
}

@end
