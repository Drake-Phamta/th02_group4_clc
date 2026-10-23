package com.group4.fruitshop.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.group4.fruitshop.data.model.Account;
import com.group4.fruitshop.util.Constants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Login account storage in SharedPreferences.
 */
@RunWith(RobolectricTestRunner.class)
public class SessionManagerTest {

    private Context context;
    private SessionManager session;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        session = new SessionManager(context);
        session.logout();
        session.setRememberMe(false, null, null);
    }

    @Test
    public void createSession_isPersistedForTheWholeApp() {
        session.createSession(new Account(7, "customer", "hash", Constants.ROLE_CUSTOMER), 3);

        SessionManager other = new SessionManager(context);
        assertTrue(other.isLoggedIn());
        assertEquals(7, other.getAccountId());
        assertEquals("customer", other.getUsername());
        assertEquals(Constants.ROLE_CUSTOMER, other.getRole());
        assertFalse(other.isAdmin());
        assertEquals(3, other.getCustomerId());
    }

    @Test
    public void adminSession_hasNoCustomerId() {
        session.createSession(new Account(1, "admin", "hash", Constants.ROLE_ADMIN), Constants.NO_ID);
        assertTrue(session.isAdmin());
        assertEquals(Constants.NO_ID, session.getCustomerId());
    }

    @Test
    public void logout_clearsSession_butKeepsRememberedCredentials() {
        session.createSession(new Account(1, "admin", "hash", Constants.ROLE_ADMIN), Constants.NO_ID);
        session.setRememberMe(true, "admin", "admin123");

        session.logout();

        assertFalse(session.isLoggedIn());
        assertEquals("", session.getRole());
        assertEquals(Constants.NO_ID, session.getAccountId());
        assertTrue(session.isRememberMe());
        assertEquals("admin", session.getSavedUsername());
        assertEquals("admin123", session.getSavedPassword());
    }

    @Test
    public void autoLogin_needsBothSessionAndRememberMe() {
        assertFalse(session.shouldAutoLogin());

        session.createSession(new Account(2, "customer", "hash", Constants.ROLE_CUSTOMER), 1);
        assertFalse(session.shouldAutoLogin());

        session.setRememberMe(true, "customer", "customer123");
        assertTrue(session.shouldAutoLogin());

        session.setRememberMe(false, null, null);
        assertFalse(session.shouldAutoLogin());
        assertEquals("", session.getSavedUsername());
        assertEquals("", session.getSavedPassword());
    }
}
