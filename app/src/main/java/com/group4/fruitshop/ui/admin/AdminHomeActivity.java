package com.group4.fruitshop.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import com.group4.fruitshop.ui.admin.fruit.FruitManagementActivity;
import com.group4.fruitshop.ui.admin.order.OrderManagementActivity;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.ui.common.Navigator;
import com.group4.fruitshop.ui.common.StubScreen;
import com.group4.fruitshop.util.Constants;

/**
 * TODO(TV2 - @scalliontor): STUB - replace this whole file with the real admin dashboard.
 * Task: docs/tasks/TV2_scalliontor_auth.md
 */
public class AdminHomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_ADMIN)) {
            return;
        }
        StubScreen.show(this, "Admin home", "TV2 · @scalliontor",
                new StubScreen.Action("Fruit management",
                        () -> startActivity(new Intent(this, FruitManagementActivity.class))),
                new StubScreen.Action("Order management",
                        () -> startActivity(new Intent(this, OrderManagementActivity.class))),
                new StubScreen.Action("Logout", () -> Navigator.logout(this)));
    }
}
