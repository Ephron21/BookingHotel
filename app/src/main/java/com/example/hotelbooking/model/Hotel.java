package com.example.hotelbooking.model;

public class Hotel {
    private int id;
    private String name;
    private String location;
    private int rating;
    private double price;
    private String checkInDate;
    private boolean available;
    private String roomType;

    // Constructors
    public Hotel() {
    }

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

    // Getters and Setters
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
                '}';
    }
}