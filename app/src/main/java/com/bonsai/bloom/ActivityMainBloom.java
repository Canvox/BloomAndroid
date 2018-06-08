package com.bonsai.bloom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class ActivityMainBloom extends AppCompatActivity
        implements FragmentCrearImagenBloom.MensajesListener, FragmentEstadisticasUsuarioBloom.MensajesListener, FragmentEstadisticasBloom.MensajesListener, FragmentHistorialTemasBloom.MensajesListener, FragmentCrearEventoBloom.MensajesListener, FragmentUnirseCursoBloom.MensajesListener, FragmentHistorialQuizzesGeneralBloom.MensajesListener, FragmentResultadoBloom.MensajesListener, FragmentCrearRespuestaBloom.MensajesListener, FragmentListaModeradorBloom.MensajesListener, FragmentCrearApunteBloom.MensajesListener, FragmentCrearPreguntaBloom.MensajesListener, FragmentSingleImageBloom.MensajesListener, FragmentListaEventosBloom.MensajesListener, FragmentListaApuntesBloom.MensajesListener, FragmentListaImagenesBloom.MensajesListener, FragmentSingleEventoBloom.MensajesListener, FragmentHistorialQuizzesBloom.MensajesListener, FragmentHomeBloom.MensajesListener, FragmentCrearQuizzBloom.MensajesListener, FragmentListaCursosBloom.MensajesListener, FragmentListaQuizzesBloom.MensajesListener, FragmentListaQuizzesBloomHabilitar.MensajesListener, FragmentResponderPreguntaBloom.MensajesListener, FragmentListaTemasBloom.MensajesListener, FragmentDescripcionApuntesBloom.MensajesListener {

    private FloatingActionButton actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
        ImageLoader.getInstance().init(config);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionButton = (FloatingActionButton) findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment anonymousFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
                if (anonymousFragment instanceof FragmentListaApuntesBloom) {
                    FragmentListaApuntesBloom activeFragment = (FragmentListaApuntesBloom) anonymousFragment;
                    activeFragment.addNewApunte();

                } else if (anonymousFragment instanceof FragmentListaCursosBloom) {
                    FragmentListaCursosBloom activeFragment = (FragmentListaCursosBloom) anonymousFragment;
                    activeFragment.unirseCurso();

                } else if (anonymousFragment instanceof FragmentListaEventosBloom) {
                    FragmentListaEventosBloom activeFragment = (FragmentListaEventosBloom) anonymousFragment;
                    activeFragment.addNewEvento();

                } else if (anonymousFragment instanceof FragmentListaImagenesBloom) {
                    FragmentListaImagenesBloom activeFragment = (FragmentListaImagenesBloom) anonymousFragment;
                    activeFragment.addNewImagen();

                }
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        Fragment fragment = new FragmentListaCursosBloom();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
    }

    public void hideActionButton () { actionButton.setVisibility(View.GONE); }
    public void showActionButton () { actionButton.setVisibility(View.VISIBLE); }

    @Override
    public void onBackPressed() {
        Fragment anonymousFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if (anonymousFragment instanceof FragmentHomeBloom) {
            FragmentHomeBloom activeFragment = (FragmentHomeBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentUnirseCursoBloom) {
            FragmentUnirseCursoBloom activeFragment = (FragmentUnirseCursoBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentListaCursosBloom) {
            FragmentListaCursosBloom activeFragment = (FragmentListaCursosBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentListaApuntesBloom) {
            FragmentListaApuntesBloom activeFragment = (FragmentListaApuntesBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentDescripcionApuntesBloom) {
            FragmentDescripcionApuntesBloom activeFragment = (FragmentDescripcionApuntesBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentCrearApunteBloom) {
            FragmentCrearApunteBloom activeFragment = (FragmentCrearApunteBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentSingleEventoBloom) {
            FragmentSingleEventoBloom activeFragment = (FragmentSingleEventoBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentListaImagenesBloom) {
            FragmentListaImagenesBloom activeFragment = (FragmentListaImagenesBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentSingleImageBloom) {
            FragmentSingleImageBloom activeFragment = (FragmentSingleImageBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentCrearEventoBloom) {
            FragmentCrearEventoBloom activeFragment = (FragmentCrearEventoBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentHistorialQuizzesBloom) {
            FragmentHistorialQuizzesBloom activeFragment = (FragmentHistorialQuizzesBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentResponderPreguntaBloom) {
            FragmentResponderPreguntaBloom activeFragment = (FragmentResponderPreguntaBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentCrearPreguntaBloom) {
            FragmentCrearPreguntaBloom activeFragment = (FragmentCrearPreguntaBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentCrearRespuestaBloom) {
            FragmentCrearRespuestaBloom activeFragment = (FragmentCrearRespuestaBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentEstadisticasUsuarioBloom) {
            FragmentEstadisticasUsuarioBloom activeFragment = (FragmentEstadisticasUsuarioBloom) anonymousFragment;
            activeFragment.backPressed();

        } else if (anonymousFragment instanceof FragmentCrearImagenBloom) {
            FragmentCrearImagenBloom activeFragment = (FragmentCrearImagenBloom) anonymousFragment;
            activeFragment.backPressed();

        } else {
            Fragment fragment = new FragmentHomeBloom();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                SharedPreferences settings = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("idusuario", "");
                editor.putString("jsonCursos", "");
                editor.putString("tipousuario", "");
                editor.commit();
                Intent mainIntent = new Intent(ActivityMainBloom.this, ActivityLoginBloom.class);
                ActivityMainBloom.this.startActivity(mainIntent);
                ActivityMainBloom.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}