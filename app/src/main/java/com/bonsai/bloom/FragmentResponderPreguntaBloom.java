package com.bonsai.bloom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class FragmentResponderPreguntaBloom extends Fragment {

	/*private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Quizzes/resolverquizz.php?opcion=consultapregunta";
    private String URLJSON2 = "http://bonsai.com.ec/quizzes/TESIS/Quizzes/resolverquizz.php?opcion=historialpreguntas";
    private String URLTIMER = "http://bonsai.com.ec/quizzes/TESIS/Estadisticas/timer.php?opcion=guardatimer&ventana=ResolverQuizz";*/
    private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Quizzes/resolverquizz.php?opcion=consultapregunta";
    private String URLJSON2 = "http://softwarefactoryuees.com.ec/BloomWEB/Quizzes/resolverquizz.php?opcion=historialpreguntas";
    private String URLTIMER = "http://softwarefactoryuees.com.ec/BloomWEB/Estadisticas/timer.php?opcion=guardatimer&ventana=ResolverQuizz";
    private String numpreg, extra1, extra2, restantes, idpregunta;
    private double resultado_maximo, resultado;
	private View v, mProgressView, mContentView;
	private GetJSON jsonTask;
    private GetJSONSaveAnswer jsonTaskSaveAnswer;
    private JSONArray respuestas;

    protected ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.icono_mas)
            .showImageOnFail(R.drawable.icono_mas)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

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
    	v = inflater.inflate(R.layout.fragment_responder_pregunta, container, false);
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

		Bundle bundle = getArguments();
        extra1 = bundle.getString("EXTRA1");
        extra2 = bundle.getString("EXTRA2");
        if(extra2.length() == 0) numpreg = "1";
        else numpreg = String.valueOf(extra2.split(",").length + 1);

		if(jsonTask != null) return v;
		jsonTask = new GetJSON(URLJSON + "&idquizz=" + extra1 + "&preguntasresueltas=" + extra2);
		jsonTask.execute();
		return v;
	}

    public void backPressed () {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.salir_quizz)
                .setCancelable(false)
                .setPositiveButton(R.string.afirmacion, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Fragment fragment = new FragmentHomeBloom();
                        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                        fragTransaction.replace(R.id.frame_container, fragment);
                        fragTransaction.commit();
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

	public class GetJSON extends AsyncTask<Void, Void, JSONObject> {

		private final String URL;
		private boolean success = false;

		GetJSON(String URL) {
			this.URL = URL;
			showProgress(true);
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
            JSONObject jsonResponse = new JSONObject();
			try {
				JSONObject jsonOb;
				JSONParser jParser = new JSONParser();
                Log.i("PRUEBA URL", URL);
				jsonOb = jParser.getJSONFromUrl(URL);
                Log.i("PRUEBA JSON", jsonOb.toString());

				if (jsonOb.getString("success").equals("true")) {
                    jsonResponse = jsonOb.getJSONArray("Estado").getJSONObject(0);
                    restantes = jsonOb.getString("restantes");
                    success = true;
                }
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
                        TextView textTitulo = (TextView) v.findViewById(R.id.textTitulo);
                        textTitulo.setText("Pregunta " + numpreg + ". Valor: " + response.getString("valor"));
                        TextView textPregunta = (TextView) v.findViewById(R.id.textPregunta);
                        textPregunta.setText(response.getString("pregunta"));
                        idpregunta = response.getString("idpregunta");
                        resultado_maximo = Double.parseDouble(response.getString("valor"));

                        if (!response.getString("imagen").equals("null")) {
                            ImageView imagePregunta = (ImageView) v.findViewById(R.id.imagePregunta);
                            imageLoader.displayImage(response.getString("imagen"), imagePregunta, options);
                        }

                        LinearLayout layRespuestas = (LinearLayout) v.findViewById(R.id.layRespuestas);
                        respuestas = response.getJSONArray("respuestas");
                        for (int x = 0; x < response.getJSONArray("respuestas").length(); x++) {
                            CheckBox cb = new CheckBox(getActivity());
                            cb.setText(respuestas.getJSONObject(x).getString("respuesta"));
                            layRespuestas.addView(cb);
                        }
                        if (extra2.equals("")) extra2 = response.getString("idpregunta");
                        else extra2 += "," + response.getString("idpregunta");

                        ImageView mResponderButton = (ImageView) v.findViewById(R.id.imageResponder);
                        mResponderButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    LinearLayout layRespuestas = (LinearLayout) v.findViewById(R.id.layRespuestas);
                                    int total_correctas = 0, total_acertadas = 0, total_falladas = 0;
                                    int childcount = layRespuestas.getChildCount();
                                    for (int i = 0; i < childcount; i++) {
                                        CheckBox check = (CheckBox) layRespuestas.getChildAt(i);
                                        if (respuestas.getJSONObject(i).getString("correcta").equals("1"))
                                            total_correctas++;
                                        if (respuestas.getJSONObject(i).getString("correcta").equals("1") && check.isChecked())
                                            total_acertadas++;
                                        if (respuestas.getJSONObject(i).getString("correcta").equals("0") && check.isChecked())
                                            total_falladas++;
                                    }
                                    resultado = resultado_maximo - (double) (total_correctas - total_acertadas + total_falladas) * resultado_maximo / (double) total_correctas;

                                    SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
                                    SharedPreferences.Editor editor = settings.edit();
                                    resultado += Double.parseDouble(settings.getString("resultado", "0"));
                                    resultado_maximo += Double.parseDouble(settings.getString("resultado_maximo", "0"));
                                    Log.i("RESULTADOS", "Obtenido: " + String.valueOf(resultado) + ", Maximo: " + String.valueOf(resultado_maximo));
                                    editor.putString("resultado", String.valueOf(resultado));
                                    editor.putString("resultado_maximo", String.valueOf(resultado_maximo));
                                    editor.commit();

                                    String correcto = "0";
                                    if (total_correctas == total_acertadas && total_falladas == 0) correcto = "1";

                                    URLJSON2 += "&id_quizz=" + extra1 + "&id_usuario=" + settings.getString("idusuario", "") + "&correcta=" + correcto + "&id_pregunta=" + idpregunta;
                                    if (restantes.equals("0")) {
                                        resultado = resultado / resultado_maximo * 100;
                                        if (resultado < 0) resultado = 0;
                                        URLJSON2 += "&resultado=" + String.valueOf(resultado);
                                    }
                                    jsonTaskSaveAnswer = new GetJSONSaveAnswer(URLJSON2);
                                    jsonTaskSaveAnswer.execute();
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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public class GetJSONSaveAnswer extends AsyncTask<Void, Void, JSONObject> {

        private final String URL;
        private boolean success = false;

        GetJSONSaveAnswer(String URL) {
            this.URL = URL;
            showProgress(true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject jsonResponse = new JSONObject();
            try {
                JSONObject jsonOb;
                JSONParser jParser = new JSONParser();
                Log.i("PRUEBA URL", URL);
                jsonOb = jParser.getJSONFromUrl(URL);
                Log.i("PRUEBA JSON", jsonOb.toString());

                if (jsonOb.getString("success").equals("true"))
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
                        if (restantes.equals("0")) {
                            SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
                            String idgrupo = settings.getString("idgrupo", "");
                            String idusuario = settings.getString("idusuario", "");
                            String timerquizz = settings.getString("timerquizz", "");

                            Calendar c = Calendar.getInstance();
                            String time = String.valueOf(round((c.get(Calendar.SECOND) - Integer.parseInt(timerquizz)) / (double) 60, 2));

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

                            Toast.makeText(getActivity(), R.string.message_quiz_finished, Toast.LENGTH_LONG).show();
                            Bundle bundl = new Bundle();
                            bundl.putString("EXTRA1", String.valueOf(resultado));
                            Fragment fragment = new FragmentResultadoBloom();
                            fragment.setArguments(bundl);
                            FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                            fragTransaction.replace(R.id.frame_container, fragment);
                            fragTransaction.commit();

                        } else {
                            Bundle bundl = new Bundle();
                            bundl.putString("EXTRA1", extra1);
                            bundl.putString("EXTRA2", extra2);
                            Fragment fragment = new FragmentResponderPreguntaBloom();
                            fragment.setArguments(bundl);
                            FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                            fragTransaction.replace(R.id.frame_container, fragment);
                            fragTransaction.commit();
                        }

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