package com.example.hotelbooking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.hotelbooking.model.Hotel;
import com.example.hotelbooking.model.Room;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HotelBooking.db";
    private static final int DATABASE_VERSION = 2;

    // Hotel Table
    private static final String TABLE_HOTEL = "hotels";
    private static final String HOTEL_ID = "id";
    private static final String HOTEL_NAME = "name";
    private static final String HOTEL_LOCATION = "location";
    private static final String HOTEL_RATING = "rating";
    private static final String HOTEL_PRICE = "price";
    private static final String HOTEL_CHECK_IN = "check_in_date";
    private static final String HOTEL_AVAILABLE = "available";
    private static final String HOTEL_ROOM_TYPE = "room_type";
    private static final String HOTEL_IMAGE = "image";

    // Room Table
    private static final String TABLE_ROOM = "rooms";
    private static final String ROOM_ID = "id";
    private static final String ROOM_HOTEL_ID = "hotel_id";
    private static final String ROOM_NUMBER = "room_number";
    private static final String ROOM_TYPE = "room_type";
    private static final String ROOM_CAPACITY = "capacity";
    private static final String ROOM_HAS_BALCONY = "has_balcony";
    private static final String ROOM_ADDITIONAL_PRICE = "additional_price";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HOTEL_TABLE = "CREATE TABLE " + TABLE_HOTEL + "("
                + HOTEL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HOTEL_NAME + " TEXT NOT NULL,"
                + HOTEL_LOCATION + " TEXT NOT NULL,"
                + HOTEL_RATING + " INTEGER,"
                + HOTEL_PRICE + " REAL,"
                + HOTEL_CHECK_IN + " TEXT,"
                + HOTEL_AVAILABLE + " INTEGER,"
                + HOTEL_ROOM_TYPE + " TEXT,"
                + HOTEL_IMAGE + " BLOB"
                + ")";
        db.execSQL(CREATE_HOTEL_TABLE);

        String CREATE_ROOM_TABLE = "CREATE TABLE " + TABLE_ROOM + "("
                + ROOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ROOM_HOTEL_ID + " INTEGER,"
                + ROOM_NUMBER + " TEXT NOT NULL,"
                + ROOM_TYPE + " TEXT,"
                + ROOM_CAPACITY + " INTEGER,"
                + ROOM_HAS_BALCONY + " INTEGER,"
                + ROOM_ADDITIONAL_PRICE + " REAL,"
                + "FOREIGN KEY(" + ROOM_HOTEL_ID + ") REFERENCES "
                + TABLE_HOTEL + "(" + HOTEL_ID + ")"
                + ")";
        db.execSQL(CREATE_ROOM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOTEL);
        onCreate(db);
    }

    public long addHotel(Hotel hotel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HOTEL_NAME, hotel.getName());
        values.put(HOTEL_LOCATION, hotel.getLocation());
        values.put(HOTEL_RATING, hotel.getRating());
        values.put(HOTEL_PRICE, hotel.getPrice());
        values.put(HOTEL_CHECK_IN, hotel.getCheckInDate());
        values.put(HOTEL_AVAILABLE, hotel.isAvailable() ? 1 : 0);
        values.put(HOTEL_ROOM_TYPE, hotel.getRoomType());
        long id = db.insert(TABLE_HOTEL, null, values);
        db.close();
        return id;
    }

    public Hotel getHotel(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HOTEL,
                new String[]{HOTEL_ID, HOTEL_NAME, HOTEL_LOCATION, HOTEL_RATING,
                        HOTEL_PRICE, HOTEL_CHECK_IN, HOTEL_AVAILABLE, HOTEL_ROOM_TYPE},
                HOTEL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        Hotel hotel = null;
        if (cursor != null && cursor.moveToFirst()) {
            hotel = new Hotel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getDouble(4),
                    cursor.getString(5),
                    cursor.getInt(6) == 1,
                    cursor.getString(7)
            );
            cursor.close();
        }
        db.close();
        return hotel;
    }

    public List<Hotel> getAllHotels() {
        List<Hotel> hotelList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HOTEL;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Hotel hotel = new Hotel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getDouble(4),
                        cursor.getString(5),
                        cursor.getInt(6) == 1,
                        cursor.getString(7)
                );
                hotelList.add(hotel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return hotelList;
    }

    public int updateHotel(Hotel hotel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HOTEL_NAME, hotel.getName());
        values.put(HOTEL_LOCATION, hotel.getLocation());
        values.put(HOTEL_RATING, hotel.getRating());
        values.put(HOTEL_PRICE, hotel.getPrice());
        values.put(HOTEL_CHECK_IN, hotel.getCheckInDate());
        values.put(HOTEL_AVAILABLE, hotel.isAvailable() ? 1 : 0);
        values.put(HOTEL_ROOM_TYPE, hotel.getRoomType());

        int rowsAffected = db.update(TABLE_HOTEL, values,
                HOTEL_ID + "=?",
                new String[]{String.valueOf(hotel.getId())});
        db.close();
        return rowsAffected;
    }

    public void deleteHotel(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROOM, ROOM_HOTEL_ID + "=?",
                new String[]{String.valueOf(id)});
        db.delete(TABLE_HOTEL, HOTEL_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public long addRoom(Room room) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ROOM_HOTEL_ID, room.getHotelId());
        values.put(ROOM_NUMBER, room.getRoomNumber());
        values.put(ROOM_TYPE, room.getRoomType());
        values.put(ROOM_CAPACITY, room.getCapacity());
        values.put(ROOM_HAS_BALCONY, room.isHasBalcony() ? 1 : 0);
        values.put(ROOM_ADDITIONAL_PRICE, room.getAdditionalPrice());

        long id = db.insert(TABLE_ROOM, null, values);
        db.close();
        return id;
    }

    public List<Room> getRoomsByHotelId(int hotelId) {
        List<Room> roomList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROOM,
                new String[]{ROOM_ID, ROOM_HOTEL_ID, ROOM_NUMBER, ROOM_TYPE,
                        ROOM_CAPACITY, ROOM_HAS_BALCONY, ROOM_ADDITIONAL_PRICE},
                ROOM_HOTEL_ID + "=?",
                new String[]{String.valueOf(hotelId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Room room = new Room(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5) == 1,
                        cursor.getDouble(6)
                );
                roomList.add(room);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return roomList;
    }

    public int getHotelCount() {
        String countQuery = "SELECT * FROM " + TABLE_HOTEL;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}