package com.group4.fruitshop.util;

/**
 * Values of Order.status and the allowed workflow: Pending -> Confirmed -> Completed.
 */
public final class OrderStatus {

    public static final String PENDING = "Pending";
    public static final String CONFIRMED = "Confirmed";
    public static final String COMPLETED = "Completed";

    /** All statuses in workflow order. */
    public static final String[] ALL = {PENDING, CONFIRMED, COMPLETED};

    private OrderStatus() {
    }

    public static boolean isValid(String status) {
        for (String s : ALL) {
            if (s.equals(status)) {
                return true;
            }
        }
        return false;
    }

    /** Returns the status that follows {@code status}, or null when it is already the last one. */
    public static String next(String status) {
        if (PENDING.equals(status)) {
            return CONFIRMED;
        }
        if (CONFIRMED.equals(status)) {
            return COMPLETED;
        }
        return null;
    }
}
