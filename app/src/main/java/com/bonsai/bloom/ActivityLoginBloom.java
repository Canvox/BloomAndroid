package com.bonsai.bloom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONObject;



public class ActivityLoginBloom extends Activity {

    private UserLoginTask mAuthTask = null;

    public String link;
    //private String URL = "http://bonsai.com.ec/quizzes/TESIS/Usuario/login.php";
    //private String URL = "http://192.168.10.230:8080/BloomWEB/TESIS/Usuario/login.php";
    //private String URL = "http://192.168.10.230:8083/Json/json.do";
    //private String URL = "http://softwarefactoryuees.com.ec/BloomWEB/login.php";
    private String URL = "https://3750f418.ngrok.io/192.168.11.213:8181/api/informacion/usuario/login";
    private EditText mCodigoView=null;
    private EditText mPasswordView=null;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //getActionBar().setTitle("");
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        mCodigoView = (EditText) findViewById(R.id.editCodigo);
        mPasswordView = (EditText) findViewById(R.id.editPassword);

        mLoginFormView = findViewById(R.id.scrollView);
        mProgressView = findViewById(R.id.login_progress);

        ImageView mLoginButton = (ImageView) findViewById(R.id.imageLogin);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        ImageView mRegisterButton = (ImageView) findViewById(R.id.imageRegistrar);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(ActivityLoginBloom.this, ActivityRegistroBloom.class);
                ActivityLoginBloom.this.startActivity(mainIntent);
            }
        });
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        ConnectionDetector cd = new ConnectionDetector(ActivityLoginBloom.this);
        if (!cd.isConnectingToInternet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLoginBloom.this);
            builder.setMessage(R.string.no_conexion)
                    .setCancelable(false)
                    .setPositiveButton(R.string.salir, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        // Reset errors.
        mCodigoView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String codigo = mCodigoView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(codigo)) {
            mCodigoView.setError(getString(R.string.error_field_required));
            focusView = mCodigoView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            link = URL + "?usuario=" + codigo + "&clave=" + password;
            Log.i("URL", link);
            mAuthTask = new UserLoginTask(link);
            mAuthTask.execute();
            //System.out.println("+++++++++++++++++++" + link);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String URL;
        private boolean success;

        UserLoginTask(String URL) {
            this.URL = URL;
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject jsonOb = new JSONObject();
            try {
                JSONParser jParser = new JSONParser();
                Log.i("PRUEBA URL", URL);
                jsonOb = jParser.getJSONFromUrl(URL);
                Log.i("PRUEBA JSON", jsonOb.toString());

                /*if (!jsonOb.getJSONArray("Estado").getJSONObject(0).getString("codigo").equals("vacio")){
                    success = true;
                } else{
                    success = false;
                }*/



                if (!jsonOb.getString("cod_usuario").equals("vacio")) {
                    success = true;
                } else {
                    success = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonOb.toString();
        }

        @Override
        protected void onPostExecute(final String response) {
            mAuthTask = null;
            showProgress(false);

            try {
                JSONObject jsonOb = new JSONObject(response);
                if (success) {
                    Toast.makeText(ActivityLoginBloom.this, R.string.message_login_success, Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("codigo", jsonOb.getString("cod_usuario"));
                    editor.putString("idusuario", jsonOb.getString("cod_identificacion"));
                    editor.putString("tipousuario", ("1"));
                    editor.putString("jsonCursos", "");
                    editor.commit();


                    SharedPreferences settings = getSharedPreferences("mysettings",
                            Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor2 = settings.edit();
                    editor2.putString("usuario", mCodigoView.getText().toString());
                    editor2.putString("codigo", jsonOb.getString("cod_usuario"));
                    editor2.putString("idusuario", jsonOb.getString("cod_identificacion"));
                    editor2.putString("tipousuario", "1");
                    //editor2.putString("nombres", jsonOb.getString("nombres"));
                    editor2.putString("nombres", "Ronny");
                    //editor2.putString("apellidos", jsonOb.getString("apellidos"));
                    editor2.putString("apellidos", "Naranjo");
                    editor2.putString("facultad", "Sistemas");
                    editor2.putString("carrera", "Desarrollo de Sistemas");


                    editor2.commit();


                    ActivityLoginBloom.this.finish();
                    Intent mainIntent = new Intent(ActivityLoginBloom.this, ActivityRegistroBloom.class);
                    mainIntent.putExtra("loginLink", link);
                    ActivityLoginBloom.this.startActivity(mainIntent);

                } else {
                    Toast.makeText(ActivityLoginBloom.this, R.string.message_login_failure, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    @Override
    public void onBackPressed() {
        ActivityLoginBloom.this.finish();
    }
}


/*

public class ActivityLoginBloom extends Activity {

    private UserLoginTask mAuthTask = null;
    //private String URL = "http://bonsai.com.ec/quizzes/TESIS/Usuario/login.php";
    //private String URL = "http://192.168.10.230:8080/BloomWEB/TESIS/Usuario/login.php";
    //private String URL = "http://192.168.10.230:8083/Json/json.do";
    private String URL = "http://softwarefactoryuees.com.ec/BloomWEB/login.php";
    private EditText mCodigoView=null;
    private EditText mPasswordView=null;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //getActionBar().setTitle("");
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        mCodigoView = (EditText) findViewById(R.id.editCodigo);
        mPasswordView = (EditText) findViewById(R.id.editPassword);

        mLoginFormView = findViewById(R.id.scrollView);
        mProgressView = findViewById(R.id.login_progress);

        ImageView mLoginButton = (ImageView) findViewById(R.id.imageLogin);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        ImageView mRegisterButton = (ImageView) findViewById(R.id.imageRegistrar);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(ActivityLoginBloom.this, ActivityRegistroBloom.class);
                ActivityLoginBloom.this.startActivity(mainIntent);
            }
        });
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        ConnectionDetector cd = new ConnectionDetector(ActivityLoginBloom.this);
        if (!cd.isConnectingToInternet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLoginBloom.this);
            builder.setMessage(R.string.no_conexion)
                    .setCancelable(false)
                    .setPositiveButton(R.string.salir, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        // Reset errors.
        mCodigoView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String codigo = mCodigoView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(codigo)) {
            mCodigoView.setError(getString(R.string.error_field_required));
            focusView = mCodigoView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            String link = URL + "?codigo=" + codigo + "&password=" + password;
            Log.i("URL", link);
            mAuthTask = new UserLoginTask(link);
            mAuthTask.execute();
        }
    }

    */
/**
     * Shows the progress UI and hides the login form.
     *//*

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String URL;
        private boolean success;

        UserLoginTask(String URL) {
            this.URL=URL;
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject jsonOb= new JSONObject();
            try {
                JSONParser jParser = new JSONParser();
                Log.i("PRUEBA URL", URL);
                jsonOb = jParser.getJSONFromUrl(URL);
                Log.i("PRUEBA JSON", jsonOb.toString());
                if(!jsonOb.getJSONArray("Estado").getJSONObject(0).getString("codigo").equals("vacio")) success = true;
                else success=false;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonOb.toString();
        }

        @Override
        protected void onPostExecute(final String response) {
            mAuthTask = null;
            showProgress(false);

            try {
                JSONObject jsonOb = new JSONObject(response);
                if (success) {
                    Toast.makeText(ActivityLoginBloom.this, R.string.message_login_success, Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("codigo", jsonOb.getJSONArray("Estado").getJSONObject(0).getString("codigo"));
                    editor.putString("idusuario", jsonOb.getJSONArray("Estado").getJSONObject(0).getString("idusuario"));
                    editor.putString("tipousuario", jsonOb.getJSONArray("Estado").getJSONObject(0).getString("tipousuario"));
                    editor.putString("jsonCursos", "");
                    editor.commit();

                    ActivityLoginBloom.this.finish();
                    Intent mainIntent = new Intent(ActivityLoginBloom.this, ActivityMainBloom.class);
                    ActivityLoginBloom.this.startActivity(mainIntent);

                } else {
                    Toast.makeText(ActivityLoginBloom.this, R.string.message_login_failure, Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    public void onBackPressed() {
        ActivityLoginBloom.this.finish();
    }
}
*/
