/*
 * Zirco Browser for Android
 * 
 * Copyright (C) 2010 J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package cn.skyui.app.library.web.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import cn.skyui.app.web.R;
import cn.skyui.app.library.web.jsbridge.BridgeWebView;

/**
 * A convenient extension of WebView.
 */
public class CustomWebView extends BridgeWebView {

    Context context;
    public ProgressBar mProgressBar;

    /**
     * Constructor.
     *
     * @param context The current context.
     */
    public CustomWebView(Context context) {
        super(context);
        this.context = context;
        initializeOptions();
    }

    /**
     * Constructor.
     *
     * @param context The current context.
     * @param attrs   The attribute set.
     */
    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initializeOptions();
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initializeOptions();
    }

    /**
     * Initialize the WebView with the options set by the user through
     * preferences.
     */
    @SuppressLint("NewApi")
    public void initializeOptions() {
        // ProgressBar
        mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 8);
        mProgressBar.setLayoutParams(layoutParams);
        Drawable drawable = context.getResources().getDrawable(R.drawable.web_progress_bar_states);
        mProgressBar.setProgressDrawable(drawable);
        addView(mProgressBar);

        // WebSettings
        WebSettings settings = getSettings();
        // User settings
        // settings.setJavaScriptEnabled(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(false);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        // Technical settings
        settings.setSupportMultipleWindows(true);
        setLongClickable(true);
        setScrollbarFadingEnabled(true);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setDrawingCacheEnabled(true);

        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        CookieManager.getInstance().setAcceptCookie(true);
//      CookieManager.setAcceptFileSchemeCookies(true);

        // LOAD_DEFAULT
        // 默认加载方式，使用这种方式，会实现快速前进后退，在同一个标签打开几个网页后，关闭网络时，可以通过前进后退来切换已经访问过的数据，同时新建网页需要网络
        // - LOAD_NO_CACHE
        // - LOAD_NORMAL
        // * 这个方式跟LOAD_NO_CACHE方式相同，不使用缓存，如果没有网络，即使以前打开过此网页也不会使用以前的网页。
        // - LOAD_CACHE_ELSE_NETWORK
        // *
        // 这个方式不论如何都会从缓存中加载，除非缓存中的网页过期，出现的问题就是打开动态网页时，不能时时更新，会出现上次打开过的状态，除非清除缓存。
        // - LOAD_CACHE_ONLY
        // * 这个方式只是会使用缓存中的数据，不会使用网络。

        if (isNetworkConnected()) {
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);// 设置 缓存模式
        }

        // 开启 DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 应用可以有数据库
        settings.setDatabaseEnabled(true);
        String dbPath = context.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabasePath(dbPath);
        // 应用可以有缓存
        settings.setAppCacheEnabled(true);
        String appCaceDir = context.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        settings.setAppCachePath(appCaceDir);


        // this.addJavascriptInterface((JavascriptInterface) this, "JavascriptInterface");
    }

    public boolean isNetworkConnected() {
        NetworkInfo info = ((ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) mProgressBar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        mProgressBar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
