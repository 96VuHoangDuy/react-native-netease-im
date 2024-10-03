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
  observeUserStranger = "observeUserStranger"
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