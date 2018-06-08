package com.bonsai.bloom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONObject;

import java.net.URLEncoder;

public class FragmentCrearRespuestaBloom extends Fragment {

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/quizzes.php?opcion=respuesta";
    private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/quizzes.php?opcion=respuesta";
	private View v, mProgressView, mContentView;
	private GetJSON jsonTask;
    private EditText editRespuesta;
    private Switch switchRespuesta;
    private int opcion = 0;

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
    	v = inflater.inflate(R.layout.fragment_crear_respuesta, container, false);
        mCallback.hideActionButton();
		mProgressView = v.findViewById(R.id.login_progress);
		mContentView = v.findViewById(R.id.scrollView);
        editRespuesta = (EditText) v.findViewById(R.id.editRespuesta);
        switchRespuesta = (Switch) v.findViewById(R.id.switch1);


        TextView textNumRespuesta = (TextView) v.findViewById(R.id.textNumRespuesta);
        textNumRespuesta.setText("Respuesta " + String.valueOf(Integer.parseInt(getArguments().getString("EXTRA3")) + 1));

        ImageView imageRespuesta = (ImageView) v.findViewById(R.id.imageRespuesta);
        imageRespuesta.setOnTouchListener(sombra);
        imageRespuesta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                opcion = 0;
                sendData();
            }
        });

        ImageView imagePregunta = (ImageView) v.findViewById(R.id.imagePregunta);
        imagePregunta.setOnTouchListener(sombra);
        imagePregunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                opcion = 1;
                sendData();
            }
        });

        ImageView imageTerminar = (ImageView) v.findViewById(R.id.imageTerminar);
        imageTerminar.setOnTouchListener(sombra);
        imageTerminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                opcion = 2;
                sendData();
            }
        });
        return v;
    }

    public void backPressed () {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.salir_crear_quizz)
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

    public void sendData() {
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
        String correcta = "0";
        if(switchRespuesta.isChecked()) correcta = "1";

        if(checkFields()) {
            if (jsonTask != null) return;
            jsonTask = new GetJSON (URLJSON + "&respuesta=" + URLEncoder.encode(editRespuesta.getText().toString()) + "&idpregunta=" + getArguments().getString("EXTRA2") + "&correcta=" + correcta);
            jsonTask.execute();
        }
    }

    public boolean checkFields() {

        // Reset errors.
        editRespuesta.setError(null);

        // Store values at the time of the login attempt.
        String respuesta = editRespuesta.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(respuesta)) {
            editRespuesta.setError(getString(R.string.error_field_required));
            focusView = editRespuesta;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else
            return true;
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
                        bundl.putString("EXTRA1", getArguments().getString("EXTRA1"));
                        bundl.putString("EXTRA2", getArguments().getString("EXTRA2"));
                        bundl.putString("EXTRA3", String.valueOf(Integer.parseInt(getArguments().getString("EXTRA3")) + 1));

                        Fragment fragment = new Fragment();
                        switch (opcion) {
                            case 0:
                                Toast.makeText(getActivity(), R.string.message_respuesta_success, Toast.LENGTH_LONG).show();
                                fragment = new FragmentCrearRespuestaBloom();
                                break;
                            case 1:
                                Toast.makeText(getActivity(), R.string.message_respuesta_success, Toast.LENGTH_LONG).show();
                                fragment = new FragmentCrearPreguntaBloom();
                                break;
                            case 2:
                                if (Integer.parseInt(getArguments().getString("EXTRA3")) > 0) {
                                    Toast.makeText(getActivity(), R.string.message_quizz_creation_success, Toast.LENGTH_LONG).show();
                                    fragment = new FragmentHomeBloom();
                                } else Toast.makeText(getActivity(), R.string.message_two_answers, Toast.LENGTH_LONG).show();
                                break;
                        }
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