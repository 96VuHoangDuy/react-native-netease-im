import { NIMUserInfo } from 'react-native-netease-im/src/User/user.type';

/**
 * Role thành viên phòng. Giá trị = tên hằng `V2NIMChatroomMemberRole` bỏ tiền tố
 * `V2NIM_CHATROOM_MEMBER_ROLE_` (verify bằng `javap` trên `chatroom-10.9.52.aar`).
 * `GUEST`/`LIMIT` là di sản old-gen, SDK V2 KHÔNG bao giờ trả — giữ lại vì chưa rà hết chỗ dùng.
 */
export enum ChatroomMemberType {
  GUEST = 'GUEST',
  LIMIT = 'LIMIT',
  NORMAL = 'NORMAL',
  CREATOR = 'CREATOR',
  MANAGER = 'MANAGER',
  /** Khách vãng lai đã đăng nhập — native trả giá trị này, trước đây enum thiếu nên lệch contract. */
  NORMAL_GUEST = 'NORMAL_GUEST',
  ANONYMOUS_GUEST = 'ANONYMOUS_GUEST',
  VIRTUAL = 'VIRTUAL',
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

/**
 * Loại tin hệ thống của phòng — giá trị = tên hằng `V2NIMChatroomMessageNotificationType` bỏ tiền tố
 * `V2NIM_CHATROOM_MESSAGE_NOTIFICATION_TYPE_` (verify bằng `javap` trên `chatroom-10.9.52.aar`,
 * đủ 19 giá trị SDK khai).
 */
export enum ChatroomNotificationType {
  MEMBER_ENTER = 'MEMBER_ENTER',
  MEMBER_EXIT = 'MEMBER_EXIT',
  MEMBER_BLOCK_ADDED = 'MEMBER_BLOCK_ADDED',
  MEMBER_BLOCK_REMOVED = 'MEMBER_BLOCK_REMOVED',
  MEMBER_CHAT_BANNED_ADDED = 'MEMBER_CHAT_BANNED_ADDED',
  MEMBER_CHAT_BANNED_REMOVED = 'MEMBER_CHAT_BANNED_REMOVED',
  MEMBER_TEMP_CHAT_BANNED_ADDED = 'MEMBER_TEMP_CHAT_BANNED_ADDED',
  MEMBER_TEMP_CHAT_BANNED_REMOVED = 'MEMBER_TEMP_CHAT_BANNED_REMOVED',
  MEMBER_INFO_UPDATED = 'MEMBER_INFO_UPDATED',
  MEMBER_KICKED = 'MEMBER_KICKED',
  ROOM_INFO_UPDATED = 'ROOM_INFO_UPDATED',
  QUEUE_CHANGE = 'QUEUE_CHANGE',
  CHAT_BANNED = 'CHAT_BANNED',
  CHAT_BANNED_REMOVED = 'CHAT_BANNED_REMOVED',
  TAG_TEMP_CHAT_BANNED_ADDED = 'TAG_TEMP_CHAT_BANNED_ADDED',
  TAG_TEMP_CHAT_BANNED_REMOVED = 'TAG_TEMP_CHAT_BANNED_REMOVED',
  MESSAGE_REVOKE = 'MESSAGE_REVOKE',
  TAGS_UPDATE = 'TAGS_UPDATE',
  ROLE_UPDATE = 'ROLE_UPDATE',
}

export type NIMChatroomMessage = {
  msgId: string;
  roomId: string;
  fromAccount: string;
  /**
   * Tên/avatar người gửi đính kèm message (`V2NIMUserInfoConfig`). **Optional** — vắng khi SDK
   * không đính kèm. Ưu tiên dùng thay vì tra member list: member list trả `nickname` rỗng với
   * CREATOR, và không tra được người đã rời phòng.
   */
  senderNickname?: string;
  senderAvatar?: string;
  text: string;
  /** V2NIMMessageType của SDK, ví dụ `V2NIM_MESSAGE_TYPE_TEXT`. */
  msgType: string;
  subType: number;
  serverExtension?: string;
  sendingState: string;
  timestamp: number;
  isSelf: boolean;
  /**
   * Chỉ có khi `msgType === 'V2NIM_MESSAGE_TYPE_NOTIFICATION'` — dữ liệu nằm trong attachment,
   * không phải `text` (tin hệ thống luôn có `text` rỗng và `subType: 0`).
   * `MEMBER_ENTER`/`MEMBER_EXIT` là đường **duy nhất** biết ai vào/ra phòng khi console Yunxin
   * chưa bật "chatroom user in/out message system delivery" — callback `observeChatroomMemberIn`
   * không bắn trong trường hợp đó.
   */
  notificationType?: ChatroomNotificationType;
  /** accid của (những) người bị tác động — người vào/ra, người bị ban... */
  targetIds?: string[];
  /** Nickname tương ứng `targetIds`, cùng thứ tự. Vắng khi server không gửi. */
  targetNicks?: string[];
  /** Người thực hiện (admin ban/kick). Vắng với tin vào/ra tự nhiên. */
  operatorId?: string;
  operatorNick?: string;
  notificationExtension?: string;
  /**
   * Chỉ có với notification loại **ban** (`*_CHAT_BANNED_*`). Ban vĩnh viễn và ban tạm là hai
   * trạng thái độc lập ở server — thiếu hai cờ này thì lúc GỠ không biết loại nào vừa được gỡ.
   */
  chatBanned?: boolean;
  tempChatBanned?: boolean;
  /** Đơn vị **GIÂY** trên cả 2 platform (khác `timestamp` — ms bên Android). `0` = đã gỡ ban tạm. */
  tempChatBannedDuration?: number;
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
