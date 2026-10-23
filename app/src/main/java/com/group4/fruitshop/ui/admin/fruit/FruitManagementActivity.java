package com.group4.fruitshop.ui.admin.fruit;

import android.content.Intent;
import android.os.Bundle;

import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.ui.common.StubScreen;
import com.group4.fruitshop.util.Constants;

/**
 * TODO(TV3 - @TrungNGD195): STUB - replace this whole file with the real fruit management screen.
 * Task: docs/tasks/TV3_TrungNGD195_admin_fruit.md
 */
public class FruitManagementActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_ADMIN)) {
            return;
        }
        StubScreen.show(this, "Fruit management", "TV3 · @TrungNGD195",
                new StubScreen.Action("Add fruit",
                        () -> startActivity(new Intent(this, FruitFormActivity.class))),
                new StubScreen.Action("Edit fruit #1",
                        () -> startActivity(new Intent(this, FruitFormActivity.class)
                                .putExtra(Constants.EXTRA_FRUIT_ID, 1))),
                new StubScreen.Action("Back", this::finish));
    }
}
