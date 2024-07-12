import { NIMCommonBooleanType } from 'react-native-netease-im/src/utils/common.type';
import {
  INimMessageReaction,
  INimMessageRemoveReaction,
  NIMMessageStatusEnum,
  NIMMessageTypeEnum,
} from '../Message/message.type';
import {
  NIMTeamOperationType,
  NIMTeamOperationTypeUpdateDetail,
} from '../Team/team.type';

export enum QueryDirectionType {
  NEW = 1,
  OLD = 0,
}

export enum NIMMessageSubTypeEnum {
  DEFAULT = 0,
  LINK = 1,
  REACTION = 2,
  REMOVE_REACTION = 3
}

export enum NIMSessionTypeEnum {
  None = 'None',
  P2P = '0',
  Team = '1',
  SUPER_TEAM = 'SUPER_TEAM',
  System = 'System',
  Ysf = 'Ysf',
  ChatRoom = 'ChatRoom',
  QChat = 'QChat',
  onlineService = 'online_service',
  systemMessage = 'system_message',
  listStranger = 'listStranger',
}

export enum NIMSessionOnlineServiceType {
  CHATBOT = 'chatbot',
  CSR = 'csr',
}

export enum NIMQueryDirectionEnum {
  QUERY_OLD = 'QUERY_OLD',
  QUERY_NEW = 'QUERY_NEW',
}

export enum NIMCustomAttachmentEnum {
  RedPacket = 'redpacket',
  BankTransfer = 'transfer',
  BankTransferSystem = 'system',
  RedPacketOpen = 'redpacketOpen',
  ProfileCard = 'ProfileCard',
  Collection = 'Collection',
  SystemImageText = 'SystemImageText',
  LinkUrl = 'url',
  AccountNotice = 'account_notice',
  Card = 'card',
}

export enum NIMMessageChatBotType {
  OFFLINE = 'offline',
  FIND_CSR = 'find_csr',
  OUT_SESSION = 'out_session',
}

export enum NIMSendAttachmentEnum {
  ONE_TO_ONE = '0',
  ONE_TO_MANY_FIXED_AMOUNT = '1',
  ONE_TO_MANY_RANDOM_AMOUNT = '2',
}

export interface CustomMessageType {
  width: number;
  height: number;
  pushContent: string;
  recentContent: string[];
}

export interface NimSessionTypeExtend {
  tipMsg?: string;
  sourceId?: { sourceId: string; sourceName: string };
  targets?: Array<{ targetId: string; targetName: string }>;
  operationType?: NIMTeamOperationType;
  isMute?: 'mute' | 'unmute';
  updateDetail?: {
    type: NIMTeamOperationTypeUpdateDetail;
    value: any;
  };
}

export interface NimSessionType {
  account: string;
  msgStatus: NIMMessageStatusEnum;
  msgType: NIMMessageTypeEnum;
  sessionType: NIMSessionTypeEnum;
  messageId: string;
  content: string;
  time: string;
  unreadCount: string;
  contactId: string;
  imagePath: string;
  name: string;
  extend?: NimSessionTypeExtend;
  isMyFriend?: boolean;
  isReplyStranger?: boolean;
  localExt?: {
    isChatBot: boolean;
    isCsr: boolean;
    isUpdated: boolean;
    name?: string;
    isHideSession?: boolean;
    isPinCode?: boolean;
    latestMsgIdWithHideSession?: string
    reaction?: INimMessageReaction;
    dataRemoveReaction?: INimMessageRemoveReaction
  };
  isOutgoing: boolean;
  mute: NIMCommonBooleanType;
  messageSubType?: NIMMessageSubTypeEnum
}

export type SessionCache = {
  size: string;
  sessionId: string;
  sizeNumber?: number;
};

export type ListSessionCacheType = {
  data: SessionCache[];
  totalSize: string;
};

export type NIMBirthdayMemberType = {
  contactId: string;
  name: string;
};
