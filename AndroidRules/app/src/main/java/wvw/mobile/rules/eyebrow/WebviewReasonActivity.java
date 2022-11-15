package wvw.mobile.rules.eyebrow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import wvw.mobile.rules.R;

public class WebviewReasonActivity extends AppCompatActivity implements ReasonerListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_reason);

        WebView webView = (WebView) findViewById(R.id.webview);
        Reasoner reasoner = new EyebrowReasoner(webView, this);

        String rdf =
                "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>." +
                "@prefix : <http://example.org/socrates#>." +
                ":Socrates a :Human ." +
                ":Human rdfs:subClassOf :Mortal ." +
                "{?A rdfs:subClassOf ?B. ?S a ?A} => {?S a ?B} .";
        reasoner.run(new ReasonCmd(rdf, this));
    }

    @Override
    public void result(String result) {
        Log.d("android-rules", "result:\n" + result);
    }

    @Override
    public void error(String error) {
        Log.d("android-rules", "error:\n" + error);
    }
}