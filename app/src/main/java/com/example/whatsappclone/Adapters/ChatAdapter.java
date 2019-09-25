package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.MessageModel;
import com.example.whatsappclone.WhatsApp_Models.MessagesPackage.FileMessage;
import com.example.whatsappclone.WhatsApp_Models.MessagesPackage.ImageMessage;
import com.example.whatsappclone.WhatsApp_Models.MessagesPackage.Message;
import com.example.whatsappclone.WhatsApp_Models.MessagesPackage.TextMessage;
import com.example.whatsappclone.WhatsApp_Models.MessagesPackage.VideoMessage;
import com.example.whatsappclone.WhatsApp_Models.MessagesPackage.VoiceMessage;
import com.vanniktech.emoji.EmojiTextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int OUT_SHORT_TEXT_VIEW = 1;
    private static final int OUT_LONG_TEXT_VIEW = 2;
    private static final int OUT_IMAGE_VIEW = 3;
    private static final int OUT_VOICE_VIEW = 4;
    private static final int OUT_VIDEO_VIEW = 5;
    private static final int OUT_FILE_VIEW = 6;
    private static final int IN_SHORT_TEXT_VIEW = 7;
    private static final int IN_LONG_TEXT_VIEW = 8;
    private static final int IN_IMAGE_VIEW = 9;
    private static final int IN_VOICE_VIEW = 10;
    private static final int IN_VIDEO_VIEW = 11;
    private static final int IN_FILE_VIEW = 12;
    private List<Message> messageList;
    private static final String TAG = "ChatAdapter";

    public ChatAdapter(Context context, List<Message> messageList) {
        this.messageList = messageList;
    }

    public void addMessage(MessageModel messageModel) {
        if (messageModel.getTextMessage() != null && !messageModel.getTextMessage().equals("")) {
            TextMessage textMessage =
                    new TextMessage(0,
                            messageModel.getPhoneNumber(),
                            messageModel.getMessageUid(),
                            messageModel.getMessageState(),
                            Long.parseLong(messageModel.getDate()),
                            messageModel.getTextMessage());
            messageList.add(textMessage);
            notifyItemInserted(messageList.size());
        } else if (messageModel.getImagePath() != null || messageModel.getImageUrl() != null) {
            ImageMessage imageMessage =
                    new ImageMessage(0,
                            messageModel.getPhoneNumber(),
                            messageModel.getMessageUid(),
                            messageModel.getMessageState(),
                            Long.parseLong(messageModel.getDate()),
                            messageModel.getImageUrl(),
                            messageModel.getImagePath());
            messageList.add(imageMessage);
            notifyItemInserted(messageList.size());
        } else if (messageModel.getVoicePath() != null || messageModel.getVoiceUrl() != null) {
            VoiceMessage voiceMessage =
                    new VoiceMessage(0,
                            messageModel.getPhoneNumber(),
                            messageModel.getMessageUid(),
                            messageModel.getMessageState(),
                            Long.parseLong(messageModel.getDate()),
                            messageModel.getVoiceUrl(),
                            messageModel.getVoicePath());
            messageList.add(voiceMessage);
            notifyItemInserted(messageList.size());
        } else if (messageModel.getVideoPath() != null || messageModel.getVideoUrl() != null) {
            VideoMessage videoMessage =
                    new VideoMessage(0,
                            messageModel.getPhoneNumber(),
                            messageModel.getMessageUid(),
                            messageModel.getMessageState(),
                            Long.parseLong(messageModel.getDate()),
                            messageModel.getVideoUrl(),
                            messageModel.getVideoPath());
            messageList.add(videoMessage);
            notifyItemInserted(messageList.size());
        } else if (messageModel.getFilePath() != null || messageModel.getFileUrl() != null) {
            FileMessage fileMessage =
                    new FileMessage(0,
                            messageModel.getPhoneNumber(),
                            messageModel.getMessageUid(),
                            messageModel.getMessageState(),
                            Long.parseLong(messageModel.getDate()),
                            messageModel.getFileUrl(),
                            messageModel.getFilePath());
            messageList.add(fileMessage);
            notifyItemInserted(messageList.size());
        }
    }

    public void updateMessage(String messageUid, DataBase.MessageState messageState) {
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getMessageUid().equals(messageUid)) {
                switch (messageState) {
                    case DELIVERED:
                        messageList.get(i).setMessageState(DataBase.DELIVERED);
                        break;
                    case READ:
                        messageList.get(i).setMessageState(DataBase.READ);
                        break;
                    case ON_SERVER:
                        if (messageList.get(i).getMessageState() == DataBase.WAIT_NETWORK)
                            messageList.get(i).setMessageState(DataBase.ON_SERVER);
                        break;
                    case MESSAGE_DELETED:
                        messageList.get(i).setMessageState(DataBase.MESSAGE_DELETED);
                        break;
                }
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getPhoneNumber().equals(UserSettings.PHONENUMBER)) {
            if (message instanceof TextMessage) {
                if (((TextMessage) message).getTextMessage().length() > 20)
                    return OUT_LONG_TEXT_VIEW;
                else return OUT_SHORT_TEXT_VIEW;
            } else if (message instanceof ImageMessage)
                return OUT_IMAGE_VIEW;
            else if (message instanceof VoiceMessage)
                return OUT_VOICE_VIEW;
            else if (message instanceof VideoMessage)
                return OUT_VIDEO_VIEW;
            else if (message instanceof FileMessage)
                return OUT_FILE_VIEW;
        } else {
            if (message instanceof TextMessage) {
                if (((TextMessage) message).getTextMessage().length() > 20)
                    return IN_LONG_TEXT_VIEW;
                else return IN_SHORT_TEXT_VIEW;
            } else if (message instanceof ImageMessage)
                return IN_IMAGE_VIEW;
            else if (message instanceof VoiceMessage)
                return IN_VOICE_VIEW;
            else if (message instanceof VideoMessage)
                return IN_VIDEO_VIEW;
            else if (message instanceof FileMessage)
                return IN_FILE_VIEW;
        }
        Log.wtf(TAG, "getItemViewType: return -1 for item type");
        return -1;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {

            case OUT_SHORT_TEXT_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_out_chat_text_one_row, parent, false);
                return new Out_ShortTextViewHolder(view);


            case OUT_LONG_TEXT_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_out_chat_text, parent, false);
                return new Out_LongTextViewHolder(view);


            case OUT_IMAGE_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_out_chat_image, parent, false);
                return new Out_ImageViewHolder(view);


            case OUT_VOICE_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_out_chat_voice, parent, false);
                return new Out_VoiceViewHolder(view);


            case OUT_VIDEO_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_out_chat_video, parent, false);
                return new Out_VideoViewHolder(view);


            case OUT_FILE_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_out_chat_file, parent, false);
                return new Out_FileViewHolder(view);

            //in coming messages
            case IN_SHORT_TEXT_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_chat_text_one_row, parent, false);
                return new In_ShortTextViewHolder(view);


            case IN_LONG_TEXT_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_chat_text, parent, false);
                return new In_LongTextViewHolder(view);


            case IN_IMAGE_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_chat_image, parent, false);
                return new In_ImageViewHolder(view);


            case IN_VOICE_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_chat_voice, parent, false);
                return new In_VoiceViewHolder(view);

            case IN_VIDEO_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_chat_video, parent, false);
                return new In_VideoViewHolder(view);

            case IN_FILE_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_chat_file, parent, false);
                return new In_FileViewHolder(view);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // out going messages
        if (holder instanceof Out_ShortTextViewHolder) { // Short text message *****
            Out_ShortTextViewHolder shortTextViewHolder = (Out_ShortTextViewHolder) holder;
            TextMessage textMessage = (TextMessage) messageList.get(holder.getAdapterPosition());
            // set text
            shortTextViewHolder.textMessage.setText(textMessage.getTextMessage());
            //set ic for message state
            switch (textMessage.getMessageState()) {
                case DataBase.WAIT_NETWORK:
                    shortTextViewHolder.messageState.setImageResource(R.drawable.ic_watch);
                    break;
                case DataBase.ON_SERVER:
                    shortTextViewHolder.messageState.setImageResource(R.drawable.ic_on_server);
                    break;
                case DataBase.DELIVERED:
                    shortTextViewHolder.messageState.setImageResource(R.drawable.ic_delivered);
                    break;
                case DataBase.READ:
                    shortTextViewHolder.messageState.setImageResource(R.drawable.ic_read);
                    break;
                case DataBase.MESSAGE_DELETED:
                    shortTextViewHolder.textMessage.setText("This message was deleted.");
                    shortTextViewHolder.messageState.setVisibility(View.GONE);
                    break;
            }
            //set time
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(textMessage.getDate());
            shortTextViewHolder.time.setText(simpleDateFormat.format(calendar.getTime()));
            // back Ground
            if (holder.getAdapterPosition() != 0) {
                if (messageList.get(holder.getAdapterPosition() - 1).getPhoneNumber().equals(UserSettings.PHONENUMBER)) {
                    shortTextViewHolder.parent.setBackgroundResource(R.drawable.shape_outgoing_bubble2);
                } else
                    shortTextViewHolder.parent.setBackgroundResource(R.drawable.shape_outgoing_bubble);
            }

        } else if (holder instanceof Out_LongTextViewHolder) { // long text message *****
            Out_LongTextViewHolder longTextViewHolder = (Out_LongTextViewHolder) holder;
            TextMessage textMessage = (TextMessage) messageList.get(holder.getAdapterPosition());
            // set text
            longTextViewHolder.textMessage.setText(textMessage.getTextMessage());
            //set ic for message state
            switch (textMessage.getMessageState()) {
                case DataBase.WAIT_NETWORK:
                    longTextViewHolder.messageState.setImageResource(R.drawable.ic_watch);
                    break;
                case DataBase.ON_SERVER:
                    longTextViewHolder.messageState.setImageResource(R.drawable.ic_on_server);
                    break;
                case DataBase.DELIVERED:
                    longTextViewHolder.messageState.setImageResource(R.drawable.ic_delivered);
                    break;
                case DataBase.READ:
                    longTextViewHolder.messageState.setImageResource(R.drawable.ic_read);
                    break;
                case DataBase.MESSAGE_DELETED:
                    longTextViewHolder.textMessage.setText("This message was deleted.");
                    longTextViewHolder.messageState.setVisibility(View.GONE);
                    break;
            }
            //set time
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(textMessage.getDate());
            longTextViewHolder.time.setText(simpleDateFormat.format(calendar.getTime()));
            // backGround
            if (holder.getAdapterPosition() != 0) {
                if (messageList.get(holder.getAdapterPosition() - 1).getPhoneNumber().equals(UserSettings.PHONENUMBER)) {
                    longTextViewHolder.parent.setBackgroundResource(R.drawable.shape_outgoing_bubble2);
                } else
                    longTextViewHolder.parent.setBackgroundResource(R.drawable.shape_outgoing_bubble);
            }

        } else if (holder instanceof Out_ImageViewHolder) {// Image message *****


        } else if (holder instanceof Out_VoiceViewHolder) {// Voice message *****


        } else if (holder instanceof Out_VideoViewHolder) {// Video message *****


        } else if (holder instanceof Out_FileViewHolder) {// File  message *****


        } else // in coming chat messages ****************
            if (holder instanceof In_ShortTextViewHolder) { // Short text message *****
                In_ShortTextViewHolder shortTextViewHolder = (In_ShortTextViewHolder) holder;
                TextMessage textMessage = (TextMessage) messageList.get(holder.getAdapterPosition());
                // set text
                shortTextViewHolder.textMessage.setText(textMessage.getTextMessage());
                // message deletion
                if (textMessage.getMessageState() == DataBase.MESSAGE_DELETED) {
                    shortTextViewHolder.textMessage.setText("This message was deleted.");
                }
                //set time
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(textMessage.getDate());
                shortTextViewHolder.time.setText(simpleDateFormat.format(calendar.getTime()));
                // backGround
                if (holder.getAdapterPosition() != 0) {
                    if (!messageList.get(holder.getAdapterPosition() - 1).getPhoneNumber().equals(UserSettings.PHONENUMBER))
                        shortTextViewHolder.parent.setBackgroundResource(R.drawable.shape_incoming_bubble2);
                    else
                        shortTextViewHolder.parent.setBackgroundResource(R.drawable.shape_incoming_bubble);
                }

            } else if (holder instanceof In_LongTextViewHolder) { // long text message *****
                In_LongTextViewHolder longTextViewHolder = (In_LongTextViewHolder) holder;
                TextMessage textMessage = (TextMessage) messageList.get(holder.getAdapterPosition());
                // set text
                longTextViewHolder.textMessage.setText(textMessage.getTextMessage());
                // message deletion
                if (textMessage.getMessageState() == DataBase.MESSAGE_DELETED) {
                    longTextViewHolder.textMessage.setText("This message was deleted.");
                }
                //set time
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(textMessage.getDate());
                longTextViewHolder.time.setText(simpleDateFormat.format(calendar.getTime()));
                // backGround
                if (holder.getAdapterPosition() != 0) {
                    if (!messageList.get(holder.getAdapterPosition() - 1).getPhoneNumber().equals(UserSettings.PHONENUMBER))
                        longTextViewHolder.parent.setBackgroundResource(R.drawable.shape_incoming_bubble2);
                    else
                        longTextViewHolder.parent.setBackgroundResource(R.drawable.shape_incoming_bubble);
                }

            } else if (holder instanceof In_ImageViewHolder) { // Image message *****


            } else if (holder instanceof In_VoiceViewHolder) { // Voice message *****


            } else if (holder instanceof In_VideoViewHolder) { // Video message *****


            } else if (holder instanceof In_FileViewHolder) { // lFile message *****

            }
    }

    // out going class's
    class Out_ShortTextViewHolder extends RecyclerView.ViewHolder {
        private EmojiTextView textMessage;
        private ImageView messageState;
        private TextView time;
        private ConstraintLayout parent;

        public Out_ShortTextViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.out_chat_text_one_message);
            time = itemView.findViewById(R.id.out_chat_text_one_time);
            messageState = itemView.findViewById(R.id.out_chat_text_one_message_state);
            parent = itemView.findViewById(R.id.out_chat_text_one_parent);
        }
    }


    class Out_LongTextViewHolder extends RecyclerView.ViewHolder {
        private EmojiTextView textMessage;
        private ImageView messageState;
        private TextView time;
        private ConstraintLayout parent;

        public Out_LongTextViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.out_chat_text_message);
            time = itemView.findViewById(R.id.out_chat_text_time);
            messageState = itemView.findViewById(R.id.out_chat_text_message_state);
            parent = itemView.findViewById(R.id.out_chat_text_parent);
        }
    }


    class Out_ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView time;
        private ConstraintLayout parent;
        private ImageView messageState;
        private CircularProgressBar progressBar;
        private ImageButton event;

        public Out_ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.out_chat_image_image_message);
            time = itemView.findViewById(R.id.out_chat_image_time);
            messageState = itemView.findViewById(R.id.out_chat_image_message_state);
            progressBar = itemView.findViewById(R.id.out_chat_image_progressBar);
            event = itemView.findViewById(R.id.out_chat_image_event);
            parent = itemView.findViewById(R.id.out_chat_image_parent);

        }
    }


    class Out_VoiceViewHolder extends RecyclerView.ViewHolder {
        private TextView time;
        private ImageView messageState;
        private CircularProgressBar progressBar;
        private ImageButton event;
        private CircleImageView profileImage;
        private SeekBar seekBar;
        private TextView voiceLength;
        private ConstraintLayout parent;

        public Out_VoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.out_chat_voice_time);
            messageState = itemView.findViewById(R.id.out_chat_voice_message_state);
            progressBar = itemView.findViewById(R.id.out_chat_voice_progressBar);
            event = itemView.findViewById(R.id.out_chat_voice_event);
            profileImage = itemView.findViewById(R.id.out_chat_voice_profile);
            seekBar = itemView.findViewById(R.id.out_chat_voice_voiceSeekBar);
            voiceLength = itemView.findViewById(R.id.out_chat_voice_length);
            parent = itemView.findViewById(R.id.out_chat_voice_parent);
        }
    }


    class Out_VideoViewHolder extends RecyclerView.ViewHolder {
        private TextView time;
        private CircularProgressBar progressBar;
        private ImageButton event;
        private ImageView messageState;
        private ConstraintLayout parent;

        public Out_VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.out_chat_video_time);
            progressBar = itemView.findViewById(R.id.out_chat_video_progressBar);
            event = itemView.findViewById(R.id.out_chat_video_event);
            messageState = itemView.findViewById(R.id.out_chat_video_message_state);
            parent = itemView.findViewById(R.id.out_chat_video_parent);

        }
    }


    class Out_FileViewHolder extends RecyclerView.ViewHolder {
        private TextView fileName;
        private TextView time;
        private CircularProgressBar progressBar;
        private ImageButton event;
        private ImageView messageState;
        private ConstraintLayout parent;

        public Out_FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.out_chat_file_name);
            time = itemView.findViewById(R.id.out_chat_file_time);
            progressBar = itemView.findViewById(R.id.out_chat_file_progressBar);
            event = itemView.findViewById(R.id.out_chat_file_event);
            messageState = itemView.findViewById(R.id.out_chat_file_message_state);
            parent = itemView.findViewById(R.id.out_chat_file_parent);
        }
    }


    // in coming class's
    class In_ShortTextViewHolder extends RecyclerView.ViewHolder {
        private EmojiTextView textMessage;
        private TextView time;
        private ConstraintLayout parent;

        public In_ShortTextViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.in_chat_text_one_message);
            time = itemView.findViewById(R.id.in_chat_text_one_time);
            parent = itemView.findViewById(R.id.in_chat_text_one_parent);
        }
    }


    class In_LongTextViewHolder extends RecyclerView.ViewHolder {
        private EmojiTextView textMessage;
        private TextView time;
        private ConstraintLayout parent;

        public In_LongTextViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.in_chat_text_message);
            time = itemView.findViewById(R.id.in_chat_text_time);
            parent = itemView.findViewById(R.id.in_chat_text_parent);
        }
    }


    class In_ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView time;
        private CircularProgressBar progressBar;
        private ImageButton event;
        private ConstraintLayout parent;

        public In_ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.in_chat_image_image_message);
            time = itemView.findViewById(R.id.in_chat_image_time);
            progressBar = itemView.findViewById(R.id.in_chat_image_progressBar);
            event = itemView.findViewById(R.id.in_chat_image_event);
            parent = itemView.findViewById(R.id.in_chat_image_parent);
        }
    }


    class In_VoiceViewHolder extends RecyclerView.ViewHolder {
        private TextView time;
        private CircularProgressBar progressBar;
        private ImageButton event;
        private CircleImageView profileImage;
        private SeekBar seekBar;
        private TextView voiceLength;
        private ConstraintLayout parent;

        public In_VoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            voiceLength = itemView.findViewById(R.id.in_chat_voice_length);
            time = itemView.findViewById(R.id.in_chat_voice_time);
            progressBar = itemView.findViewById(R.id.in_chat_voice_progressBar);
            event = itemView.findViewById(R.id.in_chat_voice_event);
            profileImage = itemView.findViewById(R.id.in_chat_voice_profile);
            seekBar = itemView.findViewById(R.id.in_chat_voice_voiceSeekBar);
            parent = itemView.findViewById(R.id.in_chat_voice_parent);
        }
    }


    class In_VideoViewHolder extends RecyclerView.ViewHolder {
        private TextView time;
        private CircularProgressBar progressBar;
        private ImageButton event;
        private ConstraintLayout parent;


        public In_VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.in_chat_video_time);
            progressBar = itemView.findViewById(R.id.in_chat_video_progressBar);
            event = itemView.findViewById(R.id.in_chat_video_event);
            parent = itemView.findViewById(R.id.in_chat_video_parent);
        }
    }


    class In_FileViewHolder extends RecyclerView.ViewHolder {
        private TextView fileName;
        private TextView time;
        private CircularProgressBar progressBar;
        private ImageButton event;
        private ConstraintLayout parent;

        public In_FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.in_chat_file_name);
            time = itemView.findViewById(R.id.in_chat_file_time);
            progressBar = itemView.findViewById(R.id.in_chat_file_progressBar);
            event = itemView.findViewById(R.id.in_chat_file_event);
            parent = itemView.findViewById(R.id.in_chat_file_parent);
        }
    }


}
