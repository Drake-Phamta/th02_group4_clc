package com.group4.fruitshop.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.group4.fruitshop.TestData;
import com.group4.fruitshop.data.DatabaseHelper;
import com.group4.fruitshop.session.SessionManager;
import com.group4.fruitshop.ui.admin.AdminHomeActivity;
import com.group4.fruitshop.ui.admin.fruit.FruitFormActivity;
import com.group4.fruitshop.ui.admin.fruit.FruitManagementActivity;
import com.group4.fruitshop.ui.admin.order.OrderManagementActivity;
import com.group4.fruitshop.ui.auth.LoginActivity;
import com.group4.fruitshop.ui.auth.RegisterActivity;
import com.group4.fruitshop.ui.customer.CustomerHomeActivity;
import com.group4.fruitshop.ui.customer.FruitDetailActivity;
import com.group4.fruitshop.ui.customer.OrderHistoryActivity;
import com.group4.fruitshop.util.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

/**
 * Opens every screen of the app. Guards the integration of all modules:
 * <ul>
 *     <li>each screen opens (RESUMED) without crashing for the right role;</li>
 *     <li>admin screens refuse customers, customer screens refuse admins, and both refuse
 *     logged-out users (BaseActivity.requireRole sends them back to the login screen).</li>
 * </ul>
 * If this test fails on your Pull Request, your screen crashes on open or misses requireRole(...).
 */
@RunWith(RobolectricTestRunner.class)
public class ScreenSmokeTest {

    private Context context;

    @Before
    public void setUp() {
        DatabaseHelper.resetInstance();
        context = ApplicationProvider.getApplicationContext();
        TestData.logOut(context);
    }

    @After
    public void tearDown() {
        DatabaseHelper.resetInstance();
    }

    @Test
    public void authScreens_openWhenLoggedOut() {
        assertOpens(new Intent(context, LoginActivity.class));
        assertOpens(new Intent(context, RegisterActivity.class));
    }

    @Test
    public void adminScreens_openForAdmin() {
        TestData.logInAs(context, "admin", "admin123");
        for (Intent screen : adminScreens()) {
            assertOpens(screen);
        }
    }

    @Test
    public void customerScreens_openForCustomer() {
        TestData.logInAs(context, "customer", "customer123");
        for (Intent screen : customerScreens()) {
            assertOpens(screen);
        }
    }

    @Test
    public void adminScreens_rejectCustomersAndGuests() {
        for (Intent screen : adminScreens()) {
            TestData.logInAs(context, "customer", "customer123");
            assertRejected(screen);
            assertRejected(screen);
        }
    }

    @Test
    public void customerScreens_rejectAdminsAndGuests() {
        for (Intent screen : customerScreens()) {
            TestData.logInAs(context, "admin", "admin123");
            assertRejected(screen);
            assertRejected(screen);
        }
    }

    private List<Intent> adminScreens() {
        return Arrays.asList(
                new Intent(context, AdminHomeActivity.class),
                new Intent(context, FruitManagementActivity.class),
                new Intent(context, FruitFormActivity.class),
                new Intent(context, FruitFormActivity.class).putExtra(Constants.EXTRA_FRUIT_ID, 1),
                new Intent(context, OrderManagementActivity.class));
    }

    private List<Intent> customerScreens() {
        return Arrays.asList(
                new Intent(context, CustomerHomeActivity.class),
                new Intent(context, FruitDetailActivity.class).putExtra(Constants.EXTRA_FRUIT_ID, 1),
                new Intent(context, OrderHistoryActivity.class));
    }

    private void assertOpens(Intent intent) {
        try (ActivityScenario<Activity> scenario = ActivityScenario.launch(intent)) {
            assertEquals(name(intent) + " should open", Lifecycle.State.RESUMED, scenario.getState());
        }
    }

    /** The screen must close itself, log the user out and go back to the login screen. */
    private void assertRejected(Intent intent) {
        shadowOf((Application) context).clearNextStartedActivities();
        try (ActivityScenario<Activity> scenario = ActivityScenario.launch(intent)) {
            assertEquals(name(intent) + " must refuse this user", Lifecycle.State.DESTROYED,
                    scenario.getState());
        }
        assertFalse(name(intent) + " must log the user out", new SessionManager(context).isLoggedIn());
        boolean openedLogin = false;
        Intent next;
        while ((next = shadowOf((Application) context).getNextStartedActivity()) != null) {
            if (next.getComponent() != null
                    && LoginActivity.class.getName().equals(next.getComponent().getClassName())) {
                openedLogin = true;
            }
        }
        assertTrue(name(intent) + " must open the login screen", openedLogin);
    }

    private static String name(Intent intent) {
        return intent.getComponent().getShortClassName();
    }
}
