package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ViewUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarSenhaActivity extends AppCompatActivity {

    private TextInputEditText campoEmail;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);
        inicializarComponentes();
    }

    public void inicializarComponentes() {
        campoEmail = findViewById(R.id.edit_text_recuperar_email);
    }

    public void enviarEmailRecuperarSenha(View view) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        if (!campoEmail.getText().toString().isEmpty()) {
            autenticacao.sendPasswordResetEmail(campoEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                campoEmail.setText("");
                                Toast.makeText(RecuperarSenhaActivity.this, "E-mail enviado com sucesso", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RecuperarSenhaActivity.this, "Falha ao enviar o e-mail", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(RecuperarSenhaActivity.this, "Preencha o e-mail", Toast.LENGTH_SHORT).show();
        }
    }
}
