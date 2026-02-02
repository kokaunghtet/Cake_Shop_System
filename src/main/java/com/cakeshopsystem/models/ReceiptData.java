package com.cakeshopsystem.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReceiptData {
    private final int orderId;
    private final LocalDateTime orderDate;
    private final String cashierName;
    private final String paymentName;

    private final List<CartItem> items;

    private final BigDecimal subtotal;
    private final BigDecimal discountAmount;
    private final BigDecimal grandTotal;

    public ReceiptData(int orderId,
                       LocalDateTime orderDate,
                       String cashierName,
                       String paymentName,
                       List<CartItem> items,
                       BigDecimal subtotal,
                       BigDecimal discountAmount,
                       BigDecimal grandTotal) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.cashierName = cashierName;
        this.paymentName = paymentName;
        this.items = items;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.grandTotal = grandTotal;
    }

    public int getOrderId() { return orderId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public String getCashierName() { return cashierName; }
    public String getPaymentName() { return paymentName; }
    public List<CartItem> getItems() { return items; }

    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getGrandTotal() { return grandTotal; }
}
