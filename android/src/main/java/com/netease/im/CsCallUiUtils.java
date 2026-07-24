package com.netease.im;

import android.content.res.Resources;
import android.view.View;

/**
 * Tiện ích UI dùng chung cho các màn call tuỳ biến (CSKH + 1-1).
 * Layout call của SDK không xử lý window insets → view ở đỉnh đè status bar/notch.
 */
final class CsCallUiUtils {

    private CsCallUiUtils() {
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
