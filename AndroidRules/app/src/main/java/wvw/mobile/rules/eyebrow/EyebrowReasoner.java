package wvw.mobile.rules.eyebrow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.List;

public class EyebrowReasoner implements Reasoner {

    private WebView webView;

    private boolean loaded = false;
    private List<ReasonCmd> queue = new ArrayList<>();

    private ReasonCmd cur;

    public EyebrowReasoner(WebView webView, Context c) {
        this.webView = webView;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "Caller");
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                webView.evaluateJavascript("init();", (value) -> {
                    // returns before init function is done
                });
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("http://10.200.82.217:8000/run.html");
//        webView.loadUrl("file:///android_asset/eyebrow/run.html");
    }

    @JavascriptInterface
    public void initDone() {
        Log.d("android-rules", "[eyebrow] initDone()");

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
                Log.d("android-rules", "[eyebrow] code:\n" + cmd.getCode());
                webView.evaluateJavascript("run(\"" + cmd.getCode() + "\");",
                    (value) -> {
                        // returns before function is done
                    });
            });
        }
    }

    @JavascriptInterface
    public void runDone(String result, String error) {
        Log.d("android-rules", "[eyebrow] runDone()");
        if (error != null)
            cur.getListener().error(error);
        else
            cur.getListener().result(result);

        runNext();
    }

//    public class WebAppInterface {
//        Context mContext;
//
//        /** Instantiate the interface and set the context */
//        WebAppInterface(Context c) {
//            mContext = c;
//        }
//
//        @JavascriptInterface
//        public void log(String msg) {
//            Log.d("android-rules", "received from JS:\n" + msg);
//        }
//    }
}