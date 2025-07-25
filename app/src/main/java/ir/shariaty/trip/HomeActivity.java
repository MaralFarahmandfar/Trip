package ir.shariaty.trip;

import android.app.DatePickerDialog;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    LinearLayout layoutNewTripForm;
    Button buttonNewTrip, buttonPickStartDate, buttonPickEndDate, buttonSaveTrip;
    TextView textStartDate, textEndDate;
    EditText editTextTripName;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Trip> tripList = new ArrayList<>();
    private TripAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // اتصال به Firebase
        db = FirebaseFirestore.getInstance();
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
        adapter = new TripAdapter(tripList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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
                            Trip savedTrip = new Trip(documentReference.getId(), name, uid, startDate, endDate);
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
        loadTrips();
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
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "کاربر وارد نشده است", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("trips")
                .whereEqualTo("uid", uid)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "خطا در بازیابی سفرها: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Trip trip = dc.getDocument().toObject(Trip.class);
                                trip = new Trip(dc.getDocument().getId(), trip.getName(), trip.getUid(), trip.getStartDate(), trip.getEndDate());
                                tripList.add(trip);
                                adapter.notifyItemInserted(tripList.size() - 1);
                                break;
                            case MODIFIED:
                                Trip updatedTrip = dc.getDocument().toObject(Trip.class);
                                updatedTrip = new Trip(dc.getDocument().getId(), updatedTrip.getName(), updatedTrip.getUid(), updatedTrip.getStartDate(), updatedTrip.getEndDate());
                                for (int i = 0; i < tripList.size(); i++) {
                                    if (tripList.get(i).getId().equals(updatedTrip.getId())) {
                                        tripList.set(i, updatedTrip);
                                        adapter.notifyItemChanged(i);
                                        break;
                                    }
                                }
                                break;
                            case REMOVED:
                                String removedId = dc.getDocument().getId();
                                for (int i = 0; i < tripList.size(); i++) {
                                    if (tripList.get(i).getId().equals(removedId)) {
                                        tripList.remove(i);
                                        adapter.notifyItemRemoved(i);
                                        break;
                                    }
                                }
                                break;
                        }
                    }
                });
    }
}