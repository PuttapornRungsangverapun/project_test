/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.por.project_test;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), remoteMessage.getData().get("tag"));

//        Intent main = new Intent(this,MessageActivity.class);
//        main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(main);

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        Dialog d = builder.create();
//        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        d.show();
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String username, String messageBody, String friendid) {
        Intent intent;
        String sender;
        if (messageBody.startsWith("call_group_from")) {
            friendid = friendid.replace("G", "");
            intent = new Intent(this, CallGroupActivity.class);
            intent.putExtra("friendid", friendid);//มันส่งobjectธรรมดามาเลยcast
            intent.putExtra("frienduser", messageBody.split(":")[1]);
            sender = username;
        } else if (messageBody.startsWith("call")) {
            friendid = friendid.replace("U", "");
            intent = new Intent(this, CallSingleActivity.class);
            intent.putExtra("friendid", friendid);//มันส่งobjectธรรมดามาเลยcast
            intent.putExtra("frienduser", messageBody.split(":")[1]);
            sender = username;
        } else if (friendid.startsWith("U")) {
            friendid = friendid.replace("U", "");
            intent = new Intent(this, MessageActivity.class);
            intent.putExtra("friendid", friendid);//มันส่งobjectธรรมดามาเลยcast
            intent.putExtra("frienduser", username);
            sender = username;
        } else {
            friendid = friendid.replace("G", "");
            intent = new Intent(this, GroupMessageActivity.class);
            intent.putExtra("groupid", friendid);
            intent.putExtra("groupname", username.split(";")[1]);
            sender = username.split(";")[0];
        }

//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "");
//        wl.acquire(3000);//ติด3วิ
//        wl.release();


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1234 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.chat)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.chat))
                .setContentTitle(sender)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{100, 100, 100, 100, 100, 100, 100, 100, 100})
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOngoing(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1234 /* ID of notification */, notificationBuilder.build());
    }
}
