package com.netease.im.session;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.netease.im.IMApplication;

public class NotificationClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            try {
                Class mainActivityClass = IMApplication.getMainActivityClass();
                if (mainActivityClass == null) {
                    return;
                }

                Intent launchIntent = new Intent(context, mainActivityClass);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                
                if (intent.getExtras() != null) {
                    launchIntent.putExtras(intent.getExtras());
                }

                context.startActivity(launchIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
