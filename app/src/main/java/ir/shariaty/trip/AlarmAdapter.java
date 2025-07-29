package ir.shariaty.trip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {
    private final Context context;
    private final List<Alarm> alarmList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public AlarmAdapter(Context context, List<Alarm> alarmList) {
        this.context = context;
        this.alarmList = alarmList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);
        holder.textViewName.setText(alarm.getName() != null ? alarm.getName() : "بدون نام");
        holder.textViewDateTime.setText(alarm.getDateTime() != null ? sdf.format(alarm.getDateTime()) : "نامشخص");
        holder.textViewNote.setText(alarm.getNote() != null && !alarm.getNote().isEmpty() ? alarm.getNote() : "بدون یادداشت");
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDateTime, textViewNote;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewAlarmName);
            textViewDateTime = itemView.findViewById(R.id.textViewAlarmDateTime);
            textViewNote = itemView.findViewById(R.id.textViewAlarmNote);
        }
    }
}