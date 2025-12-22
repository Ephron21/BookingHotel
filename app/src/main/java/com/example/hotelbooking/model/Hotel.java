package com.example.hotelbooking.model;

// 1. ADDED NECESSARY IMPORTS
import android.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class Hotel {
    private int id;
    private String name;
    private String location;
    private int rating;
    private double price;
    private String checkInDate;
    private boolean available;
    private String roomType;
    private byte[] image; // For local database (BLOB)
    private String imageUrl; // For fetching from server URL

    // Default Constructor
    public Hotel() {
    }

    // Constructor without id (for creating new hotels)
    public Hotel(String name, String location, int rating, double price,
                 String checkInDate, boolean available, String roomType) {
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.price = price;
        this.checkInDate = checkInDate;
        this.available = available;
        this.roomType = roomType;
    }

    // Constructor with id (for hotels retrieved from the database)
    public Hotel(int id, String name, String location, int rating, double price,
                 String checkInDate, boolean available, String roomType) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.price = price;
        this.checkInDate = checkInDate;
        this.available = available;
        this.roomType = roomType;
    }

    // --- Getters and Setters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Converts the Hotel object into a JSONObject to send to the server.
     * @return A JSONObject representing the hotel.
     * @throws JSONException If there is an error during JSON creation.
     */
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", getName());
        jsonBody.put("location", getLocation());
        jsonBody.put("rating", getRating());
        jsonBody.put("price", getPrice());
        jsonBody.put("checkInDate", getCheckInDate());
        jsonBody.put("available", isAvailable());
        jsonBody.put("roomType", getRoomType());

        if (getImage() != null && getImage().length > 0) {
            // 2. CORRECTED: Use the standard Android Base64 utility
            String encodedImage = Base64.encodeToString(getImage(), Base64.NO_WRAP);
            jsonBody.put("image", encodedImage); // Send as base64 string
        } else if (getImageUrl() != null && !getImageUrl().isEmpty()) {
            jsonBody.put("image_url", getImageUrl());
        }

        return jsonBody;
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", rating=" + rating +
                ", price=" + price +
                ", checkInDate='" + checkInDate + '\'' +
                ", available=" + available +
                ", roomType='" + roomType + '\'' +
                ", hasImage=" + (image != null && image.length > 0) +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
