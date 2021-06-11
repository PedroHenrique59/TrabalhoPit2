package com.fornadagora.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.Teclado;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Produto;
import com.fornadagora.vo.AlertaVO;
import com.fornadagora.vo.CategoriaVO;
import com.fornadagora.vo.ProdutoVO;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditarProdutoActivity extends AppCompatActivity {

    private Produto produto;

    private Categoria categoriaProduto;

    private Context context;

    private Toolbar toolbar;

    private TextInputEditText editTextnomeProduto;

    private AutoCompleteTextView autoCompleteCategoria;

    private Button botaoSalvar;

    private ArrayAdapter arrayAdapterCategoria;

    private List<String> listaNomeCategoria = new ArrayList<>();

    private List<Categoria> listaCategorias = new ArrayList<>();

    private boolean categoriaValida = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_produto);
        produto = getIntent().getExtras().getParcelable("produtoObj");
        context = this;
        inicializarComponentes();
        configurarToolbar();
        buscarCategoriaProduto();
        buscarCategorias();
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
        editTextnomeProduto = findViewById(R.id.editTextNomeProdutoEdit);
        autoCompleteCategoria = findViewById(R.id.autoComleteCategoriaEdit);
        botaoSalvar = findViewById(R.id.btn_salvar_alt_produto);
        arrayAdapterCategoria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomeCategoria);
    }

    public void configurarToolbar() {
        toolbar.setTitle("Editar Dados");
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

    public void buscarCategoriaProduto() {
        Query queryCategoria = ConfiguracaoFirebase.getFirebase().child("categorias").child(produto.getCategoriaVO().getIdentificador());
        queryCategoria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    categoriaProduto = snapshot.getValue(Categoria.class);
                }
                preencherCamposComDadosProduto(produto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void preencherCamposComDadosProduto(Produto produto) {
        editTextnomeProduto.setText(produto.getNome());
        autoCompleteCategoria.setText(categoriaProduto.getNome());
    }

    public void validarAntesSalvar() {
        if (!editTextnomeProduto.getText().toString().isEmpty()) {
            if (!autoCompleteCategoria.getText().toString().isEmpty()) {
                produto.setNome(editTextnomeProduto.getText().toString());
                atualizarCategoriaEscolhida();
            } else {
                Toast.makeText(this, "Favor informar a categoria!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Favor informar o nome do produto!", Toast.LENGTH_LONG).show();
        }
    }

    public void buscarCategorias() {
        Query queryCategorias = ConfiguracaoFirebase.getFirebase().child("categorias");
        queryCategorias.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listaCategorias.clear();
                    listaNomeCategoria.clear();
                    for (DataSnapshot snapCategoria : snapshot.getChildren()) {
                        Categoria categoria = snapCategoria.getValue(Categoria.class);
                        listaCategorias.add(categoria);
                        listaNomeCategoria.add(categoria.getNome());
                    }
                    autoCompleteCategoria.setAdapter(arrayAdapterCategoria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void atualizarCategoriaEscolhida() {
        categoriaValida = false;
        for (Categoria categoria : listaCategorias) {
            if (categoria.getNome().equalsIgnoreCase(autoCompleteCategoria.getText().toString())) {
                CategoriaVO categoriaVO = new CategoriaVO(categoria.getIdentificador(), categoria.getNome());
                produto.setCategoriaVO(categoriaVO);
                atualizarCategoriaListaProdutosPadaria();
                categoriaValida = true;
            }
        }
        if (!categoriaValida) {
            Toast.makeText(this, "Favor escolher uma categoria válida!", Toast.LENGTH_SHORT).show();
        }
    }

    public void atualizarCategoriaListaProdutosPadaria() {
        final DatabaseReference referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias").child(produto.getPadariaVO().getIdentificador());
        referenciaPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("listaProdutosVO").exists()) {
                        for (DataSnapshot snapProdutoVO : snapshot.child("listaProdutosVO").getChildren()) {
                            ProdutoVO produtoVO = snapProdutoVO.getValue(ProdutoVO.class);
                            if (produtoVO.getIdProduto().equalsIgnoreCase(produto.getId())) {
                                produtoVO.setIdCategoria(produto.getCategoriaVO().getIdentificador());
                                referenciaPadaria.child("listaProdutosVO").child(snapProdutoVO.getKey()).setValue(produtoVO);
                                atualizarDados();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void atualizarDados() {
        final DatabaseReference referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos").child(produto.getId());
        referenciaProduto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    produto.atualizarDados(produto, referenciaProduto);
                    Toast.makeText(context, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                    abrirTelaListarProdutos();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void abrirTelaListarProdutos() {
        Intent i = new Intent(EditarProdutoActivity.this, VerProdutosActivity.class);
        startActivity(i);
        finish();
    }
}