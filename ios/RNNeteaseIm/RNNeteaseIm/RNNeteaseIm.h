//
//  RNNeteaseIm.h
//  RNNeteaseIm
//
//  Created by Dowin on 2017/5/9.
//  Copyright © 2017年 Dowin. All rights reserved.
//


#import <React/RCTViewManager.h>

@interface RNNeteaseIm : RCTViewManager

/**
 * Bung UI nghe máy cấp hệ thống (LiveCommunicationKit) từ VoIP push payload.
 * Gọi từ AppDelegate khi PKPushRegistry nhận push — app có thể đang bị kill.
 * Nhận NSDictionary thuần để AppDelegate không phải import NERtcCallKit.
 * No-op (chỉ log) trên iOS < 17.4.
 * @param payload PKPushPayload.dictionaryPayload (raw, SDK tự parse key "nim").
 */
+ (void)reportSystemIncomingCallWithPayload:(NSDictionary *)payload;

/**
 * Login native ngay bằng credentials đã lưu (không chờ RN JS boot ~5s) để engine accept được
 * khi user bấm nghe trên màn gọi OS. Gọi từ AppDelegate NGAY TRƯỚC reportSystemIncomingCall.
 * Idempotent, an toàn gọi từ mọi trạng thái (đã login = no-op).
 */
+ (void)ensureNativeLoginForIncomingCall;

@end
