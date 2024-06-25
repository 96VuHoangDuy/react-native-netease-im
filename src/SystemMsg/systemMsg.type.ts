import { NIMCommonBooleanType } from "../utils/common.type";

export enum NIMSystemMsgTypeEnum {
  PassFriendApply = "0",
  AcceptInvite = "2",
  AckAddFriendRequest = "5",

  ApplyJoinTeam = "",
  RejectTeamApply = "",
  TeamInvite = "",
  DeclineTeamInvite = "",
  AddFriend = "",
  SuperTeamApply = "",
  SuperTeamApplyReject = "",
  SuperTeamInvite = "",
  SuperTeamInviteReject = "",
}

export enum NIMSystemMsgStatusEnum {
  init = "0",
  passed = "1",
  declined = "2",
  ignored = "3",
  expired = "4",
  extension1 = "100",
  extension2 = "101",
  extension3 = "102",
  extension4 = "103",
  extension5 = "104",
}

export interface NIMSystemMsgType {
  avatar: string;
  content: string;
  fromAccount: string;
  isVerify: NIMCommonBooleanType;
  messageId: string;
  name: string;
  status: NIMSystemMsgStatusEnum;
  targetId: string;
  time: string;
  type: NIMSystemMsgTypeEnum;
  verifyResult: string;
  verifyText: string;
}

export enum NIMCustomNotificationTypeEnum {
  OBSERVE_RECEIVE_REVOKE_MESSAGE = 1,
  OBSERVE_RECEIVE_FRIEND_REMOVED_ME = 2,
}

export interface ICustomNotificationDataDict {
  type: NIMCustomNotificationTypeEnum;
  sessionId: string;
  messageId?: string;
  isObserveReceiveRevokeMessage?: string;
  isObserveFriendRemovedMe?: string;
}
