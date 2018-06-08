package com.bonsai.bloom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bonsai.bloom.adapters.CursosAdapter;
import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FragmentListaCursosBloom extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

	//private String URLJSON = "http://192.168.10.230:8080/BloomWeb/TESIS/Cursos/listagrupos.php", jsonCursos, jsonTipoUsuario, jsonUsuario;
	private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Cursos/listagrupos.php", jsonCursos, jsonTipoUsuario, jsonUsuario;
	private View v, mProgressView, mContentView;
	private GetJSON jsonTask=null;

	private SwipeRefreshLayout swipeContainer;
	private String link;

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
    	v = inflater.inflate(R.layout.fragment_list_refresh, container, false);
		mCallback.showActionButton();
		mProgressView = v.findViewById(R.id.login_progress);
		mContentView = v.findViewById(R.id.scrollView);

		SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
		jsonCursos = settings.getString("jsonCursos", "");
		jsonTipoUsuario = settings.getString("tipousuario", "");
		jsonUsuario = settings.getString("idusuario", "");

		//Toast.makeText(getActivity(), jsonCursos, Toast.LENGTH_LONG).show();

		if(jsonTipoUsuario.equals("2"))
			mCallback.hideActionButton();

		ConnectionDetector cd = new ConnectionDetector(getActivity());
		if(!cd.isConnectingToInternet() && jsonCursos.equals("")) {
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
		link = URLJSON + "?usuario="  + settings.getString("idusuario", "") + "&tipousuario=" + settings.getString("tipousuario", "");
		jsonTask = new GetJSON(link);
		jsonTask.execute();

		swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.srlContainer);
		swipeContainer.setOnRefreshListener(this);


        Toast.makeText(getActivity(), jsonCursos, Toast.LENGTH_LONG).show();


		return v;


	}

	@Override
	public void onRefresh() {

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				jsonCursos="";
				link = URLJSON + "?usuario="  + jsonUsuario + "&tipousuario=" + jsonTipoUsuario;
				jsonTask = new GetJSON(link);
				jsonTask.execute();
				// Update data in ListView
				Toast.makeText(getActivity(), "Actualizado", Toast.LENGTH_SHORT).show();
				// Remove widget from screen.
				swipeContainer.setRefreshing(false);
			}
		}, 100);

	}



    public void backPressed () {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.salir_aplicacion)
                .setCancelable(false)
                .setPositiveButton(R.string.afirmacion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .setNegativeButton(R.string.negacion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //NA
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void unirseCurso () {

			Fragment fragment = new FragmentUnirseCursoBloom();
			FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
			fragTransaction.replace(R.id.frame_container, fragment);
			fragTransaction.commit();

    }


	public class GetJSON extends AsyncTask<Void, Void, JSONArray>  {

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
				if (jsonCursos.equals("")) {
					JSONParser jParser = new JSONParser();
					Log.i("PRUEBA URL", URL);
					jsonOb = jParser.getJSONFromUrl(URL);
					Log.i("PRUEBA JSON", jsonOb.toString());
					jsonCursos = jsonOb.toString();
				} else jsonOb = new JSONObject(jsonCursos);

				if (jsonOb.getJSONArray("Grupos").length() > 0) {
					jsonArr = jsonOb.getJSONArray("Grupos");
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

                if (success) {
                    try {

						CursosAdapter adapter = new CursosAdapter(getActivity(), response);
                        ListView list = (ListView) v.findViewById(R.id.listview);
						list.setAdapter(adapter);

						//Toast.makeText(getActivity(), list.toString(), Toast.LENGTH_LONG).show();

						list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
								try {
									String extra1 = ((TextView) v.findViewById(R.id.textHidden)).getText().toString();
									String extra2 = ((TextView) v.findViewById(R.id.textHidden2)).getText().toString();
									SharedPreferences prefs = getActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = prefs.edit();
									editor.putString("idgrupo", extra1);
									editor.putString("idmoderador", extra2);
									editor.commit();

									Fragment fragment = new FragmentHomeBloom();
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
                } else {
                    jsonCursos = "";
                    Snackbar.make(mContentView, R.string.message_cursos_empty, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    //unirseCurso(); ojo 29/08/2017
                }
            }
		}

        @Override
        protected void onCancelled() {
            jsonTask = null;
        }

	}




	@Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v("Save instance", "Destroyed");
        SharedPreferences prefs = getActivity().getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("jsonCursos", (jsonCursos == null) ? "" : jsonCursos);
		editor.commit();
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