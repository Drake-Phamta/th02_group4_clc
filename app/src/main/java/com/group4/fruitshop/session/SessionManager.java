package com.group4.fruitshop.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.group4.fruitshop.data.model.Account;
import com.group4.fruitshop.util.Constants;

/**
 * Login account storage with SharedPreferences.
 *
 * <ul>
 *     <li><b>Session</b>: who is logged in (account id, username, role, customer id).
 *     Kept until {@link #logout()}.</li>
 *     <li><b>Remember me</b>: saved username/password used to pre-fill the login form and to
 *     auto-login on the next app start ({@link #shouldAutoLogin()}). Kept after logout.</li>
 * </ul>
 */
public class SessionManager {

    private static final String PREF_NAME = "fruit_shop_session";

    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_CUSTOMER_ID = "customer_id";

    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SAVED_USERNAME = "saved_username";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves the logged-in account.
     *
     * @param customerId Customer.id of this account, or Constants.NO_ID for an admin
     */
    public void createSession(Account account, int customerId) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putInt(KEY_ACCOUNT_ID, account.getId())
                .putString(KEY_USERNAME, account.getUsername())
                .putString(KEY_ROLE, account.getRole())
                .putInt(KEY_CUSTOMER_ID, customerId)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public int getAccountId() {
        return prefs.getInt(KEY_ACCOUNT_ID, Constants.NO_ID);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    /** Constants.ROLE_ADMIN, Constants.ROLE_CUSTOMER, or "" when logged out. */
    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equals(getRole());
    }

    /** Customer.id of the logged-in customer (Constants.NO_ID for admins / logged out). */
    public int getCustomerId() {
        return prefs.getInt(KEY_CUSTOMER_ID, Constants.NO_ID);
    }

    /** Clears the session only; remembered credentials are kept for the login form. */
    public void logout() {
        prefs.edit()
                .remove(KEY_LOGGED_IN)
                .remove(KEY_ACCOUNT_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_ROLE)
                .remove(KEY_CUSTOMER_ID)
                .apply();
    }

    /** "Remember me" checked: store credentials. Unchecked: pass false to forget them. */
    public void setRememberMe(boolean remember, String username, String password) {
        if (remember) {
            prefs.edit()
                    .putBoolean(KEY_REMEMBER_ME, true)
                    .putString(KEY_SAVED_USERNAME, username)
                    .putString(KEY_SAVED_PASSWORD, password)
                    .apply();
        } else {
            prefs.edit()
                    .remove(KEY_REMEMBER_ME)
                    .remove(KEY_SAVED_USERNAME)
                    .remove(KEY_SAVED_PASSWORD)
                    .apply();
        }
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getSavedUsername() {
        return prefs.getString(KEY_SAVED_USERNAME, "");
    }

    public String getSavedPassword() {
        return prefs.getString(KEY_SAVED_PASSWORD, "");
    }

    /** True when the app may skip the login screen at start: logged in AND "remember me" on. */
    public boolean shouldAutoLogin() {
        return isLoggedIn() && isRememberMe();
    }
}
