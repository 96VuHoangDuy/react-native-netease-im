//
//  NIMSDK+ZYZJ.m
//
#import "NIMSDK+ZYZJ.h"

@implementation NIMSDK (ZYZJ)

- (NSString *)zyzjCurrentAccount {
    NSString *acc = [self.v2LoginService getLoginUser];        // authoritative dưới login V10
    if (acc.length == 0) {
        acc = [self.loginManager currentAccount];              // fallback V9 (nếu rollback login)
    }
    return acc;
}

@end
