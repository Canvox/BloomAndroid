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

import com.bonsai.bloom.adapters.QuizzesAdapter;
import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

public class FragmentListaQuizzesBloom extends Fragment {

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Quizzes/resolverquizz.php?opcion=consultaquizz";
	private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Quizzes/resolverquizz.php?opcion=consultaquizz";
	private View v, mProgressView, mContentView;
	private GetJSON jsonTask;

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
        String idgrupo = settings.getString("idgrupo", "");

        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("timerquizz", String.valueOf(seconds));
        editor.commit();

        if(jsonTask != null) return v;
        jsonTask = new GetJSON(URLJSON + "&idgrupoCon=" + idgrupo);
        jsonTask.execute();
        return v;
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
				jsonOb = jParser.getJSONFromUrl(URL);

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
                        QuizzesAdapter adapter = new QuizzesAdapter(getActivity(), response);
                        ListView list = (ListView) v.findViewById(R.id.listview);
						list.setAdapter(adapter);

						list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
								try {
									SharedPreferences prefs = getActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = prefs.edit();
									editor.putString("resultado", "0");
									editor.putString("resultado_maximo", "0");
									editor.commit();

									String extra1 = ((TextView) v.findViewById(R.id.textHidden)).getText().toString();
									Bundle bundl = new Bundle();
									bundl.putString("EXTRA1", extra1);
									bundl.putString("EXTRA2", "");
									Fragment fragment = new FragmentResponderPreguntaBloom();
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