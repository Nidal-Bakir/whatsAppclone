package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsApp_Models.VisitStatus;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VisitStatusAdapter extends RecyclerView.Adapter<VisitStatusAdapter.ViewHolder> {
    private static final String TAG = "VisitStatusAdapter";
    private List<VisitStatus> visitStatusList;
    private Context context;
    DataBase dataBase;

    public VisitStatusAdapter(Context context, List<VisitStatus> visitStatusList) {
        this.visitStatusList = visitStatusList;
        this.context = context;
        dataBase = new DataBase(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visit_story, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(dataBase.getContact(null
                , visitStatusList.get(holder.getAdapterPosition()).getPhone_number()).getContact_name());
        Glide.with(context)
                .load(dataBase.getUserProfile(null
                        , visitStatusList.get(holder.getAdapterPosition()).getPhone_number()).getImageUrl())
                .error(R.drawable.ic_default_avatar_profile)
                .into(holder.profileImage);
        holder.time.setText(visitStatusList.get(holder.getAdapterPosition()).getTime());
    }

    @Override
    public int getItemCount() {
        return visitStatusList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView name;
        TextView time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.visit_profile);
            name = itemView.findViewById(R.id.visit_name);
            time = itemView.findViewById(R.id.visit_time);
        }
    }
}
