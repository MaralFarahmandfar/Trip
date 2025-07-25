package ir.shariaty.trip;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private final List<Trip> tripList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public TripAdapter(List<Trip> tripList) {
        this.tripList = tripList;
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
        holder.name.setText(trip.getName());
        String dates = (trip.getStartDate() != null ? sdf.format(trip.getStartDate()) : "نامشخص") +
                " - " +
                (trip.getEndDate() != null ? sdf.format(trip.getEndDate()) : "نامشخص");
        holder.dates.setText(dates);

        // کلیک روی هر آیتم
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, TripDetailsActivity.class);
            intent.putExtra("trip_id", trip.getId());
            intent.putExtra("trip_name", trip.getName());
            intent.putExtra("start_date", trip.getStartDate() != null ? sdf.format(trip.getStartDate()) : null);
            intent.putExtra("end_date", trip.getEndDate() != null ? sdf.format(trip.getEndDate()) : null);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView name, dates;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tripName);
            dates = itemView.findViewById(R.id.tripDates);
        }
    }
}