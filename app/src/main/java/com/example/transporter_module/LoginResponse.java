package com.example.transporter_module;

public class LoginResponse {
    private boolean success;
    private String message;
    private Transporter transporter;  // Assuming the transporter object is returned on successful login

    // Constructor
    public LoginResponse(boolean success, String message, Transporter transporter) {
        this.success = success;
        this.message = message;
        this.transporter = transporter;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Transporter getTransporter() {
        return transporter;
    }

    // Optional: Setters if needed
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTransporter(Transporter transporter) {
        this.transporter = transporter;
    }
}
