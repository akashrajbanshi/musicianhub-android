package com.project.musicianhub.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.project.musicianhub.MusicPostActivity;
import com.project.musicianhub.ProfileActivity;
import com.project.musicianhub.util.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Generates the notification for the users
 *
 * @author Akash Rajbanshi
 * @since 1.0
 */

public class MHFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MHFirebaseMessagingService.class.getSimpleName();
    private NotificationUtils notificationUtils;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }

    }


    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {
            String type = json.getString("type");
            //if notification type is like or comment
            if (!type.equalsIgnoreCase("follow")) {
                String title = json.getString("title");
                String message = json.getString("message");
                String senderUserName = json.getString("senderUserName");
                String musicTitle = json.getString("musicTitle");
                int musicId = json.getInt("musicId");
                String userSendImagePath = json.getString("userImagePath");
                String replaceMusicSlash = userSendImagePath.replace("\\", "/");
                String userImagePath = "http:" + "//" + replaceMusicSlash;
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent("pushNotification");
                    pushNotification.putExtra("notificationCount", 1);
                    pushNotification.putExtra("senderUserName", senderUserName);
                    pushNotification.putExtra("musicTitle", musicTitle);
                    pushNotification.putExtra("musicId", musicId);
                    pushNotification.putExtra("userImagePath", userImagePath);
                    pushNotification.putExtra("type", type);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                    // app is in background, show the notification in notification tray
                    Intent resultIntent = new Intent(getApplicationContext(), MusicPostActivity.class);

                    resultIntent.putExtra("musicId", musicId);
                    showNotificationMessage(getApplicationContext(), title, message, resultIntent, userImagePath);
                } else {
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent("pushNotification");
                    pushNotification.putExtra("notificationCount", 0);
                    pushNotification.putExtra("senderUserName", senderUserName);
                    pushNotification.putExtra("musicTitle", musicTitle);
                    pushNotification.putExtra("musicId", musicId);
                    pushNotification.putExtra("userImagePath", userImagePath);
                    pushNotification.putExtra("type", type);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                    // app is in background, show the notification in notification tray
                    Intent resultIntent = new Intent(getApplicationContext(), MusicPostActivity.class);

                    resultIntent.putExtra("musicId", musicId);
                    showNotificationMessage(getApplicationContext(), title, message, resultIntent, userImagePath);

                }
            } else {
                String title = json.getString("title");
                String message = json.getString("message");
                String senderUserName = json.getString("senderUserName");
                String userSendImagePath = json.getString("userImagePath");
                int userId = json.getInt("userId");
                String replaceMusicSlash = userSendImagePath.replace("\\", "/");
                String userImagePath = "http:" + "//" + replaceMusicSlash;
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent("pushNotification");
                    pushNotification.putExtra("notificationCount", 1);
                    pushNotification.putExtra("senderUserName", senderUserName);
                    pushNotification.putExtra("userImagePath", userImagePath);
                    pushNotification.putExtra("userId", userId);
                    pushNotification.putExtra("type", type);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                    // app is in background, show the notification in notification tray
                    Intent resultIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                    showNotificationMessage(getApplicationContext(), title, message, resultIntent, userImagePath);
                } else {
                    // app is in background, show the notification in notification tray
                    Intent resultIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                    showNotificationMessage(getApplicationContext(), title, message, resultIntent, userImagePath);
                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent("pushNotification");
                    pushNotification.putExtra("notificationCount", 0);
                    pushNotification.putExtra("senderUserName", senderUserName);
                    pushNotification.putExtra("userImagePath", userImagePath);
                    pushNotification.putExtra("type", type);
                    pushNotification.putExtra("userId", userId);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, Intent intent, String userImagePath) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, intent, userImagePath);
    }
}
