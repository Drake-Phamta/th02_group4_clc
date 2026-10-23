package com.group4.fruitshop.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.group4.fruitshop.data.DatabaseHelper;
import com.group4.fruitshop.data.model.Fruit;
import com.group4.fruitshop.data.model.Order;
import com.group4.fruitshop.util.DateUtils;
import com.group4.fruitshop.util.OrderStatus;

import java.util.ArrayList;
import java.util.List;

import static com.group4.fruitshop.data.DatabaseHelper.*;

/**
 * Placing orders (customer) and managing their status (admin). Lists are newest first.
 */
public class OrderDao {

    /** Outcome of {@link #placeOrder(int, int, int)}. Only SUCCESS creates an order. */
    public enum PlaceOrderResult {
        SUCCESS,
        /** quantity <= 0 */
        INVALID_QUANTITY,
        FRUIT_NOT_FOUND,
        /** The fruit's expiry date is before today: expired fruits can never be ordered. */
        EXPIRED,
        /** quantity is greater than the stock left. */
        OUT_OF_STOCK,
        FAILED
    }

    private final DatabaseHelper dbHelper;
    private final FruitDao fruitDao;

    public OrderDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        fruitDao = new FruitDao(context);
    }

    /**
     * Customer buys {@code quantity} units of one fruit. In a single transaction this checks the
     * fruit is not expired and in stock, decreases Fruit.quantity and inserts an Order with
     * status Pending, orderDate = now and totalAmount = price x quantity.
     */
    public PlaceOrderResult placeOrder(int customerId, int fruitId, int quantity) {
        if (quantity <= 0) {
            return PlaceOrderResult.INVALID_QUANTITY;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Fruit fruit = fruitDao.getById(fruitId);
            if (fruit == null) {
                return PlaceOrderResult.FRUIT_NOT_FOUND;
            }
            if (fruit.isExpired()) {
                return PlaceOrderResult.EXPIRED;
            }
            if (quantity > fruit.getQuantity()) {
                return PlaceOrderResult.OUT_OF_STOCK;
            }

            db.execSQL("UPDATE " + TABLE_FRUIT + " SET " + FRUIT_QUANTITY + " = "
                    + FRUIT_QUANTITY + " - ? WHERE " + FRUIT_ID + " = ?", new Object[]{quantity, fruitId});

            ContentValues order = new ContentValues();
            order.put(ORDER_CUSTOMER_ID, customerId);
            order.put(ORDER_DATE, DateUtils.now());
            order.put(ORDER_TOTAL_AMOUNT, fruit.getPrice() * quantity);
            order.put(ORDER_STATUS, OrderStatus.PENDING);
            if (db.insert(TABLE_ORDER, null, order) == -1) {
                return PlaceOrderResult.FAILED;
            }

            db.setTransactionSuccessful();
            return PlaceOrderResult.SUCCESS;
        } finally {
            db.endTransaction();
        }
    }

    /** Order history of one customer, newest first. */
    public List<Order> getByCustomer(int customerId) {
        return list("SELECT o.*, c." + CUSTOMER_FULLNAME + ", c." + CUSTOMER_PHONE
                + " FROM " + TABLE_ORDER + " o JOIN " + TABLE_CUSTOMER + " c ON c." + CUSTOMER_ID
                + " = o." + ORDER_CUSTOMER_ID + " WHERE o." + ORDER_CUSTOMER_ID + " = ?"
                + " ORDER BY o." + ORDER_DATE + " DESC, o." + ORDER_ID + " DESC",
                new String[]{String.valueOf(customerId)});
    }

    /**
     * All orders with the customer's name/phone (admin), newest first.
     *
     * @param status one of OrderStatus.ALL, or null for every status
     */
    public List<Order> getAllWithCustomer(String status) {
        String sql = "SELECT o.*, c." + CUSTOMER_FULLNAME + ", c." + CUSTOMER_PHONE
                + " FROM " + TABLE_ORDER + " o JOIN " + TABLE_CUSTOMER + " c ON c." + CUSTOMER_ID
                + " = o." + ORDER_CUSTOMER_ID;
        String[] args = null;
        if (status != null) {
            sql += " WHERE o." + ORDER_STATUS + " = ?";
            args = new String[]{status};
        }
        sql += " ORDER BY o." + ORDER_DATE + " DESC, o." + ORDER_ID + " DESC";
        return list(sql, args);
    }

    public Order getById(int id) {
        List<Order> result = list("SELECT o.*, c." + CUSTOMER_FULLNAME + ", c." + CUSTOMER_PHONE
                + " FROM " + TABLE_ORDER + " o JOIN " + TABLE_CUSTOMER + " c ON c." + CUSTOMER_ID
                + " = o." + ORDER_CUSTOMER_ID + " WHERE o." + ORDER_ID + " = ?",
                new String[]{String.valueOf(id)});
        return result.isEmpty() ? null : result.get(0);
    }

    /** Sets a new status (must be one of OrderStatus.ALL). Returns true if the order was updated. */
    public boolean updateStatus(int orderId, String newStatus) {
        if (!OrderStatus.isValid(newStatus)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(ORDER_STATUS, newStatus);
        return dbHelper.getWritableDatabase().update(TABLE_ORDER, values, ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)}) == 1;
    }

    public int countAll() {
        return (int) DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), TABLE_ORDER);
    }

    public int countByStatus(String status) {
        return (int) DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), TABLE_ORDER,
                ORDER_STATUS + " = ?", new String[]{status});
    }

    private List<Order> list(String sql, String[] args) {
        List<Order> orders = new ArrayList<>();
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(sql, args)) {
            while (c.moveToNext()) {
                Order order = new Order(
                        c.getInt(c.getColumnIndexOrThrow(ORDER_ID)),
                        c.getInt(c.getColumnIndexOrThrow(ORDER_CUSTOMER_ID)),
                        c.getString(c.getColumnIndexOrThrow(ORDER_DATE)),
                        c.getDouble(c.getColumnIndexOrThrow(ORDER_TOTAL_AMOUNT)),
                        c.getString(c.getColumnIndexOrThrow(ORDER_STATUS)));
                order.setCustomerName(c.getString(c.getColumnIndexOrThrow(CUSTOMER_FULLNAME)));
                order.setCustomerPhone(c.getString(c.getColumnIndexOrThrow(CUSTOMER_PHONE)));
                orders.add(order);
            }
        }
        return orders;
    }
}
