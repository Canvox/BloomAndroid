package com.bonsai.bloom;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class FragmentHomeBloom extends Fragment {

	private View v;
    private ImageView imageResolver, imageHistorial, imageHisttemas, imageEventos, imageEstadisticas, imageTemas, imageCrear, imageAsignar, imageHabilitar;

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

        SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        String idmoderador = settings.getString("idmoderador", "");
        String tipousuario = settings.getString("tipousuario", "");
        String idusuario = settings.getString("idusuario", "");

        /*Nombre: José Domínguez
        * Modificado: 14/07/2017
        * Comentario: Se crea otro layaout fragment_home_docente para muestra del home de acuerdo al tipo de usuario*/
        if(!tipousuario.equals("2")) {
            v = inflater.inflate(R.layout.fragment_home, container, false);
            //if(!idmoderador.equals(idusuario)){
            imageResolver = (ImageView) v.findViewById(R.id.imageResolver);
            imageResolver.setOnTouchListener(sombra);
            imageResolver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentListaQuizzesBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });
            //}

            imageHistorial = (ImageView) v.findViewById(R.id.imageHistorial);
            imageHistorial.setOnTouchListener(sombra);
            imageHistorial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentHistorialQuizzesGeneralBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });


            imageEventos = (ImageView) v.findViewById(R.id.imageEventos);
            imageEventos.setOnTouchListener(sombra);
            imageEventos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentListaEventosBloom();
                    Bundle bundl = new Bundle();
                    bundl.putString("EXTRA1", "first");
                    fragment.setArguments(bundl);
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });

            imageEstadisticas = (ImageView) v.findViewById(R.id.imageEstadisticas);
            imageEstadisticas.setOnTouchListener(sombra);
            imageEstadisticas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentEstadisticasBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });

            imageTemas = (ImageView) v.findViewById(R.id.imageTemas);
            imageTemas.setOnTouchListener(sombra);
            imageTemas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentListaTemasBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });

            imageCrear = (ImageView) v.findViewById(R.id.imageCrearquizz);
            imageCrear.setOnTouchListener(sombra);
            imageCrear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentCrearQuizzBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });

            imageHabilitar = (ImageView) v.findViewById(R.id.imageHabilitar);
            imageHabilitar.setOnTouchListener(sombra);
            imageHabilitar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentListaQuizzesBloomHabilitar();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });

            imageHisttemas = (ImageView) v.findViewById(R.id.imageHisttemas);
            imageHisttemas.setOnTouchListener(sombra);
            imageHisttemas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentHistorialTemasBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });

        }else{

            v = inflater.inflate(R.layout.fragment_home_docente, container, false);

            imageHistorial = (ImageView) v.findViewById(R.id.imageHistorial);
            imageHistorial.setOnTouchListener(sombra);
            imageHistorial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentHistorialQuizzesGeneralBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });

            imageHisttemas = (ImageView) v.findViewById(R.id.imageHisttemas);
            imageHisttemas.setOnTouchListener(sombra);
            imageHisttemas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentHistorialTemasBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });


            imageEventos = (ImageView) v.findViewById(R.id.imageEventos);
            imageEventos.setOnTouchListener(sombra);
            imageEventos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentListaEventosBloom();
                    Bundle bundl = new Bundle();
                    bundl.putString("EXTRA1", "first");
                    fragment.setArguments(bundl);
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });

            imageEstadisticas = (ImageView) v.findViewById(R.id.imageEstadisticas);
            imageEstadisticas.setOnTouchListener(sombra);
            imageEstadisticas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentEstadisticasBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });

            imageAsignar = (ImageView) v.findViewById(R.id.imageAsignar);
            imageAsignar.setOnTouchListener(sombra);
            imageAsignar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new FragmentListaModeradorBloom();
                    FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                    fragTransaction.replace(R.id.frame_container, fragment);
                    fragTransaction.commit();
                }
            });

        }

        mCallback.hideActionButton();

        if (!idmoderador.equals(idusuario) && !tipousuario.equals("2")) {
            imageCrear.setVisibility(View.GONE);
            imageTemas.setVisibility(View.GONE);
            imageEstadisticas.setVisibility(View.GONE);
            imageHisttemas.setVisibility(View.GONE);
            imageHabilitar.setVisibility(View.GONE);
        }
		return v;
	}

    public void backPressed () {
        Fragment fragment = new FragmentListaCursosBloom();
        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.frame_container, fragment);
        fragTransaction.commit();
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