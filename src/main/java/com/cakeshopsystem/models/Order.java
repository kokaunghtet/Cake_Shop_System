package com.cakeshopsystem.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Order {

    private int orderId;
    private LocalDateTime orderDate = LocalDateTime.now();
    private double totalAmount;
    private User user;
    private Payment payment;

    public Order(){}

    public Order(int orderId, LocalDateTime orderDate, double totalAmount, User user, Payment payment) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.user = user;
        this.payment = payment;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                ", user=" + user +
                ", payment=" + payment +
                '}';
    }
}
