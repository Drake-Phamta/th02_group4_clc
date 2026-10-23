package com.group4.fruitshop.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.group4.fruitshop.R;
import com.group4.fruitshop.util.DateUtils;
import com.group4.fruitshop.util.OrderStatus;

/**
 * Colored badges so every screen shows expiry and order status the same way.
 * Use them on a TextView with style="@style/Widget.FruitShop.Badge".
 */
public final class Badges {

    private Badges() {
    }

    /** "Expired" (red), "Expires today" / "Expires in N days" (orange) or "Fresh" (green). */
    public static void bindExpiry(TextView badge, String expiryDate) {
        Context context = badge.getContext();
        switch (DateUtils.getExpiryStatus(expiryDate)) {
            case EXPIRED:
                badge.setText(R.string.expiry_expired);
                tint(badge, R.color.status_expired);
                break;
            case EXPIRING_SOON:
                int days = (int) DateUtils.daysUntil(expiryDate);
                badge.setText(days == 0
                        ? context.getString(R.string.expiry_today)
                        : context.getResources().getQuantityString(R.plurals.expiry_in_days, days, days));
                tint(badge, R.color.status_expiring_soon);
                break;
            default:
                badge.setText(R.string.expiry_fresh);
                tint(badge, R.color.status_fresh);
                break;
        }
    }

    /** Pending (amber), Confirmed (blue), Completed (green). */
    public static void bindOrderStatus(TextView badge, String status) {
        badge.setText(status);
        if (OrderStatus.PENDING.equals(status)) {
            tint(badge, R.color.status_pending);
        } else if (OrderStatus.CONFIRMED.equals(status)) {
            tint(badge, R.color.status_confirmed);
        } else {
            tint(badge, R.color.status_completed);
        }
    }

    private static void tint(TextView badge, @ColorRes int color) {
        ViewCompat.setBackgroundTintList(badge,
                ColorStateList.valueOf(ContextCompat.getColor(badge.getContext(), color)));
    }
}
