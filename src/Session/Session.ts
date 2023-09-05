import { NativeModules, Platform } from "react-native";
import {
  CustomMessageType,
  NimSessionType,
  QueryDirectionEnum,
  SendAttachmentType,
} from "./session.type";
const { RNNeteaseIm } = NativeModules;

/**
 * 登陆
 * @param account
 * @param token
 * @returns {*} @see observeRecentContact observeOnlineStatus
 */
function login(contactId: string, token: string) {
  return RNNeteaseIm.login(contactId, token);
}
/**
 * 退出
 * @returns {*}
 */
function logout() {
  return RNNeteaseIm.logout();
}
/**
 * 最近会话列表
 * @returns {*}
 */
function getRecentContactList() {
  return RNNeteaseIm.getRecentContactList();
}
/**
 * 删除最近会话
 * @param recentContactId
 * @returns {*}
 */
function deleteRecentContact(recentContactId: string) {
  return RNNeteaseIm.deleteRecentContact(recentContactId);
}
/**
 * 进入聊天会话
 * @param sessionId
 * @param type
 * @returns {*} @see observeReceiveMessage 接收最近20消息记录
 */
function startSession(sessionId: string, type: NimSessionType) {
  return RNNeteaseIm.startSession(sessionId, type);
}

/**
 * 退出聊天会话
 * @returns {*}
 */
function stopSession() {
  return RNNeteaseIm.stopSession();
}
/**
 * 获取云端聊天记录
 * @param messageId
 * @param limit 查询结果的条数限制
 * @returns {*}  @see 回调返回最近所有消息记录
 */
function queryMessageListEx(messageId: string, limit: number) {
  return RNNeteaseIm.queryMessageListEx(messageId, limit);
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
function queryMessageListHistory(
  sessionId: string,
  sessionType: NimSessionType,
  timeLong: string,
  direction: QueryDirectionEnum,
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
function sendTextMessage(content: string, atUserIds?: string[]) {
  return RNNeteaseIm.sendTextMessage(content, atUserIds);
}

/**
 * 发送图片消息
 * @param file 图片文件对象
 * @param type 0:图片 1：视频
 * @param displayName 文件显示名字，如果第三方 APP 不关注，可为空
 * @returns {*}
 */
function sendImageMessages(file: string, displayName?: string) {
  if (Platform.OS === "ios") {
    return RNNeteaseIm.sendImageMessages(file, displayName);
  }
  return RNNeteaseIm.sendImageMessage(file.replace("file://", ""), displayName);
}
/**
 * 发送音频消息
 * @param file 音频文件
 * @param duration 音频持续时间，单位是ms
 * @returns {*}
 */
function sendAudioMessage(file: string, duration: string) {
  return RNNeteaseIm.sendAudioMessage(file, duration);
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
function sendVideoMessage(
  file: string,
  duration: string,
  width: number,
  height: number,
  displayName?: string
) {
  return RNNeteaseIm.sendVideoMessage(
    file,
    duration,
    width,
    height,
    displayName
  );
}
/**
 * 发送地理位置消息
 * @param latitude 纬度
 * @param longitude 经度
 * @param address 地址信息描述
 * @returns {*}
 */
function sendLocationMessage(
  latitude: string,
  longitude: string,
  address: string
) {
  return RNNeteaseIm.sendLocationMessage(latitude, longitude, address);
}
/**
 * 发送系统通知
 * @param content
 * @returns {*}
 */
function sendTipMessage(content: string) {
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
function sendRedPacketMessage(
  type: SendAttachmentType,
  comments: string,
  serialNo: string
) {
  return RNNeteaseIm.sendRedPacketMessage(type, comments, serialNo);
}

/**
 * 名片
 * @param type 个人名片 群名片 公众号名片
 * @param name
 * @param imgPath
 * @param sessionId
 * @returns {*}
 */
function sendCardMessage(
  type: SendAttachmentType,
  name: string,
  imgPath: string,
  sessionId: string
) {
  return RNNeteaseIm.sendCardMessage(type, name, imgPath, sessionId);
}
/**
 * 拆红包
 * @param sendId 发送红包的sessionId
 * @param hasRedPacket '0': mantled, '1': dismantled
 * @param serialNo 流水号
 * @returns {*}
 */
function sendRedPacketOpenMessage(
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
function sendBankTransferMessage(
  amount: string,
  comments: string,
  serialNo: string
) {
  return RNNeteaseIm.sendBankTransferMessage(amount, comments, serialNo);
}
/**
 * 发送自定义消息
 * @param attachment 自定义消息内容{Width:260,Height:100,pushContent:'发来一条自定义消息',recentContent:'[自定义消息]'}
 * width, height of message, pushContent: string, recentContent: string[]
 * @returns {*}
 */
function sendCustomMessage(attachment: CustomMessageType) {
  return RNNeteaseIm.sendCustomMessage(attachment);
}
/**
 * 开启录音权限
 * @returns {*}
 */
function onTouchVoice() {
  return RNNeteaseIm.onTouchVoice();
}
/**
 * 开始录音
 * @returns {*}
 */
function startAudioRecord() {
  return RNNeteaseIm.startAudioRecord();
}

/**
 * 结束录音,自动发送
 * @returns {*} @see observeAudioRecord
 */
function endAudioRecord() {
  return RNNeteaseIm.endAudioRecord();
}

/**
 * 取消播放录音
 * @returns {*}
 */
function cancelAudioRecord() {
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
function sendForwardMessage(
  messageId: string,
  sessionId: string,
  sessionType: NimSessionType,
  content: string
) {
  return RNNeteaseIm.sendForwardMessage(
    messageId,
    sessionId,
    sessionType,
    content
  );
}
/**
 * 消息撤回
 * @param messageId
 * @returns {*}
 */
function revokeMessage(messageId: string) {
  return RNNeteaseIm.revokeMessage(messageId);
}

/**
 * 重发消息到服务器
 * @param messageId
 * @returns {*}
 */
function resendMessage(messageId: string) {
  return RNNeteaseIm.resendMessage(messageId);
}
/**
 * 消息删除
 * @param messageId
 * @returns {*}
 */
function deleteMessage(messageId: string) {
  return RNNeteaseIm.deleteMessage(messageId);
}
/**
 * 清空聊天记录
 * @param messageId
 * @returns {*}
 */
function clearMessage(sessionId: string, type: NimSessionType) {
  return RNNeteaseIm.clearMessage(sessionId, type);
}

/**
 * Android下载文件附件
 * @param messageId
 * @returns {*}
 */
function downloadAttachment(messageId: string) {
  return RNNeteaseIm.downloadAttachment(messageId, "0");
}

/**
 * 更新录音消息是否播放过的状态
 * @param messageId
 * @returns {*}
 */
function updateAudioMessagePlayStatus(messageId: string) {
  return RNNeteaseIm.updateAudioMessagePlayStatus(messageId);
}

function getLaunch() {
  if (Platform.OS === "android") {
    return RNNeteaseIm.getLaunch();
  }
}

export const NimSession = {
  login,
  logout,
  getRecentContactList,
  deleteRecentContact,
  startSession,
  stopSession,
  queryMessageListEx,
  queryMessageListHistory,
  sendTextMessage,
  sendImageMessages,
  sendAudioMessage,
  sendVideoMessage,
  sendTipMessage,
  sendRedPacketMessage,
  sendCardMessage,
  sendRedPacketOpenMessage,
  sendBankTransferMessage,
  sendCustomMessage,
  onTouchVoice,
  startAudioRecord,
  endAudioRecord,
  cancelAudioRecord,
  sendForwardMessage,
  revokeMessage,
  resendMessage,
  deleteMessage,
  clearMessage,
  downloadAttachment,
  updateAudioMessagePlayStatus,
  getLaunch,
  sendLocationMessage,
};
