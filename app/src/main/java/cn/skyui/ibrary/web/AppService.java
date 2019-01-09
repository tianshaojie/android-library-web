package cn.skyui.ibrary.web;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import cn.skyui.aidl.IAppInterface;


/**
 * Created by tiansj on 2018/2/10.
 */

public class AppService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (checkCallingOrSelfPermission("cn.skyui.aidl.permission.SERVICE_PERMISSION") == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        return new AppBinder();
    }

    private class AppBinder extends IAppInterface.Stub {
        @Override
        public String getToken() throws RemoteException {
            // 获取登录用户token，并返回
            return "token";
        }

        @Override
        public boolean isLogin() throws RemoteException {
            // 获取用户登录态，并返回
            return true;
        }

        @Override
        public long getUserId() throws RemoteException {
            // 获取用户Id，并返回
            return 0;
        }

        @Override
        public void invokeShare() throws RemoteException {
            // 调用容器的UI组件
//            SocialManager.share(activity, new);
        }
    }
}
