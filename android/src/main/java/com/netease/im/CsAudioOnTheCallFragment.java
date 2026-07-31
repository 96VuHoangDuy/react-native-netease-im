package com.netease.im;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.databinding.FragmentP2pAudioOnTheCallBinding;
import com.netease.yunxin.nertc.ui.p2p.P2PUIConfig;
import com.netease.yunxin.nertc.ui.p2p.fragment.FragmentActionBridge;
import com.netease.yunxin.nertc.ui.p2p.fragment.onthecall.AudioOnTheCallFragment;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Màn IN-CALL (audio) tuỳ biến cho CSKH — TÁCH background khỏi avatar.
 *
 * SDK call-ui mặc định load avatar vào CẢ ô avatar nhỏ (ivUserInnerAvatar) LẪN background
 * phóng to full màn (ivBg), rồi vẽ tên người gọi (tvUserName) màu TRẮNG đè lên. Hệ quả với
 * avatar là logo CSKH:
 *  - logo nền trong suốt (asset gốc) trên nền đen của clRoot -> "đen-xanh" loang lổ;
 *  - logo nền trắng -> chữ tên trắng bị chìm.
 *
 * Fix: nền = logo blur + scrim tối (đồng bộ iOS) thay cho avatar phóng to sắc nét; avatar ô nhỏ
 * nằm trực tiếp trên nền đó nên không cần khung trắng. Không đổi asset gốc.
 */
public class CsAudioOnTheCallFragment extends AudioOnTheCallFragment {

    // Cùng kích thước với nút của màn caller/callee (fragment_p2p_audio_caller.xml: 75dp icon,
    // label 14sp cách icon 8dp) để 3 màn call nhìn như một bộ.
    private static final int BUTTON_SIZE_DP = 75;
    private static final int BUTTON_GAP_DP = 30;
    private static final int LABEL_MARGIN_TOP_DP = 8;
    private static final float LABEL_TEXT_SIZE_SP = 14f;
    private static final int LABEL_COLOR = 0xFFFFFFFF;

    // Re-parent chỉ được làm 1 lần cho mỗi lần dựng view; toRenderView/renderUserInfo chạy lại nhiều lần.
    private boolean bigOperationBarBuilt;

    @Override
    protected void toRenderView(CallParam callParam, P2PUIConfig config) {
        super.toRenderView(callParam, config);
        hideVideoSwitch();
        applyTopSafeArea();
        applyBigOperationBar();
    }

    /** SDK render lại cụm nút mỗi lần đổi state (mute/loa/chuyển loại) → phải sync lại icon to. */
    @Override
    public void toUpdateUIState(int state) {
        super.toUpdateUIState(state);
        syncOperationIcons();
    }

    @Override
    public void onDestroyView() {
        bigOperationBarBuilt = false;
        super.onDestroyView();
    }

    @Override
    protected void renderUserInfo(String accId, P2PUIConfig config) {
        super.renderUserInfo(accId, config);
        hideVideoSwitch();
        applyTopSafeArea();
        applyBigOperationBar();
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

    /**
     * Đổi thanh control in-call từ pill nhỏ (icon 24dp, không label) sang 3 nút tròn to có label —
     * đồng bộ với màn caller/callee. Khách hàng phản hồi cụm nút cũ quá nhỏ, khó bấm.
     *
     * <p>Không override layout XML của SDK: {@code FragmentP2pAudioOnTheCallBinding} được generate
     * lúc compile AAR, thiếu bất kỳ id nào là NPE lúc inflate. Thay vào đó tái dùng đúng các
     * ImageView của SDK (click listener + logic mute/loa/cúp máy giữ nguyên 100%), chỉ re-parent và
     * phóng to.
     */
    private void applyBigOperationBar() {
        FragmentP2pAudioOnTheCallBinding b = getBinding();
        if (b == null) {
            return;
        }
        if (!bigOperationBarBuilt) {
            buildBigOperationBar(b);
            bigOperationBarBuilt = true;
            // SDK set lại icon NHỎ ở cuối mỗi lần click (voice_on/voice_off, speaker_on/speaker_off)
            // → afterClick của ta chạy sau đó, ghi đè bằng icon TO tương ứng.
            Function1<View, Unit> resync = v -> {
                syncOperationIcons();
                return Unit.INSTANCE;
            };
            bindAfterClick(getViewKeyMuteImageAudio(), resync);
            bindAfterClick(getViewKeyImageSpeaker(), resync);
        }
        syncOperationIcons();
    }

    private void buildBigOperationBar(FragmentP2pAudioOnTheCallBinding b) {
        LinearLayout bar = b.llOnTheCallOperation;
        Context context = bar.getContext();
        // Bỏ nền pill + padding của SDK; cụm nút giờ đứng trần trên nền màn hình như màn caller.
        bar.setBackground(null);
        bar.setPadding(0, 0, 0, 0);
        bar.setGravity(Gravity.CENTER);
        bar.removeAllViews();
        // ivCallChannelTypeChange đã GONE (app chỉ có voice call) → không add lại vào cụm nút.
        bar.addView(buildButtonColumn(context, b.ivMuteAudio,
                com.netease.yunxin.nertc.ui.R.string.ui_microphone, BUTTON_GAP_DP));
        bar.addView(buildButtonColumn(context, b.ivHangUp,
                com.netease.yunxin.nertc.ui.R.string.ui_cancel, BUTTON_GAP_DP));
        bar.addView(buildButtonColumn(context, b.ivMuteSpeaker,
                com.netease.yunxin.nertc.ui.R.string.ui_speaker, 0));
    }

    /** Một cột: icon (75dp) + label bên dưới. {@code icon} là View của SDK, chỉ đổi cha và kích thước. */
    private static View buildButtonColumn(Context context, ImageView icon, int labelRes, int marginEndDp) {
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        columnParams.setMarginEnd(dp(context, marginEndDp));
        column.setLayoutParams(columnParams);

        ViewGroup oldParent = (ViewGroup) icon.getParent();
        if (oldParent != null) {
            oldParent.removeView(icon);
        }
        int size = dp(context, BUTTON_SIZE_DP);
        column.addView(icon, new LinearLayout.LayoutParams(size, size));

        TextView label = new TextView(context);
        label.setText(labelRes);
        label.setTextColor(LABEL_COLOR);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, LABEL_TEXT_SIZE_SP);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = dp(context, LABEL_MARGIN_TOP_DP);
        column.addView(label, labelParams);
        return column;
    }

    /**
     * Đổi icon nhỏ của SDK sang bộ icon tròn to mà màn caller đang dùng. Nguồn trạng thái là
     * {@code FragmentActionBridge} — đúng nguồn mà SDK dùng để chọn icon nhỏ, nên không lệch state
     * kể cả khi user đã bật/tắt mic/loa từ màn caller trước lúc kết nối.
     */
    private void syncOperationIcons() {
        FragmentP2pAudioOnTheCallBinding b = getBinding();
        if (b == null || !bigOperationBarBuilt) {
            return;
        }
        FragmentActionBridge bridge = getBridge();
        boolean micOn = bridge == null || !bridge.isLocalMuteAudio();
        boolean speakerOn = bridge != null && bridge.isSpeakerOn();
        b.ivMuteAudio.setImageResource(micOn
                ? com.netease.yunxin.nertc.ui.R.drawable.icon_call_audio_on
                : com.netease.yunxin.nertc.ui.R.drawable.icon_call_audio_off);
        b.ivMuteSpeaker.setImageResource(speakerOn
                ? com.netease.yunxin.nertc.ui.R.drawable.icon_call_audio_speaker_on
                : com.netease.yunxin.nertc.ui.R.drawable.icon_call_audio_speaker_off);
        b.ivHangUp.setImageResource(com.netease.yunxin.nertc.ui.R.drawable.call_reject);
    }

    private static int dp(Context context, int value) {
        return Math.round(context.getResources().getDisplayMetrics().density * value);
    }

    private void applyCsStyle() {
        FragmentP2pAudioOnTheCallBinding b = getBinding();
        if (b == null) {
            return;
        }
        // getRootView() trả android.view.View — không dùng b.clRoot (ConstraintLayout) vì module
        // lib không có dependency androidx.constraintlayout.
        CsCallUiUtils.applyBrandBlurBackground(getRootView(), b.ivBg);
    }
}
