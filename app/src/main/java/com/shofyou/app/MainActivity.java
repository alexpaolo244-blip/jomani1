package com.shofyou.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipe;
    private FileUploadHelper fileUploadHelper;
    private final String HOME_URL = "https://shofyou.com";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // تحسين أداء الرسوميات
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
                
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags != Configuration.UI_MODE_NIGHT_YES) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        webView = findViewById(R.id.webview);
        swipe = findViewById(R.id.swipe);
        ImageView splashLogo = findViewById(R.id.splashLogo);
        fileUploadHelper = new FileUploadHelper(this);

        WebSettings ws = webView.getSettings();
        
        // 🔥 إعدادات السرعة القصوى
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true); // تخزين البيانات محلياً لسرعة الفتح لاحقاً
        ws.setCacheMode(WebSettings.LOAD_DEFAULT); // تفعيل الكاش
        ws.setAllowFileAccess(true);
        ws.setDatabaseEnabled(true);
        
        // تحسين تحميل الصور والميديا
        ws.setLoadsImagesAutomatically(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setMediaPlaybackRequiresUserGesture(false);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                splashLogo.setVisibility(View.GONE);
                swipe.setRefreshing(false);
                swipe.setEnabled(!(url != null && url.contains("/reels/")));
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("shofyou.com")) {
                    view.loadUrl(url);
                    return true;
                }
                startActivity(new Intent(MainActivity.this, PopupActivity.class).putExtra("url", url));
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, android.webkit.ValueCallback<android.net.Uri[]> callback, FileChooserParams params) {
                return fileUploadHelper.handleFileChooser(callback, params);
            }
        });

        swipe.setOnRefreshListener(() -> {
            if (webView.getUrl() != null && webView.getUrl().contains("/reels/")) {
                swipe.setRefreshing(false);
            } else {
                webView.reload();
            }
        });

        webView.loadUrl(HOME_URL);
        handleBack();
    }

    private void handleBack() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack();
                else new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Exit app?")
                        .setPositiveButton("Yes", (d, i) -> finish())
                        .setNegativeButton("No", null).show();
            }
        });
    }
}
