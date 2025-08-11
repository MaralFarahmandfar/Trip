package ir.shariaty.trip;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private Button buttonConfirmAdd, buttonNewAttraction, buttonAddAlarm;
    private ImageView imageViewProfile;
    private ImageButton buttonBack;
    private View layoutAddAttraction;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String tripId, tripName, startDate, endDate;
    private ListenerRegistration attractionsListener, alarmsListener;
    private boolean isLoadingAttractions = false;
    private boolean isLoadingAlarms = false;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        // اتصال به Firebase Firestore
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();

        // مقداردهی ویوها
        recyclerViewAttractions = findViewById(R.id.recyclerViewAttractions);
        recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms);
        editTextAttraction = findViewById(R.id.editTextAttraction);
        buttonConfirmAdd = findViewById(R.id.buttonConfirmAdd);
        layoutAddAttraction = findViewById(R.id.layoutAddAttraction);
        buttonNewAttraction = findViewById(R.id.buttonNewAttraction);
        buttonAddAlarm = findViewById(R.id.buttonAddAlarm);
        buttonBack = findViewById(R.id.buttonBack);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        TextView textTripTitle = findViewById(R.id.textTripTitle);
        TextView textTripStart = findViewById(R.id.textTripStartDate);
        TextView textTripEnd = findViewById(R.id.textTripEndDate);

        // دریافت اطلاعات از Intent
        tripId = getIntent().getStringExtra("trip_id");
        tripName = getIntent().getStringExtra("trip_name");
        startDate = getIntent().getStringExtra("start_date");
        endDate = getIntent().getStringExtra("end_date");

        // تنظیم عنوان و تاریخ‌ها
        textTripTitle.setText(tripName != null ? tripName : "بدون عنوان");
        textTripStart.setText("شروع: " + (startDate != null ? startDate : "نامشخص"));
        textTripEnd.setText("پایان: " + (endDate != null ? endDate : "نامشخص"));

        // تنظیم RecyclerView برای جاذبه‌ها
        recyclerViewAttractions.setLayoutManager(new LinearLayoutManager(this));
        attractionAdapter = new AttractionAdapter(this, attractionList, tripId);
        recyclerViewAttractions.setAdapter(attractionAdapter);

        // تنظیم RecyclerView برای آلارم‌ها
        recyclerViewAlarms.setLayoutManager(new LinearLayoutManager(this));
        alarmAdapter = new AlarmAdapter(this, alarmList, alarm -> {
            // حذف آلارم از لیست محلی و به‌روزرسانی فوری UI
            alarmList.remove(alarm);
            alarmAdapter.updateAlarms(new ArrayList<>(alarmList));

            // سپس حذف از Firebase
            db.collection("trips").document(tripId).collection("alarms")
                    .document(alarm.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(TripDetailsActivity.this, "آلارم حذف شد", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(TripDetailsActivity.this, "خطا در حذف آلارم", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting alarm", e);

                        // اگر حذف موفق نبود، آلارم را دوباره به لیست محلی اضافه کن و آداپتر را به‌روزرسانی کن
                        alarmList.add(alarm);
                        alarmAdapter.updateAlarms(new ArrayList<>(alarmList));
                    });
        });

        recyclerViewAlarms.setAdapter(alarmAdapter);

        // کلیک روی دکمه بازگشت
        buttonBack.setOnClickListener(v -> finish());

        // کلیک روی پروفایل برای نمایش منوی پاپ‌آپ
        imageViewProfile.setOnClickListener(v -> showProfilePopup(v));

        // کلیک برای نمایش فرم اضافه کردن جاذبه
        buttonNewAttraction.setOnClickListener(v -> {
            layoutAddAttraction.setVisibility(
                    layoutAddAttraction.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });

        // کلیک برای تأیید اضافه کردن جاذبه
        buttonConfirmAdd.setOnClickListener(v -> {
            String attractionName = editTextAttraction.getText().toString().trim();
            if (attractionName.isEmpty()) {
                Toast.makeText(this, "لطفاً نام جاذبه را وارد کنید", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "اتصال اینترنت موجود نیست", Toast.LENGTH_LONG).show();
                return;
            }
            Attraction attraction = new Attraction("", attractionName, new Date(), false);
            db.collection("trips").document(tripId).collection("attractions")
                    .add(attraction)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "جاذبه اضافه شد", Toast.LENGTH_SHORT).show();
                        editTextAttraction.setText("");
                        layoutAddAttraction.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding attraction", e);
                        Toast.makeText(this, "خطا در اضافه کردن جاذبه: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        // کلیک برای اضافه کردن آلارم
        buttonAddAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAlarmActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });

        // بارگذاری جاذبه‌ها و آلارم‌ها
        loadAttractions();
        loadAlarms();
    }

    // نمایش منوی پاپ‌آپ
    private void showProfilePopup(View anchorView) {
        // ایجاد ویو برای پاپ‌آپ
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_profile, null);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        // اتصال به ویوهای پاپ‌آپ
        TextView textViewUserName = popupView.findViewById(R.id.textViewUserName);
        Button buttonLogout = popupView.findViewById(R.id.buttonLogout);

        // تنظیم نام کاربر
        FirebaseUser user = mAuth.getCurrentUser();
        String userName = user != null ? (user.getDisplayName() != null && !user.getDisplayName().isEmpty() ? user.getDisplayName() : user.getEmail()) : "کاربر";
        textViewUserName.setText(userName);

        // کلیک روی دکمه خروج
        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "با موفقیت خارج شدید", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            popupWindow.dismiss();
        });

        // نمایش پاپ‌آپ در زیر ImageView
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(null); // برای بسته شدن با کلیک بیرون
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, location[0], location[1] + anchorView.getHeight());
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
                });
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
                });
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
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlarms();
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