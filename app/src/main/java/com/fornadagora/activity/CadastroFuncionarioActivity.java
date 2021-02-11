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
import com.fornadagora.helper.Base64Custom;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Funcionario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;

public class CadastroFuncionarioActivity extends AppCompatActivity {

    private EditText campoNome;
    private EditText campoEmail;
    private EditText campoSenha;
    private EditText campoConfirmarSenha;

    private Button botaoCadastrar;
    private ProgressBar progressBar;

    private Funcionario funcionario;

    private FirebaseAuth autenticacao;
    private DatabaseReference funcionarios;

    private static String tipoPerfil = "Funcionario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_funcionario);

        inicializarComponentes();

        progressBar.setVisibility(View.GONE);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();
                String textoConfirmarSenha = campoConfirmarSenha.getText().toString();

                if(!textoNome.isEmpty()){
                    if(!textoEmail.isEmpty()){
                        if(!textoSenha.isEmpty()){
                            if(!textoConfirmarSenha.isEmpty()){
                                if(textoConfirmarSenha.equals(textoSenha)){
                                    funcionario = new Funcionario();
                                    funcionario.setNome(textoNome);
                                    funcionario.setEmail(textoEmail);
                                    funcionario.setSenha(textoSenha);
                                    funcionario.setTipoPerfil(tipoPerfil);
                                    cadastrar(funcionario);
                                }else{
                                    Toast.makeText(CadastroFuncionarioActivity.this, "As senhas informadas não conferem!", Toast.LENGTH_SHORT).show();
                                    campoConfirmarSenha.setText("");
                                }
                            }else{
                                Toast.makeText(CadastroFuncionarioActivity.this, "Confirme a senha!", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(CadastroFuncionarioActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(CadastroFuncionarioActivity.this, "Preencha o e-mail!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CadastroFuncionarioActivity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void inicializarComponentes(){

        campoNome = findViewById(R.id.edit_text_cadastro_fun_nome);
        campoEmail = findViewById(R.id.edit_text_cadastro_fun_email);
        campoSenha = findViewById(R.id.edit_text_cadastro_fun_senha);
        campoConfirmarSenha = findViewById(R.id.edit_text_senha_fun_confirmar);

        botaoCadastrar = findViewById(R.id.btn_cadastrar_fun);
        progressBar = findViewById(R.id.progressCadastroFun);
    }

    public void cadastrar(final Funcionario funcionario){

        progressBar.setVisibility(View.VISIBLE);

        funcionarios = ConfiguracaoFirebase.getFirebase().child("funcionarios");

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                funcionario.getEmail(), funcionario.getSenha()
        ).addOnCompleteListener(
                this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CadastroFuncionarioActivity.this, "Cadastrado com sucesso" , Toast.LENGTH_SHORT).show();

                            String idFuncionario = Base64Custom.codificarBase64(funcionario.getEmail());
                            funcionario.setIdFuncionario(idFuncionario);
                            funcionario.salvar();

                            startActivity(new Intent(getApplicationContext(), MenuInicialAdminActivity.class));
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
                                erroExcecao = "ao cadastrar funcionário: " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(CadastroFuncionarioActivity.this, "Erro:" + erroExcecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
}
