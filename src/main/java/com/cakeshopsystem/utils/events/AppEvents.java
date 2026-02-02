package com.cakeshopsystem.utils.events;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public final class AppEvents {
    private static final IntegerProperty orderCompletedCounter = new SimpleIntegerProperty(0);

    private AppEvents() {}

    public static IntegerProperty orderCompletedCounterProperty() {
        return orderCompletedCounter;
    }

    public static void fireOrderCompleted() {
        orderCompletedCounter.set(orderCompletedCounter.get() + 1);
    }
}
