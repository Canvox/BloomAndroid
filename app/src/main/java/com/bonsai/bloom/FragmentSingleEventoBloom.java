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
import android.widget.TextView;

import org.json.JSONObject;

public class FragmentSingleEventoBloom extends Fragment {

	private View v;
    private ImageView imageImagenes, imageApuntes;
    private String idevento = "";

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
    	v = inflater.inflate(R.layout.fragment_single_evento, container, false);
        mCallback.hideActionButton();

        SharedPreferences settings = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("timerimagen", "");
        editor.commit();

        try {
            JSONObject jsonEvento = new JSONObject(getArguments().getString("EXTRA1"));
            TextView textTitulo = (TextView) v.findViewById(R.id.textTitulo);
            textTitulo.setText(jsonEvento.getString("descripcion"));
            idevento = jsonEvento.getString("idevento");
        } catch (Exception e) {
            e.printStackTrace();
        }

        imageImagenes = (ImageView) v.findViewById(R.id.imageImagenes);
        imageImagenes.setOnTouchListener(sombra);
        imageImagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", idevento);
                bundl.putString("EXTRA2", "first");
                bundl.putBundle("oldargs", getArguments());
                Fragment fragment = new FragmentListaImagenesBloom();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

        imageApuntes = (ImageView) v.findViewById(R.id.imageApuntes);
        imageApuntes.setOnTouchListener(sombra);
        imageApuntes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundl = new Bundle();
                bundl.putString("EXTRA1", idevento);
                bundl.putString("EXTRA2", "first");
                bundl.putBundle("oldargs", getArguments());
                Fragment fragment = new FragmentListaApuntesBloom();
                fragment.setArguments(bundl);
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

		return v;
	}

    public void backPressed () {
        Fragment fragment = new FragmentListaEventosBloom();
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