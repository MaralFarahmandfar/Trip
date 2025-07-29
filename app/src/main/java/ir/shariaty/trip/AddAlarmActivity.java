package ir.shariaty.trip;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

public class AddAlarmActivity extends AppCompatActivity {

    private static final String TAG = "AddAlarmActivity";
    private EditText editTextAlarmName, editTextAlarmNote;
    private Button buttonPickDate, buttonPickTime, buttonSaveAlarm;
    private TextView textSelectedDate, textSelectedTime;
    private FirebaseFirestore db;
    private String tripId;
    private int selectedYear = -1, selectedMonth = -1, selectedDay = -1, selectedHour = -1, selectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        // اتصال به Firestore
        db = FirebaseFirestore.getInstance();

        // گرفتن tripId از Intent
        tripId = getIntent().getStringExtra("trip_id");

        // مقداردهی ویوها
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

            // تنظیم آلارم محلی
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("alarm_name", name + (note.isEmpty() ? "" : " - " + note));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    (int) System.currentTimeMillis(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
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

                // ذخیره آلارم در Firestore
                Alarm alarm = new Alarm("", name, note, new Date(calendar.getTimeInMillis()), tripId);
                db.collection("trips").document(tripId).collection("alarms")
                        .add(alarm)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Alarm saved with ID: " + documentReference.getId());
                            Toast.makeText(this, "آلارم تنظیم و ذخیره شد", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error saving alarm", e);
                            Toast.makeText(this, "خطا در ذخیره آلارم: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(this, "خطا در تنظیم آلارم", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}