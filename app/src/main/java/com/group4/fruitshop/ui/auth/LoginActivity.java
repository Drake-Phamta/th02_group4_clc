package com.group4.fruitshop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.group4.fruitshop.R;
import com.group4.fruitshop.data.dao.AccountDao;
import com.group4.fruitshop.data.dao.CustomerDao;
import com.group4.fruitshop.data.model.Account;
import com.group4.fruitshop.data.model.Customer;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.ui.common.Navigator;
import com.group4.fruitshop.util.Constants;

/**
 * Login screen (launcher). Supports Admin/Customer roles and "remember me" auto-login.
 */
public class LoginActivity extends BaseActivity {

    private AccountDao accountDao;
    private CustomerDao customerDao;

    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private CheckBox cbRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (session.shouldAutoLogin()) {
            Navigator.openHome(this);
            return;
        } else if (session.isLoggedIn()) {
            session.logout();
        }

        setContentView(R.layout.activity_login);

        accountDao = new AccountDao(this);
        customerDao = new CustomerDao(this);

        tilUsername = findViewById(R.id.tilLoginUsername);
        tilPassword = findViewById(R.id.tilLoginPassword);
        etUsername = findViewById(R.id.etLoginUsername);
        etPassword = findViewById(R.id.etLoginPassword);
        cbRememberMe = findViewById(R.id.cbLoginRememberMe);

        if (session.isRememberMe()) {
            etUsername.setText(session.getSavedUsername());
            etPassword.setText(session.getSavedPassword());
            cbRememberMe.setChecked(true);
        }

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tvGoToRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = textOf(etUsername);
        String password = rawTextOf(etPassword);

        boolean valid = true;
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError(getString(R.string.auth_error_username_required));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.auth_error_password_required));
            valid = false;
        }
        if (!valid) {
            return;
        }

        Account account = accountDao.login(username, password);
        if (account == null) {
            tilPassword.setError(getString(R.string.auth_error_invalid_credentials));
            return;
        }

        int customerId = Constants.NO_ID;
        if (!account.isAdmin()) {
            Customer customer = customerDao.getByAccountId(account.getId());
            if (customer == null) {
                tilPassword.setError(getString(R.string.auth_error_missing_profile));
                return;
            }
            customerId = customer.getId();
        }

        session.createSession(account, customerId);
        session.setRememberMe(cbRememberMe.isChecked(), username, password);
        Navigator.openHome(this);
    }

    private static String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private static String rawTextOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }
}
