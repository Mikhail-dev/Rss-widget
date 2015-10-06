package com.mikhaildev.yotawidget.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.mikhaildev.yotawidget.R;
import com.mikhaildev.yotawidget.controller.api.ApiController;
import com.mikhaildev.yotawidget.database.NewsProvider;
import com.mikhaildev.yotawidget.database.NewsTable;
import com.mikhaildev.yotawidget.exception.ApiException;
import com.mikhaildev.yotawidget.model.News;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.matshofman.saxrssreader.RssItem;

/**
 * Class for working with data. It also works with database (some interface for working with database)
 */
public class ContentController {

    private static ContentController instance;
    private static final Object lock = new Object();


    private ContentController() {

    }

    public static ContentController getInstance() {
        if (instance==null) {
            synchronized (lock) {
                if (instance==null)
                    instance = new ContentController();
            }
        }
        return instance;
    }

    /**
     * This method updates news in database and returns count of new rows in database for selected url
     * @param context
     * @param widgetId
     * @param url
     * @return
     * @throws IOException
     */
    public int updateNews(Context context, int widgetId, String url) throws IOException {
        List<RssItem> news = ApiController.getInstance().getNews(context, url);

        if (news==null || news.size()==0) {
            return 0;
        }

        Log.d("sample", "updateNews 2");

        List<ContentValues> values = new ArrayList<>();
        for (int i = 0; i < news.size(); i++) {
            RssItem item = news.get(i);

            if (item.getPubDate()==null)
                throw new ApiException(R.string.parse_error);

            ContentValues value = new ContentValues();
            String key = item.getLink() + "\\" + item.getPubDate().getTime() + "\\" + widgetId;
            value.put(NewsTable.COLUMN_ID, key);
            value.put(NewsTable.COLUMN_LINK, item.getLink());
            value.put(NewsTable.COLUMN_TITLE, item.getTitle());
            value.put(NewsTable.COLUMN_DESCRIPTION, item.getDescription());
            value.put(NewsTable.COLUMN_CONTENT, item.getContent());
            value.put(NewsTable.COLUMN_PUBLICATION_DATE, item.getPubDate().getTime());
            value.put(NewsTable.COLUMN_WIDGET_ID, widgetId);
            values.add(value);
        }

        int updatedRows = 0;
        if (values.size()>0)
            updatedRows = context.getContentResolver().bulkInsert(NewsProvider.CONTENT_URI, values.toArray(new ContentValues[values.size()]));

        return updatedRows;
    }

    public void removeAllNews(Context context) {
        context.getContentResolver().delete(NewsProvider.CONTENT_URI, null, null);
    }

    public void removeNewsByWidgetId(Context context, int widgetId) {
        context.getContentResolver().delete(NewsProvider.CONTENT_URI,
                NewsTable.COLUMN_WIDGET_ID + "=?", new String[]{String.valueOf(widgetId)});
    }

    public News getNewsByNewsId(Context context, String newsId) {
        Cursor cursor = context.getContentResolver().query(NewsProvider.CONTENT_URI, null,
                NewsTable.COLUMN_ID + "=?", new String[] {newsId}, null);
        if (cursor==null || cursor.getCount()==0)
            return null;

        cursor.moveToFirst();

        News news = new News();
        fillNews(cursor, news);

        return news;
    }

    public News getLastNews(Context context, int widgetId) {
        Cursor cursor = context.getContentResolver().query(
                NewsProvider.CONTENT_URI,
                null,
                NewsTable.COLUMN_WIDGET_ID + "=?",
                new String[] {String.valueOf(widgetId)},
                NewsTable.DEFAULT_SORT_ORDER
        );

        if (cursor==null || cursor.getCount()==0)
            return null;

        cursor.moveToFirst();

        News news = new News();
        fillNews(cursor, news);

        return news;
    }

    /**
     * Returns news. This news is the next news after current news, which has date equal newsTime
     * @param context
     * @param widgetId
     * @param currentNewsTime a time of current news
     * @return
     */
    public News getNextNews(Context context, int widgetId, long currentNewsTime) {
        Cursor cursor = context.getContentResolver().query(
                NewsProvider.CONTENT_URI,
                null,
                NewsTable.COLUMN_WIDGET_ID + "=" + widgetId + " AND " + NewsTable.COLUMN_PUBLICATION_DATE + "<" + currentNewsTime,
                null,
                NewsTable.DEFAULT_SORT_ORDER
        );

        if (cursor==null || cursor.getCount()==0)
            return null;

        cursor.moveToFirst();

        News news = new News();
        fillNews(cursor, news);

        return news;
    }

    /**
     * Returns news. This news is the previous news before current news, which has date equal newsTime
     * @param context
     * @param widgetId
     * @param currentNewsTime a time of current news
     * @return
     */
    public News getPreviousNews(Context context, int widgetId, long currentNewsTime) {
        Cursor cursor = context.getContentResolver().query(
                NewsProvider.CONTENT_URI,
                null,
                NewsTable.COLUMN_WIDGET_ID + "=" + widgetId + " AND " + NewsTable.COLUMN_PUBLICATION_DATE + ">" + currentNewsTime,
                null,
                NewsTable.DEFAULT_SORT_ORDER
        );

        if (cursor==null || cursor.getCount()==0)
            return null;

        cursor.moveToLast();

        News news = new News();
        fillNews(cursor, news);

        return news;
    }

    private News fillNews(Cursor cursor, News news) {
        news.setNewsId(cursor.getString(cursor.getColumnIndex(NewsTable.COLUMN_ID)));
        news.setLink(cursor.getString(cursor.getColumnIndex(NewsTable.COLUMN_LINK)));
        news.setTitle(cursor.getString(cursor.getColumnIndex(NewsTable.COLUMN_TITLE)));
        news.setDescription(cursor.getString(cursor.getColumnIndex(NewsTable.COLUMN_DESCRIPTION)));
        news.setContent(cursor.getString(cursor.getColumnIndex(NewsTable.COLUMN_CONTENT)));
        news.setPublicationDate(cursor.getLong(cursor.getColumnIndex(NewsTable.COLUMN_PUBLICATION_DATE)));
        return news;
    }
}
