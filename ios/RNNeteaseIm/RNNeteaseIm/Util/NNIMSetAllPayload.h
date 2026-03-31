//
//  NNIMSetAllPayload.h
//  MI_PUSH_IOS
//
//  Created by Wangwei on 2025/12/17.
//

#import <Foundation/Foundation.h>
#import <NIMSDK/NIMSDK.h>

NS_ASSUME_NONNULL_BEGIN

@interface NNIMSetAllPayload : NSObject

// build Payload
+ (void)builderPayload:(NSMutableDictionary *)payload;

@end

NS_ASSUME_NONNULL_END

