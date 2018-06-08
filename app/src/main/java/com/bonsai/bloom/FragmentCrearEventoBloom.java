package com.bonsai.bloom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Calendar;

public class FragmentCrearEventoBloom extends Fragment {

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Eventos/eventos.php?opcion=inserta";
    private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Eventos/eventos.php?opcion=inserta";
	private View v, mProgressView, mContentView;
	private GetJSON jsonTask;
    private EditText editCalendario;
    private int año, mes, dia;


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
    	v = inflater.inflate(R.layout.fragment_crear_evento, container, false);
        mCallback.hideActionButton();
		mProgressView = v.findViewById(R.id.login_progress);
		mContentView = v.findViewById(R.id.scrollView);
        mContentView.setBackgroundColor(getResources().getColor(R.color.colorSecond));

        final Calendar c = Calendar.getInstance();
        año = c.get(Calendar.YEAR);
        mes = c.get(Calendar.MONTH);
        dia = c.get(Calendar.DAY_OF_MONTH);

        editCalendario = (EditText) v.findViewById(R.id.editCalendario);
        editCalendario.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        DatePickerDialog date_picker = new DatePickerDialog(getActivity(), datePickerListener, año, mes, dia);
                        date_picker.show();
                        break;
                    }
                }
                return true;
            }
        });

        ImageView mAgregarButton = (ImageView) v.findViewById(R.id.imageAgregar);
        mAgregarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                    EditText editTitulo = (EditText) v.findViewById(R.id.editTitulo);
                    if (checkIfEmpty(editTitulo)) {
                        Snackbar.make(mContentView, R.string.error_field_required, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        editTitulo.requestFocus();

                    } else if(checkIfEmpty(editCalendario)) {
                        Snackbar.make(mContentView, R.string.error_field_required, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        editCalendario.requestFocus();

                    } else {
                        SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
                        String idgrupo = settings.getString("idgrupo", "");

                        if (jsonTask != null) return;
                        jsonTask = new GetJSON(URLJSON + "&idgrupo=" + idgrupo + "&evento=" + URLEncoder.encode(editTitulo.getText().toString()) + "&fecha=" + editCalendario.getText().toString());
                        jsonTask.execute();
                    }
                } catch (Exception e) {
                    Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                }
            }
        });
		return v;
	}

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            editCalendario.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
        }
    };

    public void backPressed () {
        Fragment fragment = new FragmentListaEventosBloom();
        Bundle bundl = new Bundle();
        bundl.putString("EXTRA1","second");
        fragment.setArguments(bundl);
        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.frame_container, fragment);
        fragTransaction.commit();
    }

    private boolean checkIfEmpty (EditText edit) {
        return (edit.getText().toString().equals(""));
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

				if (jsonOb.getString("success").equals("true")) success = true;
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
                    Snackbar.make(mContentView, R.string.message_crear_evento_success, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    getActivity().onBackPressed();

                } else
                    Toast.makeText(getActivity(), R.string.error_subida, Toast.LENGTH_LONG).show();
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