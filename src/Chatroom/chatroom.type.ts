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
  /** accid + token lấy lại từ response `login` của middleware (static token, không dynamic). */
  accid: string;
  token: string;
  /** Link address từ backend `chatroom/addr`; rỗng thì SDK tự dò bằng LBS. */
  addrs?: string[];
  /**
   * appKey backend cấp (khác appKey khoá cứng trong manifest Android). Bắt buộc — thiếu thì NIM
   * xác thực token dưới appKey manifest và trả 102302 invalid token.
   */
  appKey: string;
  /**
   * Kiểu xác thực khi vào phòng. Backend hiện cấp DYNAMIC token (JWT có hạn) nên mặc định là
   * `dynamic`; `static` chỉ để đối chứng khi debug.
   */
  authType?: 'dynamic' | 'static';
};

export type NIMChatroomMember = {
  roomId: string;
  userId: string;
  nickname: string;
  avatar: string;
  avatarThumbnail: string;
  type: ChatroomMemberType;
  isMuted: boolean;
  isTempMuted: boolean;
  tempMuteDuration: number;
  isOnline: boolean;
  isBlocked: boolean;
  enterTime: number;
  updateTime: number;
};

/** `onlyOnline = false` khi query -> kết quả gồm cả member offline có role cố định (creator/manager). */
export type NIMChatroomMemberListResult = {
  /** Truyền lại vào `fetchChatroomMembers` để lấy trang kế. */
  pageToken: string;
  finished: boolean;
  members: NIMChatroomMember[];
};

export type NIMChatroomMessage = {
  msgId: string;
  roomId: string;
  fromAccount: string;
  text: string;
  /** V2NIMMessageType của SDK, ví dụ `V2NIM_MESSAGE_TYPE_TEXT`. */
  msgType: string;
  subType: number;
  serverExtension?: string;
  sendingState: string;
  timestamp: number;
  isSelf: boolean;
};

export interface NIMChatroomInfo {
  roomId: string;
  name: string;
  announcement?: string;
  onlineUserCount: number;
  broadcastUrl?: string;
  isLoginSuccess: boolean;
  creatorAccountId?: string;
  serverExtension?: string;
  /** Cả phòng bị khoá chat (khác với chính mình bị cấm — xem `isMuted` của member). */
  isChatBanned?: boolean;
  isValidRoom?: boolean;
  /** Chỉ có trong kết quả `login`. */
  selfMember?: NIMChatroomMember;
}
