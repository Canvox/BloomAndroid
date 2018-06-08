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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FragmentCrearQuizzBloom extends Fragment {

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Cursos/temas.php?opcion=consulta", idgrupo;
    private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Cursos/temas.php?opcion=consulta", idgrupo;
    //private String URLJSON2 = "http://bonsai.com.ec/quizzes/TESIS/Quizzes/quizzes.php?opcion=quizz";
    private String URLJSON2 = "http://softwarefactoryuees.com.ec/BloomWEB/Quizzes/quizzes.php?opcion=quizz";
	private View v, mProgressView, mContentView;
	private GetJSON jsonTask;
    private GetJSONSaveQuizz jsonTaskSaveAnswer;
    private JSONArray temas;


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
    	v = inflater.inflate(R.layout.fragment_crear_quizz, container, false);
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
        idgrupo = settings.getString("idgrupo", "");

		if(jsonTask != null) return v;
		jsonTask = new GetJSON(URLJSON + "&idgrupo=" + idgrupo);
		jsonTask.execute();
		return v;
	}

    private boolean checkIfEmpty (EditText edit) {
        return (edit.getText().toString().equals(""));
    }

    private String getTema(Spinner spin) {
        String response = "";
        try {
            for (int x = 0; x < temas.length(); x++) {
                if (temas.getJSONObject(x).getString("descripcion").equals(spin.getSelectedItem().toString()))
                    response = temas.getJSONObject(x).getString("idtema");
            }
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), getString(R.string.error_tag), e);
        }
        return response;
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
                    temas = response;
                    try {
                        List<String> spinnerArray1 =  new ArrayList<String>();
                        for (int x = 0; x < temas.length(); x++) spinnerArray1.add(temas.getJSONObject(x).getString("descripcion"));
                        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinnerArray1);
                        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        Spinner spinTemas = (Spinner) v.findViewById(R.id.spinTemas);
                        spinTemas.setAdapter(adapter1);
                        spinTemas.setSelection(0);

                        ImageView mAgregarButton = (ImageView) v.findViewById(R.id.imageAgregar);
                        mAgregarButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    EditText editTitulo = (EditText) v.findViewById(R.id.editTitulo);
                                    if(checkIfEmpty(editTitulo)) {
                                        Toast.makeText(getActivity(), R.string.message_title_quizz_required, Toast.LENGTH_LONG).show();
                                        editTitulo.requestFocus();
                                    } else {
                                        Spinner spinTemas = (Spinner) v.findViewById(R.id.spinTemas);
                                        jsonTaskSaveAnswer = new GetJSONSaveQuizz(URLJSON2 + "&idgrupo=" + idgrupo + "&idtema=" + getTema(spinTemas) + "&desc=" + URLEncoder.encode(editTitulo.getText().toString()));
                                        jsonTaskSaveAnswer.execute();
                                    }
                                } catch (Exception e) {
                                    Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.error_temas, Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            }
		}

        @Override
        protected void onCancelled() {
            jsonTask = null;
        }
	}

    public class GetJSONSaveQuizz extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private boolean success = false;

        GetJSONSaveQuizz(String URL) {
            this.URL = URL;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonResponse = new JSONObject();
            try {
                JSONParser jParser = new JSONParser();
                Log.i("PRUEBA URL", URL);
                jsonResponse = jParser.getJSONFromUrl(URL);
                Log.i("PRUEBA JSON", jsonResponse.toString());
                if (jsonResponse.getString("success").equals("true"))
                    success = true;
            } catch (Exception e) {
                Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            if (isAdded()) {
                super.onPostExecute(response);
                jsonTask = null;
                showProgress(false);

                if (success) {
                    try {
                        Bundle bundl = new Bundle();
                        bundl.putString("EXTRA1", response.getString("Estado"));
                        Fragment fragment = new FragmentCrearPreguntaBloom();
                        fragment.setArguments(bundl);
                        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                        fragTransaction.replace(R.id.frame_container, fragment);
                        fragTransaction.commit();

                    } catch (Exception e) {
                        Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.error_subida, Toast.LENGTH_LONG).show();
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