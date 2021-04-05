package com.fornadagora.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.fornadagora.R;
import com.google.android.material.textfield.TextInputEditText;

public class CadastrarCategoriaActivity extends AppCompatActivity {

    private TextInputEditText editTextNomeCategoria;

    private Button botaoSalvar;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_categoria);
        inicializarComponentes();
        configurarToolbar();
    }

    public void inicializarComponentes(){
        editTextNomeCategoria = findViewById(R.id.editTextNomeCategoria);
        toolbar = findViewById(R.id.toolbarPrincipal);
        botaoSalvar = findViewById(R.id.btn_salvar);
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
}