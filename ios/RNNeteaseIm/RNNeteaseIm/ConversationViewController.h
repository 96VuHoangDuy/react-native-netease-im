//
//  ConversationViewController.h
//  NIM
//
//  Created by Dowin on 2017/5/5.
//  Copyright © 2017年 Dowin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ImConfig.h"

typedef void(^Success)(id param);
typedef void(^Errors)(id erro);
@interface ConversationViewController : UIViewController<NIMChatManagerDelegate,NIMConversationManagerDelegate>

+(instancetype)initWithConversationViewController;
-(void)localSession:(NSInteger)index currentMessageID:(NSString *)currentMessageID direction:(int)direction sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType success:(Success)succe err:(Errors)err;
-(void)startSession:(NSString *)sessionID withType:(NSString *)type myUserName:(NSString *)myUserName myUserID:(NSString *)myUserID;
-(void)stopSession;
//-(void)sendAudioRecode:(NSString *)filePath;
/**
 *  会话页详细配置
 */
//取消录音
- (void)onCancelRecording;
//结束录音
- (void)onStopRecording;
//开始录音
- (void)onStartRecording;
//发送文本，并指定@用户（@仅适用于群组）
-(void)sendMessage:(NSString *)mess andApnsMembers:(NSArray *)members isCustomerService:(BOOL *)isCustomerService;
-(void)sendGifMessage:(NSString *)url aspectRatio:(NSString *)aspectRatio andApnsMembers:(NSArray *)members isCustomerService:(BOOL *)isCustomerService;
-(void)sendMessageTeamNotificationRequestJoin:(nonnull  NSDictionary *)sourceId targets:(nonnull NSArray *)targets type:(nonnull NSNumber*)type success:(Success)succe Err:(Errors)err;
//发送图片
-(void)sendImageMessages:(  NSString *)path displayName:(  NSString *)displayName isCustomerService:(BOOL *)isCustomerService isHighQuality:(BOOL *)isHighQuality;
//发送音频
-(void)sendAudioMessage:(  NSString *)file duration:(  NSString *)duration isCustomerService:(BOOL *)isCustomerService;

-(void)updateActionHideRecentSession:(NSString *)sessionId sessionType:(NSString *)sessionType isHideSession:(BOOL *)isHideSession isPinCode:(BOOL *)isPinCode success:(Success)success error:(Errors)error;
//发送视频
-(void)sendVideoMessage:(  NSString *)path duration:(  NSString *)duration width:(  NSNumber *)width height:(  NSNumber *)height displayName:(  NSString *)displayName isCustomerService:(BOOL *)isCustomerService;
//发送自定义消息
-(void)sendCustomMessage:(NSInteger )custType data:(NSDictionary *)dataDict;

-(void) sendMultiMediaMessage:(NSArray *)listMedia isCustomerService:(BOOL *)isCustomerService success:(Success)succes error:(Errors)error;

// just forward multiple message text
-(void)forwardMultipleTextMessage:(NSDictionary *)dataDict sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType content:(NSString *)content;

-(void)createNotificationBirthday:(NSString *)sessionId sessionType:(NSString *)sessionType memberContactId:(NSString *)memberContactId memberName:(NSString *)memberName success:(Success)success err:(Errors)err;

-(void)queryTeamByName:(NSString *)search success:(Success)success err:(Errors)err;

-(void)removeMessage:(NSString *)messageId sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType;

-(void)queryAllTeams:(Success)success err:(Errors)err;

-(void)updateMessageSentStickerBirthday:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId success:(Success)success err:(Errors)err;

-(void)updateRecentSessionIsCsrOrChatbot:(NSString *)sessionId type:(NSString *)type name:(NSString *)name;

- (NSString *)getUserName:(NSString *)userID;

-(void)updateIsSeenMessage:(BOOL *)isSeenMessage;

-(void)addEmptyRecentSession:(NSString *)sessionId sessionType:(NSString *)sessionType;

-(NSDictionary *) updateMessageOfChatBot:(NSString *)messageId sessionId:(NSString *)sessionId chatBotType:(NSString *)chatBotType;

- (void) setCancelResendMessage:(NSString *)messageId sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType;

//发送地理位置消息
-(void)sendLocationMessage:(  NSString *)latitude longitude:(  NSString *)longitude address:(  NSString *)address success:(Success)succe Err:(Errors)err;
//发送提醒消息
-(void)sendTipMessage:( NSString *)content;
//发送红包消息
- (void)sendRedPacketMessage:(NSString *)type comments:(NSString *)comments serialNo:(NSString *)serialNo;
//发送转账消息
- (void)sendBankTransferMessage:(NSString *)amount comments:(NSString *)comments serialNo:(NSString *)serialNo;
//发送拆红包消息
- (void)sendRedPacketOpenMessage:(NSString *)sendId hasRedPacket:(NSString *)hasRedPacket serialNo:(NSString *)serialNo;
//发送名片消息
- (void)sendCardMessage:(NSString *)toSessionType sessionId:(NSString *)toSessionId name:(NSString *)name imgPath:(NSString *)strImgPath cardSessionId:(NSString *)cardSessionId cardSessionType:(NSString *)cardSessionType;
//转发消息
-(void)forwardMessage:(NSArray *)messageIds sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType content:(NSString *)content success:(Success)succe;

//本地历史记录
-(void)localSessionList:(NSString *)sessionId sessionType:(NSString *)sessionType timeLong:(NSString *)timeLong direction:(NSString *)direction limit:(NSString *)limit asc:(BOOL)asc success:(Success)succe;
//撤回消息
-(void)revokeMessage:(NSString *)messageId success:(Success)succe Err:(Errors)err;
//开始播放录音
- (void)play:(NSString *)filepath isExternalSpeaker:(BOOL *)isExternalSpeaker;
//停止播放
- (void)stopPlay;

- (void)switchAudioOutputDevice:(BOOL *)isExternalSpeaker;

//好友消息提醒
-(void)muteMessage:(NSString *)contactId mute:(NSString *)mute Succ:(Success)succ Err:(Errors)err;
//清空本地聊天记录
-(void)clearMsg:(NSString *)contactId type:(NSString *)type;
//删除一条信息
-(void)deleteMsg:(NSString *)messageId;
//麦克风权限
- (void)onTouchVoiceSucc:(Success)succ Err:(Errors)err;
//更新录音消息为已播放
- (void)updateAudioMessagePlayStatus:(NSString *)messageID;

//获得撤回内容
- (NSString *)tipOnMessageRevoked:(id)message;
//更具提示生成撤回消息
- (NIMMessage *)msgWithTip:(NSString *)tip;
//重发消息
- (void)resendMessage:(NSString *)messageID success:(Success)succe err:(Errors)err;

-(void)getMessageById:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId success:(Success)success;

// search local Messages
- (void) searchMessages:(NSString *)keyWords success:(Success)succe err:(Errors)err;

- (void) readAllMessageOnlineServiceByListSession:(NSArray *)listSessionId;

- (void)searchMessagesinCurrentSession:(NSString *)keyWords anchorId:(NSString *)anchorId limit:(int)limit messageType:(NSArray *)messageType direction:(int)direction success:(Success)succe err:(Errors)err;

- (NSString *)teamNotificationSourceName:(NIMMessage *)message;

- (NSArray *)teamNotificationTargetNames:(NIMMessage *)message;

- (NSMutableDictionary *)setNotiTeamObj:(NIMMessage *)message;

- (BOOL) isPlayingRecord;

-(void)sendFileMessage:(NSString *)filePath fileName:(NSString *)fileName isCustomerService:(BOOL *)isCustomerService success:(Success)succe Err:(Errors)err;

@end
