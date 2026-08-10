package com.netease.im;

import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.p2p.P2PCallFragmentActivity;
import com.netease.yunxin.nertc.ui.p2p.P2PUIConfig;
import com.netease.yunxin.nertc.ui.p2p.fragment.P2PCallFragmentType;

/**
 * Activity call p2p tuỳ biến — inject {@link CsAudioOnTheCallFragment} cho màn IN-CALL
 * (AUDIO_ON_THE_CALL) để tách background khỏi avatar. Đăng ký qua
 * {@code CallKitUIOptions.Builder().p2pAudioActivity(CsP2PCallFragmentActivity.class)}.
 *
 * provideUIConfig mặc định của SDK trả P2PUIConfig rỗng (all default), nên build mới chỉ với
 * custom fragment là an toàn — không mất cấu hình nào.
 */
public class CsP2PCallFragmentActivity extends P2PCallFragmentActivity {

    @Override
    protected P2PUIConfig provideUIConfig(CallParam callParam) {
        return new P2PUIConfig.Builder()
                .customCallFragmentByKey(
                        P2PCallFragmentType.AUDIO_CALLER,
                        new CsAudioCallerFragment())
                .customCallFragmentByKey(
                        P2PCallFragmentType.AUDIO_CALLEE,
                        new CsAudioCalleeFragment())
                .customCallFragmentByKey(
                        P2PCallFragmentType.AUDIO_ON_THE_CALL,
                        new CsAudioOnTheCallFragment())
                .build();
    }
}
