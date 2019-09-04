package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsApp_Models.StatusPrivacyModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivacyAdapter extends RecyclerView.Adapter<PrivacyAdapter.ViewHolder> {
    private List<StatusPrivacyModel> statusPrivacyList;
    private Context context;
    private DataBase dataBase;
    private boolean isSelectedAll = false;
    private OnItemClickListener onItemClickListener;

    public PrivacyAdapter(Context context, List<StatusPrivacyModel> statusPrivacyList) {
        this.statusPrivacyList = statusPrivacyList;
        this.context = context;
        dataBase = new DataBase(context);
    }

    // select all the contact OR select nun of them
    public boolean selectAll() {
        if (isSelectedAll) {
            for (int i = 0; i < statusPrivacyList.size(); i++)
                statusPrivacyList.get(i).setAuthorized(false);
            isSelectedAll = false;
        } else {
            for (int i = 0; i < statusPrivacyList.size(); i++)
                statusPrivacyList.get(i).setAuthorized(true);
            isSelectedAll = true;
        }
        this.notifyItemRangeChanged(0, statusPrivacyList.size());
        return isSelectedAll;
    }
    public void changeAuthorizedInDataSet(int position,boolean isChecked ){
        statusPrivacyList.get(position).setAuthorized(isChecked);

    }
    public boolean changSelection(int position) {
        if (statusPrivacyList.get(position).isAuthorized())
            statusPrivacyList.get(position).setAuthorized(false);
        else statusPrivacyList.get(position).setAuthorized(true);
        this.notifyItemChanged(position);
        return statusPrivacyList.get(position).isAuthorized();
    }

    public List<StatusPrivacyModel> getDatasetFromAdapter() {
        return statusPrivacyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_privacy, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Glide.with(context)
                .load(dataBase.getUserProfile(statusPrivacyList.get(position).getUID()))
                .error(R.drawable.ic_default_avatar_profile)
                .into(holder.image);
        holder.name.setText(statusPrivacyList.get(position).getContact_name());
        holder.checkBox.setChecked(statusPrivacyList.get(position).isAuthorized());
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onCheckChanged(position,holder.checkBox.isChecked());
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return statusPrivacyList.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.privacy_contact_profile);
            name = itemView.findViewById(R.id.privacy_contact_name);
            checkBox = itemView.findViewById(R.id.privacy_checkBox);


        }
    }

    public void onItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(int position);
        void onCheckChanged(int position,boolean isChecked);
    }


}
