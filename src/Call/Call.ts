import { NativeModules, Platform } from 'react-native';
const { RNNeteaseIm } = NativeModules;

/**
 * Cuộc gọi thoại 1-1 (audio-only) qua NERTC Call Kit (call-ui 4.3.0).
 * Dùng prebuilt Call Kit UI — UI gọi/nhận do native lo; JS chỉ trigger + observe state.
 * Trạng thái vòng đời phát qua event `observeCallState` (@see NIMCallStateEnum).
 */
class NimCall {
  /**
   * 发起语音通话
   * Phát cuộc gọi thoại tới accid đích (CSR đã bound, hoặc peer để test).
   * @param accid accid người nhận
   * @param pushOptions text offline push để máy callee bị KILL nhận được notification
   *   (bên gọi phải set, nếu không server không gửi push). Chỉ text — không có avatar.
   * @returns {*} @see observeCallState
   */
  startVoiceCall(
    accid: string,
    pushOptions?: { pushTitle?: string; pushContent?: string },
  ) {
    return RNNeteaseIm.startVoiceCall(
      accid,
      pushOptions?.pushTitle ?? '',
      pushOptions?.pushContent ?? '',
    );
  }

  /**
   * 挂断
   * Cúp cuộc gọi hiện tại.
   */
  hangupCall() {
    return RNNeteaseIm.hangupCall();
  }

  /**
   * Đặt tên hiển thị cố định cho CSKH trên call UI/thông báo cuộc gọi.
   * Native dùng tên này khi accid người gọi/nhận có prefix "csr" (fix cứng CSKH).
   * Gọi 1 lần lúc init IM và khi đổi ngôn ngữ (truyền chuỗi đã localize).
   */
  setCustomerServiceCallName(name: string) {
    if (!RNNeteaseIm?.setCustomerServiceCallName) return;
    return RNNeteaseIm.setCustomerServiceCallName(name ?? '');
  }

  /**
   * Đã có quyền vẽ đè (SYSTEM_ALERT_WINDOW) chưa — cần để hiện 来电横幅 khi có cuộc gọi đến.
   * iOS không cần quyền này (banner dựng trên UIWindow riêng) → luôn true.
   */
  hasOverlayPermission(): Promise<boolean> {
    if (Platform.OS !== 'android') return Promise.resolve(true);
    return RNNeteaseIm.hasOverlayPermission();
  }

  /**
   * Mở Settings xin quyền vẽ đè. Android không báo kết quả về → gọi lại
   * hasOverlayPermission() khi app quay lại foreground nếu cần biết. No-op trên iOS.
   */
  requestOverlayPermission(): Promise<boolean> {
    if (Platform.OS !== 'android') return Promise.resolve(true);
    return RNNeteaseIm.requestOverlayPermission();
  }
}

export default new NimCall();
