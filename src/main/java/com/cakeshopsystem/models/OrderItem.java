package com.cakeshopsystem.models;

public class OrderItem {

    private int orderItemId;
    private Order order;
    private Product product;
    private Drink drink;
    private int quantity;
    private double unitPrice;

    public OrderItem(){}

    public OrderItem(int orderItemId, Order order, Product product, Drink drink, int quantity, double unitPrice) {
        this.orderItemId = orderItemId;
        this.order = order;
        this.product = product;
        this.drink = drink;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Drink getDrink() {
        return drink;
    }

    public void setDrink(Drink drink) {
        this.drink = drink;
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

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", order=" + order +
                ", product=" + product +
                ", drink=" + drink +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
