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
import com.fornadagora.model.Alerta;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Usuario;
import com.fornadagora.vo.AlertaVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private TextView textViewImgEsq1;


    private TextView textViewImgDir1;

    private FirebaseAuth autenticacao;
    private FirebaseUser user;

    private DatabaseReference referenciaFuncionario;
    private DatabaseReference referenciaUsuario;
    private DatabaseReference referenciaAlertaUsuario;

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
                if (item.getItemId() == R.id.nav_edit_dados_fun) {
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
                if (item.getItemId() == R.id.nav_excluir_conta) {
                    excluirConta();
                }
                if (item.getItemId() == R.id.nav_cadastrar_fun) {
                    Intent i = new Intent(MenuLateralActivity.this, CadastroFuncionarioActivity.class);
                    startActivity(i);
                }
                if (item.getItemId() == R.id.nav_adicionar_padaria) {
                    Intent i = new Intent(MenuLateralActivity.this, BuscarPadariaActivity.class);
                    startActivity(i);
                }
                if (item.getItemId() == R.id.nav_cadastrar_produto) {
                    Intent i = new Intent(MenuLateralActivity.this, CadastrarProdutoActivity.class);
                    startActivity(i);
                }
                if (item.getItemId() == R.id.nav_cadastrar_categoria) {
                    Intent i = new Intent(MenuLateralActivity.this, CadastrarCategoriaActivity.class);
                    startActivity(i);
                }
                if (item.getItemId() == R.id.nav_gerenciar_categorias) {
                    Intent i = new Intent(MenuLateralActivity.this, VerCategoriasActivity.class);
                    startActivity(i);
                }
                if (item.getItemId() == R.id.nav_gerenciar_produtos) {
                    Intent i = new Intent(MenuLateralActivity.this, VerProdutosActivity.class);
                    startActivity(i);
                }
                return true;
            }
        });

        getSupportActionBar().setTitle(R.string.menu);

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

        textViewImgEsq1 = findViewById(R.id.textViewImgEsq1);

        textViewImgDir1 = findViewById(R.id.textViewImgDir1);
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
                finish();
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

    public void recuperarParametro() {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            parametro = b.getString("parametro");
        }
    }

    public void configurarOpcoesNavigationView(NavigationView navigationView) {
        if (funcionario != null) {
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

            menuItem = menu.findItem(R.id.nav_cadastrar_produto);
            menuItem.setVisible(true);

            menuItem = menu.findItem(R.id.nav_cadastrar_categoria);
            menuItem.setVisible(false);

            menuItem = menu.findItem(R.id.nav_gerenciar_categorias);
            menuItem.setVisible(false);

            menuItem = menu.findItem(R.id.nav_gerenciar_produtos);
            menuItem.setVisible(true);

            menuItem = menu.findItem(R.id.nav_excluir_conta);
            menuItem.setVisible(true);

        } else if (usuario != null) {
            if (isEhAdm(usuario)) {
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

                menuItem = menu.findItem(R.id.nav_cadastrar_produto);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_cadastrar_categoria);
                menuItem.setVisible(true);

                menuItem = menu.findItem(R.id.nav_gerenciar_categorias);
                menuItem.setVisible(true);

                menuItem = menu.findItem(R.id.nav_gerenciar_produtos);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_excluir_conta);
                menuItem.setVisible(true);
            } else {
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

                menuItem = menu.findItem(R.id.nav_cadastrar_produto);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_cadastrar_categoria);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_gerenciar_categorias);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_gerenciar_produtos);
                menuItem.setVisible(false);

                menuItem = menu.findItem(R.id.nav_excluir_conta);
                menuItem.setVisible(true);
            }
        }
    }

    public boolean isEhAdm(Usuario usuario) {
        if (usuario.getTipoPerfil().equals("Administrador")) {
            ehAdm = true;
        }
        return ehAdm;
    }

    public void excluirConta() {
        if (usuario != null) {
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
        if (funcionario != null) {
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

    public void abrirDialog(final DataSnapshot dataSnap) {

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this, R.style.TemaDialog);
        materialAlertDialogBuilder.setTitle("Confirmar");
        materialAlertDialogBuilder.setMessage("Deseja realmente excluir sua conta?");

        materialAlertDialogBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (usuario != null) {
                    excluirAlertasAssociados(dataSnap);
                } else {
                    excluirFuncionario(dataSnap);
                }
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

    public void excluirAlertasAssociados(DataSnapshot snapUsuario) {
        Usuario usuario = snapUsuario.getValue(Usuario.class);
        buscarAlertasUsuarioBanco(usuario, snapUsuario);
    }

    public void buscarAlertasUsuarioBanco(final Usuario usuario, final DataSnapshot snapUsuario) {
        referenciaAlertaUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios").child(usuario.getIdUsuario());
        referenciaAlertaUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("listaAlertasVO").exists()) {
                        Map<String, AlertaVO> td = new HashMap<String, AlertaVO>();
                        for (DataSnapshot alertaSnapshot : snapshot.child("listaAlertasVO").getChildren()) {
                            AlertaVO alertaVO = alertaSnapshot.getValue(AlertaVO.class);
                            td.put(alertaSnapshot.getKey(), alertaVO);
                        }
                        ArrayList<AlertaVO> values = new ArrayList<>(td.values());
                        usuario.getListaAlertaVO().addAll(values);
                        if (!usuario.getListaAlertaVO().isEmpty()) {
                            List<AlertaVO> listaAlertaVO = new ArrayList<>();
                            listaAlertaVO.addAll(usuario.getListaAlertaVO());
                            exlcuirAlertasUsuario(listaAlertaVO, snapUsuario);
                        }
                    } else {
                        excluirUsuario(snapUsuario);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void exlcuirAlertasUsuario(final List<AlertaVO> alertaVOS, final DataSnapshot snapUsuario) {
        DatabaseReference referenciaAlerta = ConfiguracaoFirebase.getFirebase().child("alertas");
        referenciaAlerta.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapAlerta : snapshot.getChildren()) {
                        Alerta alertaBanco = snapAlerta.getValue(Alerta.class);
                        for (AlertaVO alertaVO : alertaVOS) {
                            if (alertaVO.getIdAlerta().equalsIgnoreCase(alertaBanco.getIdAlerta())) {
                                snapAlerta.getRef().removeValue();
                            }
                        }
                    }
                    excluirUsuario(snapUsuario);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void excluirUsuario(DataSnapshot dataSnap) {
        Usuario usuario = dataSnap.getValue(Usuario.class);
        reautenticarUsuario(usuario, dataSnap);
    }

    public void reautenticarUsuario(final Usuario usuario, final DataSnapshot dataSnap) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(usuario.getEmail(), usuario.getSenha());
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.delete();
                    dataSnap.getRef().removeValue();
                    deslogarUsuario();
                    finish();
                    abrirTelaLogin();
                }
            }
        });
    }

    public void excluirFuncionario(DataSnapshot dataSnap) {
        Funcionario funcionario = dataSnap.getValue(Funcionario.class);
        reautenticarFuncionario(funcionario, dataSnap);
    }

    public void reautenticarFuncionario(final Funcionario funcionario, final DataSnapshot dataSnap) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(funcionario.getEmail(), funcionario.getSenha());
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.delete();
                    dataSnap.getRef().removeValue();
                    deslogarUsuario();
                    finish();
                    abrirTelaLogin();
                }
            }
        });
    }


    public void abrirTelaLogin() {
        Intent i = new Intent(MenuLateralActivity.this, MainActivity.class);
        startActivity(i);
        Toast.makeText(getApplicationContext(), "Conta excluída com sucesso!", Toast.LENGTH_LONG).show();
    }

    public void configuraImagens() {
        if (usuario != null) {
            imagemEsq1.setImageResource(R.drawable.ic_mapa_padaria_48dp);
            textViewImgEsq1.setText("Ver Padarias");
            imagemDir1.setImageResource(R.drawable.ic_adicionar_alerta_48dp);
            textViewImgDir1.setText("Cadastrar Alerta");
            ehUsuario = true;
            configurarEventosClick();
        }
        if (funcionario != null) {
            imagemEsq1.setImageResource(R.drawable.ic_mapa_padaria_48dp);
            textViewImgEsq1.setText("Ver Padarias");
            imagemDir1.setImageResource(R.drawable.ic_alertar_usuario_48);
            textViewImgDir1.setText("Enviar Alerta");
            ehFuncionario = true;
            configurarEventosClick();
        }
    }

    public void configurarEventosClick() {
        imagemEsq1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirTelaVerPadaria(v);
            }
        });
        imagemDir1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ehUsuario) {
                    abrirTelaSalvarAlerta();
                }
                if (ehFuncionario) {
                    abrirTelaEnviarAlerta();
                }
            }
        });
    }

    public void abrirTelaSalvarAlerta() {
        Intent i = new Intent(MenuLateralActivity.this, CadastrarAlertaActivity.class);
        startActivity(i);
        finish();
    }

    public void abrirTelaEnviarAlerta() {
        Intent i = new Intent(MenuLateralActivity.this, AlertarUsuarioActivity.class);
        startActivity(i);
        finish();
    }
}