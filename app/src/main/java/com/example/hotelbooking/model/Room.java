package com.example.hotelbooking.model;

public class Room {
    private int id;
    private int hotelId;
    private String roomNumber;
    private String roomType;
    private int capacity;
    private boolean hasBalcony;
    private double additionalPrice;

    public Room() {
    }

    public Room(int hotelId, String roomNumber, String roomType, int capacity,
                boolean hasBalcony, double additionalPrice) {
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.hasBalcony = hasBalcony;
        this.additionalPrice = additionalPrice;
    }

    public Room(int id, int hotelId, String roomNumber, String roomType, int capacity,
                boolean hasBalcony, double additionalPrice) {
        this.id = id;
        this.hotelId = hotelId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.hasBalcony = hasBalcony;
        this.additionalPrice = additionalPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isHasBalcony() {
        return hasBalcony;
    }

    public void setHasBalcony(boolean hasBalcony) {
        this.hasBalcony = hasBalcony;
    }

    public double getAdditionalPrice() {
        return additionalPrice;
    }

    public void setAdditionalPrice(double additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", hotelId=" + hotelId +
                ", roomNumber='" + roomNumber + '\'' +
                ", roomType='" + roomType + '\'' +
                ", capacity=" + capacity +
                ", hasBalcony=" + hasBalcony +
                ", additionalPrice=" + additionalPrice +
                '}';
    }
}