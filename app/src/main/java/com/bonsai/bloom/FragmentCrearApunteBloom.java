package com.bonsai.bloom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;

public class FragmentCrearApunteBloom extends Fragment {

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Eventos/apuntes.php";
    //private String URLTIMER = "http://bonsai.com.ec/quizzes/TESIS/Estadisticas/timer.php?opcion=guardatimer&ventana=IngresarApuntes";
    private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Eventos/apuntes.php";
    private String URLTIMER = "http://softwarefactoryuees.com.ec/BloomWEB/Estadisticas/timer.php?opcion=guardatimer&ventana=IngresarApuntes";
    private View v, mProgressView, mContentView;
	private GetJSON jsonTask;
    private int seconds;

	MensajesListener mCallback;
    public interface MensajesListener {
        public void showActionButton();
        public void hideActionButton();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (MensajesListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Listener");
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	v = inflater.inflate(R.layout.fragment_crear_apunte, container, false);
        mCallback.hideActionButton();
		mProgressView = v.findViewById(R.id.login_progress);
		mContentView = v.findViewById(R.id.scrollView);
        mContentView.setBackgroundColor(getResources().getColor(R.color.colorSecond));

        Calendar c = Calendar.getInstance();
        seconds = c.get(Calendar.SECOND);

        ImageView mAgregarButton = (ImageView) v.findViewById(R.id.imageAgregar);
        mAgregarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ConnectionDetector cd = new ConnectionDetector(getActivity());
                    if(!cd.isConnectingToInternet()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

                    EditText editTitulo = (EditText) v.findViewById(R.id.editTitulo);
                    EditText editDescripcion = (EditText) v.findViewById(R.id.editDescripcion);
                    if (checkIfEmpty(editTitulo)) {
                        Toast.makeText(getActivity(), R.string.error_field_required, Toast.LENGTH_LONG).show();
                        editTitulo.requestFocus();
                    } else if(checkIfEmpty(editDescripcion)) {
                        Toast.makeText(getActivity(), R.string.error_field_required, Toast.LENGTH_LONG).show();
                        editDescripcion.requestFocus();
                    } else {
                        if (jsonTask != null) return;
                        jsonTask = new GetJSON(URLJSON, editTitulo.getText().toString(), editDescripcion.getText().toString());
                        jsonTask.execute();
                    }
                } catch (Exception e) {
                    Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                }
            }
        });
		return v;
	}

    public void backPressed () {
        Fragment fragment = new FragmentListaApuntesBloom();
        Bundle bundl = getArguments();
        bundl.putString("EXTRA2","second");
        fragment.setArguments(bundl);
        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.frame_container, fragment);
        fragTransaction.commit();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        Calendar c = Calendar.getInstance();
        String time = String.valueOf(round((c.get(Calendar.SECOND) - seconds) / (double) 60, 2));

        SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        String idgrupo = settings.getString("idgrupo", "");
        String idusuario = settings.getString("idusuario", "");

        URLTIMER += "&idgrupog=" + idgrupo + "&idusuario=" + idusuario + "&tiempo_uso=" + time;
        Log.i("PRUEBA URL", URLTIMER);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    java.net.URL link = new URL(URLTIMER);
                    HttpURLConnection conn = (HttpURLConnection) link.openConnection();
                    conn.setRequestMethod("GET");
                    // read the response
                    Log.i("CODE", "Response Code" + conn.getResponseCode());
                } catch (Exception e) {
                    Log.e(getString(R.string.app_name), "ERROR", e);
                }
            }
        };
        new Thread(runnable).start();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private boolean checkIfEmpty (EditText edit) {
        return (edit.getText().toString().equals(""));
    }

	public class GetJSON extends AsyncTask<Void, Void, JSONArray> {

		private final String URL;
		private boolean success = false;
        private final String titulo;
        private final String descripcion;

		GetJSON(String URL, String titulo, String descripcion) {
			this.URL = URL;
            this.titulo = titulo;
            this.descripcion = descripcion;
			showProgress(true);
		}

		@Override
		protected JSONArray doInBackground(Void... params) {
            JSONArray jsonResponse = new JSONArray();
			try {
                SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);

                HashMap<String, String> meMap = new HashMap<String, String>();
                meMap.put("evento", getArguments().getString("EXTRA1"));
                meMap.put("idusuario", settings.getString("idusuario", "") );
                meMap.put("opcion", "inserta");
                meMap.put("titulo", titulo);
                meMap.put("descripcion", descripcion);

				JSONObject jsonOb;
				JSONParser jParser = new JSONParser();
                Log.i("PRUEBA URL", URL + " " + getArguments().getString("EXTRA1"));
				jsonOb = jParser.getJSONPOSTFromUrl(URL, meMap);
                Log.i("PRUEBA JSON", jsonOb.toString());

				if (jsonOb.getString("success").equals("true")) success = true;
			} catch (Exception e) {
				Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
			}
			return jsonResponse;
		}

		@Override
		protected void onPostExecute(JSONArray response) {
			if (isAdded()) {
                super.onPostExecute(response);
                jsonTask = null;
                showProgress(false);

                if (success) {
                    Snackbar.make(mContentView, R.string.message_crear_apunte_success, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    getActivity().onBackPressed();

                } else
                    Toast.makeText(getActivity(), R.string.error_subida, Toast.LENGTH_LONG).show();
            }
		}

        @Override
        protected void onCancelled() {
            jsonTask = null;
        }
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
			mContentView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
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
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}