package com.group4.fruitshop.ui.admin.order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group4.fruitshop.R;
import com.group4.fruitshop.data.model.Order;
import com.group4.fruitshop.ui.common.Badges;
import com.group4.fruitshop.util.DateUtils;
import com.group4.fruitshop.util.FormatUtils;
import com.group4.fruitshop.util.OrderStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Order cards for the admin. The action button moves the order to its next status.
 */
public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    public interface OnAdvanceStatusListener {
        void onAdvanceStatus(Order order, String nextStatus);
    }

    private final List<Order> orders = new ArrayList<>();
    private final OnAdvanceStatusListener listener;

    public AdminOrderAdapter(OnAdvanceStatusListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        Context context = holder.itemView.getContext();

        holder.tvId.setText(context.getString(R.string.order_number, order.getId()));
        Badges.bindOrderStatus(holder.tvStatus, order.getStatus());
        String phone = order.getCustomerPhone();
        holder.tvCustomer.setText(phone == null || phone.isEmpty()
                ? order.getCustomerName()
                : context.getString(R.string.order_customer, order.getCustomerName(), phone));
        holder.tvDate.setText(DateUtils.toDisplayDateTime(order.getOrderDate()));
        holder.tvTotal.setText(FormatUtils.formatPrice(order.getTotalAmount()));

        String next = OrderStatus.next(order.getStatus());
        if (next == null) {
            holder.btnAdvance.setVisibility(View.GONE);
        } else {
            holder.btnAdvance.setVisibility(View.VISIBLE);
            holder.btnAdvance.setText(OrderStatus.CONFIRMED.equals(next)
                    ? R.string.order_action_confirm : R.string.order_action_complete);
            holder.btnAdvance.setOnClickListener(v -> listener.onAdvanceStatus(order, next));
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvId;
        final TextView tvStatus;
        final TextView tvCustomer;
        final TextView tvDate;
        final TextView tvTotal;
        final Button btnAdvance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvCustomer = itemView.findViewById(R.id.tvOrderCustomer);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnAdvance = itemView.findViewById(R.id.btnAdvanceStatus);
        }
    }
}
