package com.example.transporter_module;
public class ServerResponse {
    private String message;
    private ClientData receivedData;

    public String getMessage() {
        return message;
    }

    public ClientData getReceivedData() {
        return receivedData;
    }
}