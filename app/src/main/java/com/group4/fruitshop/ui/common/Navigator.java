package com.group4.fruitshop.ui.common;

import android.app.Activity;
import android.content.Intent;

import com.group4.fruitshop.session.SessionManager;
import com.group4.fruitshop.ui.admin.AdminHomeActivity;
import com.group4.fruitshop.ui.auth.LoginActivity;
import com.group4.fruitshop.ui.customer.CustomerHomeActivity;

/**
 * Navigation shared by all modules: role-based home and logout.
 */
public final class Navigator {

    private Navigator() {
    }

    /**
     * Opens the home screen of the logged-in role (Admin -> AdminHomeActivity,
     * Customer -> CustomerHomeActivity) as a new task and closes {@code activity}.
     * Call it right after SessionManager.createSession(...).
     */
    public static void openHome(Activity activity) {
        SessionManager session = new SessionManager(activity);
        Class<?> home;
        if (!session.isLoggedIn()) {
            home = LoginActivity.class;
        } else if (session.isAdmin()) {
            home = AdminHomeActivity.class;
        } else {
            home = CustomerHomeActivity.class;
        }
        startClearTask(activity, home);
    }

    /** Clears the session and returns to the login screen, clearing the back stack. */
    public static void logout(Activity activity) {
        new SessionManager(activity).logout();
        startClearTask(activity, LoginActivity.class);
    }

    private static void startClearTask(Activity activity, Class<?> target) {
        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
