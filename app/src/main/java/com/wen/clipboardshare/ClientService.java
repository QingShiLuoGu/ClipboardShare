package com.wen.clipboardshare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;

import com.wen.client.Client;
import com.wen.client.ProtoProcess;

public class ClientService extends Service {
    ClipboardManager cm = null;

    public ClientService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        String CHANNEL_ONE_ID = "com.primedu.cn";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new Notification.Builder(this).setChannelId(CHANNEL_ONE_ID)
                .setTicker("Nature")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("hi")
                .setContentText("is listening server clipboard")
                .setContentIntent(pendingIntent)
                .build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(1, notification);


        cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        Client client = new Client(new ProtoProcess(text -> {
            if (cm != null) {
                ClipData mClipData = ClipData.newPlainText("Label", text);
                cm.setPrimaryClip(mClipData);
            }
        }));
        client.startRead();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}