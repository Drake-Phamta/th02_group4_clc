package com.group4.fruitshop;

import android.content.Context;

import com.group4.fruitshop.data.dao.AccountDao;
import com.group4.fruitshop.data.dao.CustomerDao;
import com.group4.fruitshop.data.dao.FruitDao;
import com.group4.fruitshop.data.model.Account;
import com.group4.fruitshop.data.model.Fruit;
import com.group4.fruitshop.session.SessionManager;
import com.group4.fruitshop.util.Constants;

/**
 * Helpers for tests that work on the demo data of SeedData.
 */
public final class TestData {

    private TestData() {
    }

    /** Logs in a demo account the same way the login screen does. */
    public static void logInAs(Context context, String username, String password) {
        Account account = new AccountDao(context).login(username, password);
        if (account == null) {
            throw new AssertionError("Unknown demo account " + username);
        }
        int customerId = Constants.NO_ID;
        if (!account.isAdmin()) {
            customerId = new CustomerDao(context).getByAccountId(account.getId()).getId();
        }
        new SessionManager(context).createSession(account, customerId);
    }

    public static void logOut(Context context) {
        SessionManager session = new SessionManager(context);
        session.logout();
        session.setRememberMe(false, null, null);
    }

    public static int customerIdOf(Context context, String username, String password) {
        Account account = new AccountDao(context).login(username, password);
        return new CustomerDao(context).getByAccountId(account.getId()).getId();
    }

    public static Fruit fruitNamed(Context context, String name) {
        for (Fruit fruit : new FruitDao(context).getAll()) {
            if (fruit.getName().equals(name)) {
                return fruit;
            }
        }
        throw new AssertionError("No seed fruit named " + name);
    }
}
