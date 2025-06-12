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

import java.util.Calendar;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends AppCompatActivity {

    LinearLayout layoutNewTripForm;
    Button buttonNewTrip, buttonPickStartDate, buttonPickEndDate, buttonSaveTrip;
    TextView textStartDate, textEndDate;
    EditText editTextTripName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTrips);
        List<Trip> tripList = new ArrayList<>();
        TripAdapter adapter = new TripAdapter(tripList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

// درون buttonSaveTrip:
        buttonSaveTrip.setOnClickListener(v -> {
            String name = editTextTripName.getText().toString();
            String start = textStartDate.getText().toString();
            String end = textEndDate.getText().toString();

            if (name.isEmpty() || start.contains("انتخاب نشده") || end.contains("انتخاب نشده")) {
                Toast.makeText(this, "لطفاً همه فیلدها را کامل کنید", Toast.LENGTH_SHORT).show();
            } else {
                tripList.add(new Trip(name, start, end));
                adapter.notifyItemInserted(tripList.size() - 1);
                layoutNewTripForm.setVisibility(View.GONE);
                editTextTripName.setText("");
                textStartDate.setText("تاریخ شروع: انتخاب نشده");
                textEndDate.setText("تاریخ پایان: انتخاب نشده");
            }
        });


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // مطمئن شو اسم فایل XML تو اینه

        // اتصال به ویوها
        layoutNewTripForm = findViewById(R.id.layoutNewTripForm);
        buttonNewTrip = findViewById(R.id.buttonNewTrip);
        buttonPickStartDate = findViewById(R.id.buttonPickStartDate);
        buttonPickEndDate = findViewById(R.id.buttonPickEndDate);
        buttonSaveTrip = findViewById(R.id.buttonSaveTrip);
        textStartDate = findViewById(R.id.textStartDate);
        textEndDate = findViewById(R.id.textEndDate);
        editTextTripName = findViewById(R.id.editTextTripName);

        // نمایش فرم با کلیک روی دکمه سفر جدید
        buttonNewTrip.setOnClickListener(v -> layoutNewTripForm.setVisibility(View.VISIBLE));

        // انتخاب تاریخ شروع
        buttonPickStartDate.setOnClickListener(v -> showDatePicker(textStartDate));

        // انتخاب تاریخ پایان
        buttonPickEndDate.setOnClickListener(v -> showDatePicker(textEndDate));

        // ذخیره سفر و نمایش پیام
        buttonSaveTrip.setOnClickListener(v -> {
            String name = editTextTripName.getText().toString();
            String start = textStartDate.getText().toString();
            String end = textEndDate.getText().toString();

            if (name.isEmpty() || start.contains("انتخاب نشده") || end.contains("انتخاب نشده")) {
                Toast.makeText(this, "لطفاً همه فیلدها را کامل کنید", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "سفر '" + name + "' از " + start + " تا " + end + " ذخیره شد!", Toast.LENGTH_LONG).show();
                layoutNewTripForm.setVisibility(View.GONE);
                editTextTripName.setText("");
                textStartDate.setText("تاریخ شروع: انتخاب نشده");
                textEndDate.setText("تاریخ پایان: انتخاب نشده");
            }
        });
    }

    // نمایش دیالوگ تاریخ
    private void showDatePicker(TextView target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    target.setText(date);
                }, year, month, day);

        datePicker.show();
    }
}

