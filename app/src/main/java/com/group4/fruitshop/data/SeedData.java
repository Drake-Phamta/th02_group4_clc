package com.group4.fruitshop.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.group4.fruitshop.util.Constants;
import com.group4.fruitshop.util.DateUtils;
import com.group4.fruitshop.util.OrderStatus;
import com.group4.fruitshop.util.PasswordUtils;

import static com.group4.fruitshop.data.DatabaseHelper.*;

/**
 * Demo data inserted when the database is created.
 *
 * <p>Accounts: admin / admin123 (Admin), customer / customer123 and customer2 / customer123 (Customer).
 * Expiry dates are relative to the install day, so the demo always contains
 * expired, expiring-soon, fresh and out-of-stock fruits.
 */
final class SeedData {

    private SeedData() {
    }

    static void insertAll(SQLiteDatabase db) {
        insertAccount(db, "admin", "admin123", Constants.ROLE_ADMIN);
        long anAccount = insertAccount(db, "customer", "customer123", Constants.ROLE_CUSTOMER);
        long binhAccount = insertAccount(db, "customer2", "customer123", Constants.ROLE_CUSTOMER);

        long an = insertCustomer(db, "Nguyen Van An", "0901234567", "an.nguyen@example.com", anAccount);
        long binh = insertCustomer(db, "Tran Thi Binh", "0912345678", "binh.tran@example.com", binhAccount);

        insertFruit(db, "Fuji Apple", "Apple & Pear", 65000, 50, 20,
                "Crisp and sweet apples imported from Japan.");
        insertFruit(db, "Korean Pear", "Apple & Pear", 90000, 20, 15,
                "Large, juicy pear. Best served chilled.");
        insertFruit(db, "Banana", "Tropical", 25000, 100, 3,
                "Ripe Cavendish bananas, rich in potassium.");
        insertFruit(db, "Cat Chu Mango", "Tropical", 45000, 40, -2,
                "Fragrant mango from Dong Thap.");
        insertFruit(db, "Dragon Fruit", "Tropical", 35000, 60, 10,
                "White-flesh dragon fruit from Binh Thuan.");
        insertFruit(db, "Durian", "Tropical", 180000, 10, 7,
                "Ri6 durian with thick, creamy flesh.");
        insertFruit(db, "Orange", "Citrus", 40000, 80, 25,
                "Vinh orange, sweet and full of vitamin C.");
        insertFruit(db, "Lemon", "Citrus", 30000, 70, 30,
                "Seedless green lemons.");
        insertFruit(db, "Strawberry", "Berry", 120000, 15, 1,
                "Fresh Da Lat strawberries.");
        insertFruit(db, "Red Grapes", "Berry", 150000, 0, 12,
                "Seedless red grapes imported from the USA.");
        insertFruit(db, "Watermelon", "Melon", 20000, 25, -5,
                "Long An watermelon with red flesh.");

        insertOrder(db, an, -5, 130000, OrderStatus.COMPLETED);
        insertOrder(db, an, -2, 120000, OrderStatus.CONFIRMED);
        insertOrder(db, binh, -1, 180000, OrderStatus.PENDING);
        insertOrder(db, an, 0, 60000, OrderStatus.PENDING);
    }

    private static long insertAccount(SQLiteDatabase db, String username, String password, String role) {
        ContentValues values = new ContentValues();
        values.put(ACCOUNT_USERNAME, username);
        values.put(ACCOUNT_PASSWORD, PasswordUtils.hash(password));
        values.put(ACCOUNT_ROLE, role);
        return db.insertOrThrow(TABLE_ACCOUNT, null, values);
    }

    private static long insertCustomer(SQLiteDatabase db, String fullname, String phone, String email,
                                       long accountId) {
        ContentValues values = new ContentValues();
        values.put(CUSTOMER_FULLNAME, fullname);
        values.put(CUSTOMER_PHONE, phone);
        values.put(CUSTOMER_EMAIL, email);
        values.put(CUSTOMER_ACCOUNT_ID, accountId);
        return db.insertOrThrow(TABLE_CUSTOMER, null, values);
    }

    private static void insertFruit(SQLiteDatabase db, String name, String category, double price,
                                    int quantity, int expiresInDays, String description) {
        ContentValues values = new ContentValues();
        values.put(FRUIT_NAME, name);
        values.put(FRUIT_CATEGORY, category);
        values.put(FRUIT_PRICE, price);
        values.put(FRUIT_QUANTITY, quantity);
        values.put(FRUIT_EXPIRY_DATE, DateUtils.daysFromToday(expiresInDays));
        values.put(FRUIT_DESCRIPTION, description);
        db.insertOrThrow(TABLE_FRUIT, null, values);
    }

    private static void insertOrder(SQLiteDatabase db, long customerId, int dayOffset, double total,
                                    String status) {
        ContentValues values = new ContentValues();
        values.put(ORDER_CUSTOMER_ID, customerId);
        values.put(ORDER_DATE, DateUtils.daysFromToday(dayOffset) + " 09:30:00");
        values.put(ORDER_TOTAL_AMOUNT, total);
        values.put(ORDER_STATUS, status);
        db.insertOrThrow(TABLE_ORDER, null, values);
    }
}
