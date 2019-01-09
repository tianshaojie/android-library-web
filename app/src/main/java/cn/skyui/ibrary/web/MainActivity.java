package cn.skyui.ibrary.web;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.skyui.library.web.activity.WebViewActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_h5).setOnClickListener(v -> {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.URL, "http://skyui.cn/interest/h5.html");
            startActivity(intent);
        });
    }
}
