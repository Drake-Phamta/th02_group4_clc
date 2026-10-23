package com.group4.fruitshop.ui.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group4.fruitshop.R;
import com.group4.fruitshop.data.model.Fruit;
import com.group4.fruitshop.ui.common.Badges;
import com.group4.fruitshop.util.DateUtils;
import com.group4.fruitshop.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fruit cards for the customer. Expired fruits are dimmed and marked "Cannot order".
 */
public class CustomerFruitAdapter extends RecyclerView.Adapter<CustomerFruitAdapter.ViewHolder> {

    public interface OnFruitClickListener {
        void onFruitClick(Fruit fruit);
    }

    private static final float EXPIRED_ALPHA = 0.5f;

    private final List<Fruit> fruits = new ArrayList<>();
    private final OnFruitClickListener listener;

    public CustomerFruitAdapter(OnFruitClickListener listener) {
        this.listener = listener;
    }

    public void setFruits(List<Fruit> newFruits) {
        fruits.clear();
        fruits.addAll(newFruits);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_fruit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Fruit fruit = fruits.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(fruit.getName());
        holder.tvCategory.setText(fruit.getCategory());
        holder.tvPrice.setText(FormatUtils.formatPrice(fruit.getPrice()));
        holder.tvStock.setText(context.getString(R.string.customer_fruit_stock, fruit.getQuantity()));
        holder.tvExpiry.setText(context.getString(R.string.customer_fruit_expiry,
                DateUtils.toDisplayDate(fruit.getExpiryDate())));
        Badges.bindExpiry(holder.tvBadge, fruit.getExpiryDate());

        boolean expired = fruit.isExpired();
        holder.itemView.setAlpha(expired ? EXPIRED_ALPHA : 1f);
        holder.tvCannotOrder.setVisibility(expired ? View.VISIBLE : View.GONE);

        // Expired fruits stay clickable so the customer can still check their details.
        holder.itemView.setOnClickListener(v -> listener.onFruitClick(fruit));
    }

    @Override
    public int getItemCount() {
        return fruits.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvBadge;
        final TextView tvCategory;
        final TextView tvPrice;
        final TextView tvStock;
        final TextView tvExpiry;
        final TextView tvCannotOrder;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCustomerFruitName);
            tvBadge = itemView.findViewById(R.id.tvCustomerFruitBadge);
            tvCategory = itemView.findViewById(R.id.tvCustomerFruitCategory);
            tvPrice = itemView.findViewById(R.id.tvCustomerFruitPrice);
            tvStock = itemView.findViewById(R.id.tvCustomerFruitStock);
            tvExpiry = itemView.findViewById(R.id.tvCustomerFruitExpiry);
            tvCannotOrder = itemView.findViewById(R.id.tvCustomerFruitCannotOrder);
        }
    }
}
