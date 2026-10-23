package com.group4.fruitshop.ui.common;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.group4.fruitshop.session.SessionManager;

/**
 * Parent of every screen. Gives access to the session, the role guard, the action bar
 * back arrow and toasts.
 *
 * <p>Typical screen:
 * <pre>
 * protected void onCreate(Bundle savedInstanceState) {
 *     super.onCreate(savedInstanceState);
 *     if (!requireRole(Constants.ROLE_ADMIN)) return;   // guard first
 *     setContentView(R.layout.activity_xxx);
 *     setupActionBar(R.string.xxx_title, true);
 *     ...
 * }
 * </pre>
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
    }

    /**
     * Returns true when the logged-in user has {@code role} (Constants.ROLE_ADMIN /
     * ROLE_CUSTOMER). Otherwise logs out, opens the login screen and returns false:
     * the caller must then {@code return} immediately.
     */
    protected boolean requireRole(String role) {
        if (session.isLoggedIn() && role.equals(session.getRole())) {
            return true;
        }
        Navigator.logout(this);
        return false;
    }

    /** Sets the action bar title and shows the back arrow when {@code showBack} is true. */
    protected void setupActionBar(@StringRes int title, boolean showBack) {
        setTitle(title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(showBack);
        }
    }

    /** The action bar back arrow behaves like the system back button. */
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    protected void toast(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
