package com.getcapacitor.community.fcm;

import android.Manifest;
import android.os.Build;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

/**
 * Please read the Capacitor Android Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/android
 *
 * Created by Stewan Silva on 1/23/19.
 */
@NativePlugin()
public class FCMPlugin extends Plugin {

    @PluginMethod()
    public void subscribeTo(final PluginCall call) {
        final String topicName = call.getString("topic");

        FirebaseMessaging
                .getInstance()
                .subscribeToTopic(topicName)
                .addOnSuccessListener(aVoid -> {
                    JSObject ret = new JSObject();
                    ret.put("message", "Subscribed to topic " + topicName);
                    call.success(ret);
                })
                .addOnFailureListener(e -> call.error("Cant subscribe to topic" + topicName, e));

    }

    @PluginMethod()
    public void unsubscribeFrom(final PluginCall call) {
        final String topicName = call.getString("topic");

        FirebaseMessaging
                .getInstance()
                .unsubscribeFromTopic(topicName)
                .addOnSuccessListener(aVoid -> {
                    JSObject ret = new JSObject();
                    ret.put("message", "Unsubscribed from topic " + topicName);
                    call.success(ret);
                })
                .addOnFailureListener(e -> call.error("Cant unsubscribe from topic" + topicName, e));

    }

    @PluginMethod()
    public void deleteInstance(final PluginCall call) {
        FirebaseInstallations.getInstance().delete()
                .addOnSuccessListener(aVoid -> call.resolve())
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    call.reject("Cant delete Firebase Instance ID", e);
                });
    }

    @PluginMethod()
    public void getToken(final PluginCall call) {

        this.requestPushNotificationPermission(call);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(getActivity(), tokenResult -> {
            JSObject data = new JSObject();
            data.put("token", tokenResult.getResult());
            call.resolve(data);
        });

        FirebaseMessaging.getInstance().getToken().addOnFailureListener(e -> call.reject("Failed to get FCM registration token", e));
    }

    @PluginMethod()
    public void setAutoInit(final PluginCall call) {
        final boolean enabled = call.getBoolean("enabled", false);
        FirebaseMessaging.getInstance().setAutoInitEnabled(enabled);
        call.success();
    }

    @PluginMethod()
    public void isAutoInitEnabled(final PluginCall call) {

        final boolean enabled = FirebaseMessaging.getInstance().isAutoInitEnabled();
        JSObject data = new JSObject();
        data.put("enabled", enabled);
        call.success(data);
    }

    @PluginMethod()
    public void requestPushNotificationPermission(final PluginCall call) {

        if (Build.VERSION.SDK_INT >= 33) {
            Log.v("FCM_Request_Permission", "Requesting FCM Permission for Push Notification...");
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.v("FCM_Request_Permission", "NOT GRANTED YET: Asking via prompt...");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS},101);
            }
        }
    }


}
