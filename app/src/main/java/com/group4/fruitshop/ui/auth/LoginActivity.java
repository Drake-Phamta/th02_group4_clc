package com.group4.fruitshop.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import com.group4.fruitshop.data.dao.AccountDao;
import com.group4.fruitshop.data.dao.CustomerDao;
import com.group4.fruitshop.data.model.Account;
import com.group4.fruitshop.data.model.Customer;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.ui.common.Navigator;
import com.group4.fruitshop.ui.common.StubScreen;
import com.group4.fruitshop.util.Constants;

/**
 * TODO(TV2 - @scalliontor): STUB - replace this whole file with the real login screen.
 * Task: docs/tasks/TV2_scalliontor_auth.md
 */
public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (session.isLoggedIn()) {
            Navigator.openHome(this);
            return;
        }
        StubScreen.show(this, "Login", "TV2 · @scalliontor",
                new StubScreen.Action("Demo: log in as admin", () -> demoLogin("admin", "admin123")),
                new StubScreen.Action("Demo: log in as customer",
                        () -> demoLogin("customer", "customer123")),
                new StubScreen.Action("Register",
                        () -> startActivity(new Intent(this, RegisterActivity.class))));
    }

    private void demoLogin(String username, String password) {
        Account account = new AccountDao(this).login(username, password);
        if (account == null) {
            toast("Demo account not found");
            return;
        }
        int customerId = Constants.NO_ID;
        if (!account.isAdmin()) {
            Customer customer = new CustomerDao(this).getByAccountId(account.getId());
            if (customer != null) {
                customerId = customer.getId();
            }
        }
        session.createSession(account, customerId);
        Navigator.openHome(this);
    }
}
