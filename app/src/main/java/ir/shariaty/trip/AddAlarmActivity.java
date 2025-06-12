
package ir.shariaty.trip;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddAlarmActivity extends AppCompatActivity {

    private EditText editTextAlarmName, editTextAlarmNote;
    private Button buttonPickDate, buttonPickTime, buttonSaveAlarm;
    private TextView textSelectedDate, textSelectedTime;

    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alarm_channel",
                    "Alarm Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        editTextAlarmName = findViewById(R.id.editTextAlarmName);
        editTextAlarmNote = findViewById(R.id.editTextAlarmNote);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        buttonPickTime = findViewById(R.id.buttonPickTime);
        textSelectedDate = findViewById(R.id.textSelectedDate);
        textSelectedTime = findViewById(R.id.textSelectedTime);
        buttonSaveAlarm = findViewById(R.id.buttonSaveAlarm);

        buttonPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;
                textSelectedDate.setText("تاریخ: " + year + "/" + (month+1) + "/" + dayOfMonth);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        buttonPickTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                textSelectedTime.setText("ساعت: " + hourOfDay + ":" + minute);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        buttonSaveAlarm.setOnClickListener(v -> {
            String name = editTextAlarmName.getText().toString();
            String note = editTextAlarmNote.getText().toString();

            Calendar calendar = Calendar.getInstance();
            calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("alarm_name", name + " - " + note);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Toast.makeText(this, "آلارم تنظیم شد", Toast.LENGTH_SHORT).show();
            }

            finish();
        });
    }
}
