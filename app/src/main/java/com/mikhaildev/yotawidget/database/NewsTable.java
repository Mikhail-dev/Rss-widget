package com.mikhaildev.yotawidget.database;

import android.database.sqlite.SQLiteDatabase;


public class NewsTable {

    public static final String TABLE_NAME = "news";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_PUBLICATION_DATE = "publication_date";
    public static final String COLUMN_WIDGET_ID = "widget_id";

    public static final String DEFAULT_SORT_ORDER = COLUMN_PUBLICATION_DATE + " DESC";

    public static final String[] PROJECTION = new String[] {
            COLUMN_ID, COLUMN_TITLE, COLUMN_LINK, COLUMN_DESCRIPTION,
            COLUMN_CONTENT, COLUMN_PUBLICATION_DATE, COLUMN_WIDGET_ID
    };

    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " text primary key, "
            + COLUMN_TITLE + " text, "
            + COLUMN_LINK + " text, "
            + COLUMN_DESCRIPTION + " text, "
            + COLUMN_CONTENT + " text, "
            + COLUMN_PUBLICATION_DATE + " integer not null, "
            + COLUMN_WIDGET_ID + " integer not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        //fast implementation of upgrade
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }
}
