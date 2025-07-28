import { NIMCommonBooleanType } from 'react-native-netease-im/src/utils/common.type';
import {
  INimMessageReaction,
  INimMessageRemoveReaction,
  NIMMessage,
  NIMMessageReactionEnum,
  NIMMessageStatusEnum,
  NIMMessageTypeEnum,
} from '../Message/message.type';
import {
  NIMTeamDetailType,
  NIMTeamMemberType,
  NIMTeamOperationType,
  NIMTeamOperationTypeUpdateDetail,
} from '../Team/team.type';

export interface ICreateRecentOnlineService {
  sessionId: string;
  onlineServiceType: NIMSessionOnlineServiceType;
  nickname?: string;
}

export enum QueryDirectionType {
  NEW = 1,
  OLD = 0,
}

export enum NIMMessageSubTypeEnum {
  DEFAULT = 0,
  LINK = 1,
  REACTION = 2,
  REMOVE_REACTION = 3,
  REVOKE_MESSAGE = 4,
  MESSAGE_MULTI_MEDIA_IMAGE = 5,
  MESSAGE_MULTI_MEDIA_VIDEO = 6,
  TEMPORARY_SESSION = 7,
  NOTIFICATION_BIRTHDAY = 8,
  MESSAGE_FORWARD = 9,
}

export enum NIMSessionTypeEnum {
  None = 'None',
  P2P = '0',
  Team = '1',
  SUPER_TEAM = 'SUPER_TEAM',
  System = 'System',
  Ysf = 'Ysf',
  ChatRoom = '2',
  QChat = 'QChat',
  onlineService = 'online_service',
  systemMessage = 'system_message',
  listStranger = 'listStranger',
  listGroupChat = 'list_group_chat',
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
  OUT_SESSION = 'out_session',
  CONNECTED_CSR = 'connected_csr',
  RECONNECT_CSR = 'reconnect_csr',
}

export enum NIMSendAttachmentEnum {
  ONE_TO_ONE = '0',
  ONE_TO_MANY_FIXED_AMOUNT = '1',
  ONE_TO_MANY_RANDOM_AMOUNT = '2',
}

export interface NIMReactedUserType {
  messageId: string;
  reactionType: NIMMessageReactionEnum;
  accId: string;
  nickname: string;
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
  extendType?: string;
}

export interface NimParamsForwardMessagesToMultipleRecipients {
  recipients: Array<{ sessionId: string; sessionType: NIMSessionTypeEnum }>;
  messageIds: string[];
  content?: string;
  parentId?: string;
  isHaveMultiMedia?: boolean;
}

export interface NimParamsForwardMultiTextMessageToMultipleRecipients {
  recipients: Array<{
    sessionId: string;
    sessionType: NIMSessionTypeEnum;
    isSkipFriendCheck: boolean;
  }>;
  messageText: string;
  content?: string;
}

export interface NimSessionType {
  teamInfo?: NIMTeamDetailType;
  teamMembers?: NIMTeamMemberType[];
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
    latestMsgIdWithHideSession?: string;
    reaction?: INimMessageReaction;
    dataRemoveReaction?: INimMessageRemoveReaction;
    // revokeMessage
    revokeMessage?: {
      sessionId: string;
      messageId: string;
    };
    multiMediaType?: 'video' | 'image';
    temporarySessionRef?: ITemporarySessionRef;
    isMessageChatBotUpdated?: boolean;
    isChatBotNotifyOutSessionOfCurrentCsr?: boolean;
    onlineServiceMessage?: NIMMessage;
    reactedUsers?: NIMReactedUserType[];
    messageReacted?: NIMMessage;
    isTransferUpdated?: boolean;
  };
  isOutgoing: boolean;
  mute: NIMCommonBooleanType;
  messageSubType?: NIMMessageSubTypeEnum;
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

export type ITemporarySessionRef = {
  sessionId: string;
  sessionName: string;
  sessionType: NIMSessionTypeEnum;
};
