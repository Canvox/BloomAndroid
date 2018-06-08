package com.bonsai.bloom;

import android.app.Activity;
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

public class FragmentResultadoBloom extends Fragment {

	private View v;

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
    	v = inflater.inflate(R.layout.fragment_resultado, container, false);
        mCallback.hideActionButton();

        try {
            String resultado = getArguments().getString("EXTRA1");
            TextView textTitulo = (TextView) v.findViewById(R.id.textResultado);
            textTitulo.setText(resultado);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageView imageOtro = (ImageView) v.findViewById(R.id.imageOtro);
        imageOtro.setOnTouchListener(sombra);
        imageOtro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new FragmentListaQuizzesBloom();
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.replace(R.id.frame_container, fragment);
                fragTransaction.commit();
            }
        });

		return v;
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