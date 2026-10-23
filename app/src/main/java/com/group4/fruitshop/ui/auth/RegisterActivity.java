package com.group4.fruitshop.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.group4.fruitshop.R;
import com.group4.fruitshop.data.dao.AccountDao;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.util.Constants;

import java.util.regex.Pattern;

/**
 * Registration screen for new Customer accounts.
 */
public class RegisterActivity extends BaseActivity {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    private AccountDao accountDao;

    private TextInputLayout tilFullname;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputLayout tilPhone;
    private TextInputLayout tilEmail;

    private TextInputEditText etFullname;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupActionBar(R.string.auth_register_title, true);

        accountDao = new AccountDao(this);

        tilFullname = findViewById(R.id.tilRegisterFullname);
        tilUsername = findViewById(R.id.tilRegisterUsername);
        tilPassword = findViewById(R.id.tilRegisterPassword);
        tilConfirmPassword = findViewById(R.id.tilRegisterConfirmPassword);
        tilPhone = findViewById(R.id.tilRegisterPhone);
        tilEmail = findViewById(R.id.tilRegisterEmail);

        etFullname = findViewById(R.id.etRegisterFullname);
        etUsername = findViewById(R.id.etRegisterUsername);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        etPhone = findViewById(R.id.etRegisterPhone);
        etEmail = findViewById(R.id.etRegisterEmail);

        findViewById(R.id.btnRegister).setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        tilFullname.setError(null);
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilPhone.setError(null);
        tilEmail.setError(null);

        String fullname = textOf(etFullname);
        String username = textOf(etUsername);
        String password = rawTextOf(etPassword);
        String confirmPassword = rawTextOf(etConfirmPassword);
        String phone = textOf(etPhone);
        String email = textOf(etEmail);

        boolean valid = true;

        if (TextUtils.isEmpty(fullname)) {
            tilFullname.setError(getString(R.string.auth_error_fullname_required));
            valid = false;
        }

        if (username.length() < 3) {
            tilUsername.setError(getString(R.string.auth_error_username_length));
            valid = false;
        } else if (username.contains(" ")) {
            tilUsername.setError(getString(R.string.auth_error_username_spaces));
            valid = false;
        } else if (accountDao.isUsernameTaken(username)) {
            tilUsername.setError(getString(R.string.auth_error_username_taken));
            valid = false;
        }

        if (password.length() < 6) {
            tilPassword.setError(getString(R.string.auth_error_password_length));
            valid = false;
        }

        if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError(getString(R.string.auth_error_confirm_mismatch));
            valid = false;
        }

        if (!TextUtils.isEmpty(phone) && !PHONE_PATTERN.matcher(phone).matches()) {
            tilPhone.setError(getString(R.string.auth_error_phone_invalid));
            valid = false;
        }

        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.auth_error_email_invalid));
            valid = false;
        }

        if (!valid) {
            return;
        }

        int customerId = accountDao.registerCustomer(username, password, fullname,
                TextUtils.isEmpty(phone) ? null : phone, TextUtils.isEmpty(email) ? null : email);
        if (customerId == Constants.NO_ID) {
            tilUsername.setError(getString(R.string.auth_error_username_taken));
            return;
        }

        toast(getString(R.string.auth_register_success));
        finish();
    }

    private static String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private static String rawTextOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }
}
