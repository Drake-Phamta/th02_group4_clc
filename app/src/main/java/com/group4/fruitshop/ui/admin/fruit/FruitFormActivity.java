package com.group4.fruitshop.ui.admin.fruit;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.group4.fruitshop.R;
import com.group4.fruitshop.data.dao.FruitDao;
import com.group4.fruitshop.data.model.Fruit;
import com.group4.fruitshop.ui.common.BaseActivity;
import com.group4.fruitshop.util.Constants;
import com.group4.fruitshop.util.DateUtils;

import java.util.List;

/** Add/edit form with field validation and a calendar picker for expiry. */
public class FruitFormActivity extends BaseActivity {

    private FruitDao fruitDao;
    private int fruitId;
    private TextInputLayout nameLayout;
    private TextInputLayout categoryLayout;
    private TextInputLayout priceLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout expiryLayout;
    private EditText nameInput;
    private AutoCompleteTextView categoryInput;
    private EditText priceInput;
    private EditText quantityInput;
    private EditText expiryInput;
    private EditText descriptionInput;
    private String selectedExpiryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireRole(Constants.ROLE_ADMIN)) {
            return;
        }
        setContentView(R.layout.activity_fruit_form);
        fruitId = getIntent().getIntExtra(Constants.EXTRA_FRUIT_ID, Constants.NO_ID);
        setupActionBar(fruitId == Constants.NO_ID ? R.string.fruit_add_title : R.string.fruit_edit_title, true);

        fruitDao = new FruitDao(this);
        nameLayout = findViewById(R.id.fruit_name_layout);
        categoryLayout = findViewById(R.id.fruit_category_layout);
        priceLayout = findViewById(R.id.fruit_price_layout);
        quantityLayout = findViewById(R.id.fruit_quantity_layout);
        expiryLayout = findViewById(R.id.fruit_expiry_layout);
        nameInput = findViewById(R.id.fruit_name_input);
        categoryInput = findViewById(R.id.fruit_category_input);
        priceInput = findViewById(R.id.fruit_price_input);
        quantityInput = findViewById(R.id.fruit_quantity_input);
        expiryInput = findViewById(R.id.fruit_expiry_input);
        descriptionInput = findViewById(R.id.fruit_description_input);

        List<String> categories = fruitDao.getCategories();
        categoryInput.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories));
        expiryInput.setOnClickListener(v -> showDatePicker());
        expiryLayout.setEndIconOnClickListener(v -> showDatePicker());
        findViewById(R.id.fruit_save_button).setOnClickListener(v -> saveFruit());

        if (fruitId != Constants.NO_ID) {
            Fruit fruit = fruitDao.getById(fruitId);
            if (fruit == null) {
                toast(R.string.fruit_not_found);
                finish();
                return;
            }
            bindFruit(fruit);
        }
        updateExpiryWarning();
    }

    private void bindFruit(Fruit fruit) {
        nameInput.setText(fruit.getName());
        categoryInput.setText(fruit.getCategory(), false);
        priceInput.setText(String.valueOf(fruit.getPrice()));
        quantityInput.setText(String.valueOf(fruit.getQuantity()));
        selectedExpiryDate = fruit.getExpiryDate();
        expiryInput.setText(DateUtils.toDisplayDate(selectedExpiryDate));
        descriptionInput.setText(fruit.getDescription());
    }

    private void showDatePicker() {
        int[] date = DateUtils.toPicker(selectedExpiryDate);
        DatePickerDialog picker = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedExpiryDate = DateUtils.fromPicker(year, month, day);
                    expiryInput.setText(DateUtils.toDisplayDate(selectedExpiryDate));
                    expiryLayout.setError(null);
                    updateExpiryWarning();
                }, date[0], date[1], date[2]);
        picker.show();
    }

    private void updateExpiryWarning() {
        boolean past = DateUtils.isValidDate(selectedExpiryDate)
                && DateUtils.getExpiryStatus(selectedExpiryDate) == DateUtils.ExpiryStatus.EXPIRED;
        expiryLayout.setHelperText(getString(past
                ? R.string.fruit_past_date_warning : R.string.fruit_expiry_help));
        expiryLayout.setHelperTextColor(ColorStateList.valueOf(ContextCompat.getColor(this,
                past ? R.color.status_expired : R.color.text_secondary)));
    }

    private void saveFruit() {
        clearErrors();
        String name = nameInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String priceText = priceInput.getText().toString().trim();
        String quantityText = quantityInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        boolean valid = true;

        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.fruit_error_name));
            valid = false;
        }
        if (category.isEmpty()) {
            categoryLayout.setError(getString(R.string.fruit_error_category));
            valid = false;
        }
        double price = 0;
        try {
            price = Double.parseDouble(priceText);
            if (!(price > 0) || Double.isInfinite(price) || Double.isNaN(price)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            priceLayout.setError(getString(R.string.fruit_error_price));
            valid = false;
        }
        int quantity = 0;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            quantityLayout.setError(getString(R.string.fruit_error_quantity));
            valid = false;
        }
        if (!DateUtils.isValidDate(selectedExpiryDate)) {
            expiryLayout.setError(getString(R.string.fruit_error_expiry));
            valid = false;
        }
        if (!valid) return;

        Fruit fruit = new Fruit(name, category, price, quantity, selectedExpiryDate, description);
        boolean saved;
        try {
            if (fruitId == Constants.NO_ID) {
                saved = fruitDao.insert(fruit) != -1;
            } else {
                fruit.setId(fruitId);
                saved = fruitDao.update(fruit) == 1;
            }
        } catch (SQLiteException e) {
            saved = false;
        }
        if (!saved) {
            toast(R.string.fruit_save_failed);
            return;
        }
        toast(fruitId == Constants.NO_ID ? R.string.fruit_added : R.string.fruit_updated);
        finish();
    }

    private void clearErrors() {
        nameLayout.setError(null);
        categoryLayout.setError(null);
        priceLayout.setError(null);
        quantityLayout.setError(null);
        expiryLayout.setError(null);
    }
}
