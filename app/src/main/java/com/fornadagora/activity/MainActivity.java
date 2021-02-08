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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText campoEmail;
    private EditText campoSenha;
    private Button botaoLogar;
    private ProgressBar progressBar;

    private Usuario usuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verificarUsuarioLogado();
        inicializarComponentes();

        progressBar.setVisibility(View.GONE);

        botaoLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if(!textoEmail.isEmpty()){
                    if(!textoSenha.isEmpty()){

                        usuario = new Usuario();
                        usuario.setEmail(textoEmail);
                        usuario.setSenha(textoSenha);

                        validarLogin(usuario);

                    }else{
                        Toast.makeText(MainActivity.this, "Preencha o campo senha", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Preencha o campo e-mail", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void abrirCadastro(View view){
        Intent i = new Intent(MainActivity.this, CadastroUsuarioActivity.class);
        startActivity(i);
    }

    public void abrirRecuperarSenha(View view){
        Intent i = new Intent(MainActivity.this, RecuperarSenhaActivity.class);
        startActivity(i);
    }

    public void inicializarComponentes(){
        campoEmail = findViewById(R.id.edit_text_recuperar_email);
        campoSenha = findViewById(R.id.edit_text_login_senha);
        progressBar = findViewById(R.id.progressLogin);
        botaoLogar = findViewById(R.id.btnRecuperarSenha);
    }

    public void verificarUsuarioLogado(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if(autenticacao.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MenuInicialActivity.class));
            finish();
        }
    }

    public void validarLogin(Usuario usuario){
        progressBar.setVisibility(View.VISIBLE);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(getApplicationContext(), MenuInicialActivity.class));
                    finish();
                }else{
                    Toast.makeText(MainActivity.this,"E-mail ou senha incorretos",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}