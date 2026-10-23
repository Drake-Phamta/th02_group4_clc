package com.group4.fruitshop.ui.admin.fruit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.group4.fruitshop.R;
import com.group4.fruitshop.data.model.Fruit;
import com.group4.fruitshop.ui.common.Badges;
import com.group4.fruitshop.util.DateUtils;
import com.group4.fruitshop.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/** RecyclerView adapter for the admin fruit inventory. */
public class AdminFruitAdapter extends RecyclerView.Adapter<AdminFruitAdapter.FruitViewHolder> {

    public interface OnFruitClickListener {
        void onFruitClick(Fruit fruit);
    }

    private final Context context;
    private final OnFruitClickListener listener;
    private final List<Fruit> fruits = new ArrayList<>();

    public AdminFruitAdapter(Context context, OnFruitClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFruits(List<Fruit> items) {
        fruits.clear();
        fruits.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FruitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_fruit, parent, false);
        return new FruitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FruitViewHolder holder, int position) {
        Fruit fruit = fruits.get(position);
        holder.name.setText(fruit.getName());
        holder.category.setText(fruit.getCategory());
        holder.price.setText(FormatUtils.formatPrice(fruit.getPrice()));
        holder.quantity.setText(fruit.getQuantity() == 0
                ? context.getString(R.string.fruit_out_of_stock)
                : context.getString(R.string.fruit_quantity, fruit.getQuantity()));
        holder.expiry.setText(context.getString(R.string.fruit_expiry_line,
                DateUtils.toDisplayDate(fruit.getExpiryDate())));
        Badges.bindExpiry(holder.badge, fruit.getExpiryDate());
        boolean expired = DateUtils.getExpiryStatus(fruit.getExpiryDate())
                == DateUtils.ExpiryStatus.EXPIRED;
        holder.card.setStrokeWidth(expired ? dp(2) : dp(1));
        holder.card.setStrokeColor(ContextCompat.getColor(context,
                expired ? R.color.status_expired : R.color.divider));
        holder.expiry.setTextColor(ContextCompat.getColor(context,
                expired ? R.color.status_expired : R.color.text_secondary));
        holder.itemView.setOnClickListener(v -> listener.onFruitClick(fruit));
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public int getItemCount() {
        return fruits.size();
    }

    static class FruitViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView name;
        final TextView category;
        final TextView price;
        final TextView quantity;
        final TextView expiry;
        final TextView badge;

        FruitViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            name = itemView.findViewById(R.id.fruit_item_name);
            category = itemView.findViewById(R.id.fruit_item_category);
            price = itemView.findViewById(R.id.fruit_item_price);
            quantity = itemView.findViewById(R.id.fruit_item_quantity);
            expiry = itemView.findViewById(R.id.fruit_item_expiry);
            badge = itemView.findViewById(R.id.fruit_item_badge);
        }
    }
}
