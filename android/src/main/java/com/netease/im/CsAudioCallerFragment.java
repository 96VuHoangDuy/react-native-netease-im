package com.netease.im;

import android.view.View;

import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.databinding.FragmentP2pAudioCallerBinding;
import com.netease.yunxin.nertc.ui.p2p.P2PUIConfig;
import com.netease.yunxin.nertc.ui.p2p.fragment.caller.AudioCallerFragment;

/**
 * Màn CALLER (audio, "đang gọi đi") tuỳ biến:
 *  - Safe-area top: đẩy nút thu nhỏ ({@code ivFloatingWindow}) xuống dưới status bar (layout SDK
 *    không xử lý insets → đè notch/đồng hồ).
 *  - Ẩn nút chuyển audio→video ({@code ivCallSwitchType} + label) vì app chỉ hỗ trợ voice call.
 *    Hai việc trên áp cho MỌI cuộc gọi.
 *  - Riêng cuộc gọi CSKH: nền logo blur + scrim thay avatar phóng to sắc nét (giống màn callee /
 *    in-call, xem {@link CsCallUiUtils#applyBrandBlurBackground}).
 */
public class CsAudioCallerFragment extends AudioCallerFragment {

    @Override
    protected void toRenderView(CallParam callParam, P2PUIConfig config) {
        super.toRenderView(callParam, config);
        applyUi();
    }

    @Override
    protected void renderUserInfo(String accId, P2PUIConfig config) {
        super.renderUserInfo(accId, config);
        applyUi();
        if (CallService.isCsrAccid(accId)) {
            applyCsStyle();
        }
    }

    private void applyCsStyle() {
        FragmentP2pAudioCallerBinding b = getBinding();
        if (b == null) {
            return;
        }
        // getRootView() trả android.view.View — không dùng b.clRoot (ConstraintLayout) vì module
        // lib không có dependency androidx.constraintlayout.
        CsCallUiUtils.applyBrandBlurBackground(getRootView(), b.ivBg);
    }

    private void applyUi() {
        FragmentP2pAudioCallerBinding b = getBinding();
        if (b == null) {
            return;
        }
        CsCallUiUtils.pushBelowStatusBar(b.ivFloatingWindow);
        b.ivCallSwitchType.setVisibility(View.GONE);
        b.tvCallSwitchTypeDesc.setVisibility(View.GONE);
    }
}
