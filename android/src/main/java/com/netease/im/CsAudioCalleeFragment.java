package com.netease.im;

import android.view.View;

import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.databinding.FragmentP2pAudioCalleeBinding;
import com.netease.yunxin.nertc.ui.p2p.P2PUIConfig;
import com.netease.yunxin.nertc.ui.p2p.fragment.callee.AudioCalleeFragment;

/**
 * Màn ĐỔ CHUÔNG (audio callee) tuỳ biến.
 *
 * SDK load avatar vào ivBg (match_parent) làm nền full màn KHÔNG blur. Với avatar là logo CSKH
 * thì logo bị phóng to vỡ nét, chữ trong logo tràn màn hình. Fix: nền = logo blur + scrim tối
 * (đồng bộ iOS), avatar ô nhỏ nằm trực tiếp trên nền đó, không khung trắng.
 *
 * Call user thường có avatar ảnh thật → giữ nguyên nền mặc định của SDK.
 */
public class CsAudioCalleeFragment extends AudioCalleeFragment {

    @Override
    protected void toRenderView(CallParam callParam, P2PUIConfig config) {
        super.toRenderView(callParam, config);
        hideVideoSwitch();
    }

    @Override
    protected void renderUserInfo(String accId, P2PUIConfig config) {
        super.renderUserInfo(accId, config);
        hideVideoSwitch();
        if (CallService.isCsrAccid(accId)) {
            applyCsStyle();
        }
    }

    /** App chỉ hỗ trợ voice call → ẩn nút chuyển audio→video (mọi cuộc gọi). */
    private void hideVideoSwitch() {
        FragmentP2pAudioCalleeBinding b = getBinding();
        if (b == null) {
            return;
        }
        b.ivSwitchType.setVisibility(View.GONE);
        b.tvSwitchTypeDesc.setVisibility(View.GONE);
    }

    private void applyCsStyle() {
        FragmentP2pAudioCalleeBinding b = getBinding();
        if (b == null) {
            return;
        }
        // getRootView() trả android.view.View — không dùng b.clRoot (ConstraintLayout) vì module
        // lib không có dependency androidx.constraintlayout.
        CsCallUiUtils.applyBrandBlurBackground(getRootView(), b.ivBg);
    }
}
