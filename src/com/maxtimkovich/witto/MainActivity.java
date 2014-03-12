package com.maxtimkovich.witto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ActionBar;
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
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final String SITE_URL = "http://whyisthetowerorange.com/";
	private final String DEBUG_TAG = "WITTO";
	private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayShowTitleEnabled(false);
        
        status = (TextView) findViewById(R.id.status);
        status.setMovementMethod(LinkMovementMethod.getInstance());
        status.setLinkTextColor(getResources().getColorStateList(R.color.white));
        
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
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
    	@Override
    	protected String doInBackground(String... urls) {
    		try {
    			return downloadUrl(urls[0]);
    		} catch (IOException e) {
    			return "Unable to retrieve web page.";
    		}
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {
    		status.setText(Html.fromHtml(result));
    	}
    }
    
    private String removeTags(String html, String[] tags) {
    	for (String tag : tags) {
    		html = html.replaceAll("<\\s*"+tag+"[^>]*>", "");
    		html = html.replaceAll("</"+tag+">", "");
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

    	/* Find the line with the tower status */
    	while ((line = in.readLine()) != null) {
    		if (line.contains("<p class=\"reason\">")) {
    			break;
    		}
    	}
    	
    	stream.close();
    	
    	/* Remove the HTML tags */
    	line = removeTags(line, new String[]{"p", "font"});
    	
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
