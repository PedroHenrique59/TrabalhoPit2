package com.fornadagora.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Usuario;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class MenuLateralActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private Usuario usuario;

    private Funcionario funcionario;

    private TextView txtnome;
    private TextView txtemail;

    private ImageView imagemEsq1;
    private ImageView imagemEsq2;

    private ImageView imagemDir1;
    private ImageView imagemDir2;

    private FirebaseAuth autenticacao;

    private DatabaseReference referenciaFuncionario;
    private DatabaseReference referenciaUsuario;

    private String parametro = "";

    private NavigationView navigationView;

    private boolean ehAdm = false;
    private boolean ehUsuario = false;
    private boolean ehFuncionario = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_lateral);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

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
                if (item.getItemId() == R.id.nav_alertas_salvos_usu) {
                    Intent i = new Intent(MenuLateralActivity.this, VerAlertaUsuarioActivity.class);
                    startActivity(i);
                }
                if (item.getItemId() == R.id.nav_edit_dados_usu) {
                    Intent i = new Intent(MenuLateralActivity.this, VerDadosUsuarioActivity.class);
                    startActivity(i);
                }
                if(item.getItemId() == R.id.nav_edit_dados_fun){
                    Intent intent = new Intent(MenuLateralActivity.this, VerDadosFuncionarioActivity.class);
                    Bundle b = new Bundle();
                    b.putString("parametro", parametro);
                    intent.putExtras(b);
                    startActivity(intent);
                }
                if (item.getItemId() == R.id.nav_edit_adm_dados_fun) {
                    Intent i = new Intent(MenuLateralActivity.this, BuscarFuncionarioActivity.class);
                    startActivity(i);
                }
                if(item.getItemId() == R.id.nav_excluir_conta){
                    excluirConta();
                }
                if(item.getItemId() == R.id.nav_cadastrar_fun){
                    Intent i = new Intent(MenuLateralActivity.this, CadastroFuncionarioActivity.class);
                    startActivity(i);
                }
                if(item.getItemId() == R.id.nav_adicionar_padaria){
                    Intent i = new Intent(MenuLateralActivity.this, BuscarPadariaActivity.class);
                    startActivity(i);
                }
                return true;
            }
        });

        inicializarComponentes(headerView);
        listarDadosPerfilLogado();
        recuperarParametro();
    }

    public void inicializarComponentes(View headerView) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        txtnome = headerView.findViewById(R.id.txtnomeHeader);
        txtemail = headerView.findViewById(R.id.txtemailHeader);

        imagemEsq1 = findViewById(R.id.imageViewEsq1);
        imagemEsq2 = findViewById(R.id.imageViewEsq2);

        imagemDir1 = findViewById(R.id.imageViewDir1);
        imagemDir2 = findViewById(R.id.imageViewDir2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lateral, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_deslogar:
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

    private void deslogarUsuario() {
        try {
            autenticacao.signOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listarDadosPerfilLogado() {
        if (autenticacao.getCurrentUser() != null) {
            String id = autenticacao.getCurrentUser().getUid();

            referenciaUsuario = ConfiguracaoFirebase.getFirebase();
            referenciaUsuario = referenciaUsuario.child("usuarios").child(id);

            referenciaUsuario.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        usuario = snapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            txtnome.setText(usuario.getNome());
                            txtemail.setText(usuario.getEmail());
                            configurarOpcoesNavigationView(navigationView);
                            configuraImagens();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            referenciaFuncionario = ConfiguracaoFirebase.getFirebase();
            referenciaFuncionario = referenciaFuncionario.child("funcionarios").child(id);

            referenciaFuncionario.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        funcionario = snapshot.getValue(Funcionario.class);
                        if (funcionario != null) {
                            txtnome.setText(funcionario.getNome());
                            txtemail.setText(funcionario.getEmail());
                            configurarOpcoesNavigationView(navigationView);
                            configuraImagens();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void abrirTelaVerPadaria(View view) {
        startActivity(new Intent(getApplicationContext(), VerPadariasMapaActivity.class));
    }

    public void abrirTelaCadastrarAlerta(View view) {
        startActivity(new Intent(getApplicationContext(), CadastrarAlertaActivity.class));
    }

    public void recuperarParametro(){
        Bundle b = getIntent().getExtras();
        if(b != null){
            parametro = b.getString("parametro");
        }
    }

    public void configurarOpcoesNavigationView(NavigationView navigationView){
        if(funcionario != null){
            Menu menu = navigationView.getMenu();

            MenuItem menuItem = menu.findItem(R.id.nav_edit_dados_fun);
            menuItem.setVisible(true);

            menuItem = menu.findItem(R.id.nav_cadastrar_fun);
            menuItem.setVisible(false);

            menuItem = menu.findItem(R.id.nav_edit_dados_usu);
            menuItem.setVisible(false);

            menuItem = menu.findItem(R.id.nav_edit_adm_dados_fun);
            menuItem.setVisible(false);

            menuItem = menu.findItem(R.id.nav_alertas_salvos_usu);
            menuItem.setVisible(false);

            menuItem = menu.findItem(R.id.nav_adicionar_padaria);
            menuItem.setVisible(false);

            menuItem = menu.findItem(R.id.nav_excluir_conta);
            menuItem.setVisible(true);

        }else if(usuario != null){
            if(isEhAdm(usuario)){
                Menu menu = navigationView.getMenu();

                MenuItem menuItem = menu.findItem(R.id.nav_edit_adm_dados_fun);
                menuItem.setVisible(true);

                menuItem = menu.findItem(R.id.nav_cadastrar_fun);
                menuItem.setVisible(true);

                menuItem = menu.findItem(R.id.nav_alertas_salvos_usu);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_edit_dados_usu);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_edit_dados_fun);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_adicionar_padaria);
                menuItem.setVisible(true);

                menuItem = menu.findItem(R.id.nav_excluir_conta);
                menuItem.setVisible(true);
            }else{
                Menu menu = navigationView.getMenu();

                MenuItem menuItem = menu.findItem(R.id.nav_edit_dados_usu);
                menuItem.setVisible(true);

                menuItem = menu.findItem(R.id.nav_cadastrar_fun);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_edit_dados_fun);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_edit_adm_dados_fun);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_adicionar_padaria);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_excluir_conta);
                menuItem.setVisible(true);
            }
        }
    }

    public boolean isEhAdm(Usuario usuario){
        if(usuario.getTipoPerfil().equals("Administrador")){
            ehAdm = true;
        }
        return ehAdm;
    }

    public void excluirConta(){
        if(usuario != null){
            referenciaUsuario.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        usuario = snapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            abrirDialog(snapshot);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        if(funcionario != null){
            referenciaFuncionario.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        funcionario = snapshot.getValue(Funcionario.class);
                        if (funcionario != null) {
                            abrirDialog(snapshot);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void abrirDialog(final DataSnapshot dataSnap){

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this, R.style.TemaDialog);
        materialAlertDialogBuilder.setTitle("Confirmar");
        materialAlertDialogBuilder.setMessage("Deseja realmente excluir sua conta?");

        materialAlertDialogBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataSnap.getRef().removeValue();
                autenticacao.getCurrentUser().delete();
                deslogarUsuario();
                abrirTelaLogin();
            }
        });

        materialAlertDialogBuilder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        materialAlertDialogBuilder.create();
        materialAlertDialogBuilder.show();
    }

    public void abrirTelaLogin() {
        Intent i = new Intent(MenuLateralActivity.this, MainActivity.class);
        startActivity(i);
        Toast.makeText(getApplicationContext(), "Conta excluída com sucesso", Toast.LENGTH_LONG).show();
    }

    public void configuraImagens(){
        if(usuario != null){
            imagemEsq1.setImageResource(R.drawable.ic_mapa_padaria_48dp);
            imagemDir1.setImageResource(R.drawable.ic_adicionar_alerta_48dp);
            ehUsuario = true;
            configurarEventosClick();
        }
        if(funcionario != null){
            imagemEsq1.setImageResource(R.drawable.ic_mapa_padaria_48dp);
            imagemDir1.setImageResource(R.drawable.ic_alertar_usuario_48);
            ehFuncionario = true;
            configurarEventosClick();
        }
    }

    public void configurarEventosClick(){
        imagemEsq1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirTelaVerPadaria(v);
            }
        });
        imagemDir1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ehUsuario){
                    abrirTelaSalvarAlerta();
                }
                if(ehFuncionario){
                    abrirTelaEnviarAlerta();
                }
            }
        });
    }

    public void abrirTelaSalvarAlerta(){
        Intent i = new Intent(MenuLateralActivity.this, CadastrarAlertaActivity.class);
        startActivity(i);
    }

    public void abrirTelaEnviarAlerta(){
        Intent i = new Intent(MenuLateralActivity.this, AlertarUsuarioActivity.class);
        startActivity(i);
    }
}