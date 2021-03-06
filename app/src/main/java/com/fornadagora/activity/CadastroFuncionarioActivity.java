package com.fornadagora.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
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
import com.fornadagora.model.Usuario;
import com.fornadagora.vo.PadariaVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class CadastroFuncionarioActivity extends AppCompatActivity {

    private EditText campoNome;
    private EditText campoEmail;
    private EditText campoSenha;
    private EditText campoConfirmarSenha;

    private TextInputLayout layout_senha_fun;
    private TextInputLayout layout_confirmar_senha_fun;

    private Button botaoCadastrar;

    private ProgressBar progressBar;

    private Toolbar toolbar;

    private AutoCompleteTextView autoComplete;

    private ArrayAdapter arrayAdapterPadaria;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();

    private Funcionario funcionario;
    private PadariaVO padariaVO;
    private Usuario usuarioAdm;

    private FirebaseAuth autenticacao;
    private FirebaseAuth autenticacaoAdm;

    private DatabaseReference referenciaFuncionarios;
    private DatabaseReference referenciaPadarias;

    private static String tipoPerfil = "Funcionario";

    private String nomePadaria;

    private boolean padariaValida = false;
    private boolean ehSenhaValida = false;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_funcionario);

        inicializarComponentes();
        configurarIconeVisualizarSenha();
        configurarToolbar();
        carregarListaPadarias();
        recuperarDadosAdm();

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

        layout_senha_fun = findViewById(R.id.layout_senha_fun);
        layout_confirmar_senha_fun = findViewById(R.id.layout_confirmar_senha_fun);

        botaoCadastrar = findViewById(R.id.btn_cadastrar_fun);
        progressBar = findViewById(R.id.progressCadastroFun);
        toolbar = findViewById(R.id.toolbarPrincipal);

        autoComplete = findViewById(R.id.autoComletePadariaFun);
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomePadaria);

        context = this;
    }

    public void salvarFuncionario(final Funcionario funcionario) {

        progressBar.setVisibility(View.VISIBLE);

        referenciaFuncionarios = ConfiguracaoFirebase.getFirebase().child("funcionarios");

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        if (senhaValida()) {
            funcionario.setSenha(hashPassword(funcionario.getSenha()));
            autenticacao.createUserWithEmailAndPassword(
                    funcionario.getEmail(), funcionario.getSenha()).addOnCompleteListener(
                    this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);

                                String idFuncionario = autenticacao.getCurrentUser().getUid();
                                funcionario.setIdFuncionario(idFuncionario);
                                funcionario.salvar();

                                Toast.makeText(context, "Cadastrado com sucesso!", Toast.LENGTH_LONG).show();

                                autenticacao.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        limparCampos();
                                        logarAdm();
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
                                    erroExcecao = "ao cadastrar funcionário: " + e.getMessage();
                                    e.printStackTrace();
                                }
                                Toast.makeText(CadastroFuncionarioActivity.this, "Erro: " + erroExcecao, Toast.LENGTH_SHORT).show();
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
        } else if (campoSenha.length() < 8) {
            Toast.makeText(this, "A senha deve ter no mínimo 8 caracteres.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "A senha deve possuir letra(s) maiúscula(s)/minúscula(s), número(s) e caractere(s) especiai(s).", Toast.LENGTH_LONG).show();
        }
        return ehSenhaValida;
    }

    private String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public void carregarListaPadarias() {
        referenciaPadarias = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaPadarias.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapShotPadaria : snapshot.getChildren()) {
                        Padaria padaria = snapShotPadaria.getValue(Padaria.class);
                        padaria.setIdentificador(snapShotPadaria.getKey());
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
        nomePadaria = autoComplete.getText().toString();

        if (!textoNome.isEmpty()) {
            if (!textoEmail.isEmpty()) {
                if (!textoSenha.isEmpty()) {
                    if (!textoConfirmarSenha.isEmpty()) {
                        if (!nomePadaria.isEmpty()) {
                            if (textoConfirmarSenha.equals(textoSenha)) {
                                if (validarEmail(textoEmail)) {
                                    funcionario = new Funcionario(textoNome, textoEmail, textoSenha, tipoPerfil);
                                    if (!listaPadarias.isEmpty()) {
                                        padariaValida = false;
                                        validarPadaria(nomePadaria);
                                        if (padariaValida) {
                                            salvarFuncionario(funcionario);
                                        } else {
                                            Toast.makeText(CadastroFuncionarioActivity.this, "Informe uma padaria válida!", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(CadastroFuncionarioActivity.this, "Informe uma padaria válida!", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(CadastroFuncionarioActivity.this, "Informe um e-mail válido!", Toast.LENGTH_SHORT).show();
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

    public boolean validarEmail(String email) {
        return ValidaEmail.validarEmail(email);
    }

    public void configurarToolbar() {
        toolbar.setTitle("Cadastrar Funcionário");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_voltar_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void validarPadaria(String nomePadaria) {
        for (Padaria padaria : listaPadarias) {
            if (nomePadaria.equals(padaria.getNome())) {
                padariaVO = new PadariaVO(padaria.getIdentificador());
                funcionario.setPadariaVO(padariaVO);
                padariaValida = true;
            }
        }
    }

    public void configurarIconeVisualizarSenha() {
        layout_senha_fun.setEndIconDrawable(R.drawable.ic_visibility_off_24);
        layout_confirmar_senha_fun.setEndIconDrawable(R.drawable.ic_visibility_off_24);

        layout_senha_fun.setEndIconVisible(true);
        layout_confirmar_senha_fun.setEndIconVisible(true);

        layout_senha_fun.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campoSenha.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                    campoSenha.setTransformationMethod(null);
                    layout_senha_fun.setEndIconDrawable(R.drawable.ic_visibility_24);
                } else if (campoSenha.getTransformationMethod() == null) {
                    campoSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    layout_senha_fun.setEndIconDrawable(R.drawable.ic_visibility_off_24);
                }
            }
        });

        layout_confirmar_senha_fun.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campoConfirmarSenha.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                    campoConfirmarSenha.setTransformationMethod(null);
                    layout_confirmar_senha_fun.setEndIconDrawable(R.drawable.ic_visibility_24);
                } else if (campoConfirmarSenha.getTransformationMethod() == null) {
                    campoConfirmarSenha.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    layout_confirmar_senha_fun.setEndIconDrawable(R.drawable.ic_visibility_off_24);
                }
            }
        });
    }

    public void limparCampos() {
        campoNome.setText("");
        campoEmail.setText("");
        campoSenha.setText("");
        campoConfirmarSenha.setText("");
        campoNome.requestFocus();
    }

    public void recuperarDadosAdm() {
        autenticacaoAdm = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String id = autenticacaoAdm.getCurrentUser().getUid();
        Query queryAdm = ConfiguracaoFirebase.getFirebase().child("usuarios").child(id);
        queryAdm.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    usuarioAdm = usuario;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void logarAdm() {
        autenticacaoAdm.signInWithEmailAndPassword(usuarioAdm.getEmail(), usuarioAdm.getSenha());
    }
}
