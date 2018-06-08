package com.bonsai.bloom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bonsai.bloom.adapters.CursosAdapter;
import com.bonsai.bloom.adapters.ModeradorAdapter;
import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

public class FragmentListaModeradorBloom extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Usuario/listamoderador.php", tag = "", idgrupo;
    //private String URLJSON2 = "http://bonsai.com.ec/quizzes/TESIS/guardamoderador.php?opcion=guardamoderador";http://192.168.10.230:8080/BloomWeb/TESIS
	private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Usuario/listamoderador.php", tag = "", idgrupo,tipousuario, bandera="0";
	private String URLJSON1 = "http://softwarefactoryuees.com.ec/BloomWEB/guardamoderador.php?opcion=consultamoderador";
	private String URLJSON2 = "http://softwarefactoryuees.com.ec/BloomWEB/guardamoderador.php?opcion=guardamoderador";
	private View v, mProgressView, mContentView;
	private GetJSON jsonTask;
	private GetJSONM jsonTask2;
	private SwipeRefreshLayout swipeContainer;
    private GuardarModeradorTask guardarTask;
    private JSONArray newArr;
    private ModeradorAdapter adapter;
    private ListView list;

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
        tipousuario = settings.getString("tipousuario", "");
        idgrupo = settings.getString("idgrupo", "");

		if(jsonTask != null) return v;
		jsonTask = new GetJSON(URLJSON + "?idgrupo=" + idgrupo + "&tipo_usuario=" + tipousuario);
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
				jsonTask = new GetJSON(URLJSON + "?idgrupo=" + idgrupo + "&tipo_usuario=" + tipousuario);
				jsonTask.execute();
				// Update data in ListView
				Toast.makeText(getActivity(), "Actualizado", Toast.LENGTH_SHORT).show();
				// Remove widget from screen.
				swipeContainer.setRefreshing(false);
			}
		}, 3000);

	}

	public void MuestraModeradoractual() {
		jsonTask2 = new GetJSONM(URLJSON1+"&idgrupo=" +idgrupo);
		jsonTask2.execute();
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
					jsonArr = jsonOb.getJSONArray("Usuario");
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

                        newArr = new JSONArray();
						for (int x = 0; x < response.length(); x++) {
							JSONObject temp = response.getJSONObject(x);
                            temp.put("selected", "0");
                            temp.put("tag", Integer.toString(x));
                            newArr.put(temp);
						}

                        adapter = new ModeradorAdapter(getActivity(), newArr);
                        list = (ListView) v.findViewById(R.id.listview);
						if(bandera.equals("0")){
						View header = getActivity().getLayoutInflater().inflate(R.layout.header_simple, null);
						View footer = getActivity().getLayoutInflater().inflate(R.layout.footer_moderador, null);
                        TextView textCabecera = (TextView) header.findViewById(R.id.textCabecera);
                        textCabecera.setText(R.string.texview_cabecera_moderador);

						list.addHeaderView(header);
						list.addFooterView(footer);}
						list.setAdapter(adapter);
						list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
								try {
									tag = ((TextView) v.findViewById(R.id.textHidden)).getText().toString();
									JSONArray arrayCopy = new JSONArray(newArr.toString());
									newArr = new JSONArray();
									for (int x = 0; x < arrayCopy.length(); x++) {
										JSONObject temp = arrayCopy.getJSONObject(x);
										if (x == Integer.parseInt(tag))
											temp.put("selected", "1");
										else
											temp.put("selected", "0");
										newArr.put(temp);
									}
									adapter = new ModeradorAdapter(getActivity(), newArr);
									list.invalidateViews();
									list.refreshDrawableState();
									list.setAdapter(adapter);
									Log.i("BLOOM", newArr.toString());

								} catch (Exception e) {
									Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
								}
							}
						});

						MuestraModeradoractual();

						ImageView imageAgregar = (ImageView) v.findViewById(R.id.imageAgregar);
						imageAgregar.setOnTouchListener(sombra);
						imageAgregar.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								if (tag.equals("")) {
                                    Snackbar.make(mContentView, R.string.message_moderador_needed, Snackbar.LENGTH_LONG).show();
                                } else {
                                    try {
                                        JSONObject temp = newArr.getJSONObject(Integer.parseInt(tag));
                                        if(guardarTask != null) return;
                                        guardarTask = new GuardarModeradorTask(URLJSON2 + "&idgrupo=" + idgrupo+ "&idmoderador=" + temp.getString("idusuario"));
                                        guardarTask.execute();
                                    } catch (Exception e) {
                                        Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                                    }
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


	public class GetJSONM extends AsyncTask<Void, Void, JSONArray>  {

		private final String URL;
		private boolean success = false;

		GetJSONM(String URL) {
			this.URL = URL;
			showProgress(true);
		}

		@Override
		protected JSONArray doInBackground(Void... params) {
			JSONArray jsonArre = new JSONArray();
			try {
				JSONObject jsonOb;
				JSONParser jParsere = new JSONParser();
				Log.i("PRUEBA URL", URL);
				jsonOb = jParsere.getJSONFromUrl(URL);
				Log.i("PRUEBA JSON", jsonOb.toString());

				if (jsonOb.getString("success").equals("true")) {
					jsonArre = jsonOb.getJSONArray("Usuario");
					success = true;
				}
			} catch (Exception e) {
				Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
			}
			return jsonArre;
		}

		@Override
		protected void onPostExecute(JSONArray response) {
			if (isAdded()) {
				super.onPostExecute(response);
				jsonTask = null;
				showProgress(false);

				if (success) {
					try {

						JSONObject jsonOb = response.getJSONObject(0);
						String nombre= jsonOb.getString("nombres");
						String apellido= jsonOb.getString("apellidos");
						String carrera= jsonOb.getString("carrera");

						Toast.makeText(getActivity(), "Moderador actual: "+nombre+" "+apellido+"("+carrera+")", Toast.LENGTH_LONG).show();

					} catch (Exception e) {
						Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
					}
				} else {
					//Snackbar.make(mContentView, "No se ha asignado Moderador", Snackbar.LENGTH_LONG).setAction("Action", null).show();
					Toast.makeText(getActivity(),"No se ha asignado Moderador", Toast.LENGTH_LONG).show();
				}
			}
		}

		@Override
		protected void onCancelled() {
			jsonTask = null;
		}

	}

    public class GuardarModeradorTask extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private boolean success = false;

        GuardarModeradorTask(String URL) {
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
                guardarTask = null;
                showProgress(false);

                if (success) {
                    Toast.makeText(getActivity(), R.string.message_moderador_success, Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                } else {
                    Toast.makeText(getActivity(), R.string.error_subida, Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            }
        }

        @Override
        protected void onCancelled() {
            guardarTask = null;
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