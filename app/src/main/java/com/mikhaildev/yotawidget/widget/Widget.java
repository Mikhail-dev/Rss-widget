package com.mikhaildev.yotawidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;

import com.mikhaildev.yotawidget.R;
import com.mikhaildev.yotawidget.WidgetService;
import com.mikhaildev.yotawidget.controller.ContentController;
import com.mikhaildev.yotawidget.model.News;
import com.mikhaildev.yotawidget.ui.activity.ConfigActivity;
import com.mikhaildev.yotawidget.util.PreferenceUtils;

import java.util.Map;


public class Widget extends AppWidgetProvider {

    public static final String NEW_NEWS = "new_news";
    public static final String ERROR_STRING = "error_string";
    public static final String ACTION_NEXT_NEWS = "com.mikhaildev.yotawidget.ACTION_NEXT_NEWS";
    public static final String ACTION_PREVIOUS_NEWS = "com.mikhaildev.yotawidget.ACTION_PREVIOUS_NEWS";
    private static final String METHOD_SET_VISIBILITY = "setVisibility";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        ComponentName thisWidget = new ComponentName(context, Widget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        Map<Integer, String> selectedRowIds = PreferenceUtils.getSelectedNewsIds(context);

        for (int i = 0; i < allWidgetIds.length; i++) {
            int widgetId = allWidgetIds[i];
            if (selectedRowIds.containsKey(widgetId)) {
                String newsRowId = selectedRowIds.get(widgetId);
                News news = ContentController.getInstance().getNewsByNewsId(context, newsRowId);
                Widget.updateWidgetWithSuccessResult(context, appWidgetManager, news, widgetId);
            } else {
                Widget.updateWidgetWithSuccessResult(context, appWidgetManager, null, widgetId);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int widgetID : appWidgetIds) {
            PreferenceUtils.removeRssUrl(context, widgetID);
            PreferenceUtils.removeSelectedNewsId(context, widgetID);
            ContentController.getInstance().removeNewsByWidgetId(context, widgetID);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        WidgetService.stopUpdatingNews(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        switch (intent.getAction()) {
            case WidgetService.ACTION_UPDATE_WIDGET_NEWS_SUCCESS:
                handleUpdatingWidget(context, intent);
                break;
            case ACTION_NEXT_NEWS:
                showAnotherNews(context, intent);
                break;
            case ACTION_PREVIOUS_NEWS:
                showAnotherNews(context, intent);
                break;
            case WidgetService.ACTION_UPDATE_WIDGET_NEWS_ERROR:
                showError(context, intent);
                break;
        }
    }

    private void handleUpdatingWidget(Context context, Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (widgetId==AppWidgetManager.INVALID_APPWIDGET_ID)
            return;

        if (PreferenceUtils.getSelectedNewsIds(context).containsKey(widgetId)) {
            //значит мы уже что-то показываем, следовательно не стоит нам что-то обновлять для пользователя,
            // который может читать данную новость, но, возможно, можно показать уведомление
            //пользователю о том, что есть свежие новости
            int newNewsCount = intent.getIntExtra(Widget.NEW_NEWS, 0);
            if (newNewsCount>0) {
                //TODO ?
            }
            return;
        }

        News news = ContentController.getInstance().getLastNews(context, widgetId);

        PreferenceUtils.setSelectedNewsId(context, widgetId, news.getNewsId());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Widget.updateWidgetWithSuccessResult(context, appWidgetManager, news, widgetId);
    }

    private void showAnotherNews(Context context, Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId==AppWidgetManager.INVALID_APPWIDGET_ID)
            return;

        Map<Integer, String> selectedRowIds = PreferenceUtils.getSelectedNewsIds(context);
        News news = null;
        if (selectedRowIds.containsKey(widgetId)) {
            String selectedRowId = selectedRowIds.get(widgetId);
            News currentNews = ContentController.getInstance().getNewsByNewsId(context, selectedRowId);
            if (intent.getAction().equals(ACTION_NEXT_NEWS)) {
                news = ContentController.getInstance().getNextNews(context, widgetId, currentNews.getPublicationDate());
            } else if (intent.getAction().equals(ACTION_PREVIOUS_NEWS)) {
                news = ContentController.getInstance().getPreviousNews(context, widgetId, currentNews.getPublicationDate());
            }
        } else {
            news = ContentController.getInstance().getLastNews(context, widgetId);
        }

        if (news==null)
            return;

        PreferenceUtils.setSelectedNewsId(context, widgetId, news.getNewsId());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Widget.updateWidgetWithSuccessResult(context, appWidgetManager, news, widgetId);
    }

    private void showError(Context context, Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (widgetId==AppWidgetManager.INVALID_APPWIDGET_ID && intent.hasExtra(ERROR_STRING))
            return;

        String error = intent.getStringExtra(ERROR_STRING);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Widget.updateWidgetWithErrorResult(context, appWidgetManager, error, widgetId);
    }

    public static void updateWidgetWithSuccessResult(Context context, AppWidgetManager appWidgetManager, News news, int widgetID) {
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.w_weather);
        widgetView.setInt(R.id.error, METHOD_SET_VISIBILITY, View.GONE);

        Intent configIntent = new Intent(context, ConfigActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, configIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.settings, pIntent);

        if (news==null) {
            widgetView.setInt(R.id.main_panel, METHOD_SET_VISIBILITY, View.GONE);
            widgetView.setInt(R.id.bar, METHOD_SET_VISIBILITY, View.VISIBLE);
            appWidgetManager.updateAppWidget(widgetID, widgetView);
            return;
        } else {
            widgetView.setInt(R.id.main_panel, METHOD_SET_VISIBILITY, View.VISIBLE);
            widgetView.setInt(R.id.bar, METHOD_SET_VISIBILITY, View.GONE);
        }

        if (news.getTitle()==null) {
            widgetView.setInt(R.id.title, METHOD_SET_VISIBILITY, View.GONE);
        } else {
            widgetView.setInt(R.id.title, METHOD_SET_VISIBILITY, View.VISIBLE);
            widgetView.setTextViewText(R.id.title, Html.fromHtml(news.getTitle()));
        }
        widgetView.setTextViewText(R.id.description, Html.fromHtml(news.getDescription()));

        Intent nextNewsIntent = new Intent(context, Widget.class);
        nextNewsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        nextNewsIntent.setAction(ACTION_NEXT_NEWS);
        nextNewsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { widgetID });
        PendingIntent pIntent2 = PendingIntent.getBroadcast(context, widgetID, nextNewsIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.right_btn, pIntent2);

        Intent previousNewsIntent = new Intent(context, Widget.class);
        previousNewsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        previousNewsIntent.setAction(ACTION_PREVIOUS_NEWS);
        previousNewsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { widgetID });
        PendingIntent pIntent3 = PendingIntent.getBroadcast(context, widgetID, previousNewsIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.left_btn, pIntent3);

        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }

    public static void updateWidgetWithErrorResult(Context context, AppWidgetManager appWidgetManager, String error, int widgetID) {
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.w_weather);
        widgetView.setInt(R.id.main_panel, METHOD_SET_VISIBILITY, View.GONE);
        widgetView.setInt(R.id.bar, METHOD_SET_VISIBILITY, View.GONE);
        widgetView.setInt(R.id.error, METHOD_SET_VISIBILITY, View.VISIBLE);
        widgetView.setTextViewText(R.id.error, error);
        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }
}
