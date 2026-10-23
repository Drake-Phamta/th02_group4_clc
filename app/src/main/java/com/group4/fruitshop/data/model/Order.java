package com.group4.fruitshop.data.model;

/**
 * Table Order(id, customerId, orderDate, totalAmount, status).
 * {@code orderDate} is yyyy-MM-dd HH:mm:ss, {@code status} is one of OrderStatus.ALL.
 *
 * <p>{@code customerName} / {@code customerPhone} are NOT columns of the Order table: they are
 * filled only by OrderDao queries that join Customer (admin order list).
 */
public class Order {

    private int id;
    private int customerId;
    private String orderDate;
    private double totalAmount;
    private String status;

    private String customerName;
    private String customerPhone;

    public Order() {
    }

    public Order(int id, int customerId, String orderDate, double totalAmount, String status) {
        this.id = id;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
}
