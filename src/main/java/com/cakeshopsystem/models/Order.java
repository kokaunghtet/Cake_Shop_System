package com.cakeshopsystem.models;

import java.time.LocalDate;

public class Order {

    private int orderId;
    private LocalDate orderDate;
    private double totalAmount;
    private int userId;
    private int paymentId;

    public Order(){}

    public Order(LocalDate orderDate, int orderId, int paymentId, double totalAmount, int userId) {
        this.orderDate = orderDate;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.totalAmount = totalAmount;
        this.userId = userId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
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

    @Override
    public String toString() {
        return "Order{" +
                "orderDate=" + orderDate +
                ", orderId=" + orderId +
                ", totalAmount=" + totalAmount +
                ", userId=" + userId +
                ", paymentId=" + paymentId +
                '}';
    }
}
