package com.group4.fruitshop.data.model;

/**
 * Table Customer(id, fullname, phone, email, accountId).
 * Every customer is linked 1-1 to an Account with role Customer.
 */
public class Customer {

    private int id;
    private String fullname;
    private String phone;
    private String email;
    private int accountId;

    public Customer() {
    }

    public Customer(int id, String fullname, String phone, String email, int accountId) {
        this.id = id;
        this.fullname = fullname;
        this.phone = phone;
        this.email = email;
        this.accountId = accountId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}
