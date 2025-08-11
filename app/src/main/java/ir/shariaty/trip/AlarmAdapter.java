package ir.shariaty.trip;

import android.content.Context;
import android.util.Log;
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

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {
    private static final String TAG = "AlarmAdapter";
    private final Context context;
    private final List<Alarm> alarmList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public interface OnAlarmDeleteListener {
        void onAlarmDelete(Alarm alarm);
    }

    private final OnAlarmDeleteListener deleteListener;

    public AlarmAdapter(Context context, List<Alarm> alarmList, OnAlarmDeleteListener deleteListener) {
        this.context = context;
        this.alarmList = new ArrayList<>(alarmList);
        this.deleteListener = deleteListener;
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

        holder.buttonDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onAlarmDelete(alarm);
            }
        });
    }


    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + alarmList.size());
        return alarmList.size();
    }

    public void updateAlarms(List<Alarm> newAlarms) {
        Log.d(TAG, "Updating alarms, new size: " + newAlarms.size());
        AlarmDiffCallback diffCallback = new AlarmDiffCallback(alarmList, newAlarms);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        alarmList.clear();
        alarmList.addAll(newAlarms);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDateTime, textViewNote;
        ImageButton buttonDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewAlarmName);
            textViewDateTime = itemView.findViewById(R.id.textViewAlarmDateTime);
            textViewNote = itemView.findViewById(R.id.textViewAlarmNote);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteAlarm);
        }
    }


    static class AlarmDiffCallback extends DiffUtil.Callback {
        private final List<Alarm> oldList;
        private final List<Alarm> newList;

        AlarmDiffCallback(List<Alarm> oldList, List<Alarm> newList) {
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
            String oldId = oldList.get(oldItemPosition).getId();
            String newId = newList.get(newItemPosition).getId();
            return oldId != null && oldId.equals(newId);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Alarm oldAlarm = oldList.get(oldItemPosition);
            Alarm newAlarm = newList.get(newItemPosition);
            return (oldAlarm.getName() == null ? newAlarm.getName() == null : oldAlarm.getName().equals(newAlarm.getName())) &&
                    (oldAlarm.getNote() == null ? newAlarm.getNote() == null : oldAlarm.getNote().equals(newAlarm.getNote())) &&
                    (oldAlarm.getDateTime() == null ? newAlarm.getDateTime() == null : oldAlarm.getDateTime().equals(newAlarm.getDateTime()));
        }
    }
}