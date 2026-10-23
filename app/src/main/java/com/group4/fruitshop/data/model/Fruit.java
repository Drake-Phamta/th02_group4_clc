package com.group4.fruitshop.data.model;

import com.group4.fruitshop.util.DateUtils;

/**
 * Table Fruit(id, name, category, price, quantity, expiryDate, description).
 * {@code expiryDate} is stored as yyyy-MM-dd (see DateUtils).
 */
public class Fruit {

    private int id;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private String expiryDate;
    private String description;

    public Fruit() {
    }

    public Fruit(String name, String category, double price, int quantity,
                 String expiryDate, String description) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.description = description;
    }

    public Fruit(int id, String name, String category, double price, int quantity,
                 String expiryDate, String description) {
        this(name, category, price, quantity, expiryDate, description);
        this.id = id;
    }

    /** Expiry date is before today. */
    public boolean isExpired() {
        return DateUtils.isExpired(expiryDate);
    }

    public boolean isInStock() {
        return quantity > 0;
    }

    /** Customers may order only fruits that are in stock and not expired. */
    public boolean canBeOrdered() {
        return isInStock() && !isExpired();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
