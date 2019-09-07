package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private static final String TAG = "ContactsAdapter";
    private List<DataBase.Contact_Profile> contact_profiles;
    private Context context;
    private OnItemClickListener listener;

    public ContactsAdapter( Context context,List<DataBase.Contact_Profile> contact_profiles) {
        this.contact_profiles = contact_profiles;
        this.context = context;
    }

    public void setContact_profiles(List<DataBase.Contact_Profile> contact_profiles) {
        this.contact_profiles = contact_profiles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(context)
                .load(contact_profiles.get(position).getProfileImage().getImageUrl())
                .error(Glide.with(context).load(R.drawable.ic_default_avatar_profile))
                .into(holder.profileImage);
        holder.name.setText(contact_profiles.get(position).getContact().getContact_name());

    }

    @Override
    public int getItemCount() {
        return contact_profiles.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView name;
        ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.contact_profile);
            name = itemView.findViewById(R.id.contact_name);
            layout = itemView.findViewById(R.id.item);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition()!=RecyclerView.NO_POSITION&& listener!=null)
                        listener.onClick(contact_profiles.get(getAdapterPosition()).getContact());

                }
            });

        }
    }

    public interface OnItemClickListener {
        void onClick(DataBase.Contact contact);
    }

    public void setOnClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
