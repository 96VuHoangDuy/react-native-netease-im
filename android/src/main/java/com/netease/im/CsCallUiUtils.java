package com.netease.im;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.View;
import android.widget.ImageView;

/**
 * Tiện ích UI dùng chung cho các màn call tuỳ biến (CSKH + 1-1).
 * Layout call của SDK không xử lý window insets → view ở đỉnh đè status bar/notch.
 */
final class CsCallUiUtils {

    // Đen 45% — cùng alpha với scrim màn call iOS, cho 2 platform ra cùng tông xám (~140).
    private static final int SCRIM_COLOR = 0x73000000;

    private CsCallUiUtils() {
    }

    /**
     * Làm tối nền màn call CSKH. Nền do SDK dựng từ avatar (blur + center-crop), mà avatar CSKH là
     * logo trên nền TRẮNG ĐẶC nên nền ra trắng tinh — chữ tên trắng của SDK chìm hẳn.
     *
     * <p>Đặt color filter lên chính {@code ivBg} chứ không thay drawable: SDK load avatar qua Glide
     * BẤT ĐỒNG BỘ nên mọi bitmap set thẳng vào ivBg đều bị request về sau ghi đè, còn color filter
     * là thuộc tính của View nên áp cho cả ảnh về sau. Idempotent khi renderUserInfo chạy lại.
     */
    static void dimBrandBackground(ImageView ivBg) {
        if (ivBg == null) {
            return;
        }
        ivBg.setColorFilter(new PorterDuffColorFilter(SCRIM_COLOR, PorterDuff.Mode.SRC_ATOP));
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
