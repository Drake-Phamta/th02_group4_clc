package com.group4.fruitshop.ui.customer;

import android.content.Intent;
import android.os.Bundle;

import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.ui.common.Navigator;
import com.group4.fruitshop.ui.common.StubScreen;
import com.group4.fruitshop.util.Constants;

/**
 * TODO(TV4 - @qkhanhbe): STUB - replace this whole file with the real customer home (fruit list).
 * Task: docs/tasks/TV4_qkhanhbe_customer.md
 */
public class CustomerHomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_CUSTOMER)) {
            return;
        }
        StubScreen.show(this, "Customer home", "TV4 · @qkhanhbe",
                new StubScreen.Action("Fruit detail #1",
                        () -> startActivity(new Intent(this, FruitDetailActivity.class)
                                .putExtra(Constants.EXTRA_FRUIT_ID, 1))),
                new StubScreen.Action("Order history",
                        () -> startActivity(new Intent(this, OrderHistoryActivity.class))),
                new StubScreen.Action("Logout", () -> Navigator.logout(this)));
    }
}
