package com.netease.im;

import android.view.View;

import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.databinding.FragmentP2pAudioCalleeBinding;
import com.netease.yunxin.nertc.ui.p2p.P2PUIConfig;
import com.netease.yunxin.nertc.ui.p2p.fragment.callee.AudioCalleeFragment;

/**
 * Màn ĐỔ CHUÔNG (audio callee) tuỳ biến: chỉ ẩn nút chuyển audio→video.
 *
 * Nền để SDK tự lo (load avatar vào ivBg qua Glide kèm transform BlurCenterCorp) — giống hệt call
 * 1-1. Với CSKH, avatar là cs_call_avatar (logo xanh nền trắng) nên nền ra đúng tông thương hiệu,
 * không cần tự vẽ nền nữa.
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
            CsCallUiUtils.dimBrandBackground(getBinding() != null ? getBinding().ivBg : null);
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
}
