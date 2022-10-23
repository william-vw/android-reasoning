package wvw.mobile.rules;

import android.content.res.AssetManager;
import android.icu.util.Output;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
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

        binding = ActivityRemoteReasonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_remote_reason);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        try {
            reason();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_remote_reason);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
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
                handler.post(() -> Log.d("android-rules", "response? " + response));

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

        } catch(Exception e) {
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