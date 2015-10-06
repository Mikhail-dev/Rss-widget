package com.mikhaildev.yotawidget;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mikhaildev.yotawidget.controller.ContentController;
import com.mikhaildev.yotawidget.controller.api.ApiController;
import com.mikhaildev.yotawidget.exception.ApiException;
import com.mikhaildev.yotawidget.exception.NetworkConnectionException;
import com.mikhaildev.yotawidget.util.DateUtils;
import com.mikhaildev.yotawidget.util.PreferenceUtils;
import com.mikhaildev.yotawidget.widget.Widget;

import java.io.IOException;
import java.util.Map;


public class WidgetService extends IntentService {

    private static final String TAG = "WidgetService";

    public static final String ACTION_UPDATE_NEWS = "com.mikhaildev.yotawidget.UPDATE_NEWS";
    public static final String ACTION_STOP_UPDATING_NEWS = "com.mikhaildev.yotawidget.STOP_UPDATING_NEWS";
    public static final String ACTION_UPDATE_WIDGET_NEWS_ERROR = "com.mikhaildev.yotawidget.ACTION_UPDATE_WIDGET_NEWS_ERROR";
    public static final String ACTION_UPDATE_WIDGET_NEWS_SUCCESS = "com.mikhaildev.yotawidget.ACTION_UPDATE_WIDGET_NEWS_SUCCESS";

    private static final long UPDATE_NEWS_TIME_INTERVAL_MILLIS = 1 * DateUtils.MINUTE;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public WidgetService() {
        super("WidgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String action = intent.getAction();
            switch (action) {
                case ACTION_UPDATE_NEWS:
                    onUpdateDelayedNews();
                    break;
                case ACTION_STOP_UPDATING_NEWS:
                    onStopUpdatingNews();
                    break;
                default:
                    Log.e(TAG, "Handled unknown action");
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "onHandleIntent exception." + e.getMessage(), e);
        }
    }

    private void onUpdateDelayedNews() {
        Map<Integer, String> widgetUrls = PreferenceUtils.getRssUrls(getApplicationContext());
        for (Map.Entry<Integer, String> entry : widgetUrls.entrySet()) {
            int widgetId = entry.getKey();
            String url = entry.getValue();
            try {
                int updatedRows = ContentController.getInstance().updateNews(getApplicationContext(), widgetId, url);
                handleSuccessResult(updatedRows, widgetId);
            } catch (IOException e) {
                handleErrorResult(e, widgetId);
                e.printStackTrace();
            }
        }
    }

    private void handleSuccessResult(int updatedRows, int widgetId) {
        if (updatedRows>0) {
            Intent intent = new Intent(ACTION_UPDATE_WIDGET_NEWS_SUCCESS);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.putExtra(Widget.NEW_NEWS, updatedRows);
            sendPendingIntent(this, intent, widgetId);
        }
    }

    private void handleErrorResult(IOException e, int widgetId) {
        IOException exception = ApiController.getInstance().getException(e);

        Intent intent = new Intent(ACTION_UPDATE_WIDGET_NEWS_ERROR);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        if (exception.getClass().equals(NetworkConnectionException.class)) {
            //ошибку подключения к интернету не показываем пользователю. Не имеет смысла
        } else if (exception.getClass().equals(ApiException.class)) {
            ApiException ex = (ApiException) exception;
            intent.putExtra(Widget.ERROR_STRING, getString(ex.getMessageResourceId()));
            sendPendingIntent(this, intent, widgetId);
        } else {
            intent.putExtra(Widget.ERROR_STRING, e.getMessage());
            sendPendingIntent(this, intent, widgetId);
        }
    }

    private void onStopUpdatingNews() {
        stopDelayedTask(this, ACTION_UPDATE_NEWS);
        ContentController.getInstance().removeAllNews(this);
    }

    public static void startUpdatingNews(Context context) {
        startDelayedTask(context, ACTION_UPDATE_NEWS, UPDATE_NEWS_TIME_INTERVAL_MILLIS);
    }

    public static void stopUpdatingNews(Context context) {
        startDelayedTask(context, ACTION_STOP_UPDATING_NEWS, UPDATE_NEWS_TIME_INTERVAL_MILLIS);
    }

    public static void startDelayedTask(Context context, String action, long intervalMillis) {
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis, pendingIntent);
    }

    public static void stopDelayedTask(Context context, String action) {
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void sendPendingIntent(Context context, Intent intent, int widgetId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
    }
}
