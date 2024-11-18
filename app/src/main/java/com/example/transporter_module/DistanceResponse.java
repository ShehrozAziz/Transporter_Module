package com.example.transporter_module;
import com.google.gson.annotations.SerializedName;

public class DistanceResponse {
    @SerializedName("distances")
    private double[][] distances;

    public double[][] getDistances() {
        return distances;
    }

    public void setDistances(double[][] distances) {
        this.distances = distances;
    }

    public double getDistance() {
        if (distances != null && distances.length > 0 && distances[0].length > 0) {
            return distances[0][0]; // Assuming first distance is the required one
        }
        return -1; // Handle error
    }
}
