package ir.shariaty.trip;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;




public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.ViewHolder> {
    private static final String TAG = "AttractionAdapter";
    private final Context context;
    private final List<Attraction> attractionList;

    private String tripId;

    public AttractionAdapter(Context context, List<Attraction> attractionList, String tripId) {
        this.context = context;
        this.attractionList = new ArrayList<>(attractionList);
        this.tripId = tripId;

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



        holder.buttonDelete.setOnClickListener(v -> {
            if (attraction.getId() != null) {
                FirebaseFirestore.getInstance()
                        .collection("trips")
                        .document(tripId)
                        .collection("attractions")
                        .document(attraction.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "جاذبه حذف شد", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "خطا در حذف", Toast.LENGTH_SHORT).show();
                        });
            }
        });


// دکمه ویرایش
        holder.buttonEdit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("ویرایش جاذبه");

            final EditText input = new EditText(context);
            input.setText(attraction.getName());
            builder.setView(input);

            builder.setPositiveButton("ذخیره", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    FirebaseFirestore.getInstance()
                            .collection("trips")
                            .document(tripId)
                            .collection("attractions")
                            .document(attraction.getId())
                            .update("name", newName)
                            .addOnSuccessListener(aVoid -> {
                                attraction.setName(newName);
                                notifyItemChanged(holder.getAdapterPosition());
                                Toast.makeText(context, "ویرایش شد", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "خطا در ویرایش", Toast.LENGTH_SHORT).show();
                            });
                }
            });

            builder.setNegativeButton("انصراف", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

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
        ImageButton buttonEdit, buttonDelete;


        public ViewHolder(View itemView) {
            super(itemView);
            checkBoxAttraction = itemView.findViewById(R.id.checkBoxAttraction);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

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