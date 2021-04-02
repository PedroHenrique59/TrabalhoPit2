package com.fornadagora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.ValidaEmail;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CadastroFuncionarioActivity extends AppCompatActivity {

    private EditText campoNome;
    private EditText campoEmail;
    private EditText campoSenha;
    private EditText campoConfirmarSenha;

    private Button botaoCadastrar;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private AutoCompleteTextView autoComplete;

    private ArrayAdapter arrayAdapterPadaria;
    private List<String> listaNomePadaria = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();

    private Funcionario funcionario;

    private FirebaseAuth autenticacao;
    private DatabaseReference referenciaFuncionarios;
    private DatabaseReference referenciaPadarias;

    private static String tipoPerfil = "Funcionario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_funcionario);

        inicializarComponentes();
        configurarToolbar();
        carregarListaPadarias();

        progressBar.setVisibility(View.GONE);
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarAntesSalvar();
            }
        });
    }

    public void inicializarComponentes() {

        campoNome = findViewById(R.id.edit_text_cadastro_fun_nome);
        campoEmail = findViewById(R.id.edit_text_cadastro_fun_email);
        campoSenha = findViewById(R.id.edit_text_cadastro_fun_senha);
        campoConfirmarSenha = findViewById(R.id.edit_text_senha_fun_confirmar);

        botaoCadastrar = findViewById(R.id.btn_cadastrar_fun);
        progressBar = findViewById(R.id.progressCadastroFun);
        toolbar = findViewById(R.id.toolbarPrincipal);

        autoComplete = findViewById(R.id.autoComletePadariaFun);
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomePadaria);
    }

    public void salvarFuncionario(final Funcionario funcionario) {

        progressBar.setVisibility(View.VISIBLE);

        referenciaFuncionarios = ConfiguracaoFirebase.getFirebase().child("funcionarios");

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                funcionario.getEmail(), funcionario.getSenha()
        ).addOnCompleteListener(
                this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CadastroFuncionarioActivity.this, "Cadastrado com sucesso", Toast.LENGTH_SHORT).show();

                            String idFuncionario = autenticacao.getCurrentUser().getUid();
                            funcionario.setIdFuncionario(idFuncionario);
                            funcionario.salvar();

                            startActivity(new Intent(getApplicationContext(), MenuInicialAdminActivity.class));
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
                                erroExcecao = "ao cadastrar funcionário: " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(CadastroFuncionarioActivity.this, "Erro:" + erroExcecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    public void carregarListaPadarias() {
        referenciaPadarias = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaPadarias.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapShotPadaria : snapshot.getChildren()) {
                        Padaria padaria = snapShotPadaria.getValue(Padaria.class);
                        listaPadarias.add(padaria);
                        listaNomePadaria.add(padaria.getNome());
                    }
                    autoComplete.setAdapter(arrayAdapterPadaria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void validarAntesSalvar() {

        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();
        String textoConfirmarSenha = campoConfirmarSenha.getText().toString();
        String nomePadaria = autoComplete.getEditableText().toString();

        if (!textoNome.isEmpty()) {
            if (!textoEmail.isEmpty()) {
                if (!textoSenha.isEmpty()) {
                    if (!textoConfirmarSenha.isEmpty()) {
                        if (!nomePadaria.isEmpty()) {
                            if (textoConfirmarSenha.equals(textoSenha)) {
                                if(validarEmail(textoEmail)){
                                    funcionario = new Funcionario(textoNome, textoEmail, textoSenha, tipoPerfil);
                                    if (!listaPadarias.isEmpty()) {
                                        for (Padaria padaria : listaPadarias) {
                                            if (nomePadaria.equals(padaria.getNome())) {
                                                funcionario.setPadaria(padaria);
                                            }
                                        }
                                    }
                                    salvarFuncionario(funcionario);
                                }else{
                                    Toast.makeText(CadastroFuncionarioActivity.this, "Informe um e-mail válido", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(CadastroFuncionarioActivity.this, "As senhas informadas não conferem!", Toast.LENGTH_SHORT).show();
                                campoConfirmarSenha.setText("");
                            }
                        } else {
                            Toast.makeText(CadastroFuncionarioActivity.this, "Escolha uma padaria", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CadastroFuncionarioActivity.this, "Confirme a senha!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CadastroFuncionarioActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CadastroFuncionarioActivity.this, "Preencha o e-mail!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CadastroFuncionarioActivity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validarEmail(String email){
        return ValidaEmail.validarEmail(email);
    }

    public void configurarToolbar(){
        toolbar.setTitle("Cadastrar um funcionário");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_voltar_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirMenuLateral();
            }
        });
    }

    public void abrirMenuLateral(){
        Intent intent = new Intent(this, MenuLateralActivity.class);
        startActivity(intent);
        finish();
    }
}
