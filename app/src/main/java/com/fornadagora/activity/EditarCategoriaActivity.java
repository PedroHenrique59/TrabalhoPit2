package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.Teclado;
import com.fornadagora.model.Categoria;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class EditarCategoriaActivity extends AppCompatActivity {

    private Categoria categoria;

    private Context context;

    private Toolbar toolbar;

    private TextInputEditText editTextNomeCategoria;

    private Button botaoSalvar;

    private DatabaseReference referenciaCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_categoria);
        categoria = getIntent().getExtras().getParcelable("categoriaObj");
        context = this;
        inicializarComponentes();
        configurarToolbar();
        preencherCamposComDadosCategoria(categoria);
        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Teclado.fecharTeclado(v);
                validarAntesSalvar();
            }
        });
    }

    public void inicializarComponentes() {
        context = this;
        toolbar = findViewById(R.id.toolbarPrincipal);
        editTextNomeCategoria = findViewById(R.id.editTextNomeCategoriaEdit);
        botaoSalvar = findViewById(R.id.btn_salvar_alt_categ);
    }

    public void configurarToolbar() {
        toolbar.setTitle("Editar Dados Categoria");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_voltar_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirMenuLateral();
            }
        });
    }

    public void abrirMenuLateral() {
        Intent intent = new Intent(this, MenuLateralActivity.class);
        startActivity(intent);
        finish();
    }

    public void preencherCamposComDadosCategoria(Categoria categoria) {
        editTextNomeCategoria.setText(categoria.getNome());
    }

    public void validarAntesSalvar() {
        if (!editTextNomeCategoria.getText().toString().isEmpty()) {
            if (!categoria.getNome().equalsIgnoreCase(editTextNomeCategoria.getText().toString())) {
                categoria.setNome(editTextNomeCategoria.getText().toString());
                salvarEdicao();
            }else{
                Toast.makeText(this, "O nome informado é o mesmo!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Favor informar o nome da categoria!", Toast.LENGTH_LONG).show();
        }
    }

    public void salvarEdicao(){
        referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias").child(categoria.getIdentificador());
        referenciaCategoria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    categoria.atualizarDados(categoria, referenciaCategoria);
                    Toast.makeText(context, "Categoria atualizada com sucesso!",Toast.LENGTH_LONG).show();
                    finish();
                    abrirTelaListarCategorias();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void abrirTelaListarCategorias(){
        Intent i = new Intent(EditarCategoriaActivity.this, VerCategoriasActivity.class);
        startActivity(i);
        finish();
    }
}