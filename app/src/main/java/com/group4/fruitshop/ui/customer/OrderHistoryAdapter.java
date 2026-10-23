package com.group4.fruitshop.ui.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group4.fruitshop.R;
import com.group4.fruitshop.data.model.Order;
import com.group4.fruitshop.ui.common.Badges;
import com.group4.fruitshop.util.DateUtils;
import com.group4.fruitshop.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Order cards of the logged-in customer: #id, date, total and status badge.
 * (The Order table has no line items, so the fruits of an order are not shown.)
 */
public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private final List<Order> orders = new ArrayList<>();

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        Context context = holder.itemView.getContext();

        holder.tvId.setText(context.getString(R.string.customer_order_number, order.getId()));
        Badges.bindOrderStatus(holder.tvStatus, order.getStatus());
        holder.tvDate.setText(DateUtils.toDisplayDateTime(order.getOrderDate()));
        holder.tvTotal.setText(FormatUtils.formatPrice(order.getTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvId;
        final TextView tvStatus;
        final TextView tvDate;
        final TextView tvTotal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvCustomerOrderId);
            tvStatus = itemView.findViewById(R.id.tvCustomerOrderStatus);
            tvDate = itemView.findViewById(R.id.tvCustomerOrderDate);
            tvTotal = itemView.findViewById(R.id.tvCustomerOrderTotal);
        }
    }
}
