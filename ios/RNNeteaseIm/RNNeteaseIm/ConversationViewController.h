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

-(void)sendMessage:(NSString *)mess andApnsMembers:(NSArray *)members messageSubType:(NSInteger)messageSubType isSkipFriendCheck:(BOOL *)isSkipFriendCheck;

-(void)sendGifMessage:(NSString *)url aspectRatio:(NSString *)aspectRatio andApnsMembers:(NSArray *)members isSkipFriendCheck:(BOOL *)isSkipFriendCheck;

-(void)sendMessageTeamNotificationRequestJoin:(nonnull  NSDictionary *)sourceId targets:(nonnull NSArray *)targets type:(nonnull NSNumber*)type success:(Success)succe Err:(Errors)err;
//发送图片
-(void)sendImageMessages:(NSString *)path displayName:(NSString *)displayName isHighQuality:(BOOL *)isHighQuality isSkipCheckFriend:(BOOL *)isSkipCheckFriend parentId:(nullable NSString *)parentId indexCount:(nullable NSNumber *)indexCount;
//发送音频
-(void)sendAudioMessage:(NSString *)file duration:(NSString *)duration isSkipFriendCheck:(BOOL *)isSkipFriendCheck;

-(void)updateActionHideRecentSession:(NSString *)sessionId sessionType:(NSString *)sessionType isHideSession:(BOOL *)isHideSession isPinCode:(BOOL *)isPinCode success:(Success)success error:(Errors)error;

-(void)downloadAttachment:(nonnull NSString *)messageId sessionId:(nonnull NSString *)sessionId toSessionType:(nonnull NSString *)toSessionType;

//发送视频
-(void)sendVideoMessage:(NSString *)path duration:(NSString *)duration width:(NSNumber *)width height:(NSNumber *)height displayName:(  NSString *)displayName isSkipFriendCheck:(BOOL *)isSkipFriendCheck parentId:(nullable NSString *)parentId indexCount:(nullable NSNumber*)indexCount;
//发送自定义消息
-(void)sendCustomMessage:(NSInteger )custType data:(NSDictionary *)dataDict;

-(void) sendMultiMediaMessage:(NSArray *)listMedia parentId:(nullable NSString *)parentId isSkipFriendCheck:(BOOL *)isSkipFriendCheck success:(Success)succes error:(Errors)error;

// just forward multiple message text
-(void)forwardMultipleTextMessage:(NSDictionary *)dataDict sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType content:(NSString *)content;

-(void)forwardMultiTextMessageToMultipleRecipients:(NSDictionary *)params success:(Success)success err:(Errors)err;

-(void)createNotificationBirthday:(NSString *)sessionId sessionType:(NSString *)sessionType memberContactId:(NSString *)memberContactId memberName:(NSString *)memberName success:(Success)success err:(Errors)err;

-(void)queryTeamByName:(NSString *)search success:(Success)success err:(Errors)err;

-(void)removeMessage:(NSString *)messageId sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType;

-(void)queryAllTeams:(Success)success err:(Errors)err;

-(void)updateMessageSentStickerBirthday:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId success:(Success)success err:(Errors)err;

-(void) removeReactionMessage:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId accId:(NSString *)accId isSendMessage:(BOOL *)isSendMessage success:(Success)success err:(Errors)err;

-(void) updateReactionMessage:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId messageNotifyReactionId:(NSString *)messageNotifyReactionId reaction:(NSDictionary *)reaction success:(Success)success err:(Errors)err;

-(void)handleInComeMultiMediaMessage:(NIMMessage *)message callFrom:(NSString *)callFrom;

-(void)updateRecentSessionIsCsrOrChatbot:(NSString *)sessionId type:(NSString *)type name:(NSString *)name;

- (NSString *)getUserName:(NSString *)userID;

-(void) replyMessage:(nonnull NSDictionary *)params success:(Success)success err:(Errors)err;

-(void)updateIsSeenMessage:(BOOL *)isSeenMessage;

-(void)addEmptyRecentSession:(NSString *)sessionId sessionType:(NSString *)sessionType;

-(void)addEmptyPinRecentSession:(NSString *)sessionId sessionType:(NSString *)sessionType;

-(void)addEmptyRecentSessionCustomerService:(NSArray *)data;

-(void)addEmptyTemporarySession:(NSString *)sessionId temporarySessionRef:(NSDictionary *)temporarySessionRef success:(Success)success error:(Errors)error;

-(void)forwardMessagesToMultipleRecipients:(NSDictionary *)params success:(Success)success err:(Errors)err;

-(NSDictionary *) updateMessageOfChatBot:(NSString *)messageId sessionId:(NSString *)sessionId chatBotType:(NSString *)chatBotType;

-(void) reactionMessage:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId reaction:(NSDictionary *)reaction success:(Success)success err:(Errors)err;

- (void) setCancelResendMessage:(NSString *)messageId sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType;

//发送地理位置消息
-(void)sendLocationMessage:(NSString *)sessionId sessionType:(NSString *)sessionType latitude:(  NSString *)latitude longitude:(  NSString *)longitude address:(  NSString *)address success:(Success)succe Err:(Errors)err;
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
-(void)forwardMessage:(NSArray *)messageIds sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType content:(NSString *)content parentId:(NSString *)parentId isHaveMultiMedia:(BOOL *)isHaveMultiMedia success:(Success)succe;

//本地历史记录
-(void)localSessionList:(NSString *)sessionId sessionType:(NSString *)sessionType timeLong:(NSString *)timeLong direction:(NSString *)direction limit:(NSString *)limit asc:(BOOL)asc success:(Success)succe;
//撤回消息
-(void)revokeMessage:(NSString *)messageId success:(Success)succe Err:(Errors)err;

-(void)sendCustomNotification:(NSDictionary *)dataDict toSessionId:(NSString *)toSessionId toSessionType:(NSString *)toSessionType success:(Success)succe Err:(Errors)err;
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
-(void)deleteMsg:(NSString *)messageId success:(Success)success err:(Errors)err;
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

-(void) searchTextMessages:(NSString *)searchContent success:(Success)success err:(Errors)err;

// search local Messages
- (void) searchMessages:(NSString *)keyWords success:(Success)succe err:(Errors)err;

-(void) searchFileMessages:(Success)success err:(Errors)err;

- (void) readAllMessageOnlineServiceByListSession:(NSArray *)listSessionId;

-(void)cancelSendingMessage:(NSString *)sessionId sessionType:(NSString *)sessionType messageId:(NSString *)messageId success:(Success)success err:(Errors)err;

- (void)searchMessagesinCurrentSession:(NSString *)keyWords anchorId:(NSString *)anchorId limit:(int)limit messageType:(NSArray *)messageType direction:(int)direction messageSubTypes:(NSArray *)messageSubTypes success:(Success)succe err:(Errors)err;

- (NSString *)teamNotificationSourceName:(NIMMessage *)message;

- (NSArray *)teamNotificationTargetNames:(NIMMessage *)message;

- (NSMutableDictionary *)setNotiTeamObj:(NIMMessage *)message;

- (BOOL) isPlayingRecord;

-(void)sendFileMessage:(NSString *)filePath fileName:(NSString *)fileName fileType:(NSString *)fileType success:(Success)succe Err:(Errors)err;

-(void) sendFileMessageWithSession:(NSString *)path fileName:(NSString *)fileName fileType:(NSString*)fileType sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName  success:(Success)success err:(Errors)err;

-(void)updateRecentToTemporarySession:(NSString *)sessionId temporarySessionRef:(NSDictionary *)temporarySessionRef;

-(void)sendTextMessageWithSession:(NSString *)msgContent sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName messageSubType:(NSInteger)messageSubType;

-(void) sendImageMessageWithSession:(NSString *)path isHighQuality:(BOOL *)isHighQuality sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName;

-(void)sendVideoMessageWithSession:(NSString *)path sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName;

-(void) sendGifMessageWithSession:(NSString *)url aspectRatio:(NSString *)aspectRatio sessionId:(NSString *)sessionId sessionType:(NSString *)sessionType sessionName:(NSString *)sessionName;

@end
