package com.example.transporter_module;

import java.io.Serializable;

public class Order implements Serializable {
    private String orderID;
    private String date;
    private String destinationAddress;
    private String destinationPin;
    private int fare;
    private String name;
    private String phone;
    private String sourceAddress;
    private String sourcePin;
    private String status;
    private String time;
    private double totalDistance;
    private String userID;

    // Constructor
    public Order(String id,String date, String destinationAddress, String destinationPin, int fare,
                 String name, String phone, String sourceAddress, String sourcePin,
                 String status, String time, double totalDistance, String userID) {
        this.orderID = id;
        this.date = date;
        this.destinationAddress = destinationAddress;
        this.destinationPin = destinationPin;
        this.fare = fare;
        this.name = name;
        this.phone = phone;
        this.sourceAddress = sourceAddress;
        this.sourcePin = sourcePin;
        this.status = status;
        this.time = time;
        this.totalDistance = totalDistance;
        this.userID = userID;
    }

    // Getters and Setters
    public String getorderID(){return orderID;}
    public void setorderID(String id){this.orderID = id;}
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public String getDestinationPin() { return destinationPin; }
    public void setDestinationPin(String destinationPin) { this.destinationPin = destinationPin; }

    public int getFare() { return fare; }
    public void setFare(int fare) { this.fare = fare; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSourceAddress() { return sourceAddress; }
    public void setSourceAddress(String sourceAddress) { this.sourceAddress = sourceAddress; }

    public String getSourcePin() { return sourcePin; }
    public void setSourcePin(String sourcePin) { this.sourcePin = sourcePin; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }
}