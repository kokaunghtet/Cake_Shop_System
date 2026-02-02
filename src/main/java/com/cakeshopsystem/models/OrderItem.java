package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.OrderOption;

public class OrderItem {

    private int orderItemId;
    private int orderId;
    private Integer productId;
    private Integer drinkId;
    private OrderOption orderOption;
    private int quantity;
    private double unitPrice;
    private double lineTotal;

    public OrderItem() {
    }

    public OrderItem(int orderItemId, int orderId, Integer productId, Integer drinkId, OrderOption orderOption, int quantity, double unitPrice, double lineTotal) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.drinkId = drinkId;
        this.orderOption = orderOption;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getDrinkId() {
        return drinkId;
    }

    public void setDrinkId(Integer drinkId) {
        this.drinkId = drinkId;
    }

    public OrderOption getOrderOption() {
        return orderOption;
    }

    public void setOrderOption(OrderOption orderOption) {
        this.orderOption = orderOption;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getName() {
        return "";
    }

    public String getPrice() {
        return "";
    }
}
