package com.group4.fruitshop.ui.admin.fruit;

import android.os.Bundle;

import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.ui.common.StubScreen;
import com.group4.fruitshop.util.Constants;

/**
 * TODO(TV3 - @TrungNGD195): STUB - replace this whole file with the real add/edit fruit form.
 * Task: docs/tasks/TV3_TrungNGD195_admin_fruit.md
 */
public class FruitFormActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_ADMIN)) {
            return;
        }
        int fruitId = getIntent().getIntExtra(Constants.EXTRA_FRUIT_ID, Constants.NO_ID);
        StubScreen.show(this, fruitId == Constants.NO_ID ? "Add fruit" : "Edit fruit #" + fruitId,
                "TV3 · @TrungNGD195",
                new StubScreen.Action("Back", this::finish));
    }
}
