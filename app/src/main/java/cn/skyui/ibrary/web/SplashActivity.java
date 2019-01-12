package cn.skyui.ibrary.web;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import cn.skyui.library.web.service.WebViewPreLoadService;

/**
 * @author tianshaojie
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().postDelayed(this::enter, 1000);
        WebViewPreLoadService.startHideService(this);

    }

    private void enter() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().getDecorView().getHandler().removeCallbacksAndMessages(null);
        WebViewPreLoadService.stopHideService(this);
    }
}