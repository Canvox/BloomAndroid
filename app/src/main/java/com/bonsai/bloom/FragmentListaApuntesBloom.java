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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;

import com.bonsai.bloom.adapters.ApuntesAdapter;
import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class FragmentListaApuntesBloom extends Fragment {

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Eventos/apuntes.php?opcion=consulta";
    //private String URLTIMER = "http://bonsai.com.ec/quizzes/TESIS/Estadisticas/timer.php?opcion=guardatimer&ventana=LeerApuntes";
	private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Eventos/apuntes.php?opcion=consulta";
	private String URLTIMER = "http://softwarefactoryuees.com.ec/BloomWEB/Estadisticas/timer.php?opcion=guardatimer&ventana=LeerApuntes";
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
    	v = inflater.inflate(R.layout.fragment_list, container, false);
        mCallback.showActionButton();
		mProgressView = v.findViewById(R.id.login_progress);
		mContentView = v.findViewById(R.id.scrollView);
		mContentView.setBackgroundColor(getResources().getColor(R.color.colorSecond));

        Calendar c = Calendar.getInstance();
        seconds = c.get(Calendar.SECOND);

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
			return v;
		}

		if(jsonTask != null) return v;
		jsonTask = new GetJSON(URLJSON + "&eventocon=" + getArguments().getString("EXTRA1"));
		jsonTask.execute();
		return v;
	}

    public void backPressed () {
        Fragment fragment = new FragmentSingleEventoBloom();
        fragment.setArguments(getArguments().getBundle("oldargs"));
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

	public class GetJSON extends AsyncTask<Void, Void, JSONArray> {

		private final String URL;
		private boolean success = false;

		GetJSON(String URL) {
			this.URL = URL;
			showProgress(true);
		}

		@Override
		protected JSONArray doInBackground(Void... params) {
			JSONArray jsonArr = new JSONArray();
			try {
				JSONObject jsonOb;
				JSONParser jParser = new JSONParser();
				Log.i("PRUEBA URL", URL);
				jsonOb = jParser.getJSONFromUrl(URL);
				Log.i("PRUEBA JSON", jsonOb.toString());

				if (jsonOb.getString("success").equals("true")) {
					jsonArr = jsonOb.getJSONArray("Estado");
                    success = true;
                }
			} catch (Exception e) {
				Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
			}
			return jsonArr;
		}

		@Override
		protected void onPostExecute(JSONArray response) {
			if (isAdded()) {
                super.onPostExecute(response);
                jsonTask = null;
                showProgress(false);

                if (!success) {
                    if (getArguments().getString("EXTRA2").equals("first")) {
                        Snackbar.make(mContentView, R.string.message_apuntes_empty, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        addNewApunte();
                    } else
                        getActivity().onBackPressed();
				} else {
                    try {
                        ApuntesAdapter adapter = new ApuntesAdapter(getActivity(), response);
                        ListView list = (ListView) v.findViewById(R.id.listview);
                        View header = getActivity().getLayoutInflater().inflate(R.layout.header_simple, null);
                        TextView textCabecera = (TextView) header.findViewById(R.id.textCabecera);
                        textCabecera.setText(R.string.texview_cabecera_apuntes);
                        list.addHeaderView(header);
                        list.setAdapter(adapter);
						list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
								try {
									String extra1 = ((TextView) v.findViewById(R.id.textDescripcion)).getText().toString();
									String extra2 = ((TextView) v.findViewById(R.id.textHidden)).getText().toString();
									Bundle bundl = new Bundle();
									bundl.putString("DESCRIPCION", extra1);
									bundl.putString("IDEVENTO", extra2);
									Fragment fragment = new FragmentDescripcionApuntesBloom();
									fragment.setArguments(bundl);
									FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
									fragTransaction.replace(R.id.frame_container, fragment);
									fragTransaction.commit();
								} catch (Exception e) {
									Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
								}
							}
						});

                    } catch (Exception e) {
                        Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                    }
                }
            }
		}

        @Override
        protected void onCancelled() {
            jsonTask = null;
        }
	}

	public void addNewApunte() {
		Fragment fragment = new FragmentCrearApunteBloom();
		fragment.setArguments(getArguments());
		FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
		fragTransaction.replace(R.id.frame_container, fragment);
		fragTransaction.commit();
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