export enum NIMEventListenerEnum {
  observeRecentContact = "observeRecentContact",
  observeOnlineStatus = "observeOnlineStatus",
  observeFriend = "observeFriend",
  observeTeam = "observeTeam",
  observeBlackList = "observeBlackList",
  observeReceiveMessage = "observeReceiveMessage",
  observeReceiveSystemMsg = "observeReceiveSystemMsg",
  observeUnreadCountChange = "observeUnreadCountChange",
  observeMsgStatus = "observeMsgStatus",
  observeAudioRecord = "observeAudioRecord",
  observeDeleteMessage = "observeDeleteMessage",
  /** @deprecated Không native nào emit event này. Dùng `observeProgressSend` cho tiến độ tải/gửi attachment. */
  observeAttachmentProgress = "observeAttachmentProgress",
  observeOnKick = "observeOnKick",
  observeCustomNotification = "observeCustomNotification",
  observeProgressSend = "observeProgressSend",
  observeUserStranger = "observeUserStranger",
  /** Trạng thái vòng đời cuộc gọi thoại (NERTC Call Kit). @see NIMCallStateEnum */
  observeCallState = "observeCallState",

  // ---- Chatroom V2 ----
  // Chỉ bắn KHI ĐANG ở trong phòng; không lưu, không offline push (NIM chatroom không có roaming
  // message). Thông báo nghiệp vụ (cấm chat, khiếu nại...) đi kênh backend notification, không phải đây.
  /** Vòng đời kết nối phòng: ENTERED / EXITED / các V2NIMChatroomStatus của SDK. */
  observeChatroomStatus = "observeChatroomStatus",
  observeChatroomKicked = "observeChatroomKicked",
  /** Tin nhắn mới trong phòng: `{ roomId, messages: NIMChatroomMessage[] }`. */
  observeChatroomMessage = "observeChatroomMessage",
  observeChatroomSendMessage = "observeChatroomSendMessage",
  observeChatroomMessageRevoked = "observeChatroomMessageRevoked",
  observeChatroomMemberIn = "observeChatroomMemberIn",
  observeChatroomMemberOut = "observeChatroomMemberOut",
  /** Bổ nhiệm/thu hồi admin phòng — mobile read-only, chỉ cập nhật icon theo event này. */
  observeChatroomMemberRoleUpdated = "observeChatroomMemberRoleUpdated",
  observeChatroomMemberInfoUpdated = "observeChatroomMemberInfoUpdated",
  /** Chính mình bị cấm/gỡ cấm chat -> disable input ngay. */
  observeChatroomSelfBanned = "observeChatroomSelfBanned",
  /** Cả phòng bị khoá chat. */
  observeChatroomChatBanned = "observeChatroomChatBanned",
  observeChatroomInfoUpdated = "observeChatroomInfoUpdated"
}

export enum NIMAudioMsgStatusType {
  START = "start",
  PROGRESS = "progress",
  COMPLETED = "completed",
  STOP = "stop",
}

export interface NIMUserStranger {
  avatar: string | null;
  nickname: string;
  accId: string;
  gender: string
}

export type NIMDataObserveUserStranger = Record<string, NIMUserStranger>