package com.example.hotelbooking.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.model.Hotel;

import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;
    private OnHotelClickListener listener;

    public interface OnHotelClickListener {
        void onEditClick(Hotel hotel);
        void onDeleteClick(Hotel hotel);
        void onItemClick(Hotel hotel);
    }

    public HotelAdapter(List<Hotel> hotelList, OnHotelClickListener listener) {
        this.hotelList = hotelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);
        holder.bind(hotel);
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public void updateList(List<Hotel> newList) {
        this.hotelList = newList;
        notifyDataSetChanged();
    }

    class HotelViewHolder extends RecyclerView.ViewHolder {
        private ImageView hotelImage;
        private TextView hotelName;
        private TextView hotelLocation;
        private TextView hotelRating;
        private TextView hotelPrice;
        private Button editButton;
        private Button deleteButton;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            hotelImage = itemView.findViewById(R.id.hotelImage);
            hotelName = itemView.findViewById(R.id.hotelName);
            hotelLocation = itemView.findViewById(R.id.hotelLocation);
            hotelRating = itemView.findViewById(R.id.hotelRating);
            hotelPrice = itemView.findViewById(R.id.hotelPrice);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Hotel hotel) {
            hotelName.setText(hotel.getName());
            hotelLocation.setText(hotel.getLocation());
            hotelRating.setText("Rating: " + hotel.getRating() + "★");
            hotelPrice.setText("$" + hotel.getPrice() + "/night");

            // Load image from byte array
            byte[] imageBytes = hotel.getImage();
            if (imageBytes != null && imageBytes.length > 0) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    if (bitmap != null) {
                        hotelImage.setImageBitmap(bitmap);
                    } else {
                        hotelImage.setImageResource(R.drawable.default_hotel_image);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hotelImage.setImageResource(R.drawable.default_hotel_image);
                }
            } else {
                // Set a default image if no image is available
                hotelImage.setImageResource(R.drawable.default_hotel_image);
            }

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(hotel);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(hotel);
                }
            });
        }
    }
}