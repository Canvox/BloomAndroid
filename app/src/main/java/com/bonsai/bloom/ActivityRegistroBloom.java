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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;



public class ActivityRegistroBloom extends Activity {

    private UserRegisterTask mAuthTask = null;
    private GetJSON jsonTask = null;
    //private String URL = "http://bonsai.com.ec/quizzes/TESIS/Usuario/registro.php";
    //private String URL2 = "http://bonsai.com.ec/quizzes/TESIS/Usuario/llenafacultad.php";
    private String URL = "http://softwarefactoryuees.com.ec/BloomWEB/Usuario/registro.php";
    private String URL2 = "http://softwarefactoryuees.com.ec/BloomWEB/Usuario/llenafacultad.php";
    private EditText mNombresView;
    private EditText mApellidosView;
    private EditText mCodigoView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Spinner spinFaculty, spinCareer;
    private JSONArray facultys;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);
        //getActionBar().setTitle("");
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the register form.
        mNombresView = (EditText) findViewById(R.id.editNombre);
        mApellidosView = (EditText) findViewById(R.id.editApellido);
        mCodigoView = (EditText) findViewById(R.id.editCodigo);
        mPasswordView = (EditText) findViewById(R.id.editPassword);

        mLoginFormView = findViewById(R.id.scrollView);
        mProgressView = findViewById(R.id.login_progress);

        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        if(!cd.isConnectingToInternet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
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

        if(jsonTask != null) return;
        jsonTask = new GetJSON(URL2);
        jsonTask.execute();
    }

    public void attemptRegistro() {
        if (mAuthTask != null) {
            return;
        }

        ConnectionDetector cd = new ConnectionDetector(ActivityRegistroBloom.this);
        if (!cd.isConnectingToInternet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRegistroBloom.this);
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
        mNombresView.setError(null);
        mApellidosView.setError(null);
        mCodigoView.setError(null);
        mPasswordView.setError(null);

        SharedPreferences settings = getSharedPreferences("mysettings",
                Context.MODE_PRIVATE);


        // Store values at the time of the login attempt.


       // PRUEBA DE SHARED PREFERENCES
        mNombresView.setText(settings.getString("nombres", "defaultvalue"));
        mApellidosView.setText(settings.getString("apellidos", "defaultvalue"));
        mCodigoView.setText(settings.getString("idusuario", "defaultvalue"));
        mPasswordView.setText(settings.getString("codigo", "defaultvalue"));



        //String nombre = mNombresView.getText().toString();
        String nombre = settings.getString("nombres", "defaultvalue");
        String apellido = settings.getString("apellidos", "defaultvalue");
        String codigo = settings.getString("idusuario", "defaultvalue");
        String password = settings.getString("codigo", "defaultvalue");

        /*String nombre = "";
        String apellido = "";
        String codigo = "";
        String password = "";*/

        boolean cancel = false;
        View focusView = null;



        if (TextUtils.isEmpty(nombre)) {
            mNombresView.setError(getString(R.string.error_field_required));
            focusView = mNombresView;
            cancel = true;
        }

        if (TextUtils.isEmpty(apellido)) {
            mApellidosView.setError(getString(R.string.error_field_required));
            focusView = mApellidosView;
            cancel = true;
        }

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
            String facultad = settings.getString("facultad", "defaultvalue");
            String carrera = settings.getString("carrera", "defaultvalue");
            String tipo = "1";
            /*showProgress(true);
            String facultad = getFaculty();
            String carrera = getCareer(facultad), tipo = "1";*/
            String link = URL + "?nombres=" + URLEncoder.encode(nombre) + "&apellidos=" + URLEncoder.encode(apellido) + "&facultad=" + facultad + "&carrera=" + carrera + "&tipo_usuario=" + tipo + "&codigo=" + codigo + "&password=" + password;
            Log.i("URL", link);
            mAuthTask = new UserRegisterTask(link);
            mAuthTask.execute();
        }
    }

    /*private String getFaculty() {
        String response = "";
        try {
            for (int x = 0; x < facultys.length(); x++) {
                if (facultys.getJSONObject(x).getString("descripcion").equals(spinFaculty.getSelectedItem().toString()))
                    response = facultys.getJSONObject(x).getString("id");
            }
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), getString(R.string.error_tag), e);
        }
        return response;
    }*/

    /*private String getCareer(String fac) {
        String response = "";
        int indice = 0;
        try {
            for (int x = 0; x < facultys.length(); x++) {
                if (facultys.getJSONObject(x).getString("id").equals(fac))
                    indice = x;
            }
            for (int x = 0; x < facultys.getJSONObject(indice).getJSONArray("carreras").length(); x++) {
                if (facultys.getJSONObject(indice).getJSONArray("carreras").getJSONObject(x).getString("descripcion").equals(spinCareer.getSelectedItem().toString()))
                    response = facultys.getJSONObject(indice).getJSONArray("carreras").getJSONObject(x).getString("id");
            }
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), getString(R.string.error_tag), e);
        }
        return response;
    }*/

    /**
     * Shows the progress UI and hides the login form.
     */
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

    public class UserRegisterTask extends AsyncTask<Void, Void, String> {

        private final String URL;
        private boolean success;

        UserRegisterTask(String URL) {
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
                if(jsonOb.getJSONArray("Estado").getJSONObject(0).getString("Est").equals("S")) success = true;
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
                    Toast.makeText(ActivityRegistroBloom.this, R.string.message_registro_success, Toast.LENGTH_SHORT).show();
                    onBackPressed();

                } else {
                    Toast.makeText(ActivityRegistroBloom.this, R.string.message_registro_failure, Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e){
                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class GetJSON extends AsyncTask<Void, Void, JSONArray> {

        private final String URL;
        private boolean success = false;

        GetJSON(String URL) {
            this.URL = URL;
            showProgress(true);
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            JSONArray jsonResponse = new JSONArray();
            try {
                JSONObject jsonOb;
                JSONParser jParser = new JSONParser();
                Log.i("PRUEBA URL", URL);
                jsonOb = jParser.getJSONFromUrl(URL);
                Log.i("PRUEBA JSON", jsonOb.toString());

                if (jsonOb.getString("success").equals("true")) {
                    jsonResponse = jsonOb.getJSONArray("Estado");
                    success = true;
                }
            } catch (Exception e) {
                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONArray response) {
            super.onPostExecute(response);
            jsonTask = null;
            showProgress(false);

            if (success) {
                facultys = response;
                try {
                    List<String> spinnerArray1 =  new ArrayList<String>();
                    for (int x = 0; x < facultys.length(); x++) spinnerArray1.add(facultys.getJSONObject(x).getString("descripcion"));
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray1);
                    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinFaculty = (Spinner) findViewById(R.id.spinFaculty);
                    spinFaculty.setAdapter(adapter1);
                    spinFaculty.setSelection(0);

                    List<String> spinnerArray2 =  new ArrayList<String>();
                    for (int x = 0; x < facultys.getJSONObject(0).getJSONArray("carreras").length(); x++) spinnerArray2.add(facultys.getJSONObject(0).getJSONArray("carreras").getJSONObject(x).getString("descripcion"));
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray2);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinCareer = (Spinner) findViewById(R.id.spinCareer);
                    spinCareer.setAdapter(adapter2);
                    spinCareer.setSelection(0);

                    spinFaculty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            try {
                                List<String> spinnerArray2 = new ArrayList<String>();
                                for (int x = 0; x < facultys.getJSONObject(position).getJSONArray("carreras").length(); x++)
                                    spinnerArray2.add(facultys.getJSONObject(position).getJSONArray("carreras").getJSONObject(x).getString("descripcion"));
                                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray2);
                                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinCareer.setAdapter(adapter2);
                            } catch (Exception e) {
                                Log.e(getApplicationContext().getResources().getString(R.string.app_name), getApplicationContext().getResources().getString(R.string.error_tag), e);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }

                    });

                    ImageView mRegisterButton = (ImageView) findViewById(R.id.imageRegistrar);
                    mRegisterButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            attemptRegistro();
                        }
                    });

                } catch (Exception e) {
                    Log.e(getApplicationContext().getResources().getString(R.string.app_name), getApplicationContext().getResources().getString(R.string.error_tag), e);
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_descarga, Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        }

        @Override
        protected void onCancelled() {
            jsonTask = null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ActivityLoginBloom.class);
        startActivity(intent);
        finish();
    }
}


/*public class ActivityRegistroBloom extends Activity {

    private UserRegisterTask mAuthTask = null;
    private GetJSON jsonTask = null;
    //private String URL = "http://bonsai.com.ec/quizzes/TESIS/Usuario/registro.php";
    //private String URL2 = "http://bonsai.com.ec/quizzes/TESIS/Usuario/llenafacultad.php";
    private String URL = "http://softwarefactoryuees.com.ec/BloomWEB/Usuario/registro.php";
    private String URL2 = "http://softwarefactoryuees.com.ec/BloomWEB/Usuario/llenafacultad.php";
    private EditText mNombresView;
    private EditText mApellidosView;
    private EditText mCodigoView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Spinner spinFaculty, spinCareer;
    private JSONArray facultys;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);
        //getActionBar().setTitle("");
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the register form.
        mNombresView = (EditText) findViewById(R.id.editNombre);
        mApellidosView = (EditText) findViewById(R.id.editApellido);
        mCodigoView = (EditText) findViewById(R.id.editCodigo);
        mPasswordView = (EditText) findViewById(R.id.editPassword);

        mLoginFormView = findViewById(R.id.scrollView);
        mProgressView = findViewById(R.id.login_progress);

        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        if(!cd.isConnectingToInternet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
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

        if(jsonTask != null) return;
        jsonTask = new GetJSON(URL2);
        jsonTask.execute();
    }

    public void attemptRegistro() {
        if (mAuthTask != null) {
            return;
        }

        ConnectionDetector cd = new ConnectionDetector(ActivityRegistroBloom.this);
        if (!cd.isConnectingToInternet()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRegistroBloom.this);
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
        mNombresView.setError(null);
        mApellidosView.setError(null);
        mCodigoView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String nombre = mNombresView.getText().toString();
        String apellido = mApellidosView.getText().toString();
        String codigo = mCodigoView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(nombre)) {
            mNombresView.setError(getString(R.string.error_field_required));
            focusView = mNombresView;
            cancel = true;
        }

        if (TextUtils.isEmpty(apellido)) {
            mApellidosView.setError(getString(R.string.error_field_required));
            focusView = mApellidosView;
            cancel = true;
        }

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
            String facultad = getFaculty();
            String carrera = getCareer(facultad), tipo = "1";
            String link = URL + "?nombres=" + URLEncoder.encode(nombre) + "&apellidos=" + URLEncoder.encode(apellido) + "&facultad=" + facultad + "&carrera=" + carrera + "&tipo_usuario=" + tipo + "&codigo=" + codigo + "&password=" + password;
            Log.i("URL", link);
            mAuthTask = new UserRegisterTask(link);
            mAuthTask.execute();
        }
    }

    private String getFaculty() {
        String response = "";
        try {
            for (int x = 0; x < facultys.length(); x++) {
                if (facultys.getJSONObject(x).getString("descripcion").equals(spinFaculty.getSelectedItem().toString()))
                    response = facultys.getJSONObject(x).getString("id");
            }
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), getString(R.string.error_tag), e);
        }
        return response;
    }

    private String getCareer(String fac) {
        String response = "";
        int indice = 0;
        try {
            for (int x = 0; x < facultys.length(); x++) {
                if (facultys.getJSONObject(x).getString("id").equals(fac))
                    indice = x;
            }
            for (int x = 0; x < facultys.getJSONObject(indice).getJSONArray("carreras").length(); x++) {
                if (facultys.getJSONObject(indice).getJSONArray("carreras").getJSONObject(x).getString("descripcion").equals(spinCareer.getSelectedItem().toString()))
                    response = facultys.getJSONObject(indice).getJSONArray("carreras").getJSONObject(x).getString("id");
            }
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), getString(R.string.error_tag), e);
        }
        return response;
    }

    *//**
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

    public class UserRegisterTask extends AsyncTask<Void, Void, String> {

        private final String URL;
        private boolean success;

        UserRegisterTask(String URL) {
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
                if(jsonOb.getJSONArray("Estado").getJSONObject(0).getString("Est").equals("S")) success = true;
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
                    Toast.makeText(ActivityRegistroBloom.this, R.string.message_registro_success, Toast.LENGTH_SHORT).show();
                    onBackPressed();

                } else {
                    Toast.makeText(ActivityRegistroBloom.this, R.string.message_registro_failure, Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e){
                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class GetJSON extends AsyncTask<Void, Void, JSONArray> {

        private final String URL;
        private boolean success = false;

        GetJSON(String URL) {
            this.URL = URL;
            showProgress(true);
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            JSONArray jsonResponse = new JSONArray();
            try {
                JSONObject jsonOb;
                JSONParser jParser = new JSONParser();
                Log.i("PRUEBA URL", URL);
                jsonOb = jParser.getJSONFromUrl(URL);
                Log.i("PRUEBA JSON", jsonOb.toString());

                if (jsonOb.getString("success").equals("true")) {
                    jsonResponse = jsonOb.getJSONArray("Estado");
                    success = true;
                }
            } catch (Exception e) {
                Log.e(getResources().getString(R.string.app_name), getResources().getString(R.string.error_tag), e);
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONArray response) {
            super.onPostExecute(response);
            jsonTask = null;
            showProgress(false);

            if (success) {
                facultys = response;
                try {
                    List<String> spinnerArray1 =  new ArrayList<String>();
                    for (int x = 0; x < facultys.length(); x++) spinnerArray1.add(facultys.getJSONObject(x).getString("descripcion"));
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray1);
                    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinFaculty = (Spinner) findViewById(R.id.spinFaculty);
                    spinFaculty.setAdapter(adapter1);
                    spinFaculty.setSelection(0);

                    List<String> spinnerArray2 =  new ArrayList<String>();
                    for (int x = 0; x < facultys.getJSONObject(0).getJSONArray("carreras").length(); x++) spinnerArray2.add(facultys.getJSONObject(0).getJSONArray("carreras").getJSONObject(x).getString("descripcion"));
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray2);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinCareer = (Spinner) findViewById(R.id.spinCareer);
                    spinCareer.setAdapter(adapter2);
                    spinCareer.setSelection(0);

                    spinFaculty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            try {
                                List<String> spinnerArray2 = new ArrayList<String>();
                                for (int x = 0; x < facultys.getJSONObject(position).getJSONArray("carreras").length(); x++)
                                    spinnerArray2.add(facultys.getJSONObject(position).getJSONArray("carreras").getJSONObject(x).getString("descripcion"));
                                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray2);
                                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinCareer.setAdapter(adapter2);
                            } catch (Exception e) {
                                Log.e(getApplicationContext().getResources().getString(R.string.app_name), getApplicationContext().getResources().getString(R.string.error_tag), e);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }

                    });

                    ImageView mRegisterButton = (ImageView) findViewById(R.id.imageRegistrar);
                    mRegisterButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            attemptRegistro();
                        }
                    });

                } catch (Exception e) {
                    Log.e(getApplicationContext().getResources().getString(R.string.app_name), getApplicationContext().getResources().getString(R.string.error_tag), e);
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_descarga, Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        }

        @Override
        protected void onCancelled() {
            jsonTask = null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ActivityLoginBloom.class);
        startActivity(intent);
        finish();
    }
}*/
