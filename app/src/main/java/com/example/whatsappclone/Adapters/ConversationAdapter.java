package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private List<DataBase.Conversation> conversationList;
    private Context context;
    private DataBase dataBase;
    private OnConversationItemClickListener onConversationItemClickListener;

    public ConversationAdapter(Context context, List<DataBase.Conversation> conversationList) {
        this.conversationList = conversationList;
        this.context = context;
        dataBase = new DataBase(context);
    }

    public void updateMessage(String phoneNumber){
        for (int i = 0; i < conversationList.size(); i++) {
            if (conversationList.get(i).getPhoneNumber().equals(phoneNumber)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addConversation(DataBase.Conversation conversation) {

        for (int i = 0; i < conversationList.size(); i++) {
            if (conversationList.get(i).getPhoneNumber().equals(conversation.getPhoneNumber())) {
                conversationList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
        conversationList.add(0, conversation);
        notifyItemInserted(0);
    }

    public void deleteconversation(DataBase.Conversation conversation) {
        for (int i = 0; i < conversationList.size(); i++) {
            if (conversationList.get(i).getPhoneNumber().equals(conversation.getPhoneNumber())) {
                conversationList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
        dataBase.deleteConversation(conversation);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_chats, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final String phoneNumber = conversationList.get(holder.getAdapterPosition()).getPhoneNumber();
        Bundle lastMessageBundle = dataBase.getLastMessage(phoneNumber);
        if (lastMessageBundle != null) {
            if (lastMessageBundle.getBoolean("isMyMessage")) {
                int integerMessageState = lastMessageBundle.getInt("messageState");
                switch (integerMessageState) {
                    case DataBase.WAIT_NETWORK:
                        holder.messageState.setImageResource(R.drawable.ic_watch);
                        break;
                    case DataBase.ON_SERVER:
                        holder.messageState.setImageResource(R.drawable.ic_on_server);
                        break;
                    case DataBase.DELIVERED:
                        holder.messageState.setImageResource(R.drawable.ic_delivered);
                        break;
                    case DataBase.READ:
                        holder.messageState.setImageResource(R.drawable.ic_read);
                        break;
                }
            } else
                holder.messageState.setVisibility(View.GONE);
            holder.message.setText(lastMessageBundle.getString("message"));
        } else {// if the user delete all the message for this conversation
            holder.message.setVisibility(View.GONE);
            holder.messageState.setVisibility(View.GONE);
        }
        //profile image
        Glide.with(context)
                .load(dataBase.getUserProfile(null, phoneNumber).getImageUrl())
                .placeholder(R.color.white)
                .error(R.drawable.ic_default_avatar_profile)
                .into(holder.profileImage);
        //set the contact name
        final DataBase.Contact contact=dataBase.getContact(null, phoneNumber);
        if ( contact== null)
            holder.contactName.setText(phoneNumber);
        // set message count
        int messageCount = conversationList.get(holder.getAdapterPosition()).getMessageCount();
        if (messageCount == 0) {
            holder.messageCount.setVisibility(View.GONE);
        } else {
            holder.messageCount.setVisibility(View.VISIBLE);
            holder.messageCount.setText(String.valueOf(messageCount));
        }
        // show icon if the contact is muted
        if (conversationList.get(holder.getAdapterPosition()).getMute() == DataBase.MUTE)
            holder.mute.setVisibility(View.VISIBLE);
        else holder.mute.setVisibility(View.GONE);
        //set last time the user make chat with this contact
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(conversationList.get(holder.getAdapterPosition()).getDate());
        holder.date.setText(simpleDateFormat.format(calendar1.getTime()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // reset the message count
                dataBase.reSetMessageCount(conversationList.get(holder.getAdapterPosition()).getPhoneNumber());
                onConversationItemClickListener.onClick(contact,phoneNumber);
            }
        });

    }
    public void onConversationItemClickListener(OnConversationItemClickListener onConversationItemClickListener){
        this.onConversationItemClickListener=onConversationItemClickListener;
    }
    public interface OnConversationItemClickListener{
        // we will use the phone number if the user do not know the new Contact (Anonymous number )
        void onClick(DataBase.Contact contact,String PhoneNumber);
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView contactName;
        TextView date;
        TextView message;
        ImageView messageState, mute;
        TextView messageCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.list_chat_profile_image);
            contactName = itemView.findViewById(R.id.list_chat_contact_name);
            date = itemView.findViewById(R.id.list_chat_date);
            message = itemView.findViewById(R.id.list_chat_message);
            messageState = itemView.findViewById(R.id.list_chat_message_state);
            mute = itemView.findViewById(R.id.list_chat_mute);
            messageCount = itemView.findViewById(R.id.list_chat_message_count);


        }
    }
}
