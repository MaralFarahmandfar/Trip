package ir.shariaty.trip;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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

public class TripDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AttractionAdapter adapter;
    private ArrayList<Attraction> attractionList = new ArrayList<>();
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
        recyclerView = findViewById(R.id.recyclerViewAttractions);
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

        // نمایش اطلاعات سفر
        textTripTitle.setText(tripName != null ? tripName : "بدون عنوان");
        textTripStart.setText("تاریخ شروع: " + (startDate != null ? startDate : "نامشخص"));
        textTripEnd.setText("تاریخ پایان: " + (endDate != null ? endDate : "نامشخص"));

        // تنظیم RecyclerView
        adapter = new AttractionAdapter(this, attractionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // بررسی وضعیت کاربر
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "کاربر وارد نشده است", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // بازیابی جاذبه‌ها با چک اتصال اینترنت
        if (tripId != null) {
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "اتصال اینترنت موجود نیست، داده‌ها از کش نمایش داده می‌شوند", Toast.LENGTH_LONG).show();
            }
            db.collection("trips").document(tripId).collection("attractions")
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Toast.makeText(this, "خطا در بازیابی: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (queryDocumentSnapshots != null) {
                            attractionList.clear();
                            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                                switch (dc.getType()) {
                                    case ADDED:
                                        Attraction attraction = dc.getDocument().toObject(Attraction.class);
                                        attractionList.add(attraction);
                                        adapter.notifyItemInserted(attractionList.size() - 1);
                                        break;
                                    case MODIFIED:
                                        Attraction updatedAttraction = dc.getDocument().toObject(Attraction.class);
                                        for (int i = 0; i < attractionList.size(); i++) {
                                            if (attractionList.get(i).getName().equals(updatedAttraction.getName())) {
                                                attractionList.set(i, updatedAttraction);
                                                adapter.notifyItemChanged(i);
                                                break;
                                            }
                                        }
                                        break;
                                    case REMOVED:
                                        String removedName = dc.getDocument().toObject(Attraction.class).getName();
                                        for (int i = 0; i < attractionList.size(); i++) {
                                            if (attractionList.get(i).getName().equals(removedName)) {
                                                attractionList.remove(i);
                                                adapter.notifyItemRemoved(i);
                                                break;
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "شناسه سفر نامعتبر است", Toast.LENGTH_LONG).show();
            finish();
        }

        // افزودن جاذبه جدید
        buttonConfirmAdd.setOnClickListener(v -> {
            String name = editTextAttraction.getText().toString().trim();
            if (!name.isEmpty() && tripId != null) {
                Attraction attraction = new Attraction(name);
                db.collection("trips").document(tripId).collection("attractions")
                        .add(attraction)
                        .addOnSuccessListener(documentReference -> {
                            editTextAttraction.setText("");
                            layoutAddAttraction.setVisibility(View.GONE);
                            Toast.makeText(this, "جاذبه اضافه شد", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "خطا: " + e.getMessage(), Toast.LENGTH_LONG).show());
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

    // تابع بررسی اتصال اینترنت
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
