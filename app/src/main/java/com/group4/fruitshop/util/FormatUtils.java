package com.group4.fruitshop.util;

import java.util.Locale;

/**
 * Display formatting shared by all screens.
 */
public final class FormatUtils {

    private FormatUtils() {
    }

    /** 45000 -> "45,000 VND". */
    public static String formatPrice(double amount) {
        return String.format(Locale.US, "%,.0f VND", amount);
    }
}
