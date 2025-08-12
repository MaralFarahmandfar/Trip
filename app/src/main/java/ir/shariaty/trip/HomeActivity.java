package ir.shariaty.trip;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
    ImageView profileImageView;
    TextView textStartDate, textEndDate;
    EditText editTextTripName;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Trip> tripList = new ArrayList<>();
    private TripAdapter adapter;
    private ListenerRegistration tripsListener;
    private boolean isLoading = false;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // اتصال به Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();

        // اتصال به ویوها
        layoutNewTripForm = findViewById(R.id.layoutNewTripForm);
        buttonNewTrip = findViewById(R.id.buttonNewTrip);
        buttonPickStartDate = findViewById(R.id.buttonPickStartDate);
        buttonPickEndDate = findViewById(R.id.buttonPickEndDate);
        buttonSaveTrip = findViewById(R.id.buttonSaveTrip);
        profileImageView = findViewById(R.id.imageViewProfile);
        textStartDate = findViewById(R.id.textStartDate);
        textEndDate = findViewById(R.id.textEndDate);
        editTextTripName = findViewById(R.id.editTextTripName);

        // تنظیم RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTrips);
        adapter = new TripAdapter(this, tripList, (trip, position) -> deleteTripFromFirebase(trip));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // کلیک روی پروفایل برای نمایش منوی پاپ‌آپ
        profileImageView.setOnClickListener(v -> showProfilePopup(v));

        // بررسی وضعیت کاربر
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "No user logged in, redirecting to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // تنظیم کلیک برای دکمه‌ها
        buttonNewTrip.setOnClickListener(v -> {
            layoutNewTripForm.setVisibility(
                    layoutNewTripForm.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });

        buttonPickStartDate.setOnClickListener(v -> showDatePicker(textStartDate));
        buttonPickEndDate.setOnClickListener(v -> showDatePicker(textEndDate));

        buttonSaveTrip.setOnClickListener(v -> {
            String tripName = editTextTripName.getText().toString().trim();
            Date startDate = (Date) textStartDate.getTag();
            Date endDate = (Date) textEndDate.getTag();

            if (tripName.isEmpty() || startDate == null || endDate == null) {
                Toast.makeText(this, "لطفاً تمام فیلدها را پر کنید", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDate.after(endDate)) {
                Toast.makeText(this, "تاریخ شروع باید قبل از تاریخ پایان باشد", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();
            Trip trip = new Trip("", tripName, uid, startDate, endDate);
            db.collection("trips").add(trip)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "سفر ذخیره شد", Toast.LENGTH_SHORT).show();
                        editTextTripName.setText("");
                        textStartDate.setText("");
                        textEndDate.setText("");
                        layoutNewTripForm.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving trip", e);
                        Toast.makeText(this, "خطا در ذخیره سفر: ", Toast.LENGTH_LONG).show();
                    });
        });

        // بارگذاری سفرها
        loadTrips();
    }
    private void deleteTripFromFirebase(Trip trip) {
        if (trip == null || trip.getId() == null || trip.getId().isEmpty()) return;

        db.collection("trips").document(trip.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "سفر حذف شد", Toast.LENGTH_SHORT).show();
                    // حذف محلی حذف شود، چون Listener خودش لیست را آپدیت می‌کند
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "خطا در حذف سفر: ", Toast.LENGTH_LONG).show();
                });
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

    // نمایش دیالوگ تاریخ
    private void showDatePicker(TextView target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new android.app.DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String date = sdf.format(selectedDate.getTime());
                    target.setText(date);
                    target.setTag(selectedDate.getTime());
                }, year, month, day).show();
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
                        Toast.makeText(this, "خطا در بازیابی سفرها: ", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onStop() {
        super.onStop();
        if (tripsListener != null) {
            tripsListener.remove();
            tripsListener = null;
            Log.d(TAG, "Trips SnapshotListener removed");
        }
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadTrips();
    }
}