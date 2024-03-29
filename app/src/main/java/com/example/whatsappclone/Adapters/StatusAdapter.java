package com.example.whatsappclone.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.Status;
import com.google.android.material.snackbar.Snackbar;

import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int ADD_STATUS_HEADER_VIEW = 0;
    private final int NORMAL_STATUS = 1;
    private final String MY_STATUS = UserSettings.PHONENUMBER;
    private DataBase dataBase;
    private List<Status> statuses;
    private Context context;
    private OnAddStatusListener onAddStatusListener;
    private OnStatusItemClickListener onStatusItemClickListener;
    private Activity activity;

    public StatusAdapter(Context context, Activity activity, List<Status> statuses) {
        this.context = context;
        this.activity = activity;
        this.statuses = statuses;
        dataBase = new DataBase(context);
    }

    public void addStatusToList(Status status) {
        for (int i = 0; i < statuses.size(); i++) {
            if (statuses.get(i).getPhone_number().equals(status.getPhone_number()))
                if (statuses.get(i).getStatusUrl().equals(status.getStatusUrl()))
                    return;
                else {
                    statuses.set(i, status);
                    //notifyDataSetChanged();
                    notifyItemChanged(i + 1);
                    return;
                }
        }
        if (status.getPhone_number().equals(UserSettings.PHONENUMBER)) {
            statuses.add(0, status);
            this.notifyItemInserted(0);
        } else {
            statuses.add(status);
            this.notifyItemInserted(statuses.size());
        }
    }
    public void onProfileImageChange(){
        notifyItemChanged(0);
    }

    public void removeStatusFromList(String phone_number) {
        Iterator<Status> iterator = statuses.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            Status status = iterator.next();
            if (status.getPhone_number().equals(phone_number)) {
                iterator.remove();
                this.notifyItemRemoved(i + 1);
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
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof Normal_status) {
            final Normal_status normalStatus = (Normal_status) holder;
            Glide.with(context)
                    .load(statuses.get(holder.getAdapterPosition() - 1).getStatusUrl())
                    .placeholder(R.color.white)
                    .error(R.drawable.ic_default_avatar_profile)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            showSnackBar();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(normalStatus.status);
            // so the user can see that his store is uploading (in progress)
            if (statuses.get(holder.getAdapterPosition() - 1).isShowProgressBar())
                normalStatus.uploadProgress.setVisibility(View.VISIBLE);
            else normalStatus.uploadProgress.setVisibility(View.GONE);
            // handle if the status is my status
            if (statuses.get(holder.getAdapterPosition() - 1).getPhone_number().equals(MY_STATUS)) {
                normalStatus.ownerName.setText("Your story");


            } else {
                // other status item
                normalStatus.ownerName.setText(
                        dataBase.getContact(null
                                , statuses.get(holder.getAdapterPosition() - 1).getPhone_number()).getContact_name());
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.getAdapterPosition() != RecyclerView.NO_POSITION)
                        onStatusItemClickListener.onStatusItemClickListener(statuses.get(holder.getAdapterPosition() - 1));
                }
            });

        } else if (holder instanceof AddStatus) {
            final AddStatus addStatus = (AddStatus) holder;
            Glide.with(context)
                    .load(dataBase.getUserProfile(UserSettings.UID, null).getImageUrl())
                    .placeholder(R.color.white)
                    .error(R.drawable.ic_default_avatar_profile)
                    .into(addStatus.profileImage);
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
        void onStatusItemClickListener(Status status);
    }

    // handel add status event
    public void onAddStatus(OnAddStatusListener onAddStatusListener) {
        this.onAddStatusListener = onAddStatusListener;
    }

    public interface OnAddStatusListener {
        void onAddStatusListener();
    }


    private class Normal_status extends RecyclerView.ViewHolder {
        //CircleImageView ownerImg;
        CircleImageView status;
        TextView ownerName;
        ProgressBar uploadProgress;

        public Normal_status(@NonNull View itemView) {
            super(itemView);
            //ownerImg = itemView.findViewById(R.id.status_img_owner);
            ownerName = itemView.findViewById(R.id.status_owner);
            status = itemView.findViewById(R.id.status_img);
            uploadProgress = itemView.findViewById(R.id.uploadprogress);
        }
    }

    private class AddStatus extends RecyclerView.ViewHolder {
        CircleImageView profileImage;

        public AddStatus(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.story_profile_image);
        }
    }

    private void showSnackBar() {
        View view = activity.findViewById(R.id.chat_floating_bt);
        Snackbar.make(view, "No internet connection !", Snackbar.LENGTH_LONG).show();

    }

}
