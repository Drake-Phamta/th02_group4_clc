package com.group4.fruitshop.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.group4.fruitshop.data.DatabaseHelper;
import com.group4.fruitshop.data.model.Customer;

import static com.group4.fruitshop.data.DatabaseHelper.*;

/**
 * Customer profiles. New customers are created with AccountDao.registerCustomer(...).
 */
public class CustomerDao {

    private final DatabaseHelper dbHelper;

    public CustomerDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public Customer getById(int id) {
        return findOne(CUSTOMER_ID + " = ?", String.valueOf(id));
    }

    /** The customer profile of a logged-in account (null for admins). */
    public Customer getByAccountId(int accountId) {
        return findOne(CUSTOMER_ACCOUNT_ID + " = ?", String.valueOf(accountId));
    }

    /**
     * Updates fullname, phone and email. Returns the number of updated rows
     * (1 = success, 0 = not found or invalid values such as an empty fullname).
     */
    public int update(Customer customer) {
        ContentValues values = new ContentValues();
        values.put(CUSTOMER_FULLNAME, customer.getFullname());
        values.put(CUSTOMER_PHONE, customer.getPhone());
        values.put(CUSTOMER_EMAIL, customer.getEmail());
        try {
            return dbHelper.getWritableDatabase().update(TABLE_CUSTOMER, values,
                    CUSTOMER_ID + " = ?", new String[]{String.valueOf(customer.getId())});
        } catch (SQLException e) {
            return 0;
        }
    }

    private Customer findOne(String selection, String arg) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_CUSTOMER, null, selection, new String[]{arg},
                null, null, null)) {
            if (!c.moveToFirst()) {
                return null;
            }
            return new Customer(
                    c.getInt(c.getColumnIndexOrThrow(CUSTOMER_ID)),
                    c.getString(c.getColumnIndexOrThrow(CUSTOMER_FULLNAME)),
                    c.getString(c.getColumnIndexOrThrow(CUSTOMER_PHONE)),
                    c.getString(c.getColumnIndexOrThrow(CUSTOMER_EMAIL)),
                    c.getInt(c.getColumnIndexOrThrow(CUSTOMER_ACCOUNT_ID)));
        }
    }
}
