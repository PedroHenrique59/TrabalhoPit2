package com.fornadagora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.Teclado;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecuperarSenhaActivity extends AppCompatActivity {

    private TextInputEditText campoEmail;

    private Toolbar toolbar;

    private FirebaseAuth autenticacao;

    private Query queryUsuario;
    private Query queryFuncionario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);
        inicializarComponentes();
        configurarToolbar();
    }

    public void inicializarComponentes() {
        campoEmail = findViewById(R.id.edit_text_recuperar_email);
        toolbar = findViewById(R.id.toolbarPrincipal);
    }

    public void enviarEmailRecuperarSenha(View view) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if (!campoEmail.getText().toString().isEmpty()) {
            Teclado.fecharTeclado(view);
            validarExisteConta(campoEmail.getText().toString());
        } else {
            Toast.makeText(RecuperarSenhaActivity.this, "Preencha o campo de e-mail", Toast.LENGTH_SHORT).show();
        }
    }

    public void configurarToolbar() {
        toolbar.setTitle("Recuperar senha");
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
        Intent i = new Intent(RecuperarSenhaActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void validarExisteConta(final String email) {
        queryUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios").orderByChild("email").equalTo(email);
        queryUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    autenticacao.sendPasswordResetEmail(campoEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        campoEmail.setText("");
                                        Toast.makeText(RecuperarSenhaActivity.this, "E-mail enviado com sucesso", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(RecuperarSenhaActivity.this, "Falha ao enviar o e-mail", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    queryFuncionario = ConfiguracaoFirebase.getFirebase().child("funcionarios").orderByChild("email").equalTo(email);
                    queryFuncionario.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                autenticacao.sendPasswordResetEmail(campoEmail.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    campoEmail.setText("");
                                                    Toast.makeText(RecuperarSenhaActivity.this, "E-mail enviado com sucesso", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(RecuperarSenhaActivity.this, "Falha ao enviar o e-mail", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(RecuperarSenhaActivity.this, "Não existe nenhuma conta cadastrada para o endereço de e-mail informado.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
