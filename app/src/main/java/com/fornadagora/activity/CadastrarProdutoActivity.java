package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Produto;
import com.fornadagora.vo.CategoriaVO;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CadastrarProdutoActivity extends AppCompatActivity {

    private TextInputEditText editTextNome;

    private AutoCompleteTextView autoCompleteCategoria;

    private Button botaoSalvar;

    private Toolbar toolbar;

    private DatabaseReference referenciaCategoria;
    private DatabaseReference referenciaProduto;

    private ArrayAdapter arrayAdapterCategoria;

    private ArrayList<String> listaNomeCategoria = new ArrayList<>();
    private ArrayList<Categoria> listaCategoria = new ArrayList<>();

    private boolean categoriaExiste = false;
    private boolean produtoExiste = false;

    private Categoria categoria;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_produto);
        inicializarComponentes();
        configurarToolbar();
        carregarCategorias();

        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvar();
            }
        });
    }

    public void inicializarComponentes(){
        editTextNome = findViewById(R.id.editTextNomeProduto);
        autoCompleteCategoria = findViewById(R.id.autoComleteCategoria);
        toolbar = findViewById(R.id.toolbarPrincipal);
        botaoSalvar = findViewById(R.id.btn_cadastrar_produto);
        context = this;
        referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
        referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos");
        arrayAdapterCategoria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomeCategoria);
    }

    public void configurarToolbar(){
        toolbar.setTitle("Cadastrar Produto");
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

    public void carregarCategorias(){
        referenciaCategoria.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snapCategoria : snapshot.getChildren()){
                        Categoria cat = snapCategoria.getValue(Categoria.class);
                        cat.setIdentificador(snapCategoria.getKey());
                        listaNomeCategoria.add(cat.getNome());
                        listaCategoria.add(cat);
                    }
                    autoCompleteCategoria.setAdapter(arrayAdapterCategoria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void salvar(){
        if(!editTextNome.getText().toString().isEmpty()){
            if(!autoCompleteCategoria.getText().toString().isEmpty()){
                String nomeProduto = editTextNome.getText().toString();
                String nomeCategoria = autoCompleteCategoria.getText().toString();
                if(validarCategoria(nomeCategoria)){
                   Categoria categ = buscarCategoria(nomeCategoria);
                   CategoriaVO categVO = new CategoriaVO(categ.getIdentificador());
                   Produto produto = new Produto(nomeProduto, categVO);
                   validarProduto(produto);
                }else{
                    Toast.makeText(this, "A categoria informada não existe",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Favor escolher uma categoria para o produto",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Favor informar no nome do produto",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validarCategoria(String nome){
        categoriaExiste = false;
        if(!listaCategoria.isEmpty()){
            for(Categoria cat : listaCategoria){
                if(cat.getNome().equalsIgnoreCase(nome)){
                    categoriaExiste = true;
                }
            }
        }
        return categoriaExiste;
    }

    public Categoria buscarCategoria(String nome){
        if(!listaCategoria.isEmpty()){
            for(Categoria cat : listaCategoria){
                if(cat.getNome().equalsIgnoreCase(nome)){
                    categoria = cat;
                }
            }
        }
        return categoria;
    }

    public void validarProduto(final Produto prod){
        produtoExiste = false;
        referenciaProduto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snapProduto : snapshot.getChildren()){
                        Produto produto = snapProduto.getValue(Produto.class);
                        if(produto.getNome().equalsIgnoreCase(prod.getNome())){
                            produtoExiste = true;
                            Toast.makeText(context, "Esse produto já foi salvo", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(!produtoExiste){
                        prod.salvar();
                        Toast.makeText(context, "Produto salvo com sucesso", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    prod.salvar();
                    Toast.makeText(context, "Produto salvo com sucesso", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}