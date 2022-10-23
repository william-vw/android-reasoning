package wvw.mobile.rules;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wvw.mobile.rules.databinding.ActivityRemoteReasonBinding;

public class RemoteReasonActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityRemoteReasonBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_reason);

        findViewById(R.id.remoteButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    reason();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void reason() throws Exception {
        Model patient = setupDynamicRDF();
        // write RDF to string
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        patient.write(out, "N3");
        String patientRdf = new String(out.toByteArray());

        String n3 = loadStaticN3();

        StringBuffer rdf = new StringBuffer();
        rdf.append(patientRdf).append("\n\n").append(n3);

        String rdfStr = rdf.toString();

        JSONObject post = new JSONObject();
        // derivations, deductive_closure
        post.put("task", "explain");
        post.put("system", "eye");
        post.put("formula", rdfStr);

        String postStr = post.toString();
//        Log.d("android-rules", "post?" + postStr);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = sendPost("http://ppr.cs.dal.ca:3002/n3", postStr);
                handler.post(() -> {
                    try {
                        Log.d("android-rules", "response? " + response);

                        JSONObject obj = new JSONObject(response);
                        String html = obj.getString("success");
                        html = cleanupHtml(html);

                        WebView wv = findViewById(R.id.remoteOutput);
                        wv.loadData(html, "text/html", "UTF-8");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String cleanupHtml(String html) {
        html = removeInterval("<div class='ruleInfo'>", "</div></div>", html);
        html = removeInterval("<div class='source'>", "</div>", html);
        html = removeInterval("<span class='showHideRule'>", "</span>", html);

        html = html.replace("\\\"", "\"");
        return html;
    }

    private String removeInterval(String startTag, String endTag, String html) {
        while (html.indexOf(startTag) != -1) {
            int stIdx = html.indexOf(startTag);
            int endIdx = html.indexOf(endTag, stIdx);

            html = html.substring(0, stIdx) +
                    html.substring(endIdx + endTag.length());
        }

        return html;
    }

    // create Model & load data
    private Model setupDynamicRDF() {
        Model model = ModelFactory.createDefaultModel();

        // example patient
        model.add(
                model.createResource("http://wvw.example.com#PatientA"),
                model.createProperty("http://wvw.example.com#hasGhVasValue"),
                model.createTypedLiteral(40)
        );
        model.add(
                model.createResource("http://wvw.example.com#PatientA"),
                model.createProperty("http://wvw.example.com#height"),
                model.createTypedLiteral(170)
        );
        model.add(
                model.createResource("http://wvw.example.com#PatientA"),
                model.createProperty("http://wvw.example.com#weight"),
                model.createTypedLiteral(95)
        );
        model.add(
                model.createResource("http://wvw.example.com#PatientA"),
                model.createProperty("http://wvw.example.com#bmiValue"),
                model.createTypedLiteral(32.87)
        );

        return model;
    }

    private String loadStaticN3() throws IOException {
        return loadFile("rheumatology.n3");
    }

    private String sendPost(String location, String body) throws IOException {
        String ret = null;

        URL url = new URL(location);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(body.getBytes());
            out.flush();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            ret = readString(in);

        } catch (Exception e) {
            Log.e("android-rules", e.getMessage());
        }

        urlConnection.disconnect();
        return ret;
    }

    protected String loadFile(String path) {
        AssetManager assMan = getAssets();
        try {
            InputStream in = assMan.open(path);
            String ret = readString(in);
            in.close();

            return ret;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected String readString(InputStream in) throws IOException {
        StringBuffer str = new StringBuffer();

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while ((line = br.readLine()) != null)
            str.append(line).append("\n");

        return str.toString();
    }
}