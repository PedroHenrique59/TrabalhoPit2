package com.fornadagora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.Teclado;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Usuario;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

public class MainActivity extends AppCompatActivity {

    private EditText campoEmail;
    private EditText campoSenha;
    private Button botaoLogar;
    private TextInputLayout layout_senha_usu;

    private ProgressBar progressBar;

    private Usuario usuarioLogin;

    private Usuario usuarioRecuperado;
    private Funcionario funcionarioRecuperado;

    private boolean ehAdministrador;
    private boolean existeUsuario = false;
    private boolean existeFuncionario = false;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verificarUsuarioLogado();
    }

    public void configurarTelaParaLogin() {
        setContentView(R.layout.activity_main);
        inicializarComponentes();
        progressBar.setVisibility(View.GONE);
        botaoLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Teclado.fecharTeclado(view);

                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if (!textoEmail.isEmpty()) {
                    if (!textoSenha.isEmpty()) {

                        usuarioLogin = new Usuario();
                        usuarioLogin.setEmail(textoEmail);
                        usuarioLogin.setSenha(textoSenha);

                        obterUsuarioBd();

                    } else {
                        Toast.makeText(MainActivity.this, "Preencha o campo senha", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Preencha o campo e-mail", Toast.LENGTH_SHORT).show();
                }
            }
        });

        configurarIconeVisualizarSenha();
    }

    public void obterUsuarioBd() {
        DatabaseReference referenciaUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios");
        DatabaseReference referenciaFuncionario = ConfiguracaoFirebase.getFirebase().child("funcionarios");

        existeUsuario = false;
        existeFuncionario = false;

        referenciaUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapUsuario : snapshot.getChildren()) {
                        if (snapUsuario.child("email").getValue().equals(usuarioLogin.getEmail())) {
                            String senha = snapUsuario.child("senha").getValue().toString();
                            validarSenha(usuarioLogin.getSenha(), senha);
                            existeUsuario = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenciaFuncionario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapFuncionario : snapshot.getChildren()) {
                        if (snapFuncionario.child("email").getValue().equals(usuarioLogin.getEmail())) {
                            String senha = snapFuncionario.child("senha").getValue().toString();
                            validarSenha(usuarioLogin.getSenha(), senha);
                            existeFuncionario = true;
                        }
                    }
                    if (!existeUsuario && !existeFuncionario) {
                        Toast.makeText(MainActivity.this, "Nenhuma conta cadastrada para o endereço de e-mail informado!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void validarSenha(String senhaInformada, String senhaBanco) {
        if (BCrypt.checkpw(senhaInformada, senhaBanco)) {
            usuarioLogin.setSenha(senhaBanco);
            validarLogin(usuarioLogin);
        } else {
            autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
            autenticacao.signInWithEmailAndPassword(usuarioLogin.getEmail(), usuarioLogin.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if (autenticacao.getCurrentUser().isEmailVerified()) {
                            atualizarSenhaAuth();
                        } else {
                            Toast.makeText(MainActivity.this, "Favor verificar seu endereço de e-mail para efetuar o login.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "E-mail ou senha incorreto(s)!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void atualizarSenhaAuth(){
        final String senhaAuth = hashPassword(usuarioLogin.getSenha());
        autenticacao.getCurrentUser().updatePassword(senhaAuth).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                atualizarSenhaBD(senhaAuth);
                Log.i("senhaauth",senhaAuth);
            }
        });
    }

    private void atualizarSenhaBD(final String senhaAuth) {
        final DatabaseReference referenciaUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios").child(autenticacao.getCurrentUser().getUid());
        referenciaUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    referenciaUsuario.child("senha").setValue(senhaAuth);
                    Log.i("senhabanco", senhaAuth);
                    recuperarTipoDeUsuarioLogado();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference referenciaFuncionario = ConfiguracaoFirebase.getFirebase().child("funcionarios").child(autenticacao.getCurrentUser().getUid());
        referenciaFuncionario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    referenciaFuncionario.child("senha").setValue(senhaAuth);
                    Log.i("senhabanco", senhaAuth);
                    recuperarTipoDeUsuarioLogado();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public void abrirCadastro(View view) {
        Intent i = new Intent(MainActivity.this, CadastroUsuarioActivity.class);
        startActivity(i);
    }

    public void abrirRecuperarSenha(View view) {
        Intent i = new Intent(MainActivity.this, RecuperarSenhaActivity.class);
        startActivity(i);
    }

    public void inicializarComponentes() {
        campoEmail = findViewById(R.id.edit_text_login_email);
        campoSenha = findViewById(R.id.edit_text_login_senha);
        progressBar = findViewById(R.id.progressLogin);
        botaoLogar = findViewById(R.id.btnRecuperarSenha);
        layout_senha_usu = findViewById(R.id.layout_senha_usu_login);
    }

    public void verificarUsuarioLogado() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if (autenticacao.getCurrentUser() != null) {
            if (autenticacao.getCurrentUser().isEmailVerified()) {
                String id = autenticacao.getCurrentUser().getUid();

                DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
                DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(id);

                final DatabaseReference funcionarioRef = firebaseRef.child("funcionarios").child(id);
                usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usuarioRecuperado = snapshot.getValue(Usuario.class);
                        if (usuarioRecuperado != null) {
                            validarPerfilUsuario(usuarioRecuperado);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                funcionarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        funcionarioRecuperado = snapshot.getValue(Funcionario.class);
                        if (funcionarioRecuperado != null) {
                            validarPerfilFuncionario(funcionarioRecuperado);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                configurarTelaParaLogin();
            }
        } else {
            configurarTelaParaLogin();
        }
    }

    public void validarLogin(Usuario usuarioLogin) {
        progressBar.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(usuarioLogin.getEmail(), usuarioLogin.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    if (autenticacao.getCurrentUser().isEmailVerified()) {
                        recuperarTipoDeUsuarioLogado();
                    } else {
                        Toast.makeText(MainActivity.this, "Favor verificar seu endereço de e-mail para efetuar o login.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    public void recuperarTipoDeUsuarioLogado() {
        usuarioRecuperado = new Usuario();
        funcionarioRecuperado = new Funcionario();

        String id = autenticacao.getCurrentUser().getUid();

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(id);
        DatabaseReference funcionarioRef = firebaseRef.child("funcionarios").child(id);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuarioRecuperado = snapshot.getValue(Usuario.class);
                if (usuarioRecuperado != null) {
                    validarPerfilUsuario(usuarioRecuperado);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        funcionarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Funcionario funcionarioRecuperado = snapshot.getValue(Funcionario.class);
                if (funcionarioRecuperado != null) {
                    validarPerfilFuncionario(funcionarioRecuperado);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean validarPerfilUsuario(Usuario usuario) {
        if (usuario.getTipoPerfil().equals("Administrador")) {
            ehAdministrador = true;
        } else {
            ehAdministrador = false;
        }
        if (autenticacao.getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(getApplicationContext(), MenuLateralActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            Toast.makeText(MainActivity.this, "Favor verificar o endereço de e-mail antes efetuar o login.", Toast.LENGTH_LONG).show();
        }
        return ehAdministrador;
    }

    public void validarPerfilFuncionario(Funcionario funcionario) {
        if (funcionario.getTipoPerfil().equals("Funcionario")) {
            if (autenticacao.getCurrentUser().isEmailVerified()) {
                abriMenuLateralFuncionario(funcionario);
            } else {
                Toast.makeText(MainActivity.this, "Favor verificar o endereço de e-mail antes de efetuar o login.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void configurarIconeVisualizarSenha() {
        layout_senha_usu.setEndIconDrawable(R.drawable.ic_visibility_off_24);
        layout_senha_usu.setEndIconVisible(true);
        layout_senha_usu.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campoSenha.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                    campoSenha.setTransformationMethod(null);
                    layout_senha_usu.setEndIconDrawable(R.drawable.ic_visibility_24);
                } else if (campoSenha.getTransformationMethod() == null) {
                    campoSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    layout_senha_usu.setEndIconDrawable(R.drawable.ic_visibility_off_24);
                }
            }
        });
    }

    public void abriMenuLateralFuncionario(Funcionario funcionario) {
        Intent intent = new Intent(getApplicationContext(), MenuLateralActivity.class);
        Bundle b = new Bundle();
        b.putString("parametro", funcionario.getSenha());
        intent.putExtras(b);
        startActivity(intent);
        finish();
    }
}