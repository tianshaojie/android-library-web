package cn.skyui.ibrary.web.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import cn.skyui.aidl.IAppInterface;


/**
 * App相当于服务端，实现 WebView AIDL接口
 * @author tianshaojie
 * @date 2018/2/10.
 */
public class WebViewBridgeService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new AppBinder();
    }

    /**
     * 实现 WebView 组件内定义的 AIDL 接口，暴露出APP容器的信息和功能；
     */
    private class AppBinder extends IAppInterface.Stub {
        @Override
        public String getToken() {
            // 获取登录用户token，并返回
            return "token";
        }

        @Override
        public boolean isLogin() {
            // 获取用户登录态，并返回
            return true;
        }

        @Override
        public long getUserId() {
            // 获取用户Id，并返回
            return 0;
        }

        @Override
        public void invokeShare() {
            // 调用容器的UI组件
//            SocialManager.share(activity, new);
        }
    }
}
