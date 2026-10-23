package com.group4.fruitshop.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import com.group4.fruitshop.TestData;
import com.group4.fruitshop.data.dao.AccountDao;
import com.group4.fruitshop.data.dao.CustomerDao;
import com.group4.fruitshop.data.dao.FruitDao;
import com.group4.fruitshop.data.dao.OrderDao;
import com.group4.fruitshop.data.dao.OrderDao.PlaceOrderResult;
import com.group4.fruitshop.data.model.Account;
import com.group4.fruitshop.data.model.Customer;
import com.group4.fruitshop.data.model.Fruit;
import com.group4.fruitshop.data.model.Order;
import com.group4.fruitshop.util.Constants;
import com.group4.fruitshop.util.DateUtils;
import com.group4.fruitshop.util.OrderStatus;
import com.group4.fruitshop.util.PasswordUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs the real SQLite schema, seed data and DAOs.
 */
@RunWith(RobolectricTestRunner.class)
public class DatabaseTest {

    private Context context;
    private AccountDao accountDao;
    private CustomerDao customerDao;
    private FruitDao fruitDao;
    private OrderDao orderDao;

    @Before
    public void setUp() {
        DatabaseHelper.resetInstance();
        context = ApplicationProvider.getApplicationContext();
        accountDao = new AccountDao(context);
        customerDao = new CustomerDao(context);
        fruitDao = new FruitDao(context);
        orderDao = new OrderDao(context);
    }

    @After
    public void tearDown() {
        DatabaseHelper.resetInstance();
    }

    @Test
    public void schema_hasExactlyTheFourTablesOfTheSpec() {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        assertEquals(Arrays.asList("Account", "Customer", "Fruit", "Order"), userTables(db));
        assertEquals(Arrays.asList("id", "username", "password", "role"), columns(db, "Account"));
        assertEquals(Arrays.asList("id", "name", "category", "price", "quantity", "expiryDate",
                "description"), columns(db, "Fruit"));
        assertEquals(Arrays.asList("id", "fullname", "phone", "email", "accountId"),
                columns(db, "Customer"));
        assertEquals(Arrays.asList("id", "customerId", "orderDate", "totalAmount", "status"),
                columns(db, "\"Order\""));
    }

    @Test
    public void seed_demoAccountsLogIn_withHashedPasswords() {
        Account admin = accountDao.login("admin", "admin123");
        assertNotNull(admin);
        assertTrue(admin.isAdmin());
        assertEquals(PasswordUtils.hash("admin123"), admin.getPassword());
        assertNotEquals("admin123", admin.getPassword());

        Account customer = accountDao.login("customer", "customer123");
        assertNotNull(customer);
        assertEquals(Constants.ROLE_CUSTOMER, customer.getRole());
        Customer profile = customerDao.getByAccountId(customer.getId());
        assertEquals("Nguyen Van An", profile.getFullname());

        assertNull(accountDao.login("admin", "wrong"));
        assertNull(accountDao.login("nobody", "admin123"));
        assertNotNull("username is trimmed and case-insensitive", accountDao.login(" ADMIN ", "admin123"));
        assertNull(customerDao.getByAccountId(admin.getId()));
    }

    @Test
    public void seed_containsExpiredExpiringSoonAndOutOfStockFruits() {
        assertEquals(11, fruitDao.countAll());
        assertEquals(2, fruitDao.countExpired());
        assertEquals(Arrays.asList("Watermelon", "Cat Chu Mango"), names(fruitDao.getExpired()));
        assertEquals(DateUtils.ExpiryStatus.EXPIRING_SOON,
                DateUtils.getExpiryStatus(TestData.fruitNamed(context, "Strawberry").getExpiryDate()));
        assertFalse(TestData.fruitNamed(context, "Red Grapes").isInStock());
        assertEquals(4, orderDao.countAll());
        assertEquals(2, orderDao.countByStatus(OrderStatus.PENDING));
        assertEquals(1, orderDao.countByStatus(OrderStatus.CONFIRMED));
        assertEquals(1, orderDao.countByStatus(OrderStatus.COMPLETED));
    }

    @Test
    public void registerCustomer_createsAccountAndProfile() {
        int customerId = accountDao.registerCustomer(" newbie ", "secret1", "Le Van Cuong",
                "0987654321", "cuong@example.com");
        assertTrue(customerId > 0);

        Account account = accountDao.login("newbie", "secret1");
        assertEquals(Constants.ROLE_CUSTOMER, account.getRole());
        Customer customer = customerDao.getByAccountId(account.getId());
        assertEquals(customerId, customer.getId());
        assertEquals("Le Van Cuong", customer.getFullname());
        assertEquals("0987654321", customer.getPhone());
        assertTrue(accountDao.isUsernameTaken("NEWBIE"));
    }

    @Test
    public void registerCustomer_rejectsDuplicateOrEmptyInput() {
        assertEquals(Constants.NO_ID, accountDao.registerCustomer("Customer", "x123456", "Dup", null, null));
        assertEquals(Constants.NO_ID, accountDao.registerCustomer(" ", "x123456", "Name", null, null));
        assertEquals(Constants.NO_ID, accountDao.registerCustomer("someone", "", "Name", null, null));
        assertEquals(Constants.NO_ID, accountDao.registerCustomer("someone", "x123456", " ", null, null));
        assertFalse(accountDao.isUsernameTaken("someone"));
    }

    @Test
    public void fruitCrud() {
        long id = fruitDao.insert(new Fruit("Avocado", "Tropical", 55000, 12,
                DateUtils.daysFromToday(6), "Booth avocado"));
        assertTrue(id > 0);

        Fruit avocado = fruitDao.getById((int) id);
        assertEquals("Avocado", avocado.getName());
        assertEquals("Tropical", avocado.getCategory());
        assertEquals(55000, avocado.getPrice(), 0.001);
        assertEquals(12, avocado.getQuantity());
        assertEquals(DateUtils.daysFromToday(6), avocado.getExpiryDate());
        assertEquals("Booth avocado", avocado.getDescription());

        avocado.setPrice(60000);
        avocado.setQuantity(5);
        assertEquals(1, fruitDao.update(avocado));
        assertEquals(60000, fruitDao.getById((int) id).getPrice(), 0.001);
        assertEquals(5, fruitDao.getById((int) id).getQuantity());

        avocado.setQuantity(-1);
        assertEquals("invalid values are rejected, not crashing", 0, fruitDao.update(avocado));
        assertEquals(5, fruitDao.getById((int) id).getQuantity());

        assertEquals(1, fruitDao.delete((int) id));
        assertNull(fruitDao.getById((int) id));
        assertEquals(0, fruitDao.delete((int) id));
    }

    @Test
    public void search_matchesNameOrCategory_caseInsensitive() {
        assertEquals(Arrays.asList("Lemon", "Orange"), names(fruitDao.search("citrus")));
        assertEquals(Arrays.asList("Fuji Apple", "Korean Pear"), names(fruitDao.search("APP")));
        assertEquals(11, fruitDao.search("  ").size());
        assertEquals(11, fruitDao.getAll().size());
        assertTrue(fruitDao.search("kiwi").isEmpty());
        assertEquals(Arrays.asList("Apple & Pear", "Berry", "Citrus", "Melon", "Tropical"),
                fruitDao.getCategories());
    }

    @Test
    public void searchInStock_hidesOutOfStock_andListsExpiredLast() {
        List<Fruit> fruits = fruitDao.searchInStock(null);
        assertEquals(10, fruits.size());
        assertFalse(names(fruits).contains("Red Grapes"));
        for (int i = 0; i < fruits.size(); i++) {
            assertEquals("expired fruits must be the last two", i >= 8, fruits.get(i).isExpired());
        }
        assertEquals(Arrays.asList("Watermelon"), names(fruitDao.searchInStock("melon")));
        assertTrue(fruitDao.searchInStock("grapes").isEmpty());
    }

    @Test
    public void placeOrder_createsPendingOrder_andDecreasesStock() {
        int customerId = TestData.customerIdOf(context, "customer", "customer123");
        Fruit apple = TestData.fruitNamed(context, "Fuji Apple");

        assertEquals(PlaceOrderResult.SUCCESS, orderDao.placeOrder(customerId, apple.getId(), 2));

        assertEquals(apple.getQuantity() - 2, fruitDao.getById(apple.getId()).getQuantity());
        Order newest = orderDao.getByCustomer(customerId).get(0);
        assertEquals(OrderStatus.PENDING, newest.getStatus());
        assertEquals(130000, newest.getTotalAmount(), 0.001);
        assertTrue(newest.getOrderDate().startsWith(DateUtils.today()));
        assertEquals(5, orderDao.countAll());
    }

    @Test
    public void placeOrder_rejectsExpiredFruit_withoutTouchingStock() {
        int customerId = TestData.customerIdOf(context, "customer", "customer123");
        Fruit mango = TestData.fruitNamed(context, "Cat Chu Mango");

        assertEquals(PlaceOrderResult.EXPIRED, orderDao.placeOrder(customerId, mango.getId(), 1));
        assertEquals(mango.getQuantity(), fruitDao.getById(mango.getId()).getQuantity());
        assertEquals(4, orderDao.countAll());
    }

    @Test
    public void placeOrder_rejectsInvalidRequests() {
        int customerId = TestData.customerIdOf(context, "customer", "customer123");
        Fruit apple = TestData.fruitNamed(context, "Fuji Apple");
        Fruit grapes = TestData.fruitNamed(context, "Red Grapes");

        assertEquals(PlaceOrderResult.OUT_OF_STOCK,
                orderDao.placeOrder(customerId, apple.getId(), apple.getQuantity() + 1));
        assertEquals(PlaceOrderResult.OUT_OF_STOCK, orderDao.placeOrder(customerId, grapes.getId(), 1));
        assertEquals(PlaceOrderResult.INVALID_QUANTITY, orderDao.placeOrder(customerId, apple.getId(), 0));
        assertEquals(PlaceOrderResult.INVALID_QUANTITY, orderDao.placeOrder(customerId, apple.getId(), -3));
        assertEquals(PlaceOrderResult.FRUIT_NOT_FOUND, orderDao.placeOrder(customerId, 9999, 1));
        assertEquals("unknown customer -> rolled back", PlaceOrderResult.FAILED,
                orderDao.placeOrder(9999, apple.getId(), 1));

        assertEquals(apple.getQuantity(), fruitDao.getById(apple.getId()).getQuantity());
        assertEquals(4, orderDao.countAll());
    }

    @Test
    public void ordersAreListedNewestFirst_withCustomerInfo() {
        List<Order> all = orderDao.getAllWithCustomer(null);
        assertEquals(Arrays.asList(4, 3, 2, 1), ids(all));
        assertEquals("Nguyen Van An", all.get(0).getCustomerName());
        assertEquals("0901234567", all.get(0).getCustomerPhone());
        assertEquals("Tran Thi Binh", all.get(1).getCustomerName());

        assertEquals(Arrays.asList(4, 3), ids(orderDao.getAllWithCustomer(OrderStatus.PENDING)));
        assertEquals(Arrays.asList(1), ids(orderDao.getAllWithCustomer(OrderStatus.COMPLETED)));

        int an = TestData.customerIdOf(context, "customer", "customer123");
        assertEquals(Arrays.asList(4, 2, 1), ids(orderDao.getByCustomer(an)));
    }

    @Test
    public void updateStatus_acceptsOnlyKnownStatuses() {
        assertTrue(orderDao.updateStatus(4, OrderStatus.CONFIRMED));
        assertEquals(OrderStatus.CONFIRMED, orderDao.getById(4).getStatus());
        assertTrue(orderDao.updateStatus(4, OrderStatus.COMPLETED));
        assertEquals(OrderStatus.COMPLETED, orderDao.getById(4).getStatus());

        assertFalse(orderDao.updateStatus(4, "Cancelled"));
        assertFalse(orderDao.updateStatus(9999, OrderStatus.COMPLETED));
        assertEquals(OrderStatus.COMPLETED, orderDao.getById(4).getStatus());
        assertNull(orderDao.getById(9999));
    }

    private static List<String> userTables(SQLiteDatabase db) {
        List<String> tables = new ArrayList<>();
        try (Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'"
                + " AND name NOT LIKE 'sqlite_%' AND name <> 'android_metadata' ORDER BY name", null)) {
            while (c.moveToNext()) {
                tables.add(c.getString(0));
            }
        }
        return tables;
    }

    private static List<String> columns(SQLiteDatabase db, String table) {
        List<String> columns = new ArrayList<>();
        try (Cursor c = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (c.moveToNext()) {
                columns.add(c.getString(c.getColumnIndexOrThrow("name")));
            }
        }
        return columns;
    }

    private static List<String> names(List<Fruit> fruits) {
        List<String> names = new ArrayList<>();
        for (Fruit fruit : fruits) {
            names.add(fruit.getName());
        }
        return names;
    }

    private static List<Integer> ids(List<Order> orders) {
        List<Integer> ids = new ArrayList<>();
        for (Order order : orders) {
            ids.add(order.getId());
        }
        return ids;
    }
}
