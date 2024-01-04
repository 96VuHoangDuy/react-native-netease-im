import {
  NIMMessageChatBotType,
  NIMSessionTypeEnum,
  NimSessionTypeExtend,
} from "../Session/session.type";
import {
  NIMTeamOperationType,
  NIMTeamOperationTypeUpdateDetail,
} from "../Team/team.type";

export enum NIMMessageTypeEnum {
  TEXT = "text",
  VOICE = "voice",
  IMAGE = "image",
  VIDEO = "video",
  FILE = "file",
  ROBOT = "robot",
  BANK_TRANSFER = "transfer",
  ACCOUNT_NOTICE = "account_notice",
  EVENT = "event",
  LOCATION = "location",
  NOTIFICATION = "notification",
  TIP = "tip",
  RED_PACKET = "redpacket",
  RED_PACKET_OPEN = "redpacketOpen",
  LINK = "url",
  CARD = "card",
  CUSTOM = "custom",
  MULTIPLE_TEXT = "forwardMultipleText",
  GIF = "gif",
  UNKNOWN = "unknown",
}

export enum NIMMessageStatusEnum {
  SEND_DRAFT = "send_draft",
  SEND_FAILED = "send_failed",
  SEND_SENDING = "send_going",
  SEND_SUCCESS = "send_succeed",
  RECEIVE_READ = "receive_read",
  RECEIVE_UNREAD = "receive_unread",
}

export interface NimMessageTypeExtend extends NimSessionTypeExtend {
  duration: number;
  isPlayed: boolean;
  url: string;
  thumbPath: string;
  path: string;
  messages: NIMMessage;
  isFilePathDeleted: boolean;
  needRefreshMessage: boolean;
  isReplacePathSuccess: boolean;

  // have when message type is "card"
  extendType?: string;

  type?: string; // card session type
  name?: string; // card session name
  imgPath?: string; // card image avatar
  sessionId?: string; // card sessionId

  videoUrl?: string; //video
  coverUrl?: string; //video
  coverPath?: string;
  // location
  latitude?: string;
  longitude?: string;
  title?: string;
}

export interface NIMMessage {
  extend?: NimMessageTypeExtend;
  //  {
  //   tipMsg?: string;
  //   sourceId?: string;
  //   targets?: string[];
  //   operationType?: NIMTeamOperationType;
  //   updateDetail?: {
  //     type: NIMTeamOperationTypeUpdateDetail;
  //     value: any;
  //   };
  //   // audio message
  //   duration: number;
  //   isPlayed: boolean;
  //   url: string;
  // };
  isRemoteRead: number;

  fromUser: {
    _id: string;
    avatar: string;
    name: string;
    isCsr?: string;
    isChatBot?: string;
  };
  isOutgoing: boolean;
  isShowTime: boolean;
  msgId: string;
  msgType: NIMMessageTypeEnum;
  sessionId: string;
  sessionType: NIMSessionTypeEnum;
  status: NIMMessageStatusEnum;
  text: string;
  timeString: string;
  // audio message
  mediaPath: string;
  duration: number;
  localExt?: {
    chatBotType?: NIMMessageChatBotType;
    isCancelResend?: boolean
  };
}
