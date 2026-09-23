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

  /**
   * Gửi text. `serverExtension` là chuỗi tuỳ ý đi kèm tin (JSON của app, vd metadata trích dẫn);
   * bên nhận đọc lại ở `NIMChatroomMessage.serverExtension`.
   *
   * Mặc định `''` là CỐ Ý: bridge native nhận đúng 3 tham số, nếu để `undefined` lọt xuống thì
   * RN báo sai arity lúc chạy. Gọi 2 tham số như cũ vẫn chạy y nguyên nhờ default này.
   */
  sendTextMessage(
    roomId: string,
    text: string,
    serverExtension: string = ''
  ): Promise<NIMChatroomMessage> {
    return RNNeteaseIm.sendChatroomTextMessage(roomId, text, serverExtension);
  }

  /**
   * Cấm chat VĨNH VIỄN / gỡ (`chatBanned=false`). Gỡ vĩnh viễn không đụng hạn cấm tạm.
   * ⚠️ Chỉ creator/administrator phòng gọi được, và administrator không thao tác được lên
   * creator/administrator khác — xem QUYỀN ở `ChatroomV2Service.java`. Sai quyền thì SDK trả lỗi.
   */
  setMemberChatBanned(
    roomId: string,
    accountId: string,
    chatBanned: boolean,
    notificationExtension: string = ''
  ): Promise<boolean> {
    return RNNeteaseIm.setChatroomMemberChatBanned(
      roomId,
      accountId,
      chatBanned,
      notificationExtension
    );
  }

  /**
   * Cấm chat TẠM THỜI / gỡ. `tempChatBannedDuration` tính bằng **GIÂY** (không phải ms như
   * `timestamp` của message), tối đa 30 ngày một lần, truyền 0 để gỡ. Set lại là GHI ĐÈ hạn cũ,
   * không cộng dồn. Bridge không quy đổi đơn vị — store/UI tự quyết phút/giờ.
   */
  setMemberTempChatBanned(
    roomId: string,
    accountId: string,
    tempChatBannedDuration: number,
    notificationEnabled: boolean = true,
    notificationExtension: string = ''
  ): Promise<boolean> {
    return RNNeteaseIm.setChatroomMemberTempChatBanned(
      roomId,
      accountId,
      tempChatBannedDuration,
      notificationEnabled,
      notificationExtension
    );
  }

  /**
   * Thêm/gỡ danh sách đen NIM — công cụ ENFORCEMENT của "danh sách đen" nghiệp vụ [D-032].
   * Nặng hơn cấm chat: người bị chặn bị ĐÁ khỏi phòng (`observeChatroomKicked`) và mất kết nối.
   * ⚠️ Gọi ĐI KÈM API blacklist của backend, không gọi đơn lẻ — DB backend là nguồn sự thật,
   * hàm này chỉ tạo hiệu lực tức thì.
   */
  setMemberBlocked(
    roomId: string,
    accountId: string,
    blocked: boolean,
    notificationExtension: string = ''
  ): Promise<boolean> {
    return RNNeteaseIm.setChatroomMemberBlocked(
      roomId,
      accountId,
      blocked,
      notificationExtension
    );
  }
}

export default new NimChatroom();
