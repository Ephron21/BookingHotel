package com.example.hotelbooking.network;

/**
 * API Configuration Class
 * Contains all API endpoint URLs for the Flask backend.
 */
public class ApiConfig {

    // --- IMPORTANT ---
    // If you are using the Android Emulator, 'localhost' refers to the emulator itself.
    // To access your computer's localhost, usually you need "http://10.0.2.2:5000/api".
    // However, if you have set up port forwarding (adb reverse), "http://localhost:5000/api" will work.
    
    // User requested IP: http://172.31.239.130:5000
    // We separate the server root from the API path to handle image URLs correctly.
    public static final String SERVER_URL = "http://172.31.239.130:5000";
    public static final String BASE_URL = SERVER_URL + "/api";

    // --- Hotel API Endpoints ---
    // URL for getting all hotels (GET) and creating a new one (POST)
    public static final String HOTELS_ENDPOINT = BASE_URL + "/hotels";

    // Legacy/Alias constants to ensure compatibility with other parts of the app
    public static final String GET_ALL_HOTELS = HOTELS_ENDPOINT;
    public static final String CREATE_HOTEL = HOTELS_ENDPOINT;
    public static final String UPDATE_HOTEL = HOTELS_ENDPOINT; 
    public static final String DELETE_HOTEL = HOTELS_ENDPOINT; 
    public static final String SAVE_HOTEL = HOTELS_ENDPOINT;   

    // --- Room API Endpoints ---
    // URL for getting all rooms (GET) and creating a new one (POST)
    public static final String ROOMS_ENDPOINT = BASE_URL + "/rooms";


    /**
     * Constructs the URL for a specific hotel (GET, PUT, DELETE).
     * @param hotelId The ID of the hotel.
     * @return The complete URL (e.g., http://.../api/hotels/123)
     */
    public static String getHotelUrl(int hotelId) {
        return HOTELS_ENDPOINT + "/" + hotelId;
    }

    /**
     * Constructs the URL to get all rooms for a specific hotel.
     * @param hotelId The ID of the hotel.
     * @return The complete URL (e.g., http://.../api/rooms/hotel/123)
     */
    public static String getRoomsByHotelUrl(int hotelId) {
        // Based on the API definition: GET /rooms/hotel/:hotelId
        return ROOMS_ENDPOINT + "/hotel/" + hotelId;
    }

    /**
     * Constructs the URL for a specific room (GET, PUT, DELETE).
     * @param roomId The ID of the room.
     * @return The complete URL (e.g., http://.../api/rooms/456)
     */
    public static String getRoomUrl(int roomId) {
        return ROOMS_ENDPOINT + "/" + roomId;
    }
}
