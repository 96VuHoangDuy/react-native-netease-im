//
//  CacheUsers.m
//  RNNeteaseIm
//
//  Created by Rêu on 25/9/24.
//  Copyright © 2024 Kinooo. All rights reserved.
//

#import "CacheUsers.h"
#import "ImConfig.h"

@interface CacheUsers() {
    NSDictionary *listUsers;
}

@property (nonatomic, readwrite, strong) NSDictionary *listUsers;

@end

@implementation CacheUsers

-(void) viewDidLoad {
    [super viewDidLoad];
}

+(instancetype) initWithCacheUsers {
    static CacheUsers *cu = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        cu = [[CacheUsers alloc] init];
    });
    
    return cu;
}

-(instancetype)init {
    self = [super init];
    if (self) {
        _listUsers = [[NSMutableDictionary alloc] init];
    }
    
    return self;
}

@end
