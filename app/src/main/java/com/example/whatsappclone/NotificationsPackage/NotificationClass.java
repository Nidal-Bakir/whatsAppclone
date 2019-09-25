package com.example.whatsappclone.NotificationsPackage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;

import com.example.whatsappclone.ActivityClass.BaseChatActivity2;
import com.example.whatsappclone.ActivityClass.MainActivity;
import com.example.whatsappclone.App;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsApp_Models.MessageModel;
import com.example.whatsappclone.WhatsApp_Models.MessagesPackage.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class NotificationClass {
    public static final String MESSAGES_CHANNEL_ID = "MessagesChannel";
    Context context;
    DataBase dataBase;

    public NotificationClass(Context context) {
        this.context = context;
        dataBase = new DataBase(context);

    }

    public void notifyUser() {
        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("me");
        messagingStyle.setConversationTitle("new Messages");
        NotificationCompat.MessagingStyle.Message notificationMessage;
        Stack<NotificationMessage> notificationMessageStack = dataBase.getAllNewMessages();
        while (!notificationMessageStack.empty()) {
            NotificationMessage chatMessages = notificationMessageStack.pop();
            notificationMessage =
                    new NotificationCompat.MessagingStyle.Message(
                            chatMessages.getText(),
                            chatMessages.getTimestamp(),
                            chatMessages.getSender() + ":"
                    );
            messagingStyle.addMessage(notificationMessage);

        }


        NotificationChannel mChannel = null;
        Intent activityIntent = new Intent(context, BaseChatActivity2.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, activityIntent, 0);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_reply,
                "Reply",
                contentIntent
        ).build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID);
        builder.setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.app_logo)
                .setContentIntent(contentIntent);

        Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.popcorn);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            mChannel = new NotificationChannel(MESSAGES_CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            mChannel.setDescription("setDescription");
            mChannel.enableLights(true);
            mChannel.setSound(sound, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            mChannel.setVibrationPattern(new long[]{100, 1000});
            mChannel.setLightColor(Color.GREEN);
            mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            builder.setContentTitle(context.getString(R.string.app_name))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(0xFFFFFF)
                    .setVibrate(new long[]{100, 1000})
                    .setSound(sound)
                    .setStyle(messagingStyle)
                    .addAction(replyAction)
                    .setOnlyAlertOnce(false)
                    .setAutoCancel(true);

        }
        mNotificationManager.notify(1, builder.build());


    }

    private NotificationMessage getNotificationMessage(MessageModel messageModel) {
        String textMessage = "";
        if (messageModel.getTextMessage() != null)
            textMessage = messageModel.getTextMessage();
        else if (messageModel.getImageUrl() != null)
            textMessage = "Image!";
        else if (messageModel.getVoiceUrl() != null)
            textMessage = "Voice message";
        else if (messageModel.getVideoUrl() != null)
            textMessage = "Video!";
        else if (messageModel.getFileUrl() != null)
            textMessage = "File!";
        DataBase.Contact contact = dataBase.getContact(null, messageModel.getPhoneNumber());
        String contactName = contact == null ? messageModel.getPhoneNumber() : contact.getContact_name();

        return new NotificationMessage(textMessage, contactName, Long.parseLong(messageModel.getDate()));
    }
}
