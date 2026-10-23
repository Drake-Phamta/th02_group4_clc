package com.group4.fruitshop.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Date helpers and the single source of truth for expiry rules.
 *
 * <p>Storage formats (always use these when reading/writing the database):
 * <ul>
 *     <li>Fruit.expiryDate: {@value #DB_DATE} (e.g. 2026-10-23)</li>
 *     <li>Order.orderDate: {@value #DB_DATE_TIME} (e.g. 2026-10-23 14:05:00)</li>
 * </ul>
 *
 * <p>Expiry rule: a fruit is EXPIRED when its expiry date is before today.
 * On the expiry date itself the fruit can still be sold.
 */
public final class DateUtils {

    public static final String DB_DATE = "yyyy-MM-dd";
    public static final String DB_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DISPLAY_DATE = "dd/MM/yyyy";
    public static final String DISPLAY_DATE_TIME = "dd/MM/yyyy HH:mm";

    private static final long DAY_MILLIS = 24L * 60 * 60 * 1000;

    public enum ExpiryStatus {EXPIRED, EXPIRING_SOON, FRESH}

    private DateUtils() {
    }

    /** Today in {@value #DB_DATE}. */
    public static String today() {
        return format(new Date(), DB_DATE);
    }

    /** Current date-time in {@value #DB_DATE_TIME}, used for Order.orderDate. */
    public static String now() {
        return format(new Date(), DB_DATE_TIME);
    }

    /** Today plus {@code days} (can be negative) in {@value #DB_DATE}. */
    public static String daysFromToday(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return format(calendar.getTime(), DB_DATE);
    }

    /** Builds a {@value #DB_DATE} string from DatePickerDialog values (month is 0-based). */
    public static String fromPicker(int year, int month, int dayOfMonth) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
    }

    /**
     * Splits a {@value #DB_DATE} string into {year, month (0-based), day} for DatePickerDialog.
     * Falls back to today when the value is invalid.
     */
    public static int[] toPicker(String dbDate) {
        Calendar calendar = Calendar.getInstance();
        Date date = parseUtc(dbDate);
        if (date != null) {
            calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(date);
        }
        return new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)};
    }

    /** True if {@code value} is a real calendar date in {@value #DB_DATE}. */
    public static boolean isValidDate(String value) {
        return parseUtc(value) != null;
    }

    /** "2026-10-23" -> "23/10/2026". Returns the input unchanged when it cannot be parsed. */
    public static String toDisplayDate(String dbDate) {
        Date date = parseUtc(dbDate);
        if (date == null) {
            return dbDate;
        }
        SimpleDateFormat out = new SimpleDateFormat(DISPLAY_DATE, Locale.US);
        out.setTimeZone(TimeZone.getTimeZone("UTC"));
        return out.format(date);
    }

    /** "2026-10-23 14:05:00" -> "23/10/2026 14:05". Returns the input unchanged when it cannot be parsed. */
    public static String toDisplayDateTime(String dbDateTime) {
        if (dbDateTime == null) {
            return "";
        }
        try {
            SimpleDateFormat in = new SimpleDateFormat(DB_DATE_TIME, Locale.US);
            in.setLenient(false);
            return format(in.parse(dbDateTime), DISPLAY_DATE_TIME);
        } catch (ParseException e) {
            return dbDateTime;
        }
    }

    /** Days from today until {@code expiryDate}: 0 = today, negative = already expired. */
    public static long daysUntil(String expiryDate) {
        return daysBetween(today(), expiryDate);
    }

    /** Same as {@link #daysUntil(String)} but with an explicit "today" (used by unit tests). */
    public static long daysBetween(String fromDbDate, String toDbDate) {
        Date from = parseUtc(fromDbDate);
        Date to = parseUtc(toDbDate);
        if (from == null || to == null) {
            return Long.MIN_VALUE;
        }
        return (to.getTime() - from.getTime()) / DAY_MILLIS;
    }

    /** True when the expiry date is before today (or invalid). */
    public static boolean isExpired(String expiryDate) {
        return getExpiryStatus(expiryDate) == ExpiryStatus.EXPIRED;
    }

    public static ExpiryStatus getExpiryStatus(String expiryDate) {
        return getExpiryStatus(expiryDate, today());
    }

    /** Same as {@link #getExpiryStatus(String)} but with an explicit "today" (used by unit tests). */
    public static ExpiryStatus getExpiryStatus(String expiryDate, String today) {
        long days = daysBetween(today, expiryDate);
        if (days < 0) {
            return ExpiryStatus.EXPIRED;
        }
        if (days <= Constants.EXPIRING_SOON_DAYS) {
            return ExpiryStatus.EXPIRING_SOON;
        }
        return ExpiryStatus.FRESH;
    }

    private static String format(Date date, String pattern) {
        return new SimpleDateFormat(pattern, Locale.US).format(date);
    }

    /** Parses a {@value #DB_DATE} string at UTC midnight so day arithmetic is not affected by DST. */
    private static Date parseUtc(String dbDate) {
        if (dbDate == null || !dbDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return null;
        }
        SimpleDateFormat in = new SimpleDateFormat(DB_DATE, Locale.US);
        in.setTimeZone(TimeZone.getTimeZone("UTC"));
        in.setLenient(false);
        try {
            return in.parse(dbDate);
        } catch (ParseException e) {
            return null;
        }
    }
}
