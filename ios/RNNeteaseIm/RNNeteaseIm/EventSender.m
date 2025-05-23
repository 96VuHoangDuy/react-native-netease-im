#import "EventSender.h"
#import "RNNeteaseIm.h"

@implementation EventSender

static NSUInteger const kProgressBatchSize = 10;
static NSTimeInterval const kProgressSendInterval = 0.2; // 200ms
static CGFloat const kProgressThreshold = 0.02; // 2%

- (instancetype)initWithIm:(RNNeteaseIm *)im {
    self = [super init];
    if (self) {
        self.mainArray = [NSMutableArray array];
        self.backupArray = [NSMutableArray array];
        self.lastProgressMap = [NSMutableDictionary dictionary];
        self.isSending = NO;
        self.im = im;
    }
    return self;
}

- (void)dealloc {
    [self.sendEventTimer invalidate];
}

- (void)addParam:(NSDictionary *)param withIdKey:(NSString *)idKey {
    NSDictionary *paramObject;

    // Xử lý input đầu vào
    if ([param isKindOfClass:[NSArray class]] && [(NSArray *)param count] > 0) {
        paramObject = [(NSArray *)param firstObject];
    } else if ([param isKindOfClass:[NSDictionary class]]) {
        paramObject = (NSDictionary *)param;
    } else {
        return;
    }

    NSString *paramId = paramObject[idKey];
    if (!paramId) return;

    // Gửi riêng nếu progress đã xong
    NSNumber *progress = paramObject[@"progress"];

    if (progress && progress.floatValue >= 1.0) {
        NSDictionary *wrapped = @{@"data": @[paramObject]};
        [self.im.bridge.eventDispatcher sendAppEventWithName:@"observeProgressSend" body:wrapped];
        return;
    }

    // So sánh delta progress
    NSNumber *lastProgress = self.lastProgressMap[paramId];
    if (lastProgress && progress) {
        CGFloat delta = fabs(progress.floatValue - lastProgress.floatValue);
        if (delta < kProgressThreshold) {
            return; // Bỏ qua nếu thay đổi quá nhỏ
        }
    }

    self.lastProgressMap[paramId] = progress ?: @(0);

    // Check nếu đã tồn tại thì replace
    BOOL paramExists = NO;
    for (NSDictionary *existingParam in self.mainArray) {
        if ([existingParam[idKey] isEqual:paramId]) {
            NSUInteger idx = [self.mainArray indexOfObject:existingParam];
            [self.mainArray replaceObjectAtIndex:idx withObject:paramObject];
            paramExists = YES;
            break;
        }
    }

    if (!paramExists) {
        if (self.isSending) {
            [self.backupArray addObject:paramObject];
        } else {
            [self.mainArray addObject:paramObject];
        }
    }

    // Trigger gửi batch nếu đủ hoặc cần delay
    [self triggerSendEventAfterDelay];
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
    // ❌ Không gọi đệ quy gửi tiếp ở đây
}

- (void)triggerSendEventAfterDelay {
    if (self.sendEventTimer) return;

    self.sendEventTimer = [NSTimer scheduledTimerWithTimeInterval:kProgressSendInterval
                                                           target:self
                                                         selector:@selector(sendEventTimerFired:)
                                                         userInfo:nil
                                                          repeats:NO];
}

- (void)sendEventTimerFired:(NSTimer *)timer {
    self.sendEventTimer = nil;

    [self sendEventToReactNativeWithType:self.eventType
                               eventName:self.eventName
                              countLimit:self.countLimit];

    if (self.mainArray.count > 0) {
        [self triggerSendEventAfterDelay];
    }
}

@end
