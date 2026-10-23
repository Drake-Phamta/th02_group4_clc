package com.group4.fruitshop.data.model;

import com.group4.fruitshop.util.Constants;

/**
 * Table Account(id, username, password, role).
 * {@code password} holds the SHA-256 hash, {@code role} is Constants.ROLE_ADMIN or ROLE_CUSTOMER.
 */
public class Account {

    private int id;
    private String username;
    private String password;
    private String role;

    public Account() {
    }

    public Account(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equals(role);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
