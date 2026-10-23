package com.group4.fruitshop.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.group4.fruitshop.data.DatabaseHelper;
import com.group4.fruitshop.data.model.Account;
import com.group4.fruitshop.util.Constants;
import com.group4.fruitshop.util.PasswordUtils;

import static com.group4.fruitshop.data.DatabaseHelper.*;

/**
 * Login and registration. Always pass RAW passwords: hashing is done here.
 * Usernames are trimmed and case-insensitive ("Admin" == "admin").
 */
public class AccountDao {

    private final DatabaseHelper dbHelper;

    public AccountDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /** Returns the matching account, or null when the username/password is wrong. */
    public Account login(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return null;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_ACCOUNT, null,
                ACCOUNT_USERNAME + " = ? AND " + ACCOUNT_PASSWORD + " = ?",
                new String[]{username.trim(), PasswordUtils.hash(rawPassword)},
                null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    public boolean isUsernameTaken(String username) {
        return getByUsername(username) != null;
    }

    public Account getByUsername(String username) {
        if (username == null) {
            return null;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_ACCOUNT, null, ACCOUNT_USERNAME + " = ?",
                new String[]{username.trim()}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    public Account getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_ACCOUNT, null, ACCOUNT_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    /**
     * Creates an Account (role Customer) and its Customer row in one transaction.
     *
     * @return the new Customer id, or {@link Constants#NO_ID} if username/password/fullname is
     * empty, the username is taken or the insert fails.
     */
    public int registerCustomer(String username, String rawPassword, String fullname,
                                String phone, String email) {
        if (isBlank(username) || rawPassword == null || rawPassword.isEmpty() || isBlank(fullname)
                || isUsernameTaken(username)) {
            return Constants.NO_ID;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues account = new ContentValues();
            account.put(ACCOUNT_USERNAME, username.trim());
            account.put(ACCOUNT_PASSWORD, PasswordUtils.hash(rawPassword));
            account.put(ACCOUNT_ROLE, Constants.ROLE_CUSTOMER);
            long accountId = db.insert(TABLE_ACCOUNT, null, account);
            if (accountId == -1) {
                return Constants.NO_ID;
            }

            ContentValues customer = new ContentValues();
            customer.put(CUSTOMER_FULLNAME, fullname.trim());
            customer.put(CUSTOMER_PHONE, phone == null ? null : phone.trim());
            customer.put(CUSTOMER_EMAIL, email == null ? null : email.trim());
            customer.put(CUSTOMER_ACCOUNT_ID, accountId);
            long customerId = db.insert(TABLE_CUSTOMER, null, customer);
            if (customerId == -1) {
                return Constants.NO_ID;
            }

            db.setTransactionSuccessful();
            return (int) customerId;
        } finally {
            db.endTransaction();
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static Account fromCursor(Cursor c) {
        return new Account(
                c.getInt(c.getColumnIndexOrThrow(ACCOUNT_ID)),
                c.getString(c.getColumnIndexOrThrow(ACCOUNT_USERNAME)),
                c.getString(c.getColumnIndexOrThrow(ACCOUNT_PASSWORD)),
                c.getString(c.getColumnIndexOrThrow(ACCOUNT_ROLE)));
    }
}
