package com.netease.im;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

/**
 * Tiện ích UI dùng chung cho các màn call tuỳ biến (CSKH + 1-1).
 * Layout call của SDK không xử lý window insets → view ở đỉnh đè status bar/notch.
 */
final class CsCallUiUtils {

    // Kích thước bitmap nền trước khi blur. Nhỏ để blur = downscale/upscale gần như miễn phí;
    // BitmapDrawable kéo giãn lên full màn (tỉ lệ ~1:2 khớp màn dọc, lệch không thấy vì đã blur).
    private static final int BG_W = 64;
    private static final int BG_H = 128;
    // Logo chiếm ~60% chiều rộng nền, canh giữa — sau blur cho vệt xanh mờ ở giữa như iOS.
    private static final float LOGO_RATIO = 0.6f;
    // Scrim tối 50% phủ lên nền trắng → xám trung: chữ tên trắng của SDK đọc rõ, logo xanh vẫn nổi.
    private static final int SCRIM_COLOR = 0x80000000;

    // Nền dựng 1 lần cho cả phiên — renderUserInfo chạy lại nhiều lần không cấp phát lại.
    private static Bitmap sBrandBlurBg = null;

    private CsCallUiUtils() {
    }

    /**
     * Đặt nền màn call CSKH = logo blur + scrim tối, thay cho avatar phóng to sắc nét của SDK
     * (logo bị vỡ nét, chữ trong logo tràn màn hình). Đồng bộ với iOS.
     *
     * <p>Nền đặt lên {@code root}, KHÔNG lên {@code ivBg}: SDK load avatar vào ivBg qua Glide
     * BẤT ĐỒNG BỘ, nên mọi bitmap set thẳng vào ivBg đều bị request Glide về sau ghi đè. Ẩn ivBg
     * là cách duy nhất chắc chắn mà không cần đụng tới Glide (lib này không có Glide trên
     * compile classpath — call-ui khai `implementation`).
     *
     * <p>Logo vẽ trên nền TRẮNG trước khi blur — blur logo nền trong suốt trên nền đen của
     * clRoot chính là bug "đen-xanh" đã gặp trước đây.
     */
    static void applyBrandBlurBackground(View root, ImageView ivBg) {
        if (ivBg != null) {
            ivBg.setImageDrawable(null);
            ivBg.setVisibility(View.GONE);
        }
        if (root == null) {
            return;
        }
        Bitmap bg = brandBlurBackground(root.getContext());
        if (bg == null) {
            return;
        }
        root.setBackground(new BitmapDrawable(root.getResources(), bg));
    }

    private static Bitmap brandBlurBackground(Context context) {
        if (sBrandBlurBg != null && !sBrandBlurBg.isRecycled()) {
            return sBrandBlurBg;
        }
        if (context == null) {
            return null;
        }
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.cs_call_logo);
        if (logo == null) {
            return null;
        }
        Bitmap base = Bitmap.createBitmap(BG_W, BG_H, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(base);
        canvas.drawColor(0xFFFFFFFF);
        int logoW = Math.round(BG_W * LOGO_RATIO);
        int logoH = Math.round(logoW * (float) logo.getHeight() / logo.getWidth());
        int left = (BG_W - logoW) / 2;
        int top = (BG_H - logoH) / 2;
        canvas.drawBitmap(logo, null, new Rect(left, top, left + logoW, top + logoH), null);

        Bitmap blurred = blur(blur(base));
        new Canvas(blurred).drawColor(SCRIM_COLOR);
        sBrandBlurBg = blurred;
        return sBrandBlurBg;
    }

    /**
     * Blur "nghèo": thu nhỏ 4 lần rồi phóng lại bằng nội suy bilinear. Nguồn là logo phẳng ít chi
     * tiết nên kết quả không khác gaussian đáng kể, và không cần RenderScript (deprecated).
     */
    private static Bitmap blur(Bitmap src) {
        int w = Math.max(1, src.getWidth() / 4);
        int h = Math.max(1, src.getHeight() / 4);
        Bitmap small = Bitmap.createScaledBitmap(src, w, h, true);
        return Bitmap.createScaledBitmap(small, src.getWidth(), src.getHeight(), true);
    }

    /**
     * Đẩy {@code view} xuống dưới status bar bằng translationY (không setPadding để không bóp méo
     * icon). Set tuyệt đối nên idempotent khi render lại nhiều lần.
     */
    static void pushBelowStatusBar(View view) {
        if (view == null) {
            return;
        }
        int h = statusBarHeight(view.getResources());
        if (h > 0) {
            view.setTranslationY(h);
        }
    }

    private static int statusBarHeight(Resources res) {
        if (res == null) {
            return 0;
        }
        int resId = res.getIdentifier("status_bar_height", "dimen", "android");
        return resId > 0 ? res.getDimensionPixelSize(resId) : 0;
    }
}
