package com.group4.fruitshop.ui.customer;

import android.os.Bundle;

import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.ui.common.StubScreen;
import com.group4.fruitshop.util.Constants;

/**
 * TODO(TV4 - @qkhanhbe): STUB - replace this whole file with the real order history screen.
 * Task: docs/tasks/TV4_qkhanhbe_customer.md
 */
public class OrderHistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_CUSTOMER)) {
            return;
        }
        StubScreen.show(this, "Order history", "TV4 · @qkhanhbe",
                new StubScreen.Action("Back", this::finish));
    }
}
