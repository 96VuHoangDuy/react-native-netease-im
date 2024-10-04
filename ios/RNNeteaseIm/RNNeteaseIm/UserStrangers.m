//
//  UserStrangers.m
//  RNNeteaseIm
//
//  Created by Rêu on 3/10/24.
//

#import "UserStrangers.h"
#import "RNNeteaseIm.h"
#import "CacheUsers.h"

@interface UserStrangers() {
//    NSDictionary *listStrangers;
    
}

@property (nonatomic, strong) NSDictionary *listStrangers;
@property (nonatomic, strong) NSDictionary *preListStrangers;
@property (nonatomic, strong) NSTimer *debounceTimer;
@property (nonatomic, assign) NSTimeInterval debounceDelay;
@property (nonatomic, weak) RNNeteaseIm *im;

@end

@implementation UserStrangers

@synthesize im = _im;

+(instancetype)initWithUserStrangers {
    static UserStrangers *us = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        us = [[UserStrangers alloc] init];
    });
    
    return us;
}

-(instancetype)init {
    self = [super init];
    if (self) {
        _listStrangers = [[NSMutableDictionary alloc] init];
        _preListStrangers = [[NSMutableDictionary alloc] init];
        _debounceDelay = 2.0;
        
        [self startObserving];
    }
    
    return self;
}

-(void)setIm:(RNNeteaseIm *)im {
    _im = im;
}

- (void)dealloc {
    [self stopObserving];
}

-(void)setStranger:(NSString *)accId {
    if ([_listStrangers objectForKey:accId] != nil) return;
    
    NSMutableDictionary *updateListStrangers = _listStrangers ? [_listStrangers mutableCopy] : [[NSMutableDictionary alloc] init];
    [updateListStrangers setObject:accId forKey:accId];
    
    self.listStrangers = [updateListStrangers copy];
}

#pragma mark - Observe Setup
- (void)startObserving {
    [self addObserver:self forKeyPath:@"listStrangers" options:NSKeyValueObservingOptionNew context:nil];
}

- (void)stopObserving {
    [self removeObserver:self forKeyPath:@"listStrangers"];
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSKeyValueChangeKey, id> *)change context:(void *)context {
    NSLog(@"observeValueForKeyPath: %@", keyPath);
    if ([keyPath isEqualToString:@"listStrangers"]) {
        if ([self isNewElementAddedToDictionary:_listStrangers comparedTo:_preListStrangers]) {
            [self debouncePropertyChange];
        }
        
        _preListStrangers = [_listStrangers copy];
    }
}

- (BOOL)isNewElementAddedToDictionary:(NSDictionary *)newDict comparedTo:(NSDictionary *)oldDict {
    if (newDict.count > oldDict.count) {
        return YES;
    }
    
    for (id key in newDict) {
        if (!oldDict[key]) {
            return YES;
        }
    }
    
    return NO;
}

#pragma mark - Debouncing Logic

- (void)debouncePropertyChange {
    [self.debounceTimer invalidate];
    
    self.debounceTimer = [NSTimer scheduledTimerWithTimeInterval:self.debounceDelay
                                                          target:self
                                                        selector:@selector(handleDebounced)
                                                        userInfo:nil
                                                         repeats:NO];
}

- (void)handleDebounced {
    if ([self.listStrangers count] == 0) return;
    NSMutableArray *accIds = [[NSMutableArray alloc] init];
    for(NSString *accId in [self.listStrangers allKeys]) {
        NSDictionary *user = [[CacheUsers initWithCacheUsers] getUser:accId];
        if (user == nil) {
            [accIds addObject:accId];
        }
    }
    
    if ([accIds count] > 0) {
        [[CacheUsers initWithCacheUsers] fetchUsers:accIds completion:^(NSDictionary *response,NSError *error) {
            NSLog(@"response fetch user: %@", response);
            if (error != nil || response == nil) {
                NSLog(@"handleDebounced - fetchUsers error: %@",error);
                return;
            }
            
            [self.im.bridge.eventDispatcher sendDeviceEventWithName:@"observeUserStranger" body:response];
        }];
    }
    
    self.listStrangers = [[NSMutableDictionary alloc] init];
}

@end
