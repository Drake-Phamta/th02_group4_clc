package com.group4.fruitshop.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.group4.fruitshop.data.DatabaseHelper;
import com.group4.fruitshop.data.model.Fruit;
import com.group4.fruitshop.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

import static com.group4.fruitshop.data.DatabaseHelper.*;

/**
 * Fruit CRUD, search and expiry queries.
 * Lists are sorted by name; "keyword" matches name OR category, case-insensitive, and an
 * empty/null keyword returns everything.
 */
public class FruitDao {

    private final DatabaseHelper dbHelper;

    public FruitDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /** Returns the new id, or -1 on failure. */
    public long insert(Fruit fruit) {
        return dbHelper.getWritableDatabase().insert(TABLE_FRUIT, null, toValues(fruit));
    }

    /**
     * Updates every column of {@code fruit.getId()}. Returns the number of updated rows
     * (1 = success, 0 = not found or invalid values such as a negative quantity).
     */
    public int update(Fruit fruit) {
        try {
            return dbHelper.getWritableDatabase().update(TABLE_FRUIT, toValues(fruit),
                    FRUIT_ID + " = ?", new String[]{String.valueOf(fruit.getId())});
        } catch (SQLException e) {
            return 0;
        }
    }

    /** Returns the number of deleted rows (1 = success). */
    public int delete(int id) {
        return dbHelper.getWritableDatabase().delete(TABLE_FRUIT, FRUIT_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public Fruit getById(int id) {
        List<Fruit> result = query(FRUIT_ID + " = ?", new String[]{String.valueOf(id)}, null);
        return result.isEmpty() ? null : result.get(0);
    }

    /** All fruits (admin). */
    public List<Fruit> getAll() {
        return search(null);
    }

    /** Admin search over all fruits, including expired and out-of-stock ones. */
    public List<Fruit> search(String keyword) {
        if (isBlank(keyword)) {
            return query(null, null, FRUIT_NAME + " COLLATE NOCASE");
        }
        String like = "%" + keyword.trim() + "%";
        return query(FRUIT_NAME + " LIKE ? OR " + FRUIT_CATEGORY + " LIKE ?",
                new String[]{like, like}, FRUIT_NAME + " COLLATE NOCASE");
    }

    /** Fruits whose expiry date is before today. */
    public List<Fruit> getExpired() {
        return query(FRUIT_EXPIRY_DATE + " < ?", new String[]{DateUtils.today()},
                FRUIT_EXPIRY_DATE);
    }

    /**
     * Customer list: fruits with quantity > 0 matching {@code keyword}. Expired fruits are still
     * returned (so customers can check the date) but sorted last; use Fruit.canBeOrdered().
     */
    public List<Fruit> searchInStock(String keyword) {
        String selection = FRUIT_QUANTITY + " > 0";
        String[] args = new String[]{DateUtils.today()};
        if (!isBlank(keyword)) {
            String like = "%" + keyword.trim() + "%";
            selection += " AND (" + FRUIT_NAME + " LIKE ? OR " + FRUIT_CATEGORY + " LIKE ?)";
            args = new String[]{like, like, DateUtils.today()};
        }
        // Expired rows (expiryDate < today) get 1 and go to the bottom.
        String orderBy = "(" + FRUIT_EXPIRY_DATE + " < ?), " + FRUIT_NAME + " COLLATE NOCASE";
        return rawList("SELECT * FROM " + TABLE_FRUIT + " WHERE " + selection
                + " ORDER BY " + orderBy, args);
    }

    /** Distinct categories, sorted (for suggestions in the fruit form). */
    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT DISTINCT " + FRUIT_CATEGORY
                + " FROM " + TABLE_FRUIT + " ORDER BY " + FRUIT_CATEGORY + " COLLATE NOCASE", null)) {
            while (c.moveToNext()) {
                categories.add(c.getString(0));
            }
        }
        return categories;
    }

    public int countAll() {
        return (int) DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), TABLE_FRUIT);
    }

    public int countExpired() {
        return (int) DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), TABLE_FRUIT,
                FRUIT_EXPIRY_DATE + " < ?", new String[]{DateUtils.today()});
    }

    private List<Fruit> query(String selection, String[] args, String orderBy) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_FRUIT, null, selection, args, null, null, orderBy)) {
            return readAll(c);
        }
    }

    private List<Fruit> rawList(String sql, String[] args) {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(sql, args)) {
            return readAll(c);
        }
    }

    private static List<Fruit> readAll(Cursor c) {
        List<Fruit> fruits = new ArrayList<>();
        while (c.moveToNext()) {
            fruits.add(new Fruit(
                    c.getInt(c.getColumnIndexOrThrow(FRUIT_ID)),
                    c.getString(c.getColumnIndexOrThrow(FRUIT_NAME)),
                    c.getString(c.getColumnIndexOrThrow(FRUIT_CATEGORY)),
                    c.getDouble(c.getColumnIndexOrThrow(FRUIT_PRICE)),
                    c.getInt(c.getColumnIndexOrThrow(FRUIT_QUANTITY)),
                    c.getString(c.getColumnIndexOrThrow(FRUIT_EXPIRY_DATE)),
                    c.getString(c.getColumnIndexOrThrow(FRUIT_DESCRIPTION))));
        }
        return fruits;
    }

    private static ContentValues toValues(Fruit fruit) {
        ContentValues values = new ContentValues();
        values.put(FRUIT_NAME, fruit.getName());
        values.put(FRUIT_CATEGORY, fruit.getCategory());
        values.put(FRUIT_PRICE, fruit.getPrice());
        values.put(FRUIT_QUANTITY, fruit.getQuantity());
        values.put(FRUIT_EXPIRY_DATE, fruit.getExpiryDate());
        values.put(FRUIT_DESCRIPTION, fruit.getDescription());
        return values;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
