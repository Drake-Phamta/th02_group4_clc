package com.group4.fruitshop.ui.admin.order;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.group4.fruitshop.R;
import com.group4.fruitshop.data.dao.OrderDao;
import com.group4.fruitshop.data.model.Order;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.util.Constants;
import com.group4.fruitshop.util.FormatUtils;
import com.group4.fruitshop.util.OrderStatus;

import java.util.List;

/**
 * Admin order management: lists every order with its customer, filters by status and moves an
 * order forward through Pending -> Confirmed -> Completed.
 */
public class OrderManagementActivity extends BaseActivity
        implements AdminOrderAdapter.OnAdvanceStatusListener {

    private OrderDao orderDao;
    private AdminOrderAdapter adapter;
    private Chip chipAll;
    private Chip chipPending;
    private Chip chipConfirmed;
    private Chip chipCompleted;
    private TextView tvSummary;
    private TextView tvEmpty;

    /** Status shown in the list, null = all statuses. */
    private String statusFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_ADMIN)) {
            return;
        }
        setContentView(R.layout.activity_order_management);
        setupActionBar(R.string.order_title, true);

        orderDao = new OrderDao(this);
        tvSummary = findViewById(R.id.tvOrderSummary);
        tvEmpty = findViewById(R.id.tvOrderEmpty);
        chipAll = findViewById(R.id.chipOrderAll);
        chipPending = findViewById(R.id.chipOrderPending);
        chipConfirmed = findViewById(R.id.chipOrderConfirmed);
        chipCompleted = findViewById(R.id.chipOrderCompleted);

        RecyclerView rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(this);
        rvOrders.setAdapter(adapter);

        ChipGroup chipGroup = findViewById(R.id.chipGroupOrderStatus);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            statusFilter = checkedIds.isEmpty() ? null : statusOfChip(checkedIds.get(0));
            loadOrders();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private String statusOfChip(int chipId) {
        if (chipId == R.id.chipOrderPending) {
            return OrderStatus.PENDING;
        }
        if (chipId == R.id.chipOrderConfirmed) {
            return OrderStatus.CONFIRMED;
        }
        if (chipId == R.id.chipOrderCompleted) {
            return OrderStatus.COMPLETED;
        }
        return null;
    }

    private void loadOrders() {
        List<Order> orders = orderDao.getAllWithCustomer(statusFilter);
        adapter.setOrders(orders);
        tvEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);

        double total = 0;
        for (Order order : orders) {
            total += order.getTotalAmount();
        }
        tvSummary.setText(getResources().getQuantityString(R.plurals.order_summary,
                orders.size(), orders.size(), FormatUtils.formatPrice(total)));

        chipAll.setText(getString(R.string.order_filter_all, orderDao.countAll()));
        chipPending.setText(getString(R.string.order_filter_pending,
                orderDao.countByStatus(OrderStatus.PENDING)));
        chipConfirmed.setText(getString(R.string.order_filter_confirmed,
                orderDao.countByStatus(OrderStatus.CONFIRMED)));
        chipCompleted.setText(getString(R.string.order_filter_completed,
                orderDao.countByStatus(OrderStatus.COMPLETED)));
    }

    @Override
    public void onAdvanceStatus(Order order, String nextStatus) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.order_update_title)
                .setMessage(getString(R.string.order_update_message, order.getId(),
                        order.getStatus(), nextStatus))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> {
                    if (orderDao.updateStatus(order.getId(), nextStatus)) {
                        toast(getString(R.string.order_updated, order.getId(), nextStatus));
                    } else {
                        toast(R.string.order_update_failed);
                    }
                    loadOrders();
                })
                .show();
    }
}
