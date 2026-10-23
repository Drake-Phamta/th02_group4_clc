package com.group4.fruitshop.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database with exactly the 4 tables of the specification:
 * Account, Fruit, Customer, Order. Demo data is inserted on first launch (see SeedData).
 *
 * <p>Screens never use this class directly: go through the DAOs in {@code data.dao}.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "fruit_shop.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_ACCOUNT = "Account";
    public static final String ACCOUNT_ID = "id";
    public static final String ACCOUNT_USERNAME = "username";
    public static final String ACCOUNT_PASSWORD = "password";
    public static final String ACCOUNT_ROLE = "role";

    public static final String TABLE_FRUIT = "Fruit";
    public static final String FRUIT_ID = "id";
    public static final String FRUIT_NAME = "name";
    public static final String FRUIT_CATEGORY = "category";
    public static final String FRUIT_PRICE = "price";
    public static final String FRUIT_QUANTITY = "quantity";
    public static final String FRUIT_EXPIRY_DATE = "expiryDate";
    public static final String FRUIT_DESCRIPTION = "description";

    public static final String TABLE_CUSTOMER = "Customer";
    public static final String CUSTOMER_ID = "id";
    public static final String CUSTOMER_FULLNAME = "fullname";
    public static final String CUSTOMER_PHONE = "phone";
    public static final String CUSTOMER_EMAIL = "email";
    public static final String CUSTOMER_ACCOUNT_ID = "accountId";

    /** "Order" is an SQL keyword, so the table name must always be quoted. */
    public static final String TABLE_ORDER = "\"Order\"";
    public static final String ORDER_ID = "id";
    public static final String ORDER_CUSTOMER_ID = "customerId";
    public static final String ORDER_DATE = "orderDate";
    public static final String ORDER_TOTAL_AMOUNT = "totalAmount";
    public static final String ORDER_STATUS = "status";

    private static final String CREATE_ACCOUNT = "CREATE TABLE " + TABLE_ACCOUNT + " ("
            + ACCOUNT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ACCOUNT_USERNAME + " TEXT NOT NULL UNIQUE COLLATE NOCASE, "
            + ACCOUNT_PASSWORD + " TEXT NOT NULL, "
            + ACCOUNT_ROLE + " TEXT NOT NULL CHECK (" + ACCOUNT_ROLE + " IN ('Admin', 'Customer')))";

    private static final String CREATE_FRUIT = "CREATE TABLE " + TABLE_FRUIT + " ("
            + FRUIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FRUIT_NAME + " TEXT NOT NULL, "
            + FRUIT_CATEGORY + " TEXT NOT NULL, "
            + FRUIT_PRICE + " REAL NOT NULL CHECK (" + FRUIT_PRICE + " >= 0), "
            + FRUIT_QUANTITY + " INTEGER NOT NULL CHECK (" + FRUIT_QUANTITY + " >= 0), "
            + FRUIT_EXPIRY_DATE + " TEXT NOT NULL, "
            + FRUIT_DESCRIPTION + " TEXT)";

    private static final String CREATE_CUSTOMER = "CREATE TABLE " + TABLE_CUSTOMER + " ("
            + CUSTOMER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CUSTOMER_FULLNAME + " TEXT NOT NULL, "
            + CUSTOMER_PHONE + " TEXT, "
            + CUSTOMER_EMAIL + " TEXT, "
            + CUSTOMER_ACCOUNT_ID + " INTEGER NOT NULL UNIQUE REFERENCES "
            + TABLE_ACCOUNT + "(" + ACCOUNT_ID + ") ON DELETE CASCADE)";

    private static final String CREATE_ORDER = "CREATE TABLE " + TABLE_ORDER + " ("
            + ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ORDER_CUSTOMER_ID + " INTEGER NOT NULL REFERENCES "
            + TABLE_CUSTOMER + "(" + CUSTOMER_ID + ") ON DELETE CASCADE, "
            + ORDER_DATE + " TEXT NOT NULL, "
            + ORDER_TOTAL_AMOUNT + " REAL NOT NULL CHECK (" + ORDER_TOTAL_AMOUNT + " >= 0), "
            + ORDER_STATUS + " TEXT NOT NULL DEFAULT 'Pending' CHECK ("
            + ORDER_STATUS + " IN ('Pending', 'Confirmed', 'Completed')))";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /** Closes and forgets the shared instance. Only for tests, which get a fresh app sandbox each run. */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ACCOUNT);
        db.execSQL(CREATE_FRUIT);
        db.execSQL(CREATE_CUSTOMER);
        db.execSQL(CREATE_ORDER);
        SeedData.insertAll(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRUIT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        onCreate(db);
    }
}
