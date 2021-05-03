package com.fornadagora.activity;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.adapter.AdapterCategorias;
import com.fornadagora.adapter.AdapterProdutos;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.vo.AlertaVO;
import com.fornadagora.vo.ProdutoVO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerProdutosActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProduto;

    private DatabaseReference referenciaProduto;
    private DatabaseReference referenciaFuncionario;
    private DatabaseReference referenciaPadaria;

    private FirebaseAuth autenticacao;

    private List<Produto> listaProdutos = new ArrayList<>();
    private List<Produto> listaProdutosCarregados = new ArrayList<>();

    private Funcionario funcionarioRecuperado;
    private Padaria padariaRecuperada;

    private AdapterProdutos adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_produtos);
        inicializarComponentes();
        carregarFuncionario();
    }

    public void inicializarComponentes() {
        recyclerViewProduto = findViewById(R.id.recyclerViewProdutos);
        referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos");
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    }

    public void carregarFuncionario() {
        if (autenticacao.getCurrentUser() != null) {
            String id = autenticacao.getCurrentUser().getUid();
            Query queryFuncionario = ConfiguracaoFirebase.getFirebase().child("funcionarios").child(id);
            queryFuncionario.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        funcionarioRecuperado = snapshot.getValue(Funcionario.class);
                        buscarPadariaFuncionario();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void buscarPadariaFuncionario() {
        if (funcionarioRecuperado != null) {
            referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias").child(funcionarioRecuperado.getPadariaVO().getIdentificador());
            referenciaPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        padariaRecuperada = snapshot.getValue(Padaria.class);
                        carregarProdutos();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void carregarProdutos() {
        if (!padariaRecuperada.getListaProdutosVO().isEmpty()) {
            referenciaProduto.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        listaProdutosCarregados.clear();
                        for (DataSnapshot snapProduto : snapshot.getChildren()) {
                            Produto produtoBanco = snapProduto.getValue(Produto.class);
                            for (ProdutoVO produtoVO : padariaRecuperada.getListaProdutosVO()) {
                                if (produtoBanco.getId().equalsIgnoreCase(produtoVO.getIdProduto())) {
                                    listaProdutosCarregados.add(produtoBanco);
                                }
                            }
                        }
                        configuraRecyclerView();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void configuraRecyclerView() {
        adapter = new AdapterProdutos(listaProdutosCarregados);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewProduto.setLayoutManager(layoutManager);
        recyclerViewProduto.setHasFixedSize(true);
        recyclerViewProduto.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        recyclerViewProduto.setAdapter(adapter);
    }
}