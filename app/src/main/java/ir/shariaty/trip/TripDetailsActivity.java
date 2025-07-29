package ir.shariaty.trip;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.Date;

public class TripDetailsActivity extends AppCompatActivity {

    private static final String TAG = "TripDetailsActivity";
    private RecyclerView recyclerViewAttractions, recyclerViewAlarms;
    private AttractionAdapter attractionAdapter;
    private AlarmAdapter alarmAdapter;
    private ArrayList<Attraction> attractionList = new ArrayList<>();
    private ArrayList<Alarm> alarmList = new ArrayList<>();
    private EditText editTextAttraction;
    private Button buttonConfirmAdd;
    private View layoutAddAttraction;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String tripId, tripName, startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        // اتصال به Firebase Firestore و فعال کردن کش
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();

        // مقداردهی ویوها
        recyclerViewAttractions = findViewById(R.id.recyclerViewAttractions);
        recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms); // فرض بر وجود این ویو در layout
        editTextAttraction = findViewById(R.id.editTextAttraction);
        buttonConfirmAdd = findViewById(R.id.buttonConfirmAdd);
        layoutAddAttraction = findViewById(R.id.layoutAddAttraction);
        Button buttonNewAttraction = findViewById(R.id.buttonNewAttraction);
        Button buttonAddAlarm = findViewById(R.id.buttonAddAlarm);
        ImageButton buttonBack = findViewById(R.id.buttonBack);

        TextView textTripTitle = findViewById(R.id.textTripTitle);
        TextView textTripStart = findViewById(R.id.textTripStartDate);
        TextView textTripEnd = findViewById(R.id.textTripEndDate);

        // گرفتن اطلاعات از Intent
        tripId = getIntent().getStringExtra("trip_id");
        tripName = getIntent().getStringExtra("trip_name");
        startDate = getIntent().getStringExtra("start_date");
        endDate = getIntent().getStringExtra("end_date");

        Log.d(TAG, "Received trip_id: " + tripId + ", trip_name: " + tripName);

        // نمایش اطلاعات سفر
        textTripTitle.setText(tripName != null ? tripName : "بدون عنوان");
        textTripStart.setText("تاریخ شروع: " + (startDate != null ? startDate : "نامشخص"));
        textTripEnd.setText("تاریخ پایان: " + (endDate != null ? endDate : "نامشخص"));

        // تنظیم RecyclerView برای جاذبه‌ها
        attractionAdapter = new AttractionAdapter(this, attractionList);
        recyclerViewAttractions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAttractions.setAdapter(attractionAdapter);

        // تنظیم RecyclerView برای آلارم‌ها
        alarmAdapter = new AlarmAdapter(this, alarmList);
        recyclerViewAlarms.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAlarms.setAdapter(alarmAdapter);

        // بررسی وضعیت کاربر
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not logged in");
            Toast.makeText(this, "کاربر وارد نشده است", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // بازیابی جاذبه‌ها و آلارم‌ها
        if (tripId == null || tripId.isEmpty()) {
            Log.e(TAG, "Invalid trip_id");
            Toast.makeText(this, "شناسه سفر نامعتبر است", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!isNetworkAvailable()) {
            Log.w(TAG, "No internet connection, loading from cache");
            Toast.makeText(this, "اتصال اینترنت موجود نیست، داده‌ها از کش نمایش داده می‌شوند", Toast.LENGTH_LONG).show();
        }

        // بازیابی جاذبه‌ها
        db.collection("trips").document(tripId).collection("attractions")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error loading attractions", e);
                        Toast.makeText(this, "خطا در بازیابی جاذبه‌ها: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        attractionList.clear();
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Attraction attraction = dc.getDocument().toObject(Attraction.class);
                                    attractionList.add(attraction);
                                    attractionAdapter.notifyItemInserted(attractionList.size() - 1);
                                    Log.d(TAG, "Attraction added: " + attraction.getName());
                                    break;
                                case MODIFIED:
                                    Attraction updatedAttraction = dc.getDocument().toObject(Attraction.class);
                                    for (int i = 0; i < attractionList.size(); i++) {
                                        if (attractionList.get(i).getName().equals(updatedAttraction.getName())) {
                                            attractionList.set(i, updatedAttraction);
                                            attractionAdapter.notifyItemChanged(i);
                                            Log.d(TAG, "Attraction updated: " + updatedAttraction.getName());
                                            break;
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    String removedName = dc.getDocument().toObject(Attraction.class).getName();
                                    for (int i = 0; i < attractionList.size(); i++) {
                                        if (attractionList.get(i).getName().equals(removedName)) {
                                            attractionList.remove(i);
                                            attractionAdapter.notifyItemRemoved(i);
                                            Log.d(TAG, "Attraction removed: " + removedName);
                                            break;
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                });

        // بازیابی آلارم‌ها
        db.collection("trips").document(tripId).collection("alarms")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error loading alarms", e);
                        Toast.makeText(this, "خطا در بازیابی آلارم‌ها: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        alarmList.clear();
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Alarm alarm = dc.getDocument().toObject(Alarm.class);
                                    alarmList.add(alarm);
                                    alarmAdapter.notifyItemInserted(alarmList.size() - 1);
                                    Log.d(TAG, "Alarm added: " + alarm.getName());
                                    break;
                                case MODIFIED:
                                    Alarm updatedAlarm = dc.getDocument().toObject(Alarm.class);
                                    for (int i = 0; i < alarmList.size(); i++) {
                                        if (alarmList.get(i).getId().equals(updatedAlarm.getId())) {
                                            alarmList.set(i, updatedAlarm);
                                            alarmAdapter.notifyItemChanged(i);
                                            Log.d(TAG, "Alarm updated: " + updatedAlarm.getName());
                                            break;
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    String removedId = dc.getDocument().getId();
                                    for (int i = 0; i < alarmList.size(); i++) {
                                        if (alarmList.get(i).getId().equals(removedId)) {
                                            alarmList.remove(i);
                                            alarmAdapter.notifyItemRemoved(i);
                                            Log.d(TAG, "Alarm removed: " + removedId);
                                            break;
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                });

        // افزودن جاذبه جدید
        buttonConfirmAdd.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "اتصال اینترنت موجود نیست", Toast.LENGTH_LONG).show();
                return;
            }
            String name = editTextAttraction.getText().toString().trim();
            if (!name.isEmpty() && tripId != null) {
                Attraction attraction = new Attraction("", name, new Date());
                db.collection("trips").document(tripId).collection("attractions")
                        .add(attraction)
                        .addOnSuccessListener(documentReference -> {
                            editTextAttraction.setText("");
                            layoutAddAttraction.setVisibility(View.GONE);
                            Toast.makeText(this, "جاذبه اضافه شد", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Attraction saved: " + name);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error saving attraction", e);
                            Toast.makeText(this, "خطا در افزودن جاذبه: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(this, "نام جاذبه یا شناسه سفر نامعتبر است", Toast.LENGTH_SHORT).show();
            }
        });

        // نمایش فرم اضافه کردن جاذبه
        buttonNewAttraction.setOnClickListener(v -> layoutAddAttraction.setVisibility(View.VISIBLE));

        // دکمه بازگشت
        buttonBack.setOnClickListener(v -> finish());

        // رفتن به صفحه تنظیم آلارم
        buttonAddAlarm.setOnClickListener(v -> {
            if (tripId == null) {
                Toast.makeText(this, "شناسه سفر نامعتبر است", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(TripDetailsActivity.this, AddAlarmActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}