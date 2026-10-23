package com.group4.fruitshop.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.group4.fruitshop.util.DateUtils.ExpiryStatus;

import org.junit.Test;

/**
 * Plain JVM tests for the shared utilities (no Android needed).
 */
public class CoreUtilsTest {

    private static final String TODAY = "2026-10-23";

    @Test
    public void daysBetween_countsCalendarDays() {
        assertEquals(0, DateUtils.daysBetween(TODAY, "2026-10-23"));
        assertEquals(1, DateUtils.daysBetween(TODAY, "2026-10-24"));
        assertEquals(-1, DateUtils.daysBetween(TODAY, "2026-10-22"));
        assertEquals(366, DateUtils.daysBetween("2024-01-01", "2025-01-01"));
        assertEquals(1, DateUtils.daysBetween("2026-03-28", "2026-03-29"));
    }

    @Test
    public void expiryStatus_followsTheAgreedRule() {
        assertEquals(ExpiryStatus.EXPIRED, DateUtils.getExpiryStatus("2026-10-22", TODAY));
        // the expiry day itself is still sellable
        assertEquals(ExpiryStatus.EXPIRING_SOON, DateUtils.getExpiryStatus("2026-10-23", TODAY));
        assertEquals(ExpiryStatus.EXPIRING_SOON, DateUtils.getExpiryStatus("2026-10-26", TODAY));
        assertEquals(ExpiryStatus.FRESH, DateUtils.getExpiryStatus("2026-10-27", TODAY));
        assertEquals(ExpiryStatus.EXPIRED, DateUtils.getExpiryStatus("not-a-date", TODAY));
    }

    @Test
    public void relativeDates_agreeWithToday() {
        assertEquals(0, DateUtils.daysUntil(DateUtils.today()));
        assertEquals(5, DateUtils.daysUntil(DateUtils.daysFromToday(5)));
        assertEquals(-2, DateUtils.daysUntil(DateUtils.daysFromToday(-2)));
        assertTrue(DateUtils.isExpired(DateUtils.daysFromToday(-1)));
        assertFalse(DateUtils.isExpired(DateUtils.today()));
    }

    @Test
    public void isValidDate_rejectsImpossibleOrBadlyFormattedDates() {
        assertTrue(DateUtils.isValidDate("2024-02-29"));
        assertFalse(DateUtils.isValidDate("2026-02-29"));
        assertFalse(DateUtils.isValidDate("2026-13-01"));
        assertFalse(DateUtils.isValidDate("23/10/2026"));
        assertFalse(DateUtils.isValidDate(""));
        assertFalse(DateUtils.isValidDate(null));
    }

    @Test
    public void pickerAndDisplayConversions() {
        assertEquals("2026-01-05", DateUtils.fromPicker(2026, 0, 5));
        assertArrayEquals(new int[]{2026, 0, 5}, DateUtils.toPicker("2026-01-05"));
        assertEquals("23/10/2026", DateUtils.toDisplayDate(TODAY));
        assertEquals("23/10/2026 14:05", DateUtils.toDisplayDateTime("2026-10-23 14:05:00"));
        assertEquals("bad", DateUtils.toDisplayDate("bad"));
    }

    @Test
    public void orderStatus_onlyMovesForward() {
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.next(OrderStatus.PENDING));
        assertEquals(OrderStatus.COMPLETED, OrderStatus.next(OrderStatus.CONFIRMED));
        assertNull(OrderStatus.next(OrderStatus.COMPLETED));
        assertTrue(OrderStatus.isValid("Pending"));
        assertFalse(OrderStatus.isValid("Cancelled"));
        assertFalse(OrderStatus.isValid(null));
    }

    @Test
    public void formatPrice_usesThousandsSeparator() {
        assertEquals("45,000 VND", FormatUtils.formatPrice(45000));
        assertEquals("1,250,000 VND", FormatUtils.formatPrice(1250000));
        assertEquals("0 VND", FormatUtils.formatPrice(0));
    }

    @Test
    public void passwordHash_isSha256Hex() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                PasswordUtils.hash("abc"));
        assertEquals(PasswordUtils.hash("admin123"), PasswordUtils.hash("admin123"));
        assertNotEquals(PasswordUtils.hash("admin123"), PasswordUtils.hash("Admin123"));
    }
}
