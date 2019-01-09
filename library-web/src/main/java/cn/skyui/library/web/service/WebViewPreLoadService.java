package cn.skyui.library.web.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * @author tianshaojie
 * 进程预加载，一次性使用，主要为了创建H5进程
 * 一般在SplashActivity内调用：
 * SplashActivity onCreate  调用 startHideService
 * SplashActivity onDestory 调用 stopHideService
 */
public class WebViewPreLoadService extends Service {

    public static void startHideService(Context context){
        Intent intent = new Intent(context, WebViewPreLoadService.class);
        context.startService(intent);
    }

    public static void stopHideService(Context context){
        Intent intent = new Intent(context, WebViewPreLoadService.class);
        context.stopService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}