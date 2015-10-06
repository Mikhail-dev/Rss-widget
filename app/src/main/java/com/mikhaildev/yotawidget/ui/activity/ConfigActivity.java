package com.mikhaildev.yotawidget.ui.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mikhaildev.yotawidget.R;
import com.mikhaildev.yotawidget.WidgetService;
import com.mikhaildev.yotawidget.controller.ContentController;
import com.mikhaildev.yotawidget.util.PreferenceUtils;
import com.mikhaildev.yotawidget.widget.Widget;


public class ConfigActivity extends Activity implements View.OnClickListener {

    private static final String EXTRA_WIDGET_ID = "extra_widget_id";
    private static final String EXTRA_WIDGET_URL = "extra_widget_url";

    private int mWidgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText etText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            mWidgetID = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mWidgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetID);
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.a_config);
        etText = (EditText) findViewById(R.id.rss_link);
        findViewById(R.id.ok_btn).setOnClickListener(this);

        if (PreferenceUtils.getRssUrls(this).containsKey(mWidgetID))
            etText.setText(PreferenceUtils.getRssUrls(this).get(mWidgetID));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(EXTRA_WIDGET_ID, mWidgetID);
        savedInstanceState.putString(EXTRA_WIDGET_URL, etText.getText().toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mWidgetID = savedInstanceState.getInt(EXTRA_WIDGET_ID);
        etText.setText(savedInstanceState.getString(EXTRA_WIDGET_URL));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_btn:
                handleOkBtnClick();
                break;
        }
    }

    private void handleOkBtnClick() {
        String url = etText.getText().toString();
        if (Patterns.WEB_URL.matcher(url).matches()) {

            ContentController.getInstance().removeNewsByWidgetId(this, mWidgetID);
            PreferenceUtils.setRssUrl(this, mWidgetID, url);
            PreferenceUtils.removeSelectedNewsId(this, mWidgetID);
            WidgetService.startUpdatingNews(getApplicationContext());

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetID);
            setResult(RESULT_OK, resultValue);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            Widget.updateWidgetWithSuccessResult(this, appWidgetManager, null, mWidgetID);

            finish();
        } else {
            Toast.makeText(this, R.string.incorrect_url_link, Toast.LENGTH_SHORT).show();
        }
    }
}
