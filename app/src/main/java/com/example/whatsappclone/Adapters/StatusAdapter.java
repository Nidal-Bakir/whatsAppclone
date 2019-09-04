package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.Status;

import java.util.Iterator;
import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int ADD_STATUS_HEADER_VIEW = 0;
    private final int NORMAL_STATUS = 1;
    private final String MY_STATUS = UserSettings.PHONENUMBER;
    private DataBase dataBase;
    private List<Status> statuses;
    private Context context;
    private OnAddStatusListener onAddStatusListener;
    private OnStatusItemClickListener onStatusItemClickListener;

    public StatusAdapter(Context context, List<Status> statuses) {
        this.context = context;
        this.statuses = statuses;
        dataBase = new DataBase(context);
    }

    public void addStatusTolist(Status status) {
        statuses.add(status);
        this.notifyItemInserted(statuses.size()-1);
    }

    public void removeStatusFromList(String phone_number) {
        Iterator<Status> iterator = statuses.iterator();
            for (int i=0;iterator.hasNext();i++) {
            Status status = iterator.next();
            if (status.getPhone_number().equals(phone_number)) {
                iterator.remove();
                this.notifyItemRemoved(i);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return ADD_STATUS_HEADER_VIEW;
        return NORMAL_STATUS;
    }

    @Override
    public int getItemCount() {
        return statuses.size() + 1;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            //Inflating add status view
            case ADD_STATUS_HEADER_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.headeraddstatus, parent, false);
                return new AddStatus(view);
            //Inflating Normal status view
            case NORMAL_STATUS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status, parent, false);
                return new Normal_status(view);


        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof Normal_status) {
            Normal_status normalStatus = (Normal_status) holder;
            Glide.with(context)
                    .load(statuses.get(position - 1).getStatusUrl())
                    .error(R.drawable.ic_default_avatar_profile)
                    .into(normalStatus.status);

            // handle if the status is my status
            if (statuses.get(position - 1).getPhone_number().equals(MY_STATUS)) {
                normalStatus.ownerName.setText("Your status");

                Glide.with(context)
                        .load(dataBase.getUserProfile(UserSettings.UID))
                        .error(R.drawable.ic_default_avatar_profile)
                        .into(((Normal_status) holder).ownerImg);

            } else {
                // other status item
                Glide.with(context)
                        .load(dataBase.getUserProfile(statuses.get(position - 1).getPhone_number()))
                        .error(R.drawable.ic_default_avatar_profile)
                        .into(((Normal_status) holder).ownerImg);

                normalStatus.ownerName.setText(
                        dataBase.getContact(statuses.get(position - 1).getPhone_number()
                                , null).getContact_name());
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onStatusItemClickListener.onStatusItemClickListener(statuses.get(position - 1).getStatusUrl());
                }
            });

        } else if (holder instanceof AddStatus) {
            AddStatus addStatus = (AddStatus) holder;
            addStatus.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddStatusListener.onAddStatusListener();
                }
            });
        }
    }

    //handel the click on status item
    public void onStatusClick(OnStatusItemClickListener onStatusItemClickListener) {
        this.onStatusItemClickListener = onStatusItemClickListener;
    }

    public interface OnStatusItemClickListener {
        void onStatusItemClickListener(String url);
    }

    // handel add status event
    public void onAddStatus(OnAddStatusListener onAddStatusListener) {
        this.onAddStatusListener = onAddStatusListener;
    }

    public interface OnAddStatusListener {
        void onAddStatusListener();
    }


    private class Normal_status extends RecyclerView.ViewHolder {
        ImageView ownerImg;
        ImageView status;
        TextView ownerName;

        public Normal_status(@NonNull View itemView) {
            super(itemView);
            ownerImg = itemView.findViewById(R.id.status_img_owner);
            ownerName = itemView.findViewById(R.id.status_owner);
            status = itemView.findViewById(R.id.status_img);
        }
    }

    private class AddStatus extends RecyclerView.ViewHolder {

        public AddStatus(@NonNull View itemView) {
            super(itemView);
        }
    }

}
