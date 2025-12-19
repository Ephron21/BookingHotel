package com.example.hotelbooking.network;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Network Hotel Model
 * Represents hotel data from server (JSON)
 * Similar to local Hotel model but designed for network operations
 */
public class NetworkHotel {
    private int id;
    private String name;
    private String location;
    private int rating;
    private double price;
    private String checkInDate;
    private boolean available;
    private String roomType;
    private String createdAt;
    private String imageUrl; // <-- 1. FIELD ADDED to store the image URL

    // Empty constructor
    public NetworkHotel() {
    }

    // Constructor with all fields, including the new imageUrl
    public NetworkHotel(int id, String name, String location, int rating,
                        double price, String checkInDate, boolean available,
                        String roomType, String imageUrl) { // <-- 4. CONSTRUCTOR UPDATED
        this.id = id;
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.price = price;
        this.checkInDate = checkInDate;
        this.available = available;
        this.roomType = roomType;
        this.imageUrl = imageUrl; // <-- 4. CONSTRUCTOR UPDATED
    }

    /**
     * Create NetworkHotel from JSON Object
     * This method parses JSON response from server
     *
     * @param jsonObject JSON object from API response
     * @return NetworkHotel object
     * @throws JSONException if parsing fails
     */
    public static NetworkHotel fromJSON(JSONObject jsonObject) throws JSONException {
        NetworkHotel hotel = new NetworkHotel();

        // Parse each field from JSON
        // optInt/optString provide default values if key doesn't exist
        hotel.id = jsonObject.optInt("id", 0);
        hotel.name = jsonObject.optString("name", "");
        hotel.location = jsonObject.optString("location", "");
        hotel.rating = jsonObject.optInt("rating", 0); // Default to 0 if missing
        hotel.price = jsonObject.optDouble("price", 0.0);
        hotel.checkInDate = jsonObject.optString("check_in_date", "");
        hotel.available = jsonObject.optInt("available", 1) == 1;
        hotel.roomType = jsonObject.optString("room_type", "");
        hotel.createdAt = jsonObject.optString("created_at", "");

        // <-- 2. PARSE IMAGE URL FROM JSON -->
        // Use optString with null as default. This prevents crashes if 'image_url' is missing.
        hotel.imageUrl = jsonObject.optString("image_url", null);

        return hotel;
    }

    /**
     * Convert NetworkHotel to JSON Object
     * Used when sending data to server (POST requests)
     *
     * @return JSONObject representation
     * @throws JSONException if conversion fails
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        // Only include ID if it's not 0 (for updates)
        if (id != 0) {
            json.put("id", id);
        }

        json.put("name", name);
        json.put("location", location);
        json.put("rating", rating);
        json.put("price", price);
        json.put("check_in_date", checkInDate);
        json.put("available", available ? 1 : 0);
        json.put("room_type", roomType);
        json.put("image_url", imageUrl); // Also include image_url when sending data

        return json;
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }



    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // <-- 3. GETTER AND SETTER ADDED for imageUrl -->
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }


    @Override
    public String toString() {
        return "NetworkHotel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' + // Added imageUrl to toString for easier debugging
                '}';
    }
}
