//
//  UserStrangers.h
//  Pods
//
//  Created by Rêu on 3/10/24.
//

#import <Foundation/Foundation.h>
#import "RNNeteaseIm.h"

@interface UserStrangers : NSObject

+(instancetype)initWithUserStrangers;

-(void)setIm:(RNNeteaseIm *)im;

-(void)setStranger:(NSString *)accId;

@end
