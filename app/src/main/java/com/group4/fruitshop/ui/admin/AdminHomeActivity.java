package com.group4.fruitshop.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.group4.fruitshop.R;
import com.group4.fruitshop.data.dao.FruitDao;
import com.group4.fruitshop.data.dao.OrderDao;
import com.group4.fruitshop.ui.admin.fruit.FruitManagementActivity;
import com.group4.fruitshop.ui.admin.order.OrderManagementActivity;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.ui.common.Navigator;
import com.group4.fruitshop.util.Constants;
import com.group4.fruitshop.util.OrderStatus;

/**
 * Admin dashboard: fruit/order stats and entry points into the admin modules.
 */
public class AdminHomeActivity extends BaseActivity {

    private FruitDao fruitDao;
    private OrderDao orderDao;

    private TextView tvTotalFruits;
    private TextView tvExpiredFruits;
    private TextView tvPendingOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_ADMIN)) {
            return;
        }
        setContentView(R.layout.activity_admin_home);
        setupActionBar(R.string.admin_home_title, false);

        fruitDao = new FruitDao(this);
        orderDao = new OrderDao(this);

        ((TextView) findViewById(R.id.tvAdminHomeWelcome))
                .setText(getString(R.string.admin_home_welcome, session.getUsername()));

        tvTotalFruits = findViewById(R.id.tvAdminHomeTotalFruits);
        tvExpiredFruits = findViewById(R.id.tvAdminHomeExpiredFruits);
        tvPendingOrders = findViewById(R.id.tvAdminHomePendingOrders);

        findViewById(R.id.btnAdminHomeFruitManagement).setOnClickListener(v ->
                startActivity(new Intent(this, FruitManagementActivity.class)));
        findViewById(R.id.btnAdminHomeOrderManagement).setOnClickListener(v ->
                startActivity(new Intent(this, OrderManagementActivity.class)));
        findViewById(R.id.btnAdminHomeLogout).setOnClickListener(v -> confirmLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fruitDao == null) {
            return;
        }
        tvTotalFruits.setText(String.valueOf(fruitDao.countAll()));
        tvExpiredFruits.setText(String.valueOf(fruitDao.countExpired()));
        tvPendingOrders.setText(String.valueOf(orderDao.countByStatus(OrderStatus.PENDING)));
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.admin_home_logout_title)
                .setMessage(R.string.admin_home_logout_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> Navigator.logout(this))
                .show();
    }
}
