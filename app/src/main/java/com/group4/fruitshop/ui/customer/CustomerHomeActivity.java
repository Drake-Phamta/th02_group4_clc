package com.group4.fruitshop.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.group4.fruitshop.R;
import com.group4.fruitshop.data.dao.CustomerDao;
import com.group4.fruitshop.data.dao.FruitDao;
import com.group4.fruitshop.data.model.Customer;
import com.group4.fruitshop.data.model.Fruit;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.ui.common.Navigator;
import com.group4.fruitshop.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer home: greets the customer and lists in-stock fruits, filtered live by name or
 * category. Expired fruits are shown last and dimmed; "Hide expired" removes them.
 */
public class CustomerHomeActivity extends BaseActivity
        implements CustomerFruitAdapter.OnFruitClickListener {

    private FruitDao fruitDao;
    private CustomerFruitAdapter adapter;
    private EditText etSearch;
    private Chip chipHideExpired;
    private TextView tvCount;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_CUSTOMER)) {
            return;
        }
        setContentView(R.layout.activity_customer_home);
        setupActionBar(R.string.customer_home_title, false);

        fruitDao = new FruitDao(this);
        etSearch = findViewById(R.id.etCustomerSearch);
        chipHideExpired = findViewById(R.id.chipCustomerHideExpired);
        tvCount = findViewById(R.id.tvCustomerFruitCount);
        tvEmpty = findViewById(R.id.tvCustomerFruitEmpty);

        Customer customer = new CustomerDao(this).getById(session.getCustomerId());
        String name = customer != null ? customer.getFullname() : session.getUsername();
        ((TextView) findViewById(R.id.tvCustomerGreeting))
                .setText(getString(R.string.customer_greeting, name));

        RecyclerView rvFruits = findViewById(R.id.rvCustomerFruits);
        rvFruits.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerFruitAdapter(this);
        rvFruits.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadFruits();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        chipHideExpired.setOnCheckedChangeListener((chip, checked) -> loadFruits());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Skip when the role guard already sent the user back to Login.
        if (isFinishing()) {
            return;
        }
        // Stock changes after an order, so always reload.
        loadFruits();
    }

    private void loadFruits() {
        List<Fruit> fruits = fruitDao.searchInStock(etSearch.getText().toString());
        if (chipHideExpired.isChecked()) {
            List<Fruit> notExpired = new ArrayList<>();
            for (Fruit fruit : fruits) {
                if (!fruit.isExpired()) {
                    notExpired.add(fruit);
                }
            }
            fruits = notExpired;
        }
        adapter.setFruits(fruits);
        tvEmpty.setVisibility(fruits.isEmpty() ? View.VISIBLE : View.GONE);
        tvCount.setText(getResources().getQuantityString(R.plurals.customer_fruit_count,
                fruits.size(), fruits.size()));
    }

    @Override
    public void onFruitClick(Fruit fruit) {
        startActivity(new Intent(this, FruitDetailActivity.class)
                .putExtra(Constants.EXTRA_FRUIT_ID, fruit.getId()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_customer_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuCustomerOrderHistory) {
            startActivity(new Intent(this, OrderHistoryActivity.class));
            return true;
        }
        if (id == R.id.menuCustomerLogout) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.customer_logout_title)
                .setMessage(R.string.customer_logout_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_logout, (dialog, which) -> Navigator.logout(this))
                .show();
    }
}
