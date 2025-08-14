package ir.shariaty.trip;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    public interface OnTripDeleteListener {
        void onTripDelete(Trip trip, int position);
    }

    private final List<Trip> tripList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final Context context;
    private final OnTripDeleteListener deleteListener;

    public TripAdapter(Context context, List<Trip> tripList, OnTripDeleteListener deleteListener) {
        this.context = context;
        this.tripList = new ArrayList<>(tripList);
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = tripList.get(position);
        holder.name.setText(trip.getName() != null ? trip.getName() : "بدون عنوان");
        String dates = (trip.getStartDate() != null ? sdf.format(trip.getStartDate()) : "نامشخص") +
                " - " +
                (trip.getEndDate() != null ? sdf.format(trip.getEndDate()) : "نامشخص");
        holder.dates.setText(dates);

        // کلیک روی آیتم برای رفتن به جزئیات سفر
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TripDetailsActivity.class);
            intent.putExtra("trip_id", trip.getId());
            intent.putExtra("trip_name", trip.getName());
            intent.putExtra("start_date", trip.getStartDate() != null ? sdf.format(trip.getStartDate()) : null);
            intent.putExtra("end_date", trip.getEndDate() != null ? sdf.format(trip.getEndDate()) : null);
            context.startActivity(intent);
        });

        // کلیک روی آیکون حذف
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onTripDelete(trip, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    // متد به‌روزرسانی لیست با DiffUtil
    public void updateTrips(List<Trip> newTrips) {
        TripDiffCallback diffCallback = new TripDiffCallback(tripList, newTrips);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        tripList.clear();
        tripList.addAll(newTrips);
        diffResult.dispatchUpdatesTo(this);
    }

    // حذف آیتم محلی (برای استفاده بعد از حذف موفقیت‌آمیز در Firebase)
    public void removeTripAt(int position) {
        if (position >= 0 && position < tripList.size()) {
            tripList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView name, dates;
        ImageButton deleteButton;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tripName);
            dates = itemView.findViewById(R.id.tripDates);
            deleteButton = itemView.findViewById(R.id.buttonDeleteTrip);
        }
    }

    static class TripDiffCallback extends DiffUtil.Callback {
        private final List<Trip> oldList;
        private final List<Trip> newList;

        TripDiffCallback(List<Trip> oldList, List<Trip> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Trip oldTrip = oldList.get(oldItemPosition);
            Trip newTrip = newList.get(newItemPosition);
            return oldTrip.getName().equals(newTrip.getName()) &&
                    (oldTrip.getStartDate() == null ? newTrip.getStartDate() == null : oldTrip.getStartDate().equals(newTrip.getStartDate())) &&
                    (oldTrip.getEndDate() == null ? newTrip.getEndDate() == null : oldTrip.getEndDate().equals(newTrip.getEndDate()));
        }
    }
}
