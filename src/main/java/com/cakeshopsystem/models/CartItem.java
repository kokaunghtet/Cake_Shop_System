package com.cakeshopsystem.models;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;

import java.time.LocalDate;

public class CartItem {

    // =================================================
    // Core item properties
    // =================================================
    private final IntegerProperty productId = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty option = new SimpleStringProperty();
    private final DoubleProperty unitPrice = new SimpleDoubleProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();

    // =================================================
    // Calculated properties
    // =================================================
    private final ReadOnlyDoubleWrapper subTotal = new ReadOnlyDoubleWrapper();

    // =================================================
    // Custom cake booking fields
    // =================================================
    private final ObjectProperty<Integer> customCakeId = new SimpleObjectProperty<>(null);
    private final ObjectProperty<LocalDate> pickupDate = new SimpleObjectProperty<>(null);
    private final StringProperty customMessage = new SimpleStringProperty(null);
    private final ObjectProperty<Integer> customFlavourId = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Integer> customToppingId = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Integer> customSizeId = new SimpleObjectProperty<>(null);
    private final ObjectProperty<com.cakeshopsystem.utils.constants.CakeShape> customShape =
            new SimpleObjectProperty<>(null);

    // =================================================
    // Constructor
    // =================================================
    public CartItem(int productId, String name, String option, double unitPrice, int quantity) {
        this.productId.set(productId);
        this.name.set(name);
        this.option.set(option);
        this.unitPrice.set(unitPrice);
        this.quantity.set(quantity);

        DoubleBinding sub = this.unitPrice.multiply(this.quantity);
        this.subTotal.bind(sub);
    }

    // =================================================
    // Core getters & properties
    // =================================================
    public int getProductId() {
        return productId.get();
    }

    public IntegerProperty productIdProperty() {
        return productId;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getOption() {
        return option.get();
    }

    public StringProperty optionProperty() {
        return option;
    }

    public double getUnitPrice() {
        return unitPrice.get();
    }

    public DoubleProperty unitPriceProperty() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int qty) {
        quantity.set(qty);
    }

    // =================================================
    // Subtotal accessors
    // =================================================
    public double getSubTotal() {
        return subTotal.get();
    }

    public ReadOnlyDoubleProperty subTotalProperty() {
        return subTotal.getReadOnlyProperty();
    }

    // =================================================
    // Custom cake state helpers
    // =================================================
    public boolean isCustomCake() {
        return customCakeId.get() != null;
    }

    // =================================================
    // Custom cake individual fields
    // =================================================
    public Integer getCustomCakeId() {
        return customCakeId.get();
    }

    public ObjectProperty<Integer> customCakeIdProperty() {
        return customCakeId;
    }

    public void setCustomCakeId(Integer cakeId) {
        customCakeId.set(cakeId);
    }

    public LocalDate getPickupDate() {
        return pickupDate.get();
    }

    public ObjectProperty<LocalDate> pickupDateProperty() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate date) {
        pickupDate.set(date);
    }

    public String getCustomMessage() {
        return customMessage.get();
    }

    public StringProperty customMessageProperty() {
        return customMessage;
    }

    public void setCustomMessage(String msg) {
        customMessage.set(msg);
    }

    public Integer getCustomFlavourId() {
        return customFlavourId.get();
    }

    public void setCustomFlavourId(Integer v) {
        customFlavourId.set(v);
    }

    public Integer getCustomToppingId() {
        return customToppingId.get();
    }

    public void setCustomToppingId(Integer v) {
        customToppingId.set(v);
    }

    public Integer getCustomSizeId() {
        return customSizeId.get();
    }

    public void setCustomSizeId(Integer v) {
        customSizeId.set(v);
    }

    public com.cakeshopsystem.utils.constants.CakeShape getCustomShape() {
        return customShape.get();
    }

    public void setCustomShape(com.cakeshopsystem.utils.constants.CakeShape v) {
        customShape.set(v);
    }

    // =================================================
    // Custom cake bulk setter
    // =================================================
    public void setCustomBooking(
            Integer cakeId,
            LocalDate date,
            String msg,
            Integer flavourId,
            Integer toppingId,
            Integer sizeId,
            com.cakeshopsystem.utils.constants.CakeShape shape
    ) {
        setCustomCakeId(cakeId);
        setPickupDate(date);
        setCustomMessage(msg);
        setCustomFlavourId(flavourId);
        setCustomToppingId(toppingId);
        setCustomSizeId(sizeId);
        setCustomShape(shape);
    }
}
