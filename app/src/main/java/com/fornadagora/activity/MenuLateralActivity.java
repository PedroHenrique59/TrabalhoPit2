package com.fornadagora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.fornadagora.R;
import com.fornadagora.helper.Base64Custom;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MenuLateralActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private Usuario usuario;

    private TextView txtnomeUsu;
    private TextView txtemailUsu;

    private DatabaseReference referenciaUsuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_lateral);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        navigationView.getMenu().getItem(0).setChecked(false);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.nav_home)
                {
                    Intent i = new Intent(MenuLateralActivity.this, VerAlertaUsuarioActivity.class);
                    startActivity(i);
                }
                if(item.getItemId() == R.id.nav_gallery){
                    Intent i = new Intent(MenuLateralActivity.this, VerDadosUsuarioActivity.class);
                    startActivity(i);
                }
                return true;
            }
        });



        inicializarComponentes(headerView);
        listarDadosUsuario();
    }

    public void inicializarComponentes(View headerView) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        txtnomeUsu = headerView.findViewById(R.id.txtnomeUsuHeader);
        txtemailUsu = headerView.findViewById(R.id.txtemailUsuHeader);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lateral, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_deslogar :
                deslogarUsuario();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void deslogarUsuario(){
        try{
            autenticacao.signOut();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void listarDadosUsuario(){
        if(autenticacao.getCurrentUser() != null){
            String email = autenticacao.getCurrentUser().getEmail();
            String id = Base64Custom.codificarBase64(email);

            referenciaUsuario = ConfiguracaoFirebase.getFirebase();
            referenciaUsuario = referenciaUsuario.child("usuarios").child(id);
            referenciaUsuario.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        usuario = snapshot.getValue(Usuario.class);
                        if(usuario != null){
                            txtnomeUsu.setText(usuario.getNome());
                            txtemailUsu.setText(usuario.getEmail());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    public void abrirTelaVerPadaria(View view){
        startActivity(new Intent(getApplicationContext(), VerPadariasMapaActivity.class));
    }

    public void abrirTelaCadastrarAlerta(View view){
        startActivity(new Intent(getApplicationContext(), CadastrarAlertaActivity.class));
    }
}