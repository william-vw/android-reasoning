package wvw.mobile.rules.eye_js;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import wvw.mobile.rules.eyebrow.ReasonCmd;
import wvw.mobile.rules.eyebrow.Reasoner;

public class EyeJsReasoner implements Reasoner {

    private WebView webView;

    private boolean loaded = false;
    private List<ReasonCmd> queue = new ArrayList<>();

    private ReasonCmd cur;

    public EyeJsReasoner(WebView webView, Context c) {
        this.webView = webView;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "Caller");
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
//                webView.evaluateJavascript("init();", (value) -> {
//                    // returns before init function is done
//                });
            }

            @Override
            @TargetApi(21)
            // looks like we could load asset scripts this way
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.w("android-rules", "request: " + request.getUrl());
                // return ...
                return null;
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("file:///android_asset/eye-js/perf-console-assets.html");
    }

    @JavascriptInterface
    public void initDone() {
        Log.d("android-rules", "[eye-js] initDone()");

        loaded = true;
        runNext();
    }

    private void runNext() {
        if (!queue.isEmpty())
            run(queue.remove(0));
    }

    @SuppressLint("NewApi")
    public synchronized void run(ReasonCmd cmd) {
        if (!loaded)
            queue.add(cmd);
        else {
            cur = cmd;
            webView.post(() -> {
                Log.d("android-rules", "[eye-js] code:\n" + cmd.getCode());
                webView.evaluateJavascript("run(\"" + cmd.getCode() + "\");",
                        (value) -> {
                            // returns before function is done
                        });
            });
        }
    }

    @JavascriptInterface
    public void runDone(String result, String error) {
        Log.d("android-rules", "[eye-js] runDone()");
        if (error != null)
            cur.getListener().error(error);
        else
            cur.getListener().result(result);

        runNext();
    }
}