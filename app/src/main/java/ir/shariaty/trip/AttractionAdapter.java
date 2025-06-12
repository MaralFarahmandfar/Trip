package ir.shariaty.trip;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.ViewHolder> {
    private Context context;
    private List<Attraction> attractionList;

    public AttractionAdapter(Context context, List<Attraction> attractionList) {
        this.context = context;
        this.attractionList = attractionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attraction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attraction attraction = attractionList.get(position);
        holder.checkBox.setText(attraction.getName());
        holder.checkBox.setChecked(attraction.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            attraction.setChecked(isChecked);
        });

        holder.buttonDelete.setOnClickListener(v -> {
            attractionList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, attractionList.size());
        });

        holder.buttonEdit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("ویرایش جاذبه");

            final TextView input = new TextView(context);
            input.setPadding(32, 32, 32, 32);
            input.setText(attraction.getName());
            builder.setView(input);

            builder.setPositiveButton("ذخیره", (dialog, which) -> {
                attraction.setName(input.getText().toString());
                notifyItemChanged(position);
            });

            builder.setNegativeButton("لغو", (dialog, which) -> dialog.cancel());

            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return attractionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageButton buttonEdit, buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxAttraction);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}

