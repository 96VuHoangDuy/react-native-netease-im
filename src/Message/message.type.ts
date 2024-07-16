import {
  NIMMessageChatBotType,
  NIMMessageSubTypeEnum,
  NIMSessionTypeEnum,
  NimSessionTypeExtend,
} from '../Session/session.type';
import {
  NIMTeamOperationType,
  NIMTeamOperationTypeUpdateDetail,
} from '../Team/team.type';
import { NIMCommonBooleanType } from '../utils/common.type';

export enum NIMMessageMediaType {
  IMAGE = 'image',
  VIDEO = 'video',
}

export interface NIMMessageMediaImageData {
  file: string;
  displayName?: string;
  isHighQuality?: boolean;
}

export interface NIMMessageMediaVideoData {
  file: string;
  duration: string;
  width: number;
  height: number;
  displayName?: string;
}

interface NIMMessageMediaImage {
  type: NIMMessageMediaType.IMAGE;
  indexCount?: number;
  data: NIMMessageMediaImageData;
}

interface NIMMessageMediaVideo {
  type: NIMMessageMediaType.VIDEO;
  indexCount?: number;
  data: NIMMessageMediaVideoData;
}

export type NIMMessageMedia = NIMMessageMediaImage | NIMMessageMediaVideo;

// interface

export enum NIMMessageTypeEnum {
  TEXT = 'text',
  VOICE = 'voice',
  IMAGE = 'image',
  VIDEO = 'video',
  FILE = 'file',
  ROBOT = 'robot',
  BANK_TRANSFER = 'transfer',
  ACCOUNT_NOTICE = 'account_notice',
  EVENT = 'event',
  LOCATION = 'location',
  NOTIFICATION = 'notification',
  TIP = 'tip',
  RED_PACKET = 'redpacket',
  RED_PACKET_OPEN = 'redpacketOpen',
  LINK = 'url',
  CARD = 'card',
  CUSTOM = 'custom',
  MULTIPLE_TEXT = 'forwardMultipleText',
  GIF = 'gif',
  UNKNOWN = 'unknown',
  EMPTY_SESSION = 'EMPTY_SESSION',
}

export enum NIMMessageStatusEnum {
  SEND_DRAFT = 'send_draft',
  SEND_FAILED = 'send_failed',
  SEND_SENDING = 'send_going',
  SEND_SUCCESS = 'send_succeed',
  RECEIVE_READ = 'receive_read',
  RECEIVE_UNREAD = 'receive_unread',
}

export enum NIMMessageReactionEnum {
  HEART = 'HEART',
  LIKE = 'LIKE',
  HAHA = 'HAHA',
  SURPRISE = 'SURPRISE',
  CRY = 'CRY',
  ANGRY = 'ANGRY',
}

export type INimMessageReactionEnum = `${NIMMessageReactionEnum}`;

export interface INimMessageReactionSymbol {
  name: INimMessageReactionEnum;
  symbol: string;
}

export interface INimMessageReaction {
  accId: string;
  type: INimMessageReactionEnum;
  totalReaction: number;
  nickname: string;
  avatar: string;
}

export interface INimMessageRemoveReaction {
  sessionId: string;
  sessionType: NIMSessionTypeEnum;
  messageId: string;
  accId: string;
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
  isFileDownloading: NIMCommonBooleanType;

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
  // file
  filePath: string;
  fileName: string;
  fileSize: string;
  fileMd5: string;
  fileUrl: string;
  fileType: string;

  aspectRatio?: number;

  downloadAttStatus?: string;

  // Size IOS
  coverSizeWidth?: number;
  coverSizeHeight?: number;

  // Size Android
  imageWidth?: number;
  imageHeight?: number;

  parentId?: string;
  indexCount?: number;
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
    isCancelResend?: boolean;
    reactions?: INimMessageReaction[];
    reaction?: INimMessageReaction;
    dataRemoveReaction?: INimMessageRemoveReaction;
    revokeMessage?: {
      sessionId: string;
      messageId: string;
    };
  };
  messageSubType?: NIMMessageSubTypeEnum;
}

export enum NIMFileType {
  image = 'image/',
  audio = 'audio/',
  video = 'video/',
}
