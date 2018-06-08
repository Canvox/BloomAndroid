package com.bonsai.bloom;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class ActivitySplash extends Activity
{
    // Set the display time, in milliseconds (or extract it out as a configurable parameter)
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }
 
    @Override
    protected void onResume()
    {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                //Finish the splash activity so it can't be returned to.
                ActivitySplash.this.finish();

                SharedPreferences settings = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                String idusuario = settings.getString("idusuario", "");

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("jsonCursos", "");
                editor.commit();

                if (idusuario.equals("")) {
                    Intent mainIntent = new Intent(ActivitySplash.this, ActivityLoginBloom.class);
                    ActivitySplash.this.startActivity(mainIntent);

                } else {
                    Intent mainIntent = new Intent(ActivitySplash.this, ActivityMainBloom.class);
                    ActivitySplash.this.startActivity(mainIntent);

                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}