package com.mikhaildev.yotawidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class SystemEventReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                WidgetService.startUpdatingNews(context);
                break;
            default:
                break;
        }
    }
}
