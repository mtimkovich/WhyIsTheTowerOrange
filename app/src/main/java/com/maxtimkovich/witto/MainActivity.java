package com.maxtimkovich.witto;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class MainActivity extends Activity {
	private final String SITE_URL = "http://whyisthetowerorange.com/";
	private final String TAG = "WITTO";

	private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = (TextView) findViewById(R.id.status);
        status.setMovementMethod(LinkMovementMethod.getInstance());
        status.setLinkTextColor(getResources().getColorStateList(R.color.white));

        fetchStatus();
    }
    
    private void fetchStatus() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, SITE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        writeTowerStatus(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.getMessage());
                    }
                });

        queue.add(stringRequest);
    }

    /* Remove unwanted HTML tags */
    private String removeTags(String html, String[] tags) {
        for (String tag : tags) {
            html = html.replaceAll("<"+tag+"[^>]*>", "");
            html = html.replaceAll("</"+tag+"\\s*>", "");
        }

        return html;
    }

    private void writeTowerStatus(String html) {
        String line;
        Boolean found = false;

        BufferedReader reader = new BufferedReader(new StringReader(html));

        try {
            while ((line = reader.readLine()) != null) {
                if (line.contains("<div id=\"reason\">")) {
                    found = true;
                    break;
                }
            }

            reader.close();

            if (found) {
                line = removeTags(line, new String[]{"p", "font"});
            } else {
                line = "Error reading from website";
            }

            status.setText(Html.fromHtml(line));

        } catch (IOException error) {
            Log.e(TAG, error.getMessage());
        }
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
    			fetchStatus();
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
}
