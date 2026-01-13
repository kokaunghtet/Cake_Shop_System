package com.cakeshopsystem.models;

import java.time.LocalDateTime;

public class Order {

    private int orderId;
    private LocalDateTime orderDate = LocalDateTime.now();
    private double subTotal;
    private double discount;
    private double grandTotal;
    private Integer memberId;
    private int userId;
    private int paymentId;
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Order(){}

    public Order(int orderId, LocalDateTime orderDate, double subTotal, double discount, double grandTotal, Integer memberId, int userId, int paymentId, LocalDateTime updatedAt) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.subTotal = subTotal;
        this.discount = discount;
        this.grandTotal = grandTotal;
        this.memberId = memberId;
        this.userId = userId;
        this.paymentId = paymentId;
        this.updatedAt = updatedAt;
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

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderDate=" + orderDate +
                ", subTotal=" + subTotal +
                ", discount=" + discount +
                ", grandTotal=" + grandTotal +
                ", memberId=" + memberId +
                ", userId=" + userId +
                ", paymentId=" + paymentId +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
