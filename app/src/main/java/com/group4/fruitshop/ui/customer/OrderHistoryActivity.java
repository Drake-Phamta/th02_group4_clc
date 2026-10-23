package com.group4.fruitshop.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group4.fruitshop.R;
import com.group4.fruitshop.data.dao.OrderDao;
import com.group4.fruitshop.data.model.Order;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.util.Constants;
import com.group4.fruitshop.util.FormatUtils;

import java.util.List;

/**
 * Order history of the logged-in customer, newest first, with a summary of the total spent.
 */
public class OrderHistoryActivity extends BaseActivity {

    private OrderDao orderDao;
    private OrderHistoryAdapter adapter;
    private TextView tvSummary;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_CUSTOMER)) {
            return;
        }
        setContentView(R.layout.activity_order_history);
        setupActionBar(R.string.customer_history_title, true);

        orderDao = new OrderDao(this);
        tvSummary = findViewById(R.id.tvCustomerHistorySummary);
        tvEmpty = findViewById(R.id.tvCustomerHistoryEmpty);

        RecyclerView rvOrders = findViewById(R.id.rvCustomerOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderHistoryAdapter();
        rvOrders.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Skip when the role guard already sent the user back to Login.
        if (isFinishing()) {
            return;
        }
        // The admin may have changed a status meanwhile, so always reload.
        loadOrders();
    }

    private void loadOrders() {
        List<Order> orders = orderDao.getByCustomer(session.getCustomerId());
        adapter.setOrders(orders);
        tvEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);

        double total = 0;
        for (Order order : orders) {
            total += order.getTotalAmount();
        }
        tvSummary.setText(getResources().getQuantityString(R.plurals.customer_history_summary,
                orders.size(), orders.size(), FormatUtils.formatPrice(total)));
    }
}
