package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;

public class CadastroUsuarioActivity extends AppCompatActivity {

    private TextInputEditText campoNome;
    private TextInputEditText campoEmail;
    private TextInputEditText campoSenha;
    private TextInputEditText campoConfirmarSenha;

    private Button botaoCadastrar;
    private ProgressBar progressBar;

    private Usuario usuario;

    private FirebaseAuth autenticacao;
    private DatabaseReference usuarios;

    private static String tipoPerfil = "Usuario";

    private String tokenUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_usuario);

        inicializarComponentes();
        recuperarToken();

        progressBar.setVisibility(View.GONE);
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();
                String textoConfirmarSenha = campoConfirmarSenha.getText().toString();

                if (!textoNome.isEmpty()) {
                    if (!textoEmail.isEmpty()) {
                        if (!textoSenha.isEmpty()) {
                            if (!textoConfirmarSenha.isEmpty()) {
                                if (textoConfirmarSenha.equals(textoSenha)) {
                                    usuario = new Usuario();
                                    usuario.setNome(textoNome);
                                    usuario.setEmail(textoEmail);
                                    usuario.setSenha(textoSenha);
                                    usuario.setTipoPerfil(tipoPerfil);
                                    usuario.setToken(tokenUsuario);
                                    cadastrar(usuario);
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
        });
    }

    public void inicializarComponentes() {

        campoNome = findViewById(R.id.edit_text_cadastro_nome);
        campoEmail = findViewById(R.id.edit_text_cadastro_email);
        campoSenha = findViewById(R.id.edit_text_cadastro_senha);
        campoConfirmarSenha = findViewById(R.id.edit_text_senha_confirmar);

        botaoCadastrar = findViewById(R.id.btn_cadastrar);
        progressBar = findViewById(R.id.progressCadastro);

        campoNome.requestFocus();
    }

    public void cadastrar(final Usuario usuario) {

        progressBar.setVisibility(View.VISIBLE);

        usuarios = ConfiguracaoFirebase.getFirebase().child("usuarios");

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(
                this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CadastroUsuarioActivity.this, "Cadastrado com sucesso", Toast.LENGTH_SHORT).show();

                            String idUsuario = autenticacao.getCurrentUser().getUid();
                            usuario.setIdUsuario(idUsuario);
                            usuario.salvar();

                            startActivity(new Intent(getApplicationContext(), MenuLateralActivity.class));
                            finish();

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
                                erroExcecao = "Esta conta já foi cadastrada";
                            } catch (Exception e) {
                                erroExcecao = "ao cadastrar usuário: " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(CadastroUsuarioActivity.this, "Erro:" + erroExcecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    public void recuperarToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                tokenUsuario = task.getResult();
            }
        });
    }
}
