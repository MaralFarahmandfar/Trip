package ir.shariaty.trip;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        recyclerView = findViewById(R.id.recyclerViewAttractions);
        editTextAttraction = findViewById(R.id.editTextAttraction);
        buttonConfirmAdd = findViewById(R.id.buttonConfirmAdd);
        ImageButton buttonBack = findViewById(R.id.buttonBack);

        // برگشت
        buttonBack.setOnClickListener(v -> finish());

        // لیست جاذبه‌ها
        attractionList = new ArrayList<>();
        adapter = new AttractionAdapter(this, attractionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // تایید افزودن
        buttonConfirmAdd.setOnClickListener(v -> {
            String name = editTextAttraction.getText().toString().trim();
            if (!name.isEmpty()) {
                Attraction attraction = new Attraction(name);
                attractionList.add(attraction);
                adapter.notifyItemInserted(attractionList.size() - 1);
                editTextAttraction.setText("");
            }
        });

        // نمایش فرم افزودن جاذبه
        Button buttonNewAttraction = findViewById(R.id.buttonNewAttraction);
        View layoutAddAttraction = findViewById(R.id.layoutAddAttraction);
        buttonNewAttraction.setOnClickListener(v -> {
            layoutAddAttraction.setVisibility(View.VISIBLE);
        });
    }
}
