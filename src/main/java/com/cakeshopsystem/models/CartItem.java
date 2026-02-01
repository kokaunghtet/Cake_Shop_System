package com.cakeshopsystem.models;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;

public class CartItem {
    private final IntegerProperty productId = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty option = new SimpleStringProperty();
    private final DoubleProperty unitPrice = new SimpleDoubleProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();

    private final ReadOnlyDoubleWrapper subTotal = new ReadOnlyDoubleWrapper();

    public CartItem(int productId, String name, String option, double unitPrice, int quantity) {
        this.productId.set(productId);
        this.name.set(name);
        this.option.set(option);
        this.unitPrice.set(unitPrice);
        this.quantity.set(quantity);

        DoubleBinding sub = this.unitPrice.multiply(this.quantity);
        this.subTotal.bind(sub);
    }

    public int getProductId() { return productId.get(); }
    public IntegerProperty productIdProperty() { return productId; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getOption() { return option.get(); }
    public StringProperty optionProperty() { return option; }

    public double getUnitPrice() { return unitPrice.get(); }
    public DoubleProperty unitPriceProperty() { return unitPrice; }

    public int getQuantity() { return quantity.get(); }
    public IntegerProperty quantityProperty() { return quantity; }
    public void setQuantity(int qty) { quantity.set(qty); }

    public double getSubTotal() { return subTotal.get(); }
    public ReadOnlyDoubleProperty subTotalProperty() { return subTotal.getReadOnlyProperty(); }
}
