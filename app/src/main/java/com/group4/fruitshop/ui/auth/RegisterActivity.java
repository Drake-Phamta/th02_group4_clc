package com.group4.fruitshop.ui.auth;

import android.os.Bundle;

import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.ui.common.StubScreen;

/**
 * TODO(TV2 - @scalliontor): STUB - replace this whole file with the real register screen.
 * Task: docs/tasks/TV2_scalliontor_auth.md
 */
public class RegisterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StubScreen.show(this, "Register", "TV2 · @scalliontor",
                new StubScreen.Action("Back to login", this::finish));
    }
}
