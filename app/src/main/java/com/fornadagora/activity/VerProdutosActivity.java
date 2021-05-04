package com.fornadagora.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.adapter.AdapterProdutos;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.vo.ProdutoVO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VerProdutosActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProduto;

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
            Query queryPadaria = ConfiguracaoFirebase.getFirebase().child("padarias").child(funcionarioRecuperado.getPadariaVO().getIdentificador());
            queryPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
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
            Query queryProdutos = ConfiguracaoFirebase.getFirebase().child("produtos");
            queryProdutos.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    } else {
                        abrirTelaNaoExisteProduto();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            abrirTelaNaoExisteProduto();
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

    public void abrirTelaNaoExisteProduto() {
        Intent i = new Intent(this, NaoExisteProdutoActivity.class);
        startActivity(i);
        finish();
    }
}