//
//  NIMSDK+ZYZJ.h
//  Helper: lấy current account đúng dưới login V10 (V2NIMLoginService).
//  Lý do: sau migrate login V9→V10, API V9 `loginManager.currentAccount` trả nil,
//  gây crash (build push body nil) + sai self-identity (isMe/isOutgoing) khắp UI chat.
//
#import <NIMSDK/NIMSDK.h>

@interface NIMSDK (ZYZJ)
/// Current login account: ưu tiên V2 `getLoginUser` (đúng dưới login V10), fallback V9 currentAccount.
- (NSString *)zyzjCurrentAccount;
@end
