package com.fornadagora.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.ValidaEmail;
import com.fornadagora.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;

import org.mindrot.jbcrypt.BCrypt;

public class CadastroUsuarioActivity extends AppCompatActivity {

    private TextInputEditText campoNome;
    private TextInputEditText campoEmail;
    private TextInputEditText campoSenha;
    private TextInputEditText campoConfirmarSenha;

    private TextInputLayout layout_senha_usu;
    private TextInputLayout layout_confirmar_senha_usu;

    private Button botaoCadastrar;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private Usuario usuario;

    private Context context;

    private FirebaseAuth autenticacao;
    private DatabaseReference usuarios;

    private static String tipoPerfil = "Usuario";

    private String tokenUsuario;

    private boolean ehSenhaValida = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_usuario);

        inicializarComponentes();
        recuperarToken();
        configurarIconeVisualizarSenha();
        configurarToolbar();

        progressBar.setVisibility(View.GONE);
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarCamposAntesCadastrar();
            }
        });
    }

    public void inicializarComponentes() {

        campoNome = findViewById(R.id.edit_text_cadastro_nome);
        campoEmail = findViewById(R.id.edit_text_cadastro_email);
        campoSenha = findViewById(R.id.edit_text_cadastro_senha);
        campoConfirmarSenha = findViewById(R.id.edit_text_senha_confirmar);

        layout_senha_usu = findViewById(R.id.layout_senha_usu);
        layout_confirmar_senha_usu = findViewById(R.id.layout_confirmar_senha_usu);

        botaoCadastrar = findViewById(R.id.btn_cadastrar);
        progressBar = findViewById(R.id.progressCadastro);
        toolbar = findViewById(R.id.toolbarPrincipal);

        context = this;
        campoNome.requestFocus();
    }

    public void cadastrar(final Usuario usuario) {

        progressBar.setVisibility(View.VISIBLE);

        usuarios = ConfiguracaoFirebase.getFirebase().child("usuarios");
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        if (senhaValida()) {
            usuario.setSenha(hashPassword(usuario.getSenha()));
            autenticacao.createUserWithEmailAndPassword(
                    usuario.getEmail(), usuario.getSenha()
            ).addOnCompleteListener(
                    this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);

                                String idUsuario = autenticacao.getCurrentUser().getUid();
                                usuario.setIdUsuario(idUsuario);
                                usuario.salvar();

                                autenticacao.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(context, "Cadastro realizado com sucesso. Um e-mail com instruções para verificar o seu endereço de e-mail foi enviado.", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                String erroExcecao = "";
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    erroExcecao = "Digite uma senha mais forte!";
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    erroExcecao = "Favor digitar um e-mail válido";
                                } catch (FirebaseAuthUserCollisionException e) {
                                    erroExcecao = "Já existe uma conta cadastrada para esse endereço de e-mail!";
                                } catch (Exception e) {
                                    erroExcecao = "ao cadastrar usuário: " + e.getMessage();
                                    e.printStackTrace();
                                }
                                Toast.makeText(CadastroUsuarioActivity.this, "Erro:" + erroExcecao, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
            );
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }

    private boolean senhaValida() {
        ehSenhaValida = false;
        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";
        if (campoSenha.getText().toString().matches(pattern)) {
            ehSenhaValida = true;
        }else if(campoSenha.length() <8){
            Toast.makeText(this, "A senha deve ter no mínimo 8 caracteres.", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "A senha deve possuir letra(s) maiúscula(s)/minúscula(s), número(s) e caractere(s) especiai(s).", Toast.LENGTH_LONG).show();
        }
        return ehSenhaValida;
    }

    private String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public void recuperarToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                tokenUsuario = task.getResult();
            }
        });
    }

    public void configurarIconeVisualizarSenha() {
        layout_senha_usu.setEndIconDrawable(R.drawable.ic_visibility_off_24);
        layout_confirmar_senha_usu.setEndIconDrawable(R.drawable.ic_visibility_off_24);

        layout_senha_usu.setEndIconVisible(true);
        layout_confirmar_senha_usu.setEndIconVisible(true);

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

        layout_confirmar_senha_usu.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campoConfirmarSenha.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                    campoConfirmarSenha.setTransformationMethod(null);
                    layout_confirmar_senha_usu.setEndIconDrawable(R.drawable.ic_visibility_24);
                } else if (campoConfirmarSenha.getTransformationMethod() == null) {
                    campoConfirmarSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    layout_confirmar_senha_usu.setEndIconDrawable(R.drawable.ic_visibility_off_24);
                }
            }
        });
    }

    public boolean validarEmail(String email) {
        return ValidaEmail.validarEmail(email);
    }

    public void validarCamposAntesCadastrar() {

        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();
        String textoConfirmarSenha = campoConfirmarSenha.getText().toString();

        if (!textoNome.isEmpty()) {
            if (!textoEmail.isEmpty()) {
                if (!textoSenha.isEmpty()) {
                    if (!textoConfirmarSenha.isEmpty()) {
                        if (textoConfirmarSenha.equals(textoSenha)) {
                            if (validarEmail(textoEmail)) {
                                usuario = new Usuario(textoNome, textoEmail, textoSenha, tipoPerfil, tokenUsuario);
                                cadastrar(usuario);
                            } else {
                                Toast.makeText(CadastroUsuarioActivity.this, "Informe um e-mail válido", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CadastroUsuarioActivity.this, "As senhas informadas não conferem!", Toast.LENGTH_SHORT).show();
                            campoConfirmarSenha.setText("");
                        }
                    } else {
                        Toast.makeText(CadastroUsuarioActivity.this, "Confirme a senha!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CadastroUsuarioActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CadastroUsuarioActivity.this, "Preencha o e-mail!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CadastroUsuarioActivity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
        }
    }

    public void configurarToolbar() {
        toolbar.setTitle("Cadastre-se");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_voltar_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirTelaLogin();
            }
        });
    }

    public void abrirTelaLogin() {
        Intent i = new Intent(CadastroUsuarioActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
