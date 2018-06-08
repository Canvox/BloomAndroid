package com.bonsai.bloom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Toast;

import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class FragmentCrearPreguntaBloom extends Fragment {

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Quizzes/quizzes.php?opcion=pregunta", path;
    //private String urlServer = "http://bonsai.com.ec/quizzes/TESIS/subeFotoPreguntaAnonymous.php?pregunta=";
    private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Quizzes/quizzes.php?opcion=pregunta", path;
    private String urlServer = "http://softwarefactoryuees.com.ec/BloomWEB/subeFotoPreguntaAnonymous.php?pregunta=";
	private View v, mProgressView, mContentView;
	private GetJSON jsonTask;
    private EditText editPregunta, editValor;
    private static int RESULT_LOAD_IMG = 1;
    private boolean selected = false;


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
    	v = inflater.inflate(R.layout.fragment_crear_pregunta, container, false);
        mCallback.hideActionButton();
		mProgressView = v.findViewById(R.id.login_progress);
		mContentView = v.findViewById(R.id.scrollView);
        editPregunta = (EditText) v.findViewById(R.id.editPregunta);
        editValor = (EditText) v.findViewById(R.id.editValor);

        ImageView mAgregarButton = (ImageView) v.findViewById(R.id.imageAgregar);
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
                if(checkFields()) {
                    if (jsonTask != null) return;
                    jsonTask = new GetJSON (URLJSON + "&pregunta=" + URLEncoder.encode(editPregunta.getText().toString()) + "&idquizz=" + getArguments().getString("EXTRA1") + "&valor=" + URLEncoder.encode(editValor.getText().toString()));
                    jsonTask.execute();
                }
            }
        });

        ImageView mSelectImageButton = (ImageView) v.findViewById(R.id.imageImagen);
        mSelectImageButton.setOnTouchListener(sombra);
        mSelectImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMG);
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

    public boolean checkFields() {

        // Reset errors.
        editPregunta.setError(null);
        editValor.setError(null);

        // Store values at the time of the login attempt.
        String pregunta = editPregunta.getText().toString();
        String valor = editValor.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(pregunta)) {
            editPregunta.setError(getString(R.string.error_field_required));
            focusView = editPregunta;
            cancel = true;
        }

        if (TextUtils.isEmpty(valor)) {
            editValor.setError(getString(R.string.error_field_required));
            focusView = editValor;
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
                        if (selected)
                            uploadPhoto(response.getString("Estado"));
                        Toast.makeText(getActivity(), R.string.message_pregunta_success, Toast.LENGTH_LONG).show();
                        Bundle bundl = new Bundle();
                        bundl.putString("EXTRA1", getArguments().getString("EXTRA1"));
                        bundl.putString("EXTRA2", response.getString("Estado"));
                        bundl.putString("EXTRA3", "0");
                        Fragment fragment = new FragmentCrearRespuestaBloom();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            path = getPathFromCameraData(data, this.getActivity());
            Log.i("PICTURE", "Path: " + path);

            if (path != null) {
                selected = true;
                Toast.makeText(getActivity(), R.string.message_imagen_success, Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getActivity(), R.string.message_imagen_failure, Toast.LENGTH_LONG).show();
        }
    }

    public void uploadPhoto (final String idpregunta) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream fileInputStream = new FileInputStream(new File(path));

                    HttpURLConnection connection = null;
                    DataOutputStream outputStream = null;
                    DataInputStream inputStream = null;
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String boundary =  "*****";
                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    int maxBufferSize = 4 * 1024 * 1024;

                    Log.i("PRUEBA URL", urlServer + idpregunta);
                    URL url = new URL(urlServer + idpregunta);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

                    outputStream = new DataOutputStream( connection.getOutputStream() );
                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + path +"\"" + lineEnd);
                    outputStream.writeBytes(lineEnd);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        outputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                    int serverResponseCode = connection.getResponseCode();
                    String serverResponseMessage = connection.getResponseMessage();

                    fileInputStream.close();
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                }
            }
        };
        new Thread(runnable).start();
    }

    public static String getPathFromCameraData(Intent data, Context context) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
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