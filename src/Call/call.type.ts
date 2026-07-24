/**
 * Trạng thái vòng đời cuộc gọi thoại (NERTC Call Kit), forward qua event `observeCallState`.
 * Giá trị khớp string native emit.
 */
export enum NIMCallStateEnum {
  /** Đang phát cuộc gọi đi (chờ đối phương đổ chuông) */
  CALLING = "calling",
  /** Cuộc gọi đến (inbound) */
  INCOMING = "incoming",
  /** Đối phương đang đổ chuông */
  RINGING = "ringing",
  /** Đã kết nối (audio thông) */
  CONNECTED = "connected",
  /** Bị từ chối */
  REJECTED = "rejected",
  /** Hết giờ chờ / không trả lời */
  TIMEOUT = "timeout",
  /** Kết thúc / cúp máy */
  ENDED = "ended",
  /** Lỗi */
  ERROR = "error",
}

/** Payload event `observeCallState`. */
export interface NIMCallStateEvent {
  state: NIMCallStateEnum | string;
  /** accid đối phương (nếu có) */
  accId?: string;
  /** Mã lỗi native (nếu state = error/rejected) */
  code?: number;
  /** Mô tả thêm (nếu có) */
  message?: string;
}
