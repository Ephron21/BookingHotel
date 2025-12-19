package com.example.hotelbooking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView; // <-- 1. IMPORT ImageView
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // <-- 1. IMPORT Glide
import com.example.hotelbooking.R;
import com.example.hotelbooking.network.NetworkHotel;

import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter for Network Hotels
 * Displays hotels fetched from web server
 */
public class NetworkHotelAdapter extends RecyclerView.Adapter<NetworkHotelAdapter.ViewHolder> {

    private List<NetworkHotel> hotelList;
    private OnHotelClickListener listener;

    /**
     * Interface for handling click events
     */
    public interface OnHotelClickListener {
        void onEditClick(NetworkHotel hotel);
        void onDeleteClick(NetworkHotel hotel);
        void onItemClick(NetworkHotel hotel);
    }

    public NetworkHotelAdapter(List<NetworkHotel> hotelList, OnHotelClickListener listener) {
        this.hotelList = hotelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_network_hotel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NetworkHotel hotel = hotelList.get(position);
        holder.bind(hotel);
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    /**
     * Update the list with new data
     * This method is called after fetching new data from server
     */
    public void updateList(List<NetworkHotel> newList) {
        this.hotelList.clear();
        this.hotelList.addAll(newList);
        notifyDataSetChanged();  // Notify RecyclerView to refresh
    }

    /**
     * ViewHolder class - holds references to views
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        // UI Components
        TextView hotelName, hotelLocation, hotelRating, hotelPrice, serverIndicator;
        ImageView hotelImage; // <-- 2. ADD ImageView
        Button editButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            hotelName = itemView.findViewById(R.id.hotelName);
            hotelLocation = itemView.findViewById(R.id.hotelLocation);
            hotelRating = itemView.findViewById(R.id.hotelRating);
            hotelPrice = itemView.findViewById(R.id.hotelPrice);
            serverIndicator = itemView.findViewById(R.id.serverIndicator);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            hotelImage = itemView.findViewById(R.id.hotelImage); // <-- 3. INITIALIZE ImageView
        }

        public void bind(NetworkHotel hotel) {
            // Bind text data
            hotelName.setText(hotel.getName());
            hotelLocation.setText(hotel.getLocation());
            hotelRating.setText(String.format(Locale.US, "Rating: %d★", hotel.getRating()));
            hotelPrice.setText(String.format(Locale.US, "$%.2f/night", hotel.getPrice()));
            serverIndicator.setText("From Server");

            // --- 4. IMAGE LOADING LOGIC ---
            String imageUrl = hotel.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl) // Load the image from the URL provided by your backend
                        .placeholder(R.drawable.placeholder_image) // Show this while loading
                        .error(R.drawable.error_image) // Show this if loading fails
                        .into(hotelImage); // The target ImageView
            } else {
                // If there's no image URL, set a default placeholder
                hotelImage.setImageResource(R.drawable.placeholder_image);
            }

            // --- CLICK LISTENERS ---
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(hotel);
                }
            });

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
