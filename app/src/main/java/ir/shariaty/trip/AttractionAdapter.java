package ir.shariaty.trip;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.ViewHolder> {
    private static final String TAG = "AttractionAdapter";
    private final Context context;
    private final List<Attraction> attractionList;

    public AttractionAdapter(Context context, List<Attraction> attractionList) {
        this.context = context;
        this.attractionList = new ArrayList<>(attractionList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attraction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Binding position: " + position + ", attractionList size: " + attractionList.size());
        Attraction attraction = attractionList.get(position);
        holder.checkBoxAttraction.setText(attraction.getName() != null ? attraction.getName() : "بدون نام");
        holder.checkBoxAttraction.setChecked(false); // به‌طور پیش‌فرض غیرفعال
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + attractionList.size());
        return attractionList.size();
    }

    public void updateAttractions(List<Attraction> newAttractions) {
        Log.d(TAG, "Updating attractions, new size: " + newAttractions.size());
        AttractionDiffCallback diffCallback = new AttractionDiffCallback(attractionList, newAttractions);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        attractionList.clear();
        attractionList.addAll(newAttractions);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxAttraction;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBoxAttraction = itemView.findViewById(R.id.checkBoxAttraction);
        }
    }

    static class AttractionDiffCallback extends DiffUtil.Callback {
        private final List<Attraction> oldList;
        private final List<Attraction> newList;

        AttractionDiffCallback(List<Attraction> oldList, List<Attraction> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            String oldId = oldList.get(oldItemPosition).getId();
            String newId = newList.get(newItemPosition).getId();
            return oldId != null && oldId.equals(newId);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Attraction oldAttraction = oldList.get(oldItemPosition);
            Attraction newAttraction = newList.get(newItemPosition);
            return (oldAttraction.getName() == null ? newAttraction.getName() == null : oldAttraction.getName().equals(newAttraction.getName())) &&
                    (oldAttraction.getCreatedAt() == null ? newAttraction.getCreatedAt() == null : oldAttraction.getCreatedAt().equals(newAttraction.getCreatedAt()));
        }
    }
}