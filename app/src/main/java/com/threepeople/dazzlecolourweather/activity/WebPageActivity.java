package com.threepeople.dazzlecolourweather.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.threepeople.dazzlecolourweather.R;
import com.threepeople.dazzlecolourweather.utils.MyLog;
import com.threepeople.dazzlecolourweather.utils.Tools;

/**
 * @deprecated 应用内网页加载异常，暂弃用
 */
@Deprecated
public class WebPageActivity extends AppCompatActivity {
    private final MyLog myLog = new MyLog("WebPageTAG");

    private WebView webPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_page);

        String cityId = getIntent().getStringExtra(Tools.cityIdKey);
        if (cityId != null) {
            myLog.d("请求的城市ID ==> " + cityId);
            webPage = findViewById(R.id.webPage);
            webPage.setWebViewClient(new WebViewClient());
            String url = "http://mzkt.xyz/weather?cityAddress=" + cityId;
            myLog.d("url ==> " + url);
            webPage.loadUrl(url);
        } else {
            Toast.makeText(this, "请求错误", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (webPage.canGoBack()) {
            webPage.goBack();
        } else {
            super.onBackPressed();
        }
    }
}