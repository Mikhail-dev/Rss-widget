package com.mikhaildev.yotawidget.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;


public class PreferenceUtils {

    private static final String WIDGET_PREFERENCES = "widget_preferences";
    public static final String WIDGET_URLS = "widget_urls";
    public static final String WIDGET_SELECTED_NEWS_IDS = "widget_selected_news_ids";


    private PreferenceUtils() {

    }

    /**
     * Returns map with identificators of rows. It's identificator of news, which user read now.
     * Key - widgetId, Value - newsId
     * @param context
     * @return Map<Integer, String>
     */
    public static Map<Integer, String> getSelectedNewsIds(Context context) {
        String selectedNewsIdsAsString = getWidgetPreferences(context).getString(WIDGET_SELECTED_NEWS_IDS, "");

        if (selectedNewsIdsAsString.isEmpty() || selectedNewsIdsAsString.equals("[]"))
            return new HashMap<>();

        java.lang.reflect.Type type = new TypeToken<HashMap<Integer, String>>(){}.getType();
        Map<Integer, String> map = new Gson().fromJson(selectedNewsIdsAsString, type);

        return map;
    }

    public static void setSelectedNewsId(Context context, int widgetID, String rowId) {
        Map<Integer, String> selectedNewsIds = getSelectedNewsIds(context);
        selectedNewsIds.put(widgetID, rowId);
        selectedNewsIdsToPreferences(context, selectedNewsIds);
    }

    public static void removeSelectedNewsId(Context context, int widgetId) {
        Map<Integer, String> map = getSelectedNewsIds(context);
        map.remove(widgetId);
        selectedNewsIdsToPreferences(context, map);
    }

    /**
     * This is map with urls of widgets
     * @param context
     * @return Map<Integer, String>
     */
    public static Map<Integer, String> getRssUrls(Context context) {
        String storedHashMapString = getWidgetPreferences(context).getString(WIDGET_URLS, "");
        if (storedHashMapString.isEmpty() || storedHashMapString.equals("[]"))
            return new HashMap<>();

        java.lang.reflect.Type type = new TypeToken<HashMap<Integer, String>>(){}.getType();
        Map<Integer, String> urls = new Gson().fromJson(storedHashMapString, type);

        return urls;
    }

    public static void setRssUrl(Context context, int widgetID, String url) {
        Map<Integer, String> urls = getRssUrls(context);
        urls.put(widgetID, url);
        urlsToPreferences(context, urls);
    }

    public static void removeRssUrl(Context context, int widgetId) {
        Map<Integer, String> urls = getRssUrls(context);
        urls.remove(widgetId);
        urlsToPreferences(context, urls);
    }

    private static void selectedNewsIdsToPreferences(Context context, Map<Integer, String> urls) {
        String json = new Gson().toJson(urls);
        getWidgetPreferences(context).edit().putString(WIDGET_SELECTED_NEWS_IDS, json).commit();
    }

    private static void urlsToPreferences(Context context, Map<Integer, String> urls) {
        String json = new Gson().toJson(urls);
        getWidgetPreferences(context).edit().putString(WIDGET_URLS, json).commit();
    }

    private static SharedPreferences getWidgetPreferences(Context context) {
        return context.getSharedPreferences(WIDGET_PREFERENCES, Context.MODE_MULTI_PROCESS);
    }
}
