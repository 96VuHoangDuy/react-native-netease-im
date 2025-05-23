import { NativeModules, Platform } from 'react-native';
import {
  CustomMessageType,
  NIMSessionTypeEnum,
  NIMQueryDirectionEnum,
  NIMSendAttachmentEnum,
  QueryDirectionType,
  NIMSessionOnlineServiceType,
  NIMBirthdayMemberType,
  NIMMessageSubTypeEnum,
  ICreateRecentOnlineService,
  NimParamsForwardMessagesToMultipleRecipients,
  NimParamsForwardMultiTextMessageToMultipleRecipients,
  ITemporarySessionRef,
  NimSessionType,
} from './session.type';
import {
    INimCreateMessageReaction,
  INimMessageReaction,
  NIMMessage,
  NIMMessageChatBotInfo,
  NIMMessageMedia,
  NIMMessageTypeEnum,
  NimMessageTypeExtend,
} from '../Message/message.type';
import { ICustomNotificationDataDict } from '../SystemMsg/systemMsg.type';
const { RNNeteaseIm } = NativeModules;

class NimSession {
  /**
   * 登陆
   * @param account
   * @param token
   * @returns {*} @see observeRecentContact observeOnlineStatus
   */
  login(contactId: string, token: string, appKey: string) {
    return RNNeteaseIm.login(contactId, token, appKey);
  }

  /**
   * 登陆
   * @param account
   * @param token
   * @returns {*} @see observeRecentContact
   */
  autoLogin(contactId: string, token: string, appKey: string) {
    return RNNeteaseIm.autoLogin(contactId, token, appKey);
  }
  /**
   * 退出
   * @returns {*}
   */
  logout() {
    return RNNeteaseIm.logout();
  }
  /**
   * 最近会话列表
   * @returns {*}
   */
  getRecentContactList() {
    return RNNeteaseIm.getRecentContactList();
  }
  /**
   * 删除最近会话
   * @param recentContactId
   * @returns {*}
   */
  deleteRecentContact(recentContactId: string) {
    return RNNeteaseIm.deleteRecentContact(recentContactId);
  }

  removeSession(sessionId: string, sessionType: NIMSessionTypeEnum) {
    return RNNeteaseIm.removeSession(sessionId, sessionType);
  }

  /**
   * 进入聊天会话
   * @param sessionId
   * @param type
   * @returns {*} @see observeReceiveMessage 接收最近20消息记录
   */
  startSession(
    sessionId: string,
    type: NIMSessionTypeEnum,
    myUserName: string = '',
    myUserID: string = ''
  ) {
    if (Platform.OS === 'ios') {
      return RNNeteaseIm.startSession(sessionId, type, myUserName, myUserID);
    }
    return RNNeteaseIm.startSession(sessionId, type);
  }

  /**
   * 退出聊天会话
   * @returns {*}
   */
  stopSession() {
    return RNNeteaseIm.stopSession();
  }
  /**
   * 获取云端聊天记录
   * @param messageId
   * @param limit 查询结果的条数限制
   * @returns {*}  @see 回调返回最近所有消息记录
   */
  queryMessageListEx(
    messageId: string,
    limit: number,
    direction: QueryDirectionType,
    sessionId?: string,
    sessionType?: NIMSessionTypeEnum
  ) {
    return RNNeteaseIm.queryMessageListEx(
      messageId,
      limit,
      direction,
      sessionId,
      sessionType
    );
  }

  searchFileMessages(): Promise<Record<string, NIMMessage[]>> {
    return RNNeteaseIm.searchFileMessages();
  }

  searchTextMessages(
    searchContent: string
  ): Promise<Record<string, NIMMessage[]>> {
    return RNNeteaseIm.searchTextMessages(searchContent);
  }

  searchMessages(keyWords: string): Promise<Record<string, NIMMessage[]>> {
    return RNNeteaseIm.searchMessages(keyWords);
  }

  searchMessagesInCurrentSession({
    keyWords,
    anchorId,
    limit,
    messageType,
    direction,
    messageSubTypes,
    isDisableDownloadMedia,
  }: {
    keyWords: string;
    anchorId: string;
    limit: number;
    messageType: Array<NIMMessageTypeEnum>;
    direction: QueryDirectionType;
    messageSubTypes?: Array<NIMMessageSubTypeEnum>;
    isDisableDownloadMedia?: boolean;
  }): Promise<Record<string, NIMMessage[]>> {
    
    return RNNeteaseIm.searchMessagesinCurrentSession(
      keyWords,
      anchorId,
      limit,
      messageType,
      direction,
      messageSubTypes,
      isDisableDownloadMedia ?? false,
    );
  }

  /**
   * 获取最近聊天内容
   * @param sessionId 聊天会话ID,
   * @param sessionType 聊天类型 0 单聊，1 群聊
   * @param timeLong 消息时间点 0为最新
   * @param direction 查询方向：'new' 新的; 'old' 旧的
   * @param limit 查询结果的条数限制
   * @param asc 查询结果的排序规则，如果为 true，结果按照时间升级排列，如果为 false，按照时间降序排列
   * @returns {*}  @see 回调返回最近所有消息记录
   */
  //TODO: change 'asc' params to boolean in native code
  queryMessageListHistory(
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    timeLong: string,
    direction: NIMQueryDirectionEnum,
    limit: number,
    asc: boolean
  ) {
    return RNNeteaseIm.queryMessageListHistory(
      sessionId,
      sessionType,
      timeLong,
      direction,
      limit,
      asc
    );
  }
  /**
   *1.发送文本消息
   * @param content 文本内容
  * @param atUserIds @的群成员ID ["abc","abc12"]
   */
  replyMessage(params: { content: string; messageId: string,
    isSkipFriendCheck?: boolean;
    isSkipTipForStranger?: boolean; }) {
    return RNNeteaseIm.replyMessage(params);
  }

  /**
   *1.发送文本消息
   * @param content 文本内容
   * @param atUserIds @的群成员ID ["abc","abc12"]
   */
  sendTextMessage(
    content: string,
    atUserIds?: string[],
    messageSubType?: number,
    isSkipFriendCheck?: boolean,
    isSkipTipForStranger?: boolean
  ) {
    return RNNeteaseIm.sendTextMessage(
      content,
      atUserIds ?? [],
      messageSubType ?? NIMMessageSubTypeEnum.DEFAULT,
      isSkipFriendCheck ?? false,
      isSkipTipForStranger ?? false
    );
  }

  sendCustomMessageOfChatbot(sessionId: string, customerServiceType: string) {
    return RNNeteaseIm.sendCustomMessageOfChatbot(
      sessionId,
      customerServiceType
    );
  }

  sendGifMessage(
    url: string,
    aspectRatio: string,
    atUserIds?: string[],
    isSkipFriendCheck?: boolean,
    isSkipTipForStranger?: boolean
  ) {
    return RNNeteaseIm.sendGifMessage(
      url,
      aspectRatio,
      atUserIds ?? [],
      isSkipFriendCheck ?? false,
      isSkipTipForStranger ?? false
    );
  }

  /**
   * 发送图片消息
   * @param file 图片文件对象
   * @param type 0:图片 1：视频
   * @param displayName 文件显示名字，如果第三方 APP 不关注，可为空
   * @returns {*}
   */
  sendImageMessages(
    file: string,
    displayName?: string,
    isHighQuality?: boolean,
    isSkipFriendCheck?: boolean,
    isSkipTipForStranger?: boolean
  ) {
    if (Platform.OS === 'ios') {
      return RNNeteaseIm.sendImageMessages(
        file,
        displayName ?? '',
        isHighQuality ?? false,
        isSkipFriendCheck ?? false,
        isSkipTipForStranger ?? false
      );
    }

    return RNNeteaseIm.sendImageMessage(
      file.replace('file://', ''),
      displayName ?? '',
      isHighQuality ?? false,
      isSkipFriendCheck ?? false,
      isSkipTipForStranger ?? false
    );
  }

  /**
   * 发送音频消息
   * @param file 音频文件
   * @param duration 音频持续时间，单位是ms
   * @returns {*}
   */
  sendAudioMessage(
    file: string,
    duration: string,
    isSkipFriendCheck?: boolean,
    isSkipTipForStranger?: boolean
  ) {
    return RNNeteaseIm.sendAudioMessage(
      file,
      duration,
      isSkipFriendCheck ?? false,
      isSkipTipForStranger ?? false
    );
  }

  sendMultiMediaMessage(
    listMedia: NIMMessageMedia[],
    isSkipFriendCheck?: boolean,
    isSkipTipForStranger?: boolean
  ) {
    return RNNeteaseIm.sendMultiMediaMessage(
      listMedia,
      isSkipFriendCheck ?? false,
      isSkipTipForStranger ?? false
    );
  }

  /**
   * 发送视频消息
   * @param file 视频文件
   * @param duration 视频持续时间
   * @param width 视频宽度
   * @param height 视频高度
   * @param displayName 视频显示名，可为空
   * @returns {*}
   */
  sendVideoMessage(
    file: string,
    duration: string,
    width: number,
    height: number,
    displayName?: string,
    isSkipFriendCheck?: boolean,
    isSkipTipForStranger?: boolean
  ) {
    return RNNeteaseIm.sendVideoMessage(
      file,
      duration,
      width,
      height,
      displayName,
      isSkipFriendCheck ?? false,
      isSkipTipForStranger ?? false
    );
  }
  /**
   * 发送地理位置消息
   * @param sessionId
   * @param sessionType
   * @param latitude 纬度
   * @param longitude 经度
   * @param address 地址信息描述
   * @returns {*}
   */
  sendLocationMessage(
    sessionId: string,
    sessionType: string,
    latitude: string,
    longitude: string,
    address: string
  ) {
    return RNNeteaseIm.sendLocationMessage(
      sessionId,
      sessionType,
      latitude,
      longitude,
      address
    );
  }
  /**
   * 发送系统通知
   * @param content
   * @returns {*}
   */
  sendTipMessage(content: string) {
    return RNNeteaseIm.sendTipMessage(content);
  }

  /**
   * 红包
   * @param type 红包类型
   *          0   一对一
   *          1   一对多(固定金额)
   *          2   一对多(随机金额)
   * @param comments 红包描祝福语 "[恭喜发财，大吉大利]"
   * @param serialNo 流水号
   * @returns {*}
   */
  sendRedPacketMessage(
    type: NIMSendAttachmentEnum,
    comments: string,
    serialNo: string
  ) {
    return RNNeteaseIm.sendRedPacketMessage(type, comments, serialNo);
  }

  setMessageNotify(contactId: string, needNotify: '0' | '1') {
    return RNNeteaseIm.setMessageNotify(contactId, needNotify);
  }

  /**
   * 名片
   * @param type 个人名片 群名片 公众号名片
   * @param name
   * @param imgPath
   * @param sessionId
   * @returns {*}
   */
  sendCardMessage(
    toSessionType: string,
    toSessionId: string,
    name: string,
    imgPath: string,
    cardSessionId: string,
    cardSessionType: string
  ) {
    return RNNeteaseIm.sendCardMessage(
      toSessionType,
      toSessionId,
      name,
      imgPath,
      cardSessionId,
      cardSessionType
    );
  }
  /**
   * 拆红包
   * @param sendId 发送红包的sessionId
   * @param hasRedPacket '0': mantled, '1': dismantled
   * @param serialNo 流水号
   * @returns {*}
   */
  sendRedPacketOpenMessage(
    sendId: string,
    hasRedPacket: string,
    serialNo: string
  ) {
    return RNNeteaseIm.sendRedPacketOpenMessage(sendId, hasRedPacket, serialNo);
  }
  /**
   * 转账
   * min 转账最小金额
   * max 转账最大金额
   * @param amount 转账金额
   * @param comments 转账说明
   * @param serialNo 流水号
   * @returns {*}
   */
  sendBankTransferMessage(amount: string, comments: string, serialNo: string) {
    return RNNeteaseIm.sendBankTransferMessage(amount, comments, serialNo);
  }
  /**
   * 发送自定义消息
   * @param attachment
   * @returns {*}
   */
  sendCustomMessage(custType: number, attachment: any) {
    return RNNeteaseIm.sendCustomMessage(custType, attachment);
  }

  /**
   * 
   * @param dataDict 
   * @param sessionId 
   * @param sessionType 
   * @param content 
   * @returns 
   *  NimSession.forwardMultipleTextMessage(
      { messages: NIMMessage[] },
      '14198181486',
      NIMSessionTypeEnum.P2P,
      'test 12345',
    );
   */
  forwardMultipleTextMessage(
    dataDict: {
      messages: NIMMessage[];
    },
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    content: string | null
  ) {
    return RNNeteaseIm.forwardMultipleTextMessage(
      dataDict,
      sessionId,
      sessionType,
      content
    );
  }
  /**
   * 开启录音权限
   * @returns {*}
   */
  onTouchVoice() {
    return RNNeteaseIm.onTouchVoice();
  }
  /**
   * 开始录音
   * @returns {*}
   */
  startAudioRecord() {
    return RNNeteaseIm.startAudioRecord();
  }

  /**
   * 结束录音,自动发送
   * @returns {*} @see observeAudioRecord
   */
  endAudioRecord() {
    return RNNeteaseIm.endAudioRecord();
  }

  /**
   * 取消播放录音
   * @returns {*}
   */
  cancelAudioRecord() {
    return RNNeteaseIm.cancelAudioRecord();
  }
  /**
   * 转发消息操作
   * @param messageId
   * @param sessionId
   * @param sessionType
   * @param content
   * @returns {*}
   */
  sendForwardMessage(
    messageIds: string[],
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    content: string,
    parentId: string,
    isHaveMultiMedia: boolean
  ) {
    return RNNeteaseIm.sendForwardMessage(
      messageIds,
      sessionId,
      sessionType,
      content,
      parentId,
      isHaveMultiMedia
    );
  }
  /**
   * 消息撤回
   * @param messageId
   * @returns {*}
   */
  revokeMessage(messageId: string) {
    return RNNeteaseIm.revokeMessage(messageId);
  }

  /**
   * 重发消息到服务器
   * @param messageId
   * @returns {*}
   */
  resendMessage(messageId: string) {
    return RNNeteaseIm.resendMessage(messageId);
  }
  /**
   * 消息删除
   * @param messageId
   * @returns {*}
   */
  deleteMessage(messageId: string) {
    return RNNeteaseIm.deleteMessage(messageId);
  }
  /**
   * 清空聊天记录
   * @param messageId
   * @returns {*}
   */
  clearMessage(sessionId: string, type: NIMSessionTypeEnum) {
    return RNNeteaseIm.clearMessage(sessionId, type);
  }

  /**
   * Android下载文件附件
   * @param messageId
   * @returns {*}
   */
  downloadAttachment(
    messageId: string,
    sessionId: string,
    sessionType: NIMSessionTypeEnum
  ) {
    return RNNeteaseIm.downloadAttachment(messageId, sessionId, sessionType);
  }

  /**
   * 更新录音消息是否播放过的状态
   * @param messageId
   * @returns {*}
   */
  updateAudioMessagePlayStatus(messageId: string) {
    return RNNeteaseIm.updateAudioMessagePlayStatus(messageId);
  }

  getLaunch() {
    if (Platform.OS === 'android') {
      return RNNeteaseIm.getLaunch();
    }
  }

  addEmptyRecentSession(sessionId: string, sessionType: NIMSessionTypeEnum) {
    return RNNeteaseIm.addEmptyRecentSession(sessionId, sessionType);
  }

  addEmptyRecentSessionWithoutMessage(
    sessionId: string,
    sessionType: NIMSessionTypeEnum
  ): Promise<NimSessionType> {
    return RNNeteaseIm.addEmptyRecentSessionWithoutMessage(
      sessionId,
      sessionType
    );
  }

  cancelSendingMessage(
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    messageId: string
  ) {
    return RNNeteaseIm.cancelSendingMessage(sessionId, sessionType, messageId);
  }

  updateRecentSessionIsCsrOrChatbot(
    sessionId: string,
    type: `${NIMSessionOnlineServiceType}`,
    nickname?: string
  ) {
    return RNNeteaseIm.updateRecentSessionIsCsrOrChatbot(
      sessionId,
      type,
      nickname
    );
  }

  removeMessage(
    messageId: string,
    sessionId: string,
    sessionType: NIMSessionTypeEnum
  ) {
    return RNNeteaseIm.removeMessage(messageId, sessionId, sessionType);
  }

  updateMessageOfChatBot(
    messageId: string,
    sessionId: string,
    chatBotType: string,
    chatBotInfo?: NIMMessageChatBotInfo
  ) {
    return RNNeteaseIm.updateMessageOfChatBot(
      messageId,
      sessionId,
      chatBotType,
      chatBotInfo
    );
  }

  readAllMessageOnlineServiceByListSession(listSessionId: string[]) {
    return RNNeteaseIm.readAllMessageOnlineServiceByListSession(listSessionId);
  }

  readAllMessageBySession(sessionId: string, sessionType: NIMSessionTypeEnum) {
    return RNNeteaseIm.readAllMessageBySession(sessionId, sessionType);
  }

  getMessageById(
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    messageId: string
  ) {
    return RNNeteaseIm.getMessageById(sessionId, sessionType, messageId);
  }

  setCancelResendMessage(
    messageId: string,
    sessionId: string,
    sessionType: NIMSessionTypeEnum
  ) {
    return RNNeteaseIm.setCancelResendMessage(
      messageId,
      sessionId,
      sessionType
    );
  }

  updateIsSeenMessage(isSeenMessage: boolean) {
    return RNNeteaseIm.updateIsSeenMessage(isSeenMessage);
  }

  updateActionHideRecentSession(
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    isHideSession: boolean,
    isPinCode: boolean
  ) {
    return RNNeteaseIm.updateActionHideRecentSession(
      sessionId,
      sessionType,
      isHideSession,
      isPinCode
    );
  }

  sendFileMessage(filePath: string, fileName: string, fileType: string) {
    return RNNeteaseIm.sendFileMessage(filePath, fileName, fileType);
  }

  createNotificationBirthday(
    sessonId: string,
    sessionType: NIMSessionTypeEnum,
    member?: NIMBirthdayMemberType
  ) {
    return RNNeteaseIm.createNotificationBirthday(
      sessonId,
      sessionType,
      member?.contactId,
      member?.name
    );
  }

  updateMessageSentStickerBirthday(
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    messageId: string
  ) {
    return RNNeteaseIm.updateMessageSentStickerBirthday(
      sessionId,
      sessionType,
      messageId
    );
  }

  setStrangerRecentReplyed(sessionId: string) {
    return RNNeteaseIm.setStrangerRecentReplyed(sessionId);
  }

  sendCustomNotification(
    dataDict: {
      data: ICustomNotificationDataDict;
    },
    toSessionId: string,
    toSessionType: NIMSessionTypeEnum
  ) {
    return RNNeteaseIm.sendCustomNotification(
      dataDict,
      toSessionId,
      toSessionType
    );
  }

  sendFileMessageWithSession(
    path: string,
    fileName: string,
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    sessionName: string
  ) {
    return RNNeteaseIm.sendFileMessageWithSession(
      path,
      fileName,
      sessionId,
      sessionType,
      sessionName
    );
  }

  sendTextMessageWithSession(
    msgContent: string,
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    sessionName: string,
    messageSubType?: NIMMessageSubTypeEnum
  ) {
    return RNNeteaseIm.sendTextMessageWithSession(
      msgContent,
      sessionId,
      sessionType,
      sessionName,
      messageSubType
    );
  }

  sendImageMessageWithSession(
    path: string,
    isHighQuality: boolean,
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    sessionName: string
  ) {
    return RNNeteaseIm.sendImageMessageWithSession(
      path,
      isHighQuality,
      sessionId,
      sessionType,
      sessionName
    );
  }

  sendVideoMessageWithSession(
    path: string,
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    sessionName: string
  ) {
    return RNNeteaseIm.sendVideoMessageWithSession(
      path,
      sessionId,
      sessionType,
      sessionName
    );
  }

  sendGifMessageWithSession(
    url: string,
    aspectRatio: string,
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    sessionName: string
  ) {
    return RNNeteaseIm.sendGifMessageWithSession(
      url,
      aspectRatio,
      sessionId,
      sessionType,
      sessionName
    );
  }

  reactionMessage(
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    messageId: string,
    reaction: INimCreateMessageReaction 
  ) {
    return RNNeteaseIm.reactionMessage(
      sessionId,
      sessionType,
      messageId,
      reaction
    );
  }

  removeReactionMessage(
    sessionId: string,
    sessionType: NIMSessionTypeEnum,
    messageId: string,
    accId: string,
    isSendMessage: boolean
  ) {
    return RNNeteaseIm.removeReactionMessage(
      sessionId,
      sessionType,
      messageId,
      accId,
      isSendMessage
    );
  }

  updateReactionMessage(
    params: {
      sessionId: string;
      sessionType: NIMSessionTypeEnum;
      messageId: string;
      messageNotifyReactionId: string;
      reaction: INimMessageReaction;
      isSkipUpdateReactedUsers: boolean
    }) {
    return RNNeteaseIm.updateReactionMessage(
      params
    );
  }

  addEmptyRecentSessionCustomerService(data: ICreateRecentOnlineService[]) {
    return RNNeteaseIm.addEmptyRecentSessionCustomerService(data);
  }

  forwardMessagesToMultipleRecipients(
    params: NimParamsForwardMessagesToMultipleRecipients
  ) {
    return RNNeteaseIm.forwardMessagesToMultipleRecipients(params);
  }

  forwardMultiTextMessageToMultipleRecipients(
    params: NimParamsForwardMultiTextMessageToMultipleRecipients
  ) {
    return RNNeteaseIm.forwardMultiTextMessageToMultipleRecipients(params);
  }

  addEmptyPinRecentSession(sessionId: string, sessionType: NIMSessionTypeEnum) {
    return RNNeteaseIm.addEmptyPinRecentSession(sessionId, sessionType);
  }

  addEmptyTemporarySession(
    sessionId: string,
    temporarySessionRef: ITemporarySessionRef
  ) {
    return RNNeteaseIm.addEmptyTemporarySession(sessionId, temporarySessionRef);
  }

  removeTemporarySessionRef(sessionId: string) {
    return RNNeteaseIm.removeTemporarySessionRef(sessionId);
  }

  updateRecentToTemporarySession(
    sessionId: string,
    messageId: string,
    temporarySessionRef: ITemporarySessionRef
  ) {
    return RNNeteaseIm.updateRecentToTemporarySession(
      sessionId,
      messageId,
      temporarySessionRef
    );
  }

  updateMessageOfCsr(messageId: string, sessionId: string) {
    return RNNeteaseIm.updateMessageOfCsr(messageId, sessionId);
  }

  setListCustomerServiceAndChatbot(
    data: Record<string, NIMSessionOnlineServiceType>
  ) {
    return RNNeteaseIm.setListCustomerServiceAndChatbot(data);
  }

  startObserverMediaChange() {
    return RNNeteaseIm.startObserverMediaChange();
  }

  stopObserverMediaChange() {
    return RNNeteaseIm.stopObserverMediaChange();
  }

  removeReactedUsers(sessionId: string, sessionType: NIMSessionTypeEnum) {
    return RNNeteaseIm.removeReactedUsers(sessionId, sessionType)
  }

  getOwnedGroupCount(): Promise<number> {
    return RNNeteaseIm.getOwnedGroupCount()
  }

  hasMultipleMessages(sessionId: string, sessionType: NIMSessionTypeEnum): Promise<boolean> {
    return RNNeteaseIm.hasMultipleMessages(sessionId, sessionType)
  }

  updateIsTransferMessage(sessionId: string, sessionType: NIMSessionTypeEnum, messageId: string) {
    return RNNeteaseIm.updateIsTransferMessage(sessionId, sessionType, messageId)
  }
}

export default new NimSession();
