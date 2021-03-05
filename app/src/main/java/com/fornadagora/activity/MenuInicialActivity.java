package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.fornadagora.MainActivity2Kt;
import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.auth.FirebaseAuth;

public class MenuInicialActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_inicial);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Fornadagora");
        setSupportActionBar(toolbar);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_sair :
                deslogarUsuario();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deslogarUsuario(){
        try{
            autenticacao.signOut();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void abrirTelaVerPadaria(View view){
        startActivity(new Intent(getApplicationContext(), VerPadariasMapaActivity.class));
    }

    public void abrirTelaCadastrarAlerta(View view){
        startActivity(new Intent(getApplicationContext(), CadastrarAlertaActivity.class));
    }

    public void abrirTelaVerAlertas(View view){
        startActivity(new Intent(getApplicationContext(), VerAlertaUsuarioActivity.class));
    }
    public void abrirTelaNotificacao(View view){
        startActivity(new Intent(getApplicationContext(), com.fornadagora.MainActivity2.class));
    }
}
