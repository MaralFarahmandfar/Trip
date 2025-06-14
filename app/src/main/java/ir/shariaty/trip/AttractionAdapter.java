package ir.shariaty.trip;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;

import android.view.View;
import android.view.ViewGroup;
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
        holder.textView.setText(attractionList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return attractionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            CheckBox attractionName = itemView.findViewById(R.id.checkBoxAttraction);
        }
    }
}