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
  observeAttachmentProgress = "observeAttachmentProgress",
  observeOnKick = "observeOnKick",
  observeCustomNotification = "observeCustomNotification",
  observeProgressSend = "observeProgressSend",
}

export enum NIMAudioMsgStatusType {
  START = "start",
  PROGRESS = "progress",
  COMPLETED = "completed",
  STOP = "stop",
}
