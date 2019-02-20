package cn.skyui.library.web.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import cn.skyui.aidl.IAppInterface;
import cn.skyui.library.web.R;
import cn.skyui.library.web.jsmethod.JavaScriptMethod;
import cn.skyui.library.web.jsbridge.DefaultHandler;
import cn.skyui.library.web.widget.CustomWebChromeClient;
import cn.skyui.library.web.widget.CustomWebView;
import cn.skyui.library.web.widget.CustomWebViewClient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

/**
 * 1. 独立进程：AIDL通信，获取App用户态，调用主App分享界面等
 * 2. 使用JsBridge作为js和java通信：https://github.com/lzyzsd/JsBridge
 * 3. 进度条
 * 4. Title自动更新
 *
 * @author tianshaojie
 * @date 2018/1/15
 */
public class WebViewActivity extends BaseWebViewActivity implements SwipeBackActivityBase {

    private static final String TAG = "WebViewActivity";
    private static final String ACTION = "cn.skyui.aidl.IAppInterface";

    public static final String URL = "url";
    public static final String TITLE = "title";

    private SwipeBackActivityHelper mHelper;
    private CustomWebView mWebView;
    private JavaScriptMethod javaScriptMethod;
    private String sourceUrl;     // 打开页面时的URL
    private String currentUrl;    // 点击链接后的URL

    private IAppInterface appInterface;

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            appInterface = IAppInterface.Stub.asInterface(service);
            javaScriptMethod.setAppInterface(appInterface);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            appInterface = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayout());
        setConfigCallback((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        initIntentData();
        if (sourceUrl == null || sourceUrl.length() == 0) {
            finish();
            return;
        }
        javaScriptMethod = new JavaScriptMethod(this);
        bindService();
        initView();
    }

    protected int getContentViewLayout() {
        return R.layout.activity_webview;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    private void initIntentData() {
        Intent intent = getIntent();
        sourceUrl = intent.getStringExtra(URL);
        if (sourceUrl == null || sourceUrl.length() == 0) {
            Toast.makeText(this, "Url不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUrl = sourceUrl;
        String title = intent.getStringExtra(TITLE);
        if (title != null) {
            setTitle(title);
        }
    }

    private void bindService() {
        Intent service = new Intent(ACTION);
        service.setPackage(getPackageName());
        bindService(service, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void initView() {
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        // 代码添加WebView
        FrameLayout mWebContainer = (FrameLayout) findViewById(R.id.layoutWebViewParent);
        mWebView = new CustomWebView(this);
        mWebContainer.addView(mWebView);

        mWebView.setLongClickable(true);
        mWebView.setScrollbarFadingEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setDrawingCacheEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // WebView远程代码执行漏洞修复
        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.removeJavascriptInterface("accessibility");
        mWebView.removeJavascriptInterface("accessibilityTraversal");

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });
        mWebView.setDefaultHandler(new DefaultHandler());
        mWebView.loadUrl(sourceUrl);
        mWebView.setWebViewClient(new CustomWebViewClient(this, mWebView));
        mWebView.setWebChromeClient(new CustomWebChromeClient(this));

        // 初始化JS调用方法
        for (String key : javaScriptMethod.handlers.keySet()) {
            mWebView.registerHandler(key, javaScriptMethod.handlers.get(key));
        }
    }

    @Override
    protected void onDestroy() {
        // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再onDestroy
        if (mWebView != null) {
            ViewParent parent = mWebView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mWebView);
            }

            mWebView.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            mWebView.getSettings().setJavaScriptEnabled(false);
            mWebView.clearHistory();
            mWebView.clearView();
            mWebView.removeAllViews();
            mWebView.mProgressBar = null;
            mWebView.destroy();
            mWebView = null;
        }
        setConfigCallback(null);
        if (appInterface != null) {
            unbindService(mServiceConnection);
        }
        super.onDestroy();
//        System.exit(0);
    }

    public void setConfigCallback(WindowManager windowManager) {
        try {
            Field field = WebView.class.getDeclaredField("mWebViewCore");
            field = field.getType().getDeclaredField("mBrowserFrame");
            field = field.getType().getDeclaredField("sConfigCallback");
            field.setAccessible(true);
            Object configCallback = field.get(null);

            if (null == configCallback) {
                return;
            }

            field = field.getType().getDeclaredField("mWindowManager");
            field.setAccessible(true);
            field.set(configCallback, windowManager);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                onBackClick();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onBackClick() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackClick();
            return true;
        } else if (id == R.id.action_refresh) {
            mWebView.loadUrl(currentUrl);
        } /*else if(id == R.id.action_share) {
            try {
                appInterface.invokeShare();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }*/ else if (id == R.id.action_copy) {
            ClipboardManager clipboard = (ClipboardManager) getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newUri(getApplication().getContentResolver(), "uri", Uri.parse(currentUrl)));
        } else if (id == R.id.action_browser) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl)));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 使隐藏菜单项显示icon
     */
    @SuppressLint("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }
}
