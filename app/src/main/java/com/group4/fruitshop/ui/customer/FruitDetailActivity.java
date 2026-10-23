package com.group4.fruitshop.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.group4.fruitshop.R;
import com.group4.fruitshop.data.dao.FruitDao;
import com.group4.fruitshop.data.dao.OrderDao;
import com.group4.fruitshop.data.model.Fruit;
import com.group4.fruitshop.ui.common.Badges;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.util.Constants;
import com.group4.fruitshop.util.DateUtils;
import com.group4.fruitshop.util.FormatUtils;

/**
 * Fruit detail and ordering. The customer picks a quantity (1..stock) and sees the total
 * update live. Expired or out-of-stock fruits cannot be ordered: the Order button is disabled
 * here and OrderDao.placeOrder() rejects them as well.
 */
public class FruitDetailActivity extends BaseActivity {

    private FruitDao fruitDao;
    private OrderDao orderDao;
    private int fruitId;
    private Fruit fruit;

    private TextView tvName;
    private TextView tvBadge;
    private TextView tvPrice;
    private TextView tvCategory;
    private TextView tvStock;
    private TextView tvExpiry;
    private TextView tvDescription;
    private TextView tvQuantityHint;
    private TextView tvWarning;
    private TextView tvTotal;
    private EditText etQuantity;
    private Button btnMinus;
    private Button btnPlus;
    private Button btnOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_CUSTOMER)) {
            return;
        }
        setContentView(R.layout.activity_fruit_detail);
        setupActionBar(R.string.customer_detail_title, true);

        fruitDao = new FruitDao(this);
        orderDao = new OrderDao(this);
        fruitId = getIntent().getIntExtra(Constants.EXTRA_FRUIT_ID, Constants.NO_ID);

        tvName = findViewById(R.id.tvCustomerDetailName);
        tvBadge = findViewById(R.id.tvCustomerDetailBadge);
        tvPrice = findViewById(R.id.tvCustomerDetailPrice);
        tvCategory = findViewById(R.id.tvCustomerDetailCategory);
        tvStock = findViewById(R.id.tvCustomerDetailStock);
        tvExpiry = findViewById(R.id.tvCustomerDetailExpiry);
        tvDescription = findViewById(R.id.tvCustomerDetailDescription);
        tvQuantityHint = findViewById(R.id.tvCustomerQuantityHint);
        tvWarning = findViewById(R.id.tvCustomerDetailWarning);
        tvTotal = findViewById(R.id.tvCustomerDetailTotal);
        etQuantity = findViewById(R.id.etCustomerQuantity);
        btnMinus = findViewById(R.id.btnCustomerQuantityMinus);
        btnPlus = findViewById(R.id.btnCustomerQuantityPlus);
        btnOrder = findViewById(R.id.btnCustomerOrder);

        btnMinus.setOnClickListener(v -> changeQuantity(-1));
        btnPlus.setOnClickListener(v -> changeQuantity(1));
        btnOrder.setOnClickListener(v -> onOrderClicked());
        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateTotal();
            }
        });

        if (loadFruit()) {
            setQuantity(1);
        }
    }

    /** Reads the fruit again and refreshes the screen. Closes the screen if it no longer exists. */
    private boolean loadFruit() {
        fruit = fruitDao.getById(fruitId);
        if (fruit == null) {
            toast(R.string.customer_order_not_found);
            finish();
            return false;
        }
        bindFruit();
        return true;
    }

    private void bindFruit() {
        tvName.setText(fruit.getName());
        Badges.bindExpiry(tvBadge, fruit.getExpiryDate());
        tvPrice.setText(FormatUtils.formatPrice(fruit.getPrice()));
        tvCategory.setText(fruit.getCategory());
        tvStock.setText(String.valueOf(fruit.getQuantity()));
        tvExpiry.setText(getString(R.string.customer_detail_expiry_value,
                DateUtils.toDisplayDate(fruit.getExpiryDate()), expiryRemaining()));
        tvExpiry.setTextColor(ContextCompat.getColor(this,
                fruit.isExpired() ? R.color.status_expired : R.color.text_primary));
        tvDescription.setText(TextUtils.isEmpty(fruit.getDescription())
                ? getString(R.string.customer_detail_no_description) : fruit.getDescription());

        boolean orderable = fruit.canBeOrdered();
        if (fruit.isExpired()) {
            tvWarning.setText(R.string.customer_order_expired);
            tvWarning.setVisibility(View.VISIBLE);
        } else if (!fruit.isInStock()) {
            tvWarning.setText(R.string.customer_detail_out_of_stock);
            tvWarning.setVisibility(View.VISIBLE);
        } else {
            tvWarning.setVisibility(View.GONE);
        }
        btnOrder.setEnabled(orderable);
        btnMinus.setEnabled(orderable);
        btnPlus.setEnabled(orderable);
        etQuantity.setEnabled(orderable);

        // Keep the chosen quantity valid when the stock changed.
        int quantity = readQuantity();
        if (quantity > fruit.getQuantity()) {
            setQuantity(Math.max(1, fruit.getQuantity()));
        } else {
            updateTotal();
        }
    }

    /** "5 days left", "last day to order" or "Expired". */
    private String expiryRemaining() {
        long days = DateUtils.daysUntil(fruit.getExpiryDate());
        if (days < 0) {
            return getString(R.string.expiry_expired);
        }
        if (days == 0) {
            return getString(R.string.customer_detail_expires_today);
        }
        return getResources().getQuantityString(R.plurals.customer_detail_days_left,
                (int) days, (int) days);
    }

    private void changeQuantity(int delta) {
        int quantity = readQuantity() + delta;
        if (quantity < 1) {
            setQuantity(1);
        } else if (quantity > fruit.getQuantity()) {
            setQuantity(fruit.getQuantity());
            toast(getString(R.string.customer_quantity_max, fruit.getQuantity()));
        } else {
            setQuantity(quantity);
        }
    }

    private void setQuantity(int quantity) {
        String text = String.valueOf(quantity);
        etQuantity.setText(text);
        etQuantity.setSelection(text.length());
    }

    /** The typed quantity, or 0 when the field is empty. */
    private int readQuantity() {
        String text = etQuantity.getText().toString().trim();
        if (text.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Total = price x quantity, plus a hint that turns red when the quantity is out of range. */
    private void updateTotal() {
        if (fruit == null) {
            return;
        }
        int quantity = readQuantity();
        tvTotal.setText(FormatUtils.formatPrice(fruit.getPrice() * quantity));

        int hintColor = R.color.text_secondary;
        String hint = getString(R.string.customer_quantity_range, fruit.getQuantity());
        if (!fruit.canBeOrdered()) {
            hint = "";
        } else if (quantity < 1) {
            hint = getString(R.string.customer_order_invalid_quantity);
            hintColor = R.color.status_expired;
        } else if (quantity > fruit.getQuantity()) {
            hint = getString(R.string.customer_order_out_of_stock, fruit.getQuantity());
            hintColor = R.color.status_expired;
        }
        tvQuantityHint.setText(hint);
        tvQuantityHint.setTextColor(ContextCompat.getColor(this, hintColor));
    }

    private void onOrderClicked() {
        int quantity = readQuantity();
        if (fruit.isExpired()) {
            toast(R.string.customer_order_expired);
            return;
        }
        if (quantity < 1) {
            toast(R.string.customer_order_invalid_quantity);
            return;
        }
        if (quantity > fruit.getQuantity()) {
            toast(getString(R.string.customer_order_out_of_stock, fruit.getQuantity()));
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.customer_confirm_title)
                .setMessage(getString(R.string.customer_confirm_message, fruit.getName(), quantity,
                        FormatUtils.formatPrice(fruit.getPrice() * quantity)))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.customer_action_order, (dialog, which) -> placeOrder(quantity))
                .show();
    }

    private void placeOrder(int quantity) {
        OrderDao.PlaceOrderResult result =
                orderDao.placeOrder(session.getCustomerId(), fruitId, quantity);
        if (result == OrderDao.PlaceOrderResult.SUCCESS) {
            toast(R.string.customer_order_success);
            startActivity(new Intent(this, OrderHistoryActivity.class));
            finish();
            return;
        }

        // Rejected: the fruit may have changed meanwhile (stock, expiry, deleted), so reload it.
        if (!loadFruit()) {
            return;
        }
        switch (result) {
            case INVALID_QUANTITY:
                toast(R.string.customer_order_invalid_quantity);
                break;
            case FRUIT_NOT_FOUND:
                toast(R.string.customer_order_not_found);
                finish();
                break;
            case EXPIRED:
                toast(R.string.customer_order_expired);
                break;
            case OUT_OF_STOCK:
                toast(getString(R.string.customer_order_out_of_stock, fruit.getQuantity()));
                break;
            default:
                toast(R.string.customer_order_failed);
                break;
        }
    }
}
