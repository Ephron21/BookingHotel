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

import com.bumptech.glide.Glide;
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

            // Logic to load image: URL first, then byte array, then default
            boolean imageLoaded = false;

            // 1. Try URL
            if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(hotel.getImageUrl())
                        .placeholder(R.drawable.default_hotel_image)
                        .error(R.drawable.default_hotel_image)
                        .into(hotelImage);
                imageLoaded = true;
            }

            // 2. Try Byte Array (if URL failed or empty)
            if (!imageLoaded) {
                byte[] imageBytes = hotel.getImage();
                if (imageBytes != null && imageBytes.length > 0) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        if (bitmap != null) {
                            hotelImage.setImageBitmap(bitmap);
                            imageLoaded = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // 3. Default fallback
            if (!imageLoaded) {
                // If Glide is loading, it handles placeholder.
                // But if we didn't trigger Glide and didn't have bytes:
                // However, Glide call above is async. If we call setImageBitmap after Glide starts, it might conflict.
                // Simplified logic:
                
                if (hotel.getImageUrl() == null || hotel.getImageUrl().isEmpty()) {
                     // No URL, so we rely on bytes or default
                     if (hotel.getImage() != null && hotel.getImage().length > 0) {
                          try {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(hotel.getImage(), 0, hotel.getImage().length);
                                hotelImage.setImageBitmap(bitmap);
                          } catch (Exception e) {
                                hotelImage.setImageResource(R.drawable.default_hotel_image);
                          }
                     } else {
                          hotelImage.setImageResource(R.drawable.default_hotel_image);
                     }
                }
                // If URL exists, Glide handles it (including error placeholder)
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
