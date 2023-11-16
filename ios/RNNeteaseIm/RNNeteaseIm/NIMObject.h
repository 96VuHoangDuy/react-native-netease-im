//
//  NIMObject.h
//  RNNeteaseIm
//
//  Created by Dowin on 2017/5/17.
//  Copyright © 2017年 Dowin. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ImConfig.h"

@interface NIMObject : NSObject<NIMCustomAttachment>
+(instancetype)initNIMObject;
@property(nonatomic,strong)NSString *attachment;
//下载本地视频
- (void)downLoadAttachment:(NSString *)path filePath:(NSString *)filePath Error:(void(^)(NSError *error))handler progress:(void(^)(float progress))succ;
@end
