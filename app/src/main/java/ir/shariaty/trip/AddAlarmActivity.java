package ir.shariaty.trip;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
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

    private int selectedYear = -1, selectedMonth = -1, selectedDay = -1, selectedHour = -1, selectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        editTextAlarmName = findViewById(R.id.editTextAlarmName);
        editTextAlarmNote = findViewById(R.id.editTextAlarmNote);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        buttonPickTime = findViewById(R.id.buttonPickTime);
        textSelectedDate = findViewById(R.id.textSelectedDate);
        textSelectedTime = findViewById(R.id.textSelectedTime);
        buttonSaveAlarm = findViewById(R.id.buttonSaveAlarm);

        // انتخاب تاریخ
        buttonPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;
                textSelectedDate.setText("تاریخ: " + year + "/" + (month + 1) + "/" + dayOfMonth);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // انتخاب ساعت
        buttonPickTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                textSelectedTime.setText("ساعت: " + hourOfDay + ":" + String.format("%02d", minute));
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        // ذخیره آلارم
        buttonSaveAlarm.setOnClickListener(v -> {
            String name = editTextAlarmName.getText().toString().trim();
            String note = editTextAlarmNote.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "نام آلارم را وارد کنید", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedYear == -1 || selectedHour == -1) {
                Toast.makeText(this, "تاریخ و ساعت را انتخاب کنید", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);

            if (calendar.before(Calendar.getInstance())) {
                Toast.makeText(this, "زمان انتخاب شده معتبر نیست", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("alarm_name", name + (note.isEmpty() ? "" : " - " + note));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    (int) System.currentTimeMillis(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Toast.makeText(this, "اجازه آلارم دقیق داده نشده است", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Toast.makeText(this, "آلارم تنظیم شد", Toast.LENGTH_SHORT).show();
            }

            finish();
        });
    }
}
