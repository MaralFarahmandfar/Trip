package ir.shariaty.trip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.ViewHolder> {
    private final Context context;
    private final List<Attraction> attractionList;

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
        holder.checkBoxAttraction.setText(attraction.getName() != null ? attraction.getName() : "بدون نام");
        // می‌توانید وضعیت CheckBox را بر اساس داده‌های Attraction تنظیم کنید
        holder.checkBoxAttraction.setChecked(false); // به‌طور پیش‌فرض غیرفعال
    }

    @Override
    public int getItemCount() {
        return attractionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxAttraction;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBoxAttraction = itemView.findViewById(R.id.checkBoxAttraction);
        }
    }
}