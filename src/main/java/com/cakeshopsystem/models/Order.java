package com.cakeshopsystem.models;

import java.time.LocalDateTime;

public class Order {

    private int orderId;
    private LocalDateTime orderDate = LocalDateTime.now();
    private double totalAmount;
    private int userId;
    private int paymentId;

    public Order(){}

    public Order(int orderId, LocalDateTime orderDate, double totalAmount, int userId, int paymentId) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.paymentId = paymentId;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                ", userId=" + userId +
                ", paymentId=" + paymentId +
                '}';
    }
}
