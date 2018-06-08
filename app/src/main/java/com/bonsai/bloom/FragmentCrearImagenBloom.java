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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bonsai.bloom.helper.ConnectionDetector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class FragmentCrearImagenBloom extends Fragment {

    //private String urlServer = "http://bonsai.com.ec/quizzes/TESIS/Eventos/subeFotoAnonymous.php", path;
    //private String URLTIMER = "http://bonsai.com.ec/quizzes/TESIS/Estadisticas/timer.php?opcion=guardatimer&ventana=SubirImagenes";
    private String urlServer = "http://softwarefactoryuees.com.ec/BloomWEB/Eventos/subeFotoAnonymous.php", path;
    private String URLTIMER = "http://softwarefactoryuees.com.ec/BloomWEB/Estadisticas/timer.php?opcion=guardatimer&ventana=SubirImagenes";
    private View v, mProgressView, mContentView;
    private static int RESULT_LOAD_IMG = 1;
    private int seconds;
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
    	v = inflater.inflate(R.layout.fragment_crear_imagen, container, false);
        mCallback.hideActionButton();
		mProgressView = v.findViewById(R.id.login_progress);
		mContentView = v.findViewById(R.id.scrollView);
        mContentView.setBackgroundColor(getResources().getColor(R.color.colorSecond));

        Calendar c = Calendar.getInstance();
        seconds = c.get(Calendar.SECOND);

        ImageView mSaveImageButton = (ImageView) v.findViewById(R.id.imageImagen);
        mSaveImageButton.setOnTouchListener(sombra);
        mSaveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected) {
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
                    uploadPhoto();
                    Snackbar.make(mContentView, R.string.message_image_sent, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    Fragment fragment = new FragmentListaEventosBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();

                } else
                    Snackbar.make(mContentView, R.string.message_image_not_selected, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        ImageView mAgregarImageButton = (ImageView) v.findViewById(R.id.imageAgregar);
        mAgregarImageButton.setOnTouchListener(sombra);
        mAgregarImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMG);
            }
        });
        return v;
    }

    public void backPressed () {
        Fragment fragment = new FragmentListaImagenesBloom();
        Bundle bundl = getArguments();
        bundl.putString("EXTRA2","second");
        fragment.setArguments(bundl);
        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.frame_container, fragment);
        fragTransaction.commit();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        Calendar c = Calendar.getInstance();
        String time = String.valueOf(round((c.get(Calendar.SECOND) - seconds) / (double) 60, 2));

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            path = getPathFromCameraData(data, this.getActivity());
            Log.i("PICTURE", "Path: " + path);

            if (path != null) {
                Toast.makeText(getActivity(), R.string.message_imagen_success, Toast.LENGTH_LONG).show();
                selected = true;
                File imageFile = new File(path);
                if(imageFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    ImageView imageSelected = (ImageView) v.findViewById(R.id.imageSelected);
                    imageSelected.setImageBitmap(myBitmap);
                }
            } else
                Toast.makeText(getActivity(), R.string.message_imagen_failure, Toast.LENGTH_LONG).show();
        }
    }

    public void uploadPhoto () {
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

                    SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
                    String idusuario = settings.getString("idusuario", "");
                    EditText editCaption = (EditText) v.findViewById(R.id.editCaption);

                    urlServer += "?idusuario=" + idusuario + "&caption=" + URLEncoder.encode(editCaption.getText().toString()) + "&evento=" + getArguments().getString("EXTRA1");
                    Log.i("PRUEBA URL", urlServer);
                    URL url = new URL(urlServer);
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
                    Log.i("PRUEBA", serverResponseMessage);
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