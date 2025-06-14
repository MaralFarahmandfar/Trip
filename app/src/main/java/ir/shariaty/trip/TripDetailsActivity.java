package ir.shariaty.trip;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TripDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AttractionAdapter adapter;
    private ArrayList<Attraction> attractionList;
    private EditText editTextAttraction;
    private Button buttonConfirmAdd;
    private View layoutAddAttraction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        // اتصال به ویوها
        recyclerView = findViewById(R.id.recyclerViewAttractions);
        editTextAttraction = findViewById(R.id.editTextAttraction);
        buttonConfirmAdd = findViewById(R.id.buttonConfirmAdd);
        layoutAddAttraction = findViewById(R.id.layoutAddAttraction);
        Button buttonNewAttraction = findViewById(R.id.buttonNewAttraction);
        ImageButton buttonBack = findViewById(R.id.buttonBack);

        TextView textTripTitle = findViewById(R.id.textTripTitle);
        TextView textTripStart = findViewById(R.id.textTripStartDate);
        TextView textTripEnd = findViewById(R.id.textTripEndDate);

        // گرفتن اطلاعات سفر از Intent
        String tripName = getIntent().getStringExtra("trip_name");
        String startDate = getIntent().getStringExtra("start_date");
        String endDate = getIntent().getStringExtra("end_date");

        textTripTitle.setText(tripName);
        textTripStart.setText("تاریخ شروع: " + startDate);
        textTripEnd.setText("تاریخ پایان: " + endDate);

        // لیست جاذبه‌ها
        attractionList = new ArrayList<>();
        adapter = new AttractionAdapter(this, attractionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // افزودن جاذبه جدید
        buttonConfirmAdd.setOnClickListener(v -> {
            String name = editTextAttraction.getText().toString().trim();
            if (!name.isEmpty()) {
                attractionList.add(new Attraction(name));
                adapter.notifyItemInserted(attractionList.size() - 1);
                editTextAttraction.setText("");
            }
        });

        // نمایش فرم اضافه کردن جاذبه
        buttonNewAttraction.setOnClickListener(v -> layoutAddAttraction.setVisibility(View.VISIBLE));

        // دکمه بازگشت
        buttonBack.setOnClickListener(v -> finish());
    }
}
