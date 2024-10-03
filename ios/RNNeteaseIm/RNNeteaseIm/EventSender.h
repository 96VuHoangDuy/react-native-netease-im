// EventSender.h
#import <Foundation/Foundation.h>
#import "RNNeteaseIm.h"
@interface EventSender: NSObject

@property (nonatomic, strong) NSMutableArray *mainArray;
@property (nonatomic, strong) NSMutableArray *backupArray;
@property (nonatomic, assign) BOOL isSending;
@property (nonatomic, strong) NSTimer *sendEventTimer;
@property (nonatomic, strong) RNNeteaseIm *im;

//- (void) setIm: (RNNeteaseIm *)im;
- (instancetype)initWithIm: (RNNeteaseIm *)im;
//- (instancetype)initWithBridge: (RCTBridge *)bridge;
// Public methods for using this class in other Objective-C files
- (void)addParam:(NSDictionary *)param withIdKey:(NSString *)idKey;
- (void)sendEventToReactNativeWithType:(NSString *)type eventName:(NSString *)eventName countLimit:(NSUInteger)countLimit;
- (void)triggerSendEventAfterDelay:(NSString *)type eventName:(NSString *)eventName countLimit:(NSUInteger)countLimit;

@end
