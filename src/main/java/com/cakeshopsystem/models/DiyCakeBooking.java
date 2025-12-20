package com.cakeshopsystem.models;

import java.time.LocalDateTime;

public class DiyCakeBooking {

    private int diyCakeBookingId;
    private Booking booking;
    private Customer customer;
    private PrebakedCake prebakedCake;
    private Order order;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double serviceCharges;

    public DiyCakeBooking(){}

    public DiyCakeBooking(int diyCakeBookingId, Booking booking, Customer customer, PrebakedCake prebakedCake, Order order, LocalDateTime startTime, LocalDateTime endTime, double serviceCharges) {
        this.diyCakeBookingId = diyCakeBookingId;
        this.booking = booking;
        this.customer = customer;
        this.prebakedCake = prebakedCake;
        this.order = order;
        this.startTime = startTime;
        this.endTime = endTime;
        this.serviceCharges = serviceCharges;
    }

    public int getDiyCakeBookingId() {
        return diyCakeBookingId;
    }

    public void setDiyCakeBookingId(int diyCakeBookingId) {
        this.diyCakeBookingId = diyCakeBookingId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public PrebakedCake getPrebakedCake() {
        return prebakedCake;
    }

    public void setPrebakedCake(PrebakedCake prebakedCake) {
        this.prebakedCake = prebakedCake;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getServiceCharges() {
        return serviceCharges;
    }

    public void setServiceCharges(double serviceCharges) {
        this.serviceCharges = serviceCharges;
    }

    @Override
    public String toString() {
        return "DiyCakeBooking{" +
                "diyCakeBookingId=" + diyCakeBookingId +
                ", booking=" + booking +
                ", customer=" + customer +
                ", prebakedCake=" + prebakedCake +
                ", order=" + order +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", serviceCharges=" + serviceCharges +
                '}';
    }
}
