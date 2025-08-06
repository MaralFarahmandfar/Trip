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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private ListenerRegistration attractionsListener, alarmsListener;
    private boolean isLoadingAttractions = false;
    private boolean isLoadingAlarms = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        // اتصال به Firebase Firestore
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false) // غیرفعال کردن کش
                .build();
        db.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();

        // مقداردهی ویوها
        recyclerViewAttractions = findViewById(R.id.recyclerViewAttractions);
        recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms);
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
        attractionAdapter = new AttractionAdapter(this, attractionList, tripId);
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
            Log.w(TAG, "No internet connection, app requires internet since cache is disabled");
            Toast.makeText(this, "اتصال اینترنت موجود نیست، لطفاً به اینترنت متصل شوید", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // بازیابی جاذبه‌ها و آلارم‌ها
        loadAttractions();
        loadAlarms();

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
                            attraction.setId(documentReference.getId());
                            editTextAttraction.setText("");
                            layoutAddAttraction.setVisibility(View.GONE);
                            Toast.makeText(this, "جاذبه اضافه شد", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Attraction saved: " + name + ", id: " + documentReference.getId());
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
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // رفتن به صفحه تنظیم آلارم
        buttonAddAlarm.setOnClickListener(v -> {
            if (tripId == null) {
                Toast.makeText(this, "شناسه سفر نامعتبر است", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(TripDetailsActivity.this, AddAlarmActivity.class);
                intent.putExtra("trip_id", tripId);
                startActivity(intent);
            }
        });
    }

    private void loadAttractions() {
        if (isLoadingAttractions) {
            Log.d(TAG, "loadAttractions skipped: already loading");
            return;
        }
        isLoadingAttractions = true;
        attractionsListener = db.collection("trips").document(tripId).collection("attractions")
                .addSnapshotListener(this, (queryDocumentSnapshots, e) -> {
                    isLoadingAttractions = false;
                    if (e != null) {
                        Log.e(TAG, "Error loading attractions", e);
                        Toast.makeText(this, "خطا در بازیابی جاذبه‌ها: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        updateAttractions(queryDocumentSnapshots);
                    }
                });
    }

    private void updateAttractions(QuerySnapshot queryDocumentSnapshots) {
        if (queryDocumentSnapshots != null) {
            List<Attraction> newAttractions = new ArrayList<>();
            for (var doc : queryDocumentSnapshots.getDocuments()) {
                Attraction attraction = doc.toObject(Attraction.class);
                attraction.setId(doc.getId());
                newAttractions.add(attraction);
                Log.d(TAG, "Attraction processed: " + attraction.getName() + ", id: " + doc.getId());
            }
            attractionAdapter.updateAttractions(newAttractions);
            Log.d(TAG, "Attractions updated, size: " + newAttractions.size());
        }
    }

    private void loadAlarms() {
        if (isLoadingAlarms) {
            Log.d(TAG, "loadAlarms skipped: already loading");
            return;
        }
        isLoadingAlarms = true;
        alarmsListener = db.collection("trips").document(tripId).collection("alarms")
                .addSnapshotListener(this, (queryDocumentSnapshots, e) -> {
                    isLoadingAlarms = false;
                    if (e != null) {
                        Log.e(TAG, "Error loading alarms", e);
                        Toast.makeText(this, "خطا در بازیابی آلارم‌ها: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        updateAlarms(queryDocumentSnapshots);
                    }
                });
    }

    private void updateAlarms(QuerySnapshot queryDocumentSnapshots) {
        if (queryDocumentSnapshots != null) {
            List<Alarm> newAlarms = new ArrayList<>();
            for (var doc : queryDocumentSnapshots.getDocuments()) {
                Alarm alarm = doc.toObject(Alarm.class);
                alarm.setId(doc.getId());
                newAlarms.add(alarm);
                Log.d(TAG, "Alarm processed: " + alarm.getName() + ", id: " + doc.getId());
            }
            alarmAdapter.updateAlarms(newAlarms);
            Log.d(TAG, "Alarms updated, size: " + newAlarms.size());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (attractionsListener != null) {
            attractionsListener.remove();
            attractionsListener = null;
            Log.d(TAG, "Attractions SnapshotListener removed");
        }
        if (alarmsListener != null) {
            alarmsListener.remove();
            alarmsListener = null;
            Log.d(TAG, "Alarms SnapshotListener removed");
        }
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