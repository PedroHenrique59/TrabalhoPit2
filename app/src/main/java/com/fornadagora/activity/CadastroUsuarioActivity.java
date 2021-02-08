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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroUsuarioActivity extends AppCompatActivity {

    private EditText campoNome;
    private EditText campoEmail;
    private EditText campoSenha;

    private Button botaoCadastrar;
    private ProgressBar progressBar;

    private Usuario usuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_usuario);

        inicializarComponentes();

        progressBar.setVisibility(View.GONE);
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if(!textoNome.isEmpty()){
                    if(!textoEmail.isEmpty()){
                        if(!textoSenha.isEmpty()){

                            usuario = new Usuario();
                            usuario.setNome(textoNome);
                            usuario.setEmail(textoEmail);
                            usuario.setSenha(textoSenha);

                            cadastrar(usuario);

                        }else{
                            Toast.makeText(CadastroUsuarioActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT);
                        }
                    }else{
                        Toast.makeText(CadastroUsuarioActivity.this, "Preencha o e-mail!", Toast.LENGTH_SHORT);
                    }
                }else{
                    Toast.makeText(CadastroUsuarioActivity.this, "Preencha o nome!", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    public void inicializarComponentes(){

        campoNome = findViewById(R.id.edit_text_cadastro_nome);
        campoEmail = findViewById(R.id.edit_text_cadastro_email);
        campoSenha = findViewById(R.id.edit_text_cadastro_senha);

        botaoCadastrar = findViewById(R.id.btn_cadastrar);
        progressBar = findViewById(R.id.progressCadastro);
    }

    public void cadastrar(Usuario usuario){

        progressBar.setVisibility(View.VISIBLE);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(
                this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(CadastroUsuarioActivity.this, "Cadastrado com sucesso" , Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(getApplicationContext(), MenuInicialActivity.class));
                            finish();

                        }else{
                            progressBar.setVisibility(View.GONE);

                            String erroExcecao = "";
                            try{
                                throw task.getException();
                            }catch (FirebaseAuthWeakPasswordException e){
                             erroExcecao = "Digite uma senha mais forte!";
                            }catch(FirebaseAuthInvalidCredentialsException e){
                                erroExcecao = "Favor digitar um e-mail válido";
                            }catch (FirebaseAuthUserCollisionException e){
                                erroExcecao = "Esta conta já foi cadastrada";
                            }catch (Exception e){
                                erroExcecao = "ao cadastrar usuário: " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(CadastroUsuarioActivity.this, "Erro:" + erroExcecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
}
