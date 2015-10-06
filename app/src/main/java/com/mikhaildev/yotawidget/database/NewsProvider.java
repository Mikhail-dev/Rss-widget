package com.mikhaildev.yotawidget.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;


public class NewsProvider extends ContentProvider {

    private DatabaseHelper database;

    private static final int NEWS = 10;
    private static final int NEWS_ID = 20;

    private static final String AUTHORITY = "com.mikhaildev.yotawidget.database.NewsProvider";

    private static final String NEWS_PATH = "news";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + NEWS_PATH);


    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, NEWS_PATH, NEWS);
        sURIMatcher.addURI(AUTHORITY, NEWS_PATH + "/#", NEWS_ID);
    }

    @Override
    public boolean onCreate() {
        database = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        checkColumns(projection);
        queryBuilder.setTables(NewsTable.TABLE_NAME);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case NEWS:
                break;
            case NEWS_ID:
                queryBuilder.appendWhere(NewsTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        int updatedRowsCount = 0;
        try {
            for (ContentValues cv : values) {
                boolean success = db.insertWithOnConflict(NewsTable.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE) !=-1;
                if (success) ++updatedRowsCount;
            }
            db.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(uri, null);
        } finally {
            db.endTransaction();
        }
        return updatedRowsCount;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case NEWS:
                id = sqlDB.insertWithOnConflict(NewsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(NEWS_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case NEWS:
                rowsDeleted = sqlDB.delete(NewsTable.TABLE_NAME, selection, selectionArgs);
                break;
            case NEWS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(NewsTable.TABLE_NAME, NewsTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(NewsTable.TABLE_NAME,
                            NewsTable.COLUMN_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case NEWS:
                rowsUpdated = sqlDB.update(NewsTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NEWS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(NewsTable.TABLE_NAME,
                            values, NewsTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(NewsTable.TABLE_NAME, values,
                            NewsTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = { NewsTable.COLUMN_ID, NewsTable.COLUMN_TITLE, NewsTable.COLUMN_CONTENT, NewsTable.COLUMN_PUBLICATION_DATE};
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));

            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

}
