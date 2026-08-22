import { NativeModules } from 'react-native';
import {
  ChatroomMessageOrderBy,
  IParamsLoginChatroom,
  NIMChatroomInfo,
  NIMChatroomMember,
  NIMChatroomMemberListResult,
  NIMChatroomMessage,
} from 'react-native-netease-im/src/Chatroom/chatroom.type';
const { RNNeteaseIm } = NativeModules;

/**
 * FLOW: -
 * ROLE: Mặt JS của bridge NIM Chatroom V2 — imperative call sang native; event realtime đi qua
 *       `NativeAppEventEmitter` với tên trong `NIMEventListenerEnum.observeChatroom*`.
 * BREAKS: Đổi tên HOẶC số/thứ tự tham số ở đây phải đổi đồng thời `RNNeteaseImModule.java` (Android)
 *         và `RNNeteaseIm.m` + `ChatroomViewController` (iOS). Chữ ký lệch giữa 2 platform là crash
 *         LÚC CHẠY trên platform bị bỏ sót, compile vẫn xanh nên không có gì chặn lại. Sửa `.java`
 *         xong phải sync sang `node_modules` rồi build lại (KHÔNG patch-package).
 */
class NimChatroom {
  /**
   * Vào phòng. Dùng static token: `accid`/`token` lấy lại từ response `login` middleware,
   * `addrs` lấy từ backend `chatroom/addr` (bỏ trống thì chỉ còn LBS tự dò địa chỉ).
   */
  login(params: IParamsLoginChatroom): Promise<NIMChatroomInfo> {
    return RNNeteaseIm.loginChatroom(params);
  }

  logout(roomId: string): Promise<boolean> {
    return RNNeteaseIm.logoutChatroom(roomId);
  }

  fetchChatroomInfo(roomId: string): Promise<NIMChatroomInfo> {
    return RNNeteaseIm.fetchChatroomInfo(roomId);
  }

  /** Lấy member theo accountId — dùng cho profile tối thiểu khi bấm vào một người trong phòng. */
  fetchChatroomMember(
    roomId: string,
    userIds: string[]
  ): Promise<NIMChatroomMember[]> {
    return RNNeteaseIm.fetchChatroomMember(roomId, userIds);
  }

  /**
   * Member list phân trang; truyền `pageToken` của lần trước để lấy trang kế.
   * `onlyOnline = false` để lấy cả thành viên offline có role cố định (creator/manager).
   */
  fetchChatroomMembers(
    roomId: string,
    limit: number = 100,
    pageToken: string = '',
    onlyOnline: boolean = true
  ): Promise<NIMChatroomMemberListResult> {
    return RNNeteaseIm.fetchChatroomMembers(roomId, limit, pageToken, onlyOnline);
  }

  /**
   * Lịch sử tin nhắn (server giữ mặc định 10 ngày).
   * V2 phân trang theo THỜI GIAN, không theo messageId như bản old-gen: truyền `createTime` của tin
   * cũ nhất đang hiển thị để lấy tiếp; `beginTime = 0` là lấy từ hiện tại lùi về.
   */
  fetchMessageHistory(
    roomId: string,
    limit: number = 20,
    beginTime: number = 0,
    orderBy: ChatroomMessageOrderBy = ChatroomMessageOrderBy.DESC
  ): Promise<NIMChatroomMessage[]> {
    return RNNeteaseIm.fetchMessageHistory(roomId, limit, beginTime, orderBy);
  }

  /** Chẩn đoán: appKey SDK đang dùng lúc runtime (khác appKey manifest nếu đã updateAppKey). */
  getSdkAppKey(): Promise<{ sdkAppKey: string }> {
    return RNNeteaseIm.getNimSdkAppKey();
  }

  sendTextMessage(roomId: string, text: string): Promise<NIMChatroomMessage> {
    return RNNeteaseIm.sendChatroomTextMessage(roomId, text);
  }
}

export default new NimChatroom();
