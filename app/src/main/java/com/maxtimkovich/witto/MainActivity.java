package com.maxtimkovich.witto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final String SITE_URL = "http://whyisthetowerorange.com/";
	private final String DEBUG_TAG = "WITTO";

	private TextView status;
	private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = (TextView) findViewById(R.id.status);
        status.setMovementMethod(LinkMovementMethod.getInstance());
        status.setLinkTextColor(getResources().getColorStateList(R.color.white));
        
        loading = (ProgressBar) findViewById(R.id.progress);
        
        writeTowerStatus();
    }
    
    private void writeTowerStatus() {
        /* Check the status of the network */
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
        	new DownloadWebpageTask().execute(SITE_URL);
        } else {
        	status.setText("No network connection available.");
        }
    }
    
    /* This is performed in a separate thread from the UI */
    private class DownloadWebpageTask extends AsyncTask<String, Boolean, String> {
    	@Override
    	protected void onPreExecute() {
    		loading.setVisibility(View.VISIBLE);
    	}

    	@Override
    	protected String doInBackground(String... urls) {
    		try {
    			publishProgress(false);
    			String result = downloadUrl(urls[0]);
    			publishProgress(true);

    			return result;
    		} catch (IOException e) {
    			return "Unable to retrieve web page.";
    		}
    	}
    	
    	protected void onProgressUpdate(Boolean... finished) {
    		loading.setIndeterminate(!finished[0]);
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {
    		loading.setVisibility(View.GONE);

    		status.setText(Html.fromHtml(result));
    	}
    }
    
    /* Remove unwanted HTML tags */
    private String removeTags(String html, String[] tags) {
    	for (String tag : tags) {
    		html = html.replaceAll("<"+tag+"[^>]*>", "");
    		html = html.replaceAll("</"+tag+"\\s*>", "");
    	}
    	
    	return html;
    }
    
    /* Convert the URL stream to a string */
    private String downloadUrl(String myurl) throws IOException {
    	URL url = new URL(myurl);
    	InputStream stream = null;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
            
        conn.connect();
        int response = conn.getResponseCode();
            
        Log.d(DEBUG_TAG, "The response is: " + response);
        stream = conn.getInputStream();
    	
    	BufferedReader in = new BufferedReader(new InputStreamReader(stream));
    	
    	String line;
    	Boolean found = false;

    	/* Find the line with the tower status */
    	while ((line = in.readLine()) != null) {
    		if (line.contains("<div id=\"reason\">")) {
    			found = true;
    			break;
    		}
    	}
    	
    	stream.close();
    	
    	if (found) {
            /* Remove the HTML tags */
            line = removeTags(line, new String[]{"p", "font"});
    	} else {
    		line = "Error reading from website";
    	}
    	
    	return line;
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.refresh:
    			writeTowerStatus();
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
}
