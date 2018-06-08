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
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bonsai.bloom.adapters.HistorialQuizzesGeneralAdapter;
import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

public class FragmentHistorialQuizzesGeneralBloom extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/historialquizz.php?opcion=consultahistorial";
	private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/historialquizz.php?opcion=consultahistorial",idgrupo, bandera="0";
	private View v, mProgressView, mContentView, header;
	private GetJSON jsonTask;
	private SwipeRefreshLayout swipeContainer;

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
		mCallback.hideActionButton();
		mProgressView = v.findViewById(R.id.login_progress);
		mContentView = v.findViewById(R.id.scrollView);
        mProgressView.setBackgroundColor(getResources().getColor(R.color.colorSecond));

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
		idgrupo = settings.getString("idgrupo", "");

		if(jsonTask != null) return v;
		jsonTask = new GetJSON(URLJSON + "&idgrupoCon=" + idgrupo);
		jsonTask.execute();

		swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.srlContainer);
		swipeContainer.setOnRefreshListener(this);

		return v;
	}

	@Override
	public void onRefresh() {

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				bandera="1";
				jsonTask = new GetJSON(URLJSON + "&idgrupoCon=" + idgrupo);
				jsonTask.execute();
				// Update data in ListView
				Toast.makeText(getActivity(), "Actualizado", Toast.LENGTH_SHORT).show();
				// Remove widget from screen.
				swipeContainer.setRefreshing(false);
			}
		}, 100);

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

                if (success) {
                    try {
						HistorialQuizzesGeneralAdapter adapter = new HistorialQuizzesGeneralAdapter(getActivity(), response);
                        ListView list = (ListView) v.findViewById(R.id.listview);
						if(bandera.equals("0")){
						header = getActivity().getLayoutInflater().inflate(R.layout.header_simple,null);
                        TextView textCabecera = (TextView) header.findViewById(R.id.textCabecera);
                        textCabecera.setText(R.string.texview_cabecera_historial_quizzes);
						list.addHeaderView(header);}
						list.setAdapter(adapter);
						list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
								try {
                                    String extra1 = ((TextView) v.findViewById(R.id.textHidden)).getText().toString();
                                    Bundle bundl = new Bundle();
                                    bundl.putString("EXTRA1", extra1);
                                    Fragment fragment = new FragmentHistorialQuizzesBloom();
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
                } else {
                    Toast.makeText(getActivity(), R.string.error_descarga, Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
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