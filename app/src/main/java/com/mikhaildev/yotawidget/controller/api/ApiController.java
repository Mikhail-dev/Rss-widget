package com.mikhaildev.yotawidget.controller.api;

import android.content.Context;

import com.mikhaildev.yotawidget.R;
import com.mikhaildev.yotawidget.exception.ApiException;
import com.mikhaildev.yotawidget.exception.NetworkConnectionException;
import com.mikhaildev.yotawidget.util.Utils;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;


public class ApiController {

    private static final Object lock = new Object();
    private static ApiController instance;

    public static ApiController getInstance() {
        if (instance==null) {
            synchronized (lock) {
                if (instance==null)
                    instance = new ApiController();
            }
        }
        return instance;
    }

    private ApiController() {

    }


    /**
     * Returns List of {@link RssItem}
     * @param context
     * @return List<RssItem>
     * @throws IOException
     */
    public List<RssItem> getNews(Context context, String sUrl) throws IOException {
        if (!Utils.isThereInternetConnection(context))
            throw new NetworkConnectionException();

        RssFeed feed = null;
        URL url = new URL(sUrl);
        try {
            feed = RssReader.read(url);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new ApiException(R.string.parse_error);
        }

        return feed!=null ? feed.getRssItems() : null;
    }

    public IOException getException(IOException e) {
        if (e.getClass().equals(ApiException.class)) {
            return e;
        } else if (e!=null && (
                           e.getClass().equals(java.net.UnknownHostException.class)
                        || e.getClass().equals(java.net.SocketTimeoutException.class)
                        || e.getClass().equals(NetworkConnectionException.class))) {
            return new NetworkConnectionException();
        } else if (e!=null && e.getClass().equals(java.net.MalformedURLException.class)) {
            return new ApiException(R.string.incorrect_url_link);
        } else {
            return new ApiException(R.string.unknown_error);
        }
    }
}
