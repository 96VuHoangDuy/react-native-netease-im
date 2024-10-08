#import "EventSender.h"
#import "RNNeteaseIm.h"

@implementation EventSender

- (instancetype)initWithIm: (RNNeteaseIm *)im {
    self = [super init];
    if (self) {
        self.mainArray = [NSMutableArray array];
        self.backupArray = [NSMutableArray array];
        self.isSending = NO;
        self.im = im;
    }
    return self;
}


- (void)addParam:(NSDictionary *)param withIdKey:(NSString *)idKey {
    NSDictionary *paramObject;

    // Kiểm tra nếu param là mảng
    if ([param isKindOfClass:[NSArray class]] && [(NSArray *)param count] > 0) {
       // Giả sử nếu là mảng thì chỉ lấy phần tử đầu tiên
       paramObject = [(NSArray *)param firstObject];
    } else if ([param isKindOfClass:[NSDictionary class]]) {
       // Nếu param là object (NSDictionary)
       paramObject = (NSDictionary *)param;
    } else {
       // Trường hợp không hợp lệ, dừng lại
       return;
    }
    
    NSString *paramId = paramObject[idKey];


    BOOL paramExists = NO;
    for (NSDictionary *existingParam in self.mainArray) {
        if ([existingParam[idKey] isEqual:paramId]) {
            [self.mainArray replaceObjectAtIndex:[self.mainArray indexOfObject:existingParam] withObject:param];
            paramExists = YES;
            break;
        }
    }

    if (!paramExists) {
        if (self.isSending) {
            [self.backupArray addObject:param];
        } else {
            [self.mainArray addObject:param];
        }
    }
}

- (void)sendEventToReactNativeWithType:(NSString *)type eventName:(NSString *)eventName countLimit:(NSUInteger)countLimit {
    if (self.isSending || self.mainArray.count == 0) return;

    self.isSending = YES;
    NSUInteger countToSend = MIN(countLimit, self.mainArray.count);
    NSArray *paramsToSend = [self.mainArray subarrayWithRange:NSMakeRange(0, countToSend)];

    if (paramsToSend.count > 0) {
        NSDictionary *param = @{@"data": paramsToSend};
        [self.im.bridge.eventDispatcher sendAppEventWithName:eventName body:param];
    }

    [self.mainArray removeObjectsInRange:NSMakeRange(0, countToSend)];

    if (self.backupArray.count > 0) {
        [self.mainArray addObjectsFromArray:self.backupArray];
        [self.backupArray removeAllObjects];
    }

    self.isSending = NO;

    if (self.mainArray.count > 0) {
        [self sendEventToReactNativeWithType:type eventName:eventName countLimit:countLimit];
    }
}

- (void)triggerSendEventAfterDelay:(NSString *)type eventName:(NSString *)eventName countLimit:(NSUInteger)countLimit {
    [self.sendEventTimer invalidate];
    self.sendEventTimer = [NSTimer scheduledTimerWithTimeInterval:1
                                                           target:self
                                                         selector:@selector(sendEventTimerFired:)
                                                         userInfo:@{@"type": type, @"eventName": eventName, @"countLimit": @(countLimit)}
                                                          repeats:NO];
}

- (void)sendEventTimerFired:(NSTimer *)timer {
    NSString *type = timer.userInfo[@"type"];
    NSString *eventName = timer.userInfo[@"eventName"];
    NSUInteger countLimit = [timer.userInfo[@"countLimit"] unsignedIntegerValue];
    [self sendEventToReactNativeWithType:type eventName:eventName countLimit:countLimit];
}

@end
