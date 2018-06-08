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
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bonsai.bloom.adapters.CursosAdapter;
import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

public class FragmentUnirseCursoBloom extends Fragment {

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Cursos/activagrupos.php?opcion=grupos", jsonCursos;
    //private String URLJSON2 = "http://bonsai.com.ec/quizzes/TESIS/Cursos/activagrupos.php?opcion=unecurso";
    private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Cursos/activagrupos.php?opcion=grupos", jsonCursos;
    private String URLJSON2 ="http://softwarefactoryuees.com.ec/BloomWEB/Cursos/activagrupos.php?opcion=unecurso";

	private View v, mProgressView, mContentView;
	private GetJSON jsonTask;
    private unirseJSON unirseTask;

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
		mCallback.hideActionButton();
		mProgressView = v.findViewById(R.id.login_progress);
		mContentView = v.findViewById(R.id.scrollView);

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

		SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        jsonCursos = settings.getString("jsonCursos", "");
        Log.i("TEST",jsonCursos);
		if(jsonTask != null) return v;
		jsonTask = new GetJSON(URLJSON + "&usuario="  + settings.getString("idusuario", "") + "&tipousuario=" + settings.getString("tipousuario", ""));
		jsonTask.execute();
		return v;
	}

	public void backPressed () {
        if (jsonCursos.equals("")) {
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
        } else {
            Fragment fragment = new FragmentListaCursosBloom();
            FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
            fragTransaction.replace(R.id.frame_container, fragment);
            fragTransaction.commit();
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
			JSONArray jsonArr = new JSONArray();
			try {
				JSONObject jsonOb;
				JSONParser jParser = new JSONParser();
				Log.i("PRUEBA URL", URL);
				jsonOb = jParser.getJSONFromUrl(URL);
				Log.i("PRUEBA JSON", jsonOb.toString());

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
                        View header = getActivity().getLayoutInflater().inflate(R.layout.header_simple, null);
                        TextView textCabecera = (TextView) header.findViewById(R.id.textCabecera);
                        textCabecera.setText(R.string.texview_cabecera_unirse);
                        list.addHeaderView(header);
						list.setAdapter(adapter);

						list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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

                                    String idgrupo = ((TextView) v.findViewById(R.id.textHidden)).getText().toString();
                                    unirseCurso(idgrupo);
                                } catch (Exception e) {
                                    Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                    }
                } else {
                    Snackbar.make(mContentView, R.string.message_unirse_cursos_empty, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    //getActivity().onBackPressed(); ojo 29/08/2017
                    Fragment fragment = new FragmentListaCursosBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            }
		}

        @Override
        protected void onCancelled() {
            jsonTask = null;
        }
	}

    private void unirseCurso(final String idgrupo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.prompt_nuevo_curso)
                .setCancelable(false)
                .setPositiveButton(R.string.afirmacion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
                        if(unirseTask != null) return;
                        unirseTask = new unirseJSON(URLJSON2 + "&usuario="  + settings.getString("idusuario", "") + "&grupo=", idgrupo);
                        unirseTask.execute();
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

    public class unirseJSON extends AsyncTask<Void, Void, JSONArray> {

        private final String URL;
        private final String idgrupo;
        private boolean success = false;

        unirseJSON(String URL, String idgrupo) {
            this.URL = URL;
            this.idgrupo = idgrupo;
            showProgress(true);
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            JSONArray jsonArr = new JSONArray();
            try {
                JSONObject jsonOb;
                JSONParser jParser = new JSONParser();
                Log.i("PRUEBA URL", URL + idgrupo);
                jsonOb = jParser.getJSONFromUrl(URL + idgrupo);
                Log.i("PRUEBA JSON", jsonOb.toString());

                if (jsonOb.getString("success").equals("true"))
                    success = true;
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
                    SharedPreferences prefs = getActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("idgrupo", idgrupo);
                    editor.putString("jsonCursos", "");
                    editor.commit();

                    Fragment fragment = new FragmentHomeBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                } else {
                    Toast.makeText(getActivity(), R.string.error_subida, Toast.LENGTH_LONG).show();
                }
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