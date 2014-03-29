package com.jakecsells.huntnv;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
        final Button button = (Button) findViewById(R.id.button_rate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Uri uri = Uri.parse("market://details?id=" + getBaseContext().getPackageName());
            	Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            	try {
            	  startActivity(goToMarket);
            	} catch (ActivityNotFoundException e) {
            	  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getBaseContext().getPackageName())));
            	}
            }
        });
	}

	
    @Override
    public void onBackPressed() {
        this.finish();
    }

}
