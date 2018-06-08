package com.bonsai.bloom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.bonsai.bloom.adapters.ImagenesAdapter;
import com.bonsai.bloom.helper.ConnectionDetector;
import com.bonsai.bloom.helper.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class FragmentListaImagenesBloom extends Fragment {

	//private String URLJSON = "http://bonsai.com.ec/quizzes/TESIS/Eventos/imageneseventos.php?opcion=consultaimagen", path, timerimagen;
    //private String URLTIMER = "http://bonsai.com.ec/quizzes/TESIS/Estadisticas/timer.php?opcion=guardatimer&ventana=VerImagenes";
	//private String urlServer = "http://www.bonsai.com.ec/quizzes/TESIS/Eventos/subeFoto.php";
	private String URLJSON = "http://softwarefactoryuees.com.ec/BloomWEB/Eventos/imageneseventos.php?opcion=consultaimagen", path, timerimagen;
	private String URLTIMER = "http://softwarefactoryuees.com.ec/BloomWEB/Estadisticas/timer.php?opcion=guardatimer&ventana=VerImagenes";
	private String urlServer = "http://softwarefactoryuees.com.ec/BloomWEB/Eventos/subeFoto.php";
	private View v, mProgressView, mContentView;
	private GetJSON jsonTask;
	private static int RESULT_LOAD_IMG = 1;

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
    	v = inflater.inflate(R.layout.fragment_grid, container, false);
		mCallback.showActionButton();
		mProgressView = v.findViewById(R.id.login_progress);
		mContentView = v.findViewById(R.id.scrollView);
        mContentView.setBackgroundColor(getResources().getColor(R.color.colorSecond));

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

        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);
		SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        timerimagen = settings.getString("timerimagen", "");
        if (timerimagen.equals("")) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("timerimagen", String.valueOf(seconds));
            editor.commit();
            timerimagen = String.valueOf(seconds);
        }

		if(jsonTask != null) return v;
		jsonTask = new GetJSON(URLJSON + "&eventocon=" + getArguments().getString("EXTRA1"));
		jsonTask.execute();
		return v;
	}

	public void addNewImagen() {
		Fragment fragment = new FragmentCrearImagenBloom();
		fragment.setArguments(getArguments());
		FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
		fragTransaction.replace(R.id.frame_container, fragment);
		fragTransaction.commit();
	}

    public void backPressed () {
        Fragment fragment = new FragmentSingleEventoBloom();
        fragment.setArguments(getArguments().getBundle("oldargs"));
        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.frame_container, fragment);
        fragTransaction.commit();

        Calendar c = Calendar.getInstance();
        String time = String.valueOf(round((c.get(Calendar.SECOND) - Integer.parseInt(timerimagen)) / (double) 60, 2));

        SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        String idgrupo = settings.getString("idgrupo", "");
        String idusuario = settings.getString("idusuario", "");

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
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
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

                if (!success) {
                    if (getArguments().getString("EXTRA2").equals("first")) {
                        Snackbar.make(mContentView, R.string.message_imagenes_empty, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        addNewImagen();
                    } else
                        getActivity().onBackPressed();
				} else {
                    try {
                        ImagenesAdapter adapter = new ImagenesAdapter(getActivity(), response);
                        GridView grid = (GridView) v.findViewById(R.id.grid);
                        grid.setAdapter(adapter);
                        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                String extra1 = ((TextView) v.findViewById(R.id.textHidden)).getText().toString();
                                Bundle bundl = new Bundle();
                                bundl.putString("EXTRA1", extra1);
                                bundl.putBundle("oldargs", getArguments());
                                Fragment fragment = new FragmentSingleImageBloom();
                                fragment.setArguments(bundl);
                                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                                fragTransaction.replace(R.id.frame_container, fragment);
                                fragTransaction.commit();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(getActivity().getResources().getString(R.string.app_name), getActivity().getResources().getString(R.string.error_tag), e);
                    }
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
}