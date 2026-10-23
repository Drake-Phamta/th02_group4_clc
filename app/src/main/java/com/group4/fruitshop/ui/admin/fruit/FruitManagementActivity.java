package com.group4.fruitshop.ui.admin.fruit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.group4.fruitshop.R;
import com.group4.fruitshop.data.dao.FruitDao;
import com.group4.fruitshop.data.model.Fruit;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.util.Constants;
import com.group4.fruitshop.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/** Admin list, search, expiry filters and fruit actions. */
public class FruitManagementActivity extends BaseActivity
        implements AdminFruitAdapter.OnFruitClickListener {

    private FruitDao fruitDao;
    private AdminFruitAdapter adapter;
    private EditText searchInput;
    private TextView summary;
    private TextView empty;
    private ChipGroup filterGroup;
    private String expiryFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_ADMIN)) {
            return;
        }
        setContentView(R.layout.activity_fruit_management);
        setupActionBar(R.string.fruit_title, true);

        fruitDao = new FruitDao(this);
        searchInput = findViewById(R.id.fruit_search_input);
        summary = findViewById(R.id.fruit_summary);
        empty = findViewById(R.id.fruit_empty);
        filterGroup = findViewById(R.id.fruit_filter_group);

        RecyclerView list = findViewById(R.id.fruit_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminFruitAdapter(this, this);
        list.setAdapter(adapter);

        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadFruits();
            }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });
        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.get(0) == R.id.fruit_filter_all) {
                expiryFilter = "all";
            } else if (checkedIds.get(0) == R.id.fruit_filter_expired) {
                expiryFilter = "expired";
            } else {
                expiryFilter = "soon";
            }
            loadFruits();
        });

        FloatingActionButton add = findViewById(R.id.fruit_add_button);
        add.setOnClickListener(v -> startActivity(new Intent(this, FruitFormActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fruitDao != null) loadFruits();
    }

    private void loadFruits() {
        List<Fruit> matches = fruitDao.search(searchInput.getText().toString());
        List<Fruit> visible = new ArrayList<>();
        int expiredCount = 0;
        int soonCount = 0;
        for (Fruit fruit : matches) {
            DateUtils.ExpiryStatus status = DateUtils.getExpiryStatus(fruit.getExpiryDate());
            if (status == DateUtils.ExpiryStatus.EXPIRED) expiredCount++;
            if (status == DateUtils.ExpiryStatus.EXPIRING_SOON) soonCount++;
            if ("expired".equals(expiryFilter) && status != DateUtils.ExpiryStatus.EXPIRED) continue;
            if ("soon".equals(expiryFilter) && status != DateUtils.ExpiryStatus.EXPIRING_SOON) continue;
            visible.add(fruit);
        }
        int shownExpired = 0;
        for (Fruit fruit : visible) {
            if (fruit.isExpired()) shownExpired++;
        }
        adapter.setFruits(visible);
        summary.setText(getString(R.string.fruit_summary, visible.size(), shownExpired));
        empty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        ((Chip) findViewById(R.id.fruit_filter_all))
                .setText(getString(R.string.fruit_filter_all, matches.size()));
        ((Chip) findViewById(R.id.fruit_filter_expired))
                .setText(getString(R.string.fruit_filter_expired, expiredCount));
        ((Chip) findViewById(R.id.fruit_filter_soon))
                .setText(getString(R.string.fruit_filter_soon_count, soonCount));
    }

    @Override
    public void onFruitClick(Fruit fruit) {
        String description = fruit.getDescription() == null || fruit.getDescription().trim().isEmpty()
                ? getString(R.string.fruit_no_description) : fruit.getDescription();
        String details = getString(R.string.fruit_detail_message,
                fruit.getName(), fruit.getCategory(),
                com.group4.fruitshop.util.FormatUtils.formatPrice(fruit.getPrice()),
                fruit.getQuantity() == 0 ? getString(R.string.fruit_out_of_stock) : fruit.getQuantity(),
                DateUtils.toDisplayDate(fruit.getExpiryDate()),
                expiryLabel(fruit), description);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.fruit_detail_title)
                .setMessage(details)
                .setNegativeButton(R.string.fruit_action_close, null)
                .setNeutralButton(R.string.fruit_action_edit, (dialog, which) ->
                        startActivity(new Intent(this, FruitFormActivity.class)
                                .putExtra(Constants.EXTRA_FRUIT_ID, fruit.getId())))
                .setPositiveButton(R.string.fruit_action_delete, (dialog, which) -> confirmDelete(fruit))
                .show();
    }

    private String expiryLabel(Fruit fruit) {
        switch (DateUtils.getExpiryStatus(fruit.getExpiryDate())) {
            case EXPIRED: return getString(R.string.fruit_status_expired);
            case EXPIRING_SOON:
                int days = (int) DateUtils.daysUntil(fruit.getExpiryDate());
                return days == 0 ? getString(R.string.fruit_status_today)
                        : getString(R.string.fruit_status_days, days);
            default: return getString(R.string.fruit_status_fresh);
        }
    }

    private void confirmDelete(Fruit fruit) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.fruit_delete_title)
                .setMessage(getString(R.string.fruit_delete_message, fruit.getName()))
                .setNegativeButton(R.string.fruit_action_cancel, null)
                .setPositiveButton(R.string.fruit_action_delete, (dialog, which) -> {
                    if (fruitDao.delete(fruit.getId()) == 1) {
                        toast(getString(R.string.fruit_deleted, fruit.getName()));
                    } else {
                        toast(R.string.fruit_delete_failed);
                    }
                    loadFruits();
                })
                .show();
    }
}
