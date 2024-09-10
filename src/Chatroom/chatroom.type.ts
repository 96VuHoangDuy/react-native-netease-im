import { NIMUserInfo } from 'react-native-netease-im/src/User/user.type';

export enum ChatroomMemberType {
  GUEST = 'GUEST',
  LIMIT = 'LIMIT',
  NORMAL = 'NORMAL',
  CREATOR = 'CREATOR',
  MANAGER = 'MANAGER',
  ANONYMOUS_GUEST = 'ANONYMOUS_GUEST',
}

export enum ChatroomMessageOrderBy {
  ASC = "ASC",
  DESC = "DESC"
}

export type IParamsLoginChatroom = {
  roomId: string;
  nickname: string;
  avatar: string;
};

export type NIMChatroomMember = {
  userId: string;
  nickname: string;
  avatar: string;
  avatarThumbnail: string;
  type: ChatroomMemberType;
  isMuted: boolean;
  isTempMuted: boolean;
  tempMuteDuration: number;
  isOnline: boolean;
};

export interface NIMChatroomInfo {
  roomId: string;
  name: string;
  announcement?: string;
  onlineUserCount: number;
  broadcastUrl?: string;
  isLoginSuccess: boolean
}
