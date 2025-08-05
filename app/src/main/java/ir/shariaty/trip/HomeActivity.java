package ir.shariaty.trip;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    LinearLayout layoutNewTripForm;
    Button buttonNewTrip, buttonPickStartDate, buttonPickEndDate, buttonSaveTrip;
    TextView textStartDate, textEndDate;
    EditText editTextTripName;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Trip> tripList = new ArrayList<>();
    private TripAdapter adapter;
    private ListenerRegistration tripsListener;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // اتصال به Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false) // غیرفعال کردن کش
                .build();
        db.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();

        // اتصال به ویوها
        layoutNewTripForm = findViewById(R.id.layoutNewTripForm);
        buttonNewTrip = findViewById(R.id.buttonNewTrip);
        buttonPickStartDate = findViewById(R.id.buttonPickStartDate);
        buttonPickEndDate = findViewById(R.id.buttonPickEndDate);
        buttonSaveTrip = findViewById(R.id.buttonSaveTrip);
        textStartDate = findViewById(R.id.textStartDate);
        textEndDate = findViewById(R.id.textEndDate);
        editTextTripName = findViewById(R.id.editTextTripName);

        // تنظیم RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTrips);
        adapter = new TripAdapter(this, tripList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // بررسی وضعیت کاربر
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not logged in");
            Toast.makeText(this, "کاربر وارد نشده است", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // نمایش فرم با کلیک روی دکمه سفر جدید
        buttonNewTrip.setOnClickListener(v -> layoutNewTripForm.setVisibility(View.VISIBLE));

        // انتخاب تاریخ شروع
        buttonPickStartDate.setOnClickListener(v -> showDatePicker(textStartDate));

        // انتخاب تاریخ پایان
        buttonPickEndDate.setOnClickListener(v -> showDatePicker(textEndDate));

        // ذخیره سفر در Firestore
        buttonSaveTrip.setOnClickListener(v -> {
            String name = editTextTripName.getText().toString().trim();
            Date startDate = (Date) textStartDate.getTag();
            Date endDate = (Date) textEndDate.getTag();

            if (name.isEmpty() || startDate == null || endDate == null) {
                Toast.makeText(this, "لطفاً همه فیلدها را کامل کنید", Toast.LENGTH_SHORT).show();
            } else if (endDate.before(startDate)) {
                Toast.makeText(this, "تاریخ پایان باید بعد از تاریخ شروع باشد", Toast.LENGTH_SHORT).show();
            } else if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "کاربر وارد نشده است", Toast.LENGTH_SHORT).show();
            } else {
                String uid = mAuth.getCurrentUser().getUid();
                Trip newTrip = new Trip("", name, uid, startDate, endDate);
                db.collection("trips")
                        .add(newTrip)
                        .addOnSuccessListener(documentReference -> {
                            newTrip.setId(documentReference.getId());
                            Toast.makeText(this, "سفر '" + name + "' با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show();
                            layoutNewTripForm.setVisibility(View.GONE);
                            editTextTripName.setText("");
                            textStartDate.setText("تاریخ شروع: انتخاب نشده");
                            textEndDate.setText("تاریخ پایان: انتخاب نشده");
                            textStartDate.setTag(null);
                            textEndDate.setTag(null);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "خطا در ذخیره‌سازی: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (tripsListener != null) {
            tripsListener.remove();
            tripsListener = null;
            Log.d(TAG, "Existing SnapshotListener removed in onStart");
        }
        loadTrips();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tripsListener != null) {
            tripsListener.remove();
            tripsListener = null;
            Log.d(TAG, "SnapshotListener removed in onStop");
        }
    }

    // نمایش دیالوگ تاریخ
    private void showDatePicker(TextView target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String date = sdf.format(selectedDate.getTime());
                    target.setText(date);
                    target.setTag(selectedDate.getTime());
                }, year, month, day);

        datePicker.show();
    }

    // بازیابی سفرها از Firestore
    private void loadTrips() {
        if (isLoading) {
            Log.d(TAG, "loadTrips skipped: already loading");
            return;
        }
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "کاربر وارد نشده است", Toast.LENGTH_SHORT).show();
            return;
        }
        isLoading = true;
        String uid = mAuth.getCurrentUser().getUid();
        tripsListener = db.collection("trips")
                .whereEqualTo("uid", uid)
                .addSnapshotListener(this, (queryDocumentSnapshots, e) -> {
                    isLoading = false;
                    if (e != null) {
                        Log.e(TAG, "Error loading trips", e);
                        Toast.makeText(this, "خطا در بازیابی سفرها: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        updateTrips(queryDocumentSnapshots);
                    }
                });
    }

    private void updateTrips(QuerySnapshot queryDocumentSnapshots) {
        if (queryDocumentSnapshots != null) {
            // استفاده از Set برای جلوگیری از آیتم‌های تکراری
            Set<String> tripIds = new HashSet<>();
            List<Trip> newTrips = new ArrayList<>();
            for (var doc : queryDocumentSnapshots.getDocuments()) {
                String docId = doc.getId();
                if (!tripIds.contains(docId)) {
                    Trip trip = doc.toObject(Trip.class);
                    trip.setId(docId);
                    newTrips.add(trip);
                    tripIds.add(docId);
                    Log.d(TAG, "Trip processed: " + trip.getName() + ", id: " + docId);
                } else {
                    Log.w(TAG, "Duplicate trip ignored: " + docId);
                }
            }
            adapter.updateTrips(newTrips);
            Log.d(TAG, "Trips updated, size: " + newTrips.size());
        }
    }
}