package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Categoria;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CadastrarCategoriaActivity extends AppCompatActivity {

    private TextInputEditText editTextNomeCategoria;

    private Button botaoSalvar;

    private Toolbar toolbar;

    private Categoria categoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_categoria);
        inicializarComponentes();
        configurarToolbar();

        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvar();
            }
        });
    }

    public void inicializarComponentes(){
        editTextNomeCategoria = findViewById(R.id.editTextNomeCategoria);
        toolbar = findViewById(R.id.toolbarPrincipal);
        botaoSalvar = findViewById(R.id.btn_cadastrar_categ);
    }

    public void configurarToolbar(){
        toolbar.setTitle("Cadastrar Categoria");
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

    public void salvar(){
        if(!editTextNomeCategoria.getText().toString().isEmpty()){
            String nomeCategoria = editTextNomeCategoria.getText().toString();
            categoria = new Categoria(nomeCategoria);
            categoria.salvar();
        }else{
            Toast.makeText(this, "Favor preencher o nome da categoria", Toast.LENGTH_SHORT).show();
        }
    }
}