package com.example.hotelbooking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.hotelbooking.model.Hotel;
import com.example.hotelbooking.model.Room;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "HotelBooking.db";
    private static final int DATABASE_VERSION = 3;

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
                + TABLE_HOTEL + "(" + HOTEL_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_ROOM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOTEL);
        onCreate(db);
    }

    /**
     * Adds a hotel to the database.
     * IMPORTANT: If the hotel object has an ID > 0 (e.g., from server sync),
     * we force that ID into the database to keep IDs consistent.
     */
    public long addHotel(Hotel hotel) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            // --- FIX FOR SYNC ISSUE ---
            // If the hotel comes from the server, it will have a valid ID.
            // We must use this ID to ensure UPDATE/DELETE operations work correctly against the server.
            if (hotel.getId() > 0) {
                values.put(HOTEL_ID, hotel.getId());
            }
            // --------------------------

            values.put(HOTEL_NAME, hotel.getName());
            values.put(HOTEL_LOCATION, hotel.getLocation());
            values.put(HOTEL_RATING, hotel.getRating());
            values.put(HOTEL_PRICE, hotel.getPrice());
            values.put(HOTEL_CHECK_IN, hotel.getCheckInDate());
            values.put(HOTEL_AVAILABLE, hotel.isAvailable() ? 1 : 0);
            values.put(HOTEL_ROOM_TYPE, hotel.getRoomType());
            values.put(HOTEL_IMAGE, hotel.getImage());

            id = db.insert(TABLE_HOTEL, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to add hotel to database", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return id;
    }

    public Hotel getHotel(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Hotel hotel = null;
        try {
            cursor = db.query(TABLE_HOTEL, null, HOTEL_ID + "=?",
                    new String[]{String.valueOf(id)}, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                hotel = new Hotel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(HOTEL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_LOCATION)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(HOTEL_RATING)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(HOTEL_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_CHECK_IN)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(HOTEL_AVAILABLE)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_ROOM_TYPE))
                );
                hotel.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow(HOTEL_IMAGE)));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get hotel from database", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return hotel;
    }

    public Hotel getLastAddedHotel() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Hotel hotel = null;
        try {
            String query = "SELECT * FROM " + TABLE_HOTEL + " ORDER BY " + HOTEL_ID + " DESC LIMIT 1";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                hotel = new Hotel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(HOTEL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_LOCATION)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(HOTEL_RATING)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(HOTEL_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_CHECK_IN)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(HOTEL_AVAILABLE)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_ROOM_TYPE))
                );
                hotel.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow(HOTEL_IMAGE)));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get last added hotel", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return hotel;
    }

    public void deleteAllHotels() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_HOTEL, null, null);
            // Also clear rooms since they are dependent (though CASCADE might not work if logic is manual, but DB constraint handles it)
            // But let's be safe if we are wiping to sync.
            db.delete(TABLE_ROOM, null, null); 
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to delete all hotels", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<Hotel> getAllHotels() {
        List<Hotel> hotelList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + TABLE_HOTEL;
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Hotel hotel = new Hotel(
                            cursor.getInt(cursor.getColumnIndexOrThrow(HOTEL_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_LOCATION)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(HOTEL_RATING)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(HOTEL_PRICE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_CHECK_IN)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(HOTEL_AVAILABLE)) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow(HOTEL_ROOM_TYPE))
                    );
                    hotel.setImage(cursor.getBlob(cursor.getColumnIndexOrThrow(HOTEL_IMAGE)));
                    hotelList.add(hotel);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get all hotels from database", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return hotelList;
    }

    public int updateHotel(Hotel hotel) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(HOTEL_NAME, hotel.getName());
            values.put(HOTEL_LOCATION, hotel.getLocation());
            values.put(HOTEL_RATING, hotel.getRating());
            values.put(HOTEL_PRICE, hotel.getPrice());
            values.put(HOTEL_CHECK_IN, hotel.getCheckInDate());
            values.put(HOTEL_AVAILABLE, hotel.isAvailable() ? 1 : 0);
            values.put(HOTEL_ROOM_TYPE, hotel.getRoomType());
            values.put(HOTEL_IMAGE, hotel.getImage());

            rowsAffected = db.update(TABLE_HOTEL, values,
                    HOTEL_ID + "=?",
                    new String[]{String.valueOf(hotel.getId())});
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to update hotel", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return rowsAffected;
    }

    public void deleteHotel(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_HOTEL, HOTEL_ID + "=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to delete hotel", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public long addRoom(Room room) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(ROOM_HOTEL_ID, room.getHotelId());
            values.put(ROOM_NUMBER, room.getRoomNumber());
            values.put(ROOM_TYPE, room.getRoomType());
            values.put(ROOM_CAPACITY, room.getCapacity());
            values.put(ROOM_HAS_BALCONY, room.isHasBalcony() ? 1 : 0);
            values.put(ROOM_ADDITIONAL_PRICE, room.getAdditionalPrice());

            id = db.insert(TABLE_ROOM, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to add room", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return id;
    }

    public List<Room> getRoomsByHotelId(int hotelId) {
        List<Room> roomList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_ROOM, null, ROOM_HOTEL_ID + "=?",
                    new String[]{String.valueOf(hotelId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Room room = new Room(
                            cursor.getInt(cursor.getColumnIndexOrThrow(ROOM_ID)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(ROOM_HOTEL_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(ROOM_NUMBER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(ROOM_TYPE)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(ROOM_CAPACITY)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(ROOM_HAS_BALCONY)) == 1,
                            cursor.getDouble(cursor.getColumnIndexOrThrow(ROOM_ADDITIONAL_PRICE))
                    );
                    roomList.add(room);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get rooms by hotel id", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return roomList;
    }

    public int getHotelCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            String countQuery = "SELECT COUNT(*) FROM " + TABLE_HOTEL;
            cursor = db.rawQuery(countQuery, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting hotel count", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return count;
    }
}
