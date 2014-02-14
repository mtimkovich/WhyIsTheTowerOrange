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
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final String SITE_URL = "http://whyisthetowerorange.com/";
	private final String DEBUG_TAG = "HttpExample";
	private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        status = (TextView) findViewById(R.id.status);
        
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
        	new DownloadWebpageTask().execute(SITE_URL);
        } else {
        	status.setText("No nework connection available.");
        }
    }
    
    /* This is performed in a seperate thread from the UI */
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
    	@Override
    	protected String doInBackground(String... urls) {
    		try {
    			return downloadUrl(urls[0]);
    		} catch (IOException e) {
    			return "Unable to retrieve web page. URL may be invalid.";
    		}
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {
    		status.setText(result);
    	}
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
    	
    	String output = "";
    	String line;

    	/* Find the line with the tower status */
    	while ((line = in.readLine()) != null) {
    		if (line.contains("<p class=\"reason\">")) {
    			output = line;
    			break;
    		}
    	}
    	
    	stream.close();
    	
    	/* Remove the HTML tags */
    	output = output.replaceAll("<[^>]*>", "");
    	
    	return output;
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
