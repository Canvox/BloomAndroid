package com.bonsai.bloom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bonsai.bloom.adapters.TemasAdapter;
import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class FragmentListaTemasBloom extends Fragment {

	private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Cursos/temas.php?opcion=consulta", idgrupo;
    //private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Cursos/temas.php?opcion=consulta", idgrupo;
    private String URLJSON2 = "http://softwarefactoryuees.com.ec/BloomWEB/Cursos/temas.php?opcion=borrar";
	private String URLJSON3 = "http://softwarefactoryuees.com.ec/BloomWEB/Cursos/temas.php?opcion=inserta";

	private View v, mProgressView, mContentView, header;
	private GetJSON jsonTask;
    private BorrarTemaJSON borrarTask;
    private CrearTemaJSON crearTask;
    private boolean added = false;

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

		loadData();
		return v;
	}

    private void loadData() {
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

        SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        idgrupo = settings.getString("idgrupo", "");

        if(jsonTask != null) return;
        jsonTask = new GetJSON(URLJSON + "&idgrupo=" + idgrupo);
        jsonTask.execute();
    }


	public class GetJSON extends AsyncTask<Void, Void, JSONArray> {

		private final String URL;
		private boolean success = true;

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

				if (jsonOb.getJSONArray("Estado").length() > 0) {
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
						TemasAdapter adapter = new TemasAdapter(getActivity(), response);
						ListView list = (ListView) v.findViewById(R.id.listview);
                        if(!added) {
                            header = getActivity().getLayoutInflater().inflate(R.layout.header_tema, null);
                            list.addHeaderView(header);
                        }
						list.setAdapter(adapter);
                        added = true;

						list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, final View vclick, int position, long id) {
                                ConnectionDetector cd = new ConnectionDetector(getActivity());
                                if (!cd.isConnectingToInternet()) {
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

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage(R.string.message_borrar_tema)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.afirmacion, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                String id_tema = ((TextView) vclick.findViewById(R.id.textHidden)).getText().toString();
                                                if(borrarTask != null) return;
                                                borrarTask = new BorrarTemaJSON(URLJSON2 + "&idtema=" + id_tema);
                                                borrarTask.execute();
                                            }
                                        })
                                        .setNegativeButton(R.string.negacion, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) { }
                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });

                        ImageView mAgregarButton = (ImageView) header.findViewById(R.id.imageAgregar);
                        mAgregarButton.setOnTouchListener(sombra);
                        mAgregarButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ConnectionDetector cd = new ConnectionDetector(getActivity());
                                if (!cd.isConnectingToInternet()) {
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
                                EditText editTema = (EditText) header.findViewById(R.id.editTema);
                                if (TextUtils.isEmpty(editTema.getText().toString())) {
                                    editTema.setError(getString(R.string.error_field_required));
                                    editTema.requestFocus();
                                } else {
                                    if(crearTask != null) return;
                                    crearTask = new CrearTemaJSON(URLJSON3  + "&idgrupo=" + idgrupo + "&tema=" + URLEncoder.encode(editTema.getText().toString()));
                                    crearTask.execute();
                                    editTema.setText("");
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

    public class BorrarTemaJSON extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private boolean success = false;

        BorrarTemaJSON(String URL) {
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
                    Toast.makeText(getActivity(), R.string.message_borrar_tema_success, Toast.LENGTH_LONG).show();
                    loadData();
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

    public class CrearTemaJSON extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private boolean success = false;

        CrearTemaJSON(String URL) {
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
                crearTask = null;
                showProgress(false);

                if (success) {
                    Toast.makeText(getActivity(), R.string.message_crear_tema_success, Toast.LENGTH_LONG).show();
                    loadData();
                } else {
                    Toast.makeText(getActivity(), R.string.error_subida, Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            }
        }

        @Override
        protected void onCancelled() {
            crearTask = null;
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

    View.OnTouchListener sombra = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView view = (ImageView) v;
                    view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.DARKEN);
                    view.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:
                    v.performClick();
                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v;
                    view.getDrawable().clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        }
    };
}