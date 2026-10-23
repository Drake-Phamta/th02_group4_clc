package com.group4.fruitshop.util;

/**
 * App-wide constants shared by every module. Do not hard-code these values elsewhere.
 */
public final class Constants {

    // Values of Account.role
    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_CUSTOMER = "Customer";

    // Intent extras
    /** int: id of the fruit to edit (FruitFormActivity) or to show (FruitDetailActivity). */
    public static final String EXTRA_FRUIT_ID = "extra_fruit_id";

    /** A fruit whose expiry date is within this many days (and not past) is "expiring soon". */
    public static final int EXPIRING_SOON_DAYS = 3;

    /** Value returned by DAOs/session when there is no valid id. */
    public static final int NO_ID = -1;

    private Constants() {
    }
}
