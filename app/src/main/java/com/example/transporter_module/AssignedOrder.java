package com.example.transporter_module;

import java.io.Serializable;

public class AssignedOrder implements Serializable {
    private String orderID;
    private String date;
    private String destination;
    private String destinationPin;
    private int fare;
    private String source;
    private String sourcePin;
    private String status;
    private String time;
    private String transporterID;
    private String userID;
    private String name;
    private String phone;

    // Constructor
    public AssignedOrder(String orderID, String date, String destination, String destinationPin, int fare,
                 String name,String phone,String source, String sourcePin, String status, String time, String transporterID, String userID) {
        this.orderID = orderID;
        this.date = date;
        this.destination = destination;
        this.destinationPin = destinationPin;
        this.fare = fare;
        this.source = source;
        this.sourcePin = sourcePin;
        this.status = status;
        this.time = time;
        this.transporterID = transporterID;
        this.userID = userID;
        this.name = name;
        this.phone = phone;
    }

    // Getters
    public String getName(){return  name;}
    public String getPhone(){return phone;}
    public String getOrderID() {
        return orderID;
    }

    public String getDate() {
        return date;
    }

    public String getDestination() {
        return destination;
    }

    public String getDestinationPin() {
        return destinationPin;
    }

    public int getFare() {
        return fare;
    }

    public String getSource() {
        return source;
    }

    public String getSourcePin() {
        return sourcePin;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public String getTransporterID() {
        return transporterID;
    }

    public String getUserID() {
        return userID;
    }
}

