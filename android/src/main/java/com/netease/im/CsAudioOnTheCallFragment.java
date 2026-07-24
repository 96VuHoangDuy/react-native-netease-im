package com.netease.im;

import android.view.View;

import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.databinding.FragmentP2pAudioOnTheCallBinding;
import com.netease.yunxin.nertc.ui.p2p.P2PUIConfig;
import com.netease.yunxin.nertc.ui.p2p.fragment.onthecall.AudioOnTheCallFragment;

/**
 * Màn IN-CALL (audio) tuỳ biến cho CSKH — TÁCH background khỏi avatar.
 *
 * SDK call-ui mặc định load avatar vào CẢ ô avatar nhỏ (ivUserInnerAvatar) LẪN background
 * phóng to full màn (ivBg), rồi vẽ tên người gọi (tvUserName) màu TRẮNG đè lên. Hệ quả với
 * avatar là logo CSKH:
 *  - logo nền trong suốt (asset gốc) trên nền đen của clRoot -> "đen-xanh" loang lổ;
 *  - logo nền trắng -> chữ tên trắng bị chìm.
 *
 * Fix: ẩn hẳn ivBg (không dùng avatar làm nền), đặt nền tối riêng cho clRoot -> chữ tên trắng
 * đọc rõ; giữ avatar ô nhỏ trên nền TRẮNG để logo hiển thị "xanh nền trắng" như trong app.
 * Không đổi asset gốc.
 */
public class CsAudioOnTheCallFragment extends AudioOnTheCallFragment {

    // Nền màn call: xanh lá đậm brand (chữ tên trắng của SDK đọc rõ).
    private static final int BG_DARK = 0xFF123D22;
    // Nền ô avatar: trắng -> logo xanh hiển thị "xanh nền trắng".
    private static final int AVATAR_BG_WHITE = 0xFFFFFFFF;

    @Override
    protected void toRenderView(CallParam callParam, P2PUIConfig config) {
        super.toRenderView(callParam, config);
        hideVideoSwitch();
        applyTopSafeArea();
    }

    @Override
    protected void renderUserInfo(String accId, P2PUIConfig config) {
        super.renderUserInfo(accId, config);
        hideVideoSwitch();
        applyTopSafeArea();
        // CHỈ áp UI tuỳ biến cho cuộc gọi CSKH. Call user thường có avatar ảnh thật —
        // giữ nguyên hành vi mặc định (avatar phóng to làm nền) vì trông đúng/đẹp hơn.
        if (CallService.isCsrAccid(accId)) {
            applyCsStyle();
        }
    }

    /** App chỉ hỗ trợ voice call → ẩn nút chuyển audio→video ở thanh control (mọi cuộc gọi). */
    private void hideVideoSwitch() {
        FragmentP2pAudioOnTheCallBinding b = getBinding();
        if (b == null) {
            return;
        }
        b.ivCallChannelTypeChange.setVisibility(View.GONE);
    }

    /**
     * Nút thu nhỏ (góc trên trái) + timer (giữa trên) của SDK đè status bar/notch → đẩy xuống dưới
     * status bar. Áp cho MỌI cuộc gọi (CSKH + 1-1).
     */
    private void applyTopSafeArea() {
        FragmentP2pAudioOnTheCallBinding b = getBinding();
        if (b == null) {
            return;
        }
        CsCallUiUtils.pushBelowStatusBar(b.ivFloatingWindow);
        CsCallUiUtils.pushBelowStatusBar(b.tvCountdown);
    }

    private void applyCsStyle() {
        FragmentP2pAudioOnTheCallBinding b = getBinding();
        if (b == null) {
            return;
        }
        // Không dùng avatar phóng to làm nền (tránh nuốt chữ / loang lổ).
        b.ivBg.setImageDrawable(null);
        b.ivBg.setVisibility(View.GONE);
        // Nền tối cho root view. Dùng getRootView() (trả android.view.View) thay vì b.clRoot
        // (kiểu ConstraintLayout) để không cần androidx.constraintlayout trong classpath của lib.
        View root = getRootView();
        if (root != null) {
            root.setBackgroundColor(BG_DARK);
        }
        // Avatar ô nhỏ: nền trắng để logo hiển thị đúng "xanh nền trắng".
        b.flUserAvatar.setBackgroundColor(AVATAR_BG_WHITE);
        b.ivUserInnerAvatar.setBackgroundColor(AVATAR_BG_WHITE);
    }
}
