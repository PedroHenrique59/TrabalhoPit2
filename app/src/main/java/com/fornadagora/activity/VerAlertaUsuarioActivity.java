package com.fornadagora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.adapter.AdapterAlertaUsuario;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Alerta;
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

public class VerAlertaUsuarioActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAlerta;

    private List<AlertaVO> listaAlertasVO = new ArrayList<>();
    private List<Alerta> listaAlertas = new ArrayList<>();
    private List<Alerta> listaAlertaComPadariaProduto = new ArrayList<>();

    private DatabaseReference referenciaUsuario;
    private DatabaseReference referenciaAlerta;
    private DatabaseReference referenciaPadaria;

    private FirebaseAuth autenticacao;

    private AdapterAlertaUsuario adapter;

    private Produto produtoRecuperado;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_alerta_usuario);
        inicializarComponentes();
        listarAlertas();
    }

    public void inicializarComponentes() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        recyclerViewAlerta = findViewById(R.id.recyclerViewAlerta);
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
    }

    public void listarAlertas() {
        if (autenticacao.getCurrentUser() != null) {
            String idUsuario = autenticacao.getCurrentUser().getUid();
            referenciaUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios").child(idUsuario);
            referenciaUsuario.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        listaAlertasVO.clear();
                        Map<String, AlertaVO> td = new HashMap<String, AlertaVO>();
                        for (DataSnapshot snapAlerta : snapshot.child("listaAlertasVO").getChildren()) {
                            AlertaVO alertaVO = snapAlerta.getValue(AlertaVO.class);
                            td.put(snapAlerta.getKey(), alertaVO);
                        }
                        ArrayList<AlertaVO> listaAlertas = new ArrayList<>(td.values());
                        listaAlertasVO.addAll(listaAlertas);
                        buscarAlertas(listaAlertasVO);
                    } else {
                        abrirTelaNaoExisteAlerta();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void configuraRecyclerView() {
        adapter = new AdapterAlertaUsuario(listaAlertaComPadariaProduto);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewAlerta.setLayoutManager(layoutManager);
        recyclerViewAlerta.setHasFixedSize(true);
        recyclerViewAlerta.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        recyclerViewAlerta.setAdapter(adapter);
    }

    public void abrirTelaNaoExisteAlerta() {
        Intent i = new Intent(VerAlertaUsuarioActivity.this, NaoExisteAlertaActivity.class);
        startActivity(i);
        finish();
    }

    public void buscarAlertas(final List<AlertaVO> listaAlertasVO) {
        referenciaAlerta = ConfiguracaoFirebase.getFirebase().child("alertas");
        referenciaAlerta.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listaAlertas.clear();
                    for (DataSnapshot snapAlerta : snapshot.getChildren()) {
                        Alerta alerta = snapAlerta.getValue(Alerta.class);
                        alerta.setIdAlerta(snapAlerta.getKey());
                        for (AlertaVO alertaVO : listaAlertasVO) {
                            if (alertaVO.getIdAlerta().equalsIgnoreCase(alerta.getIdAlerta())) {
                                listaAlertas.add(alerta);
                            }
                        }
                    }
                    carregarPadariasEProdutos(listaAlertas);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void carregarPadariasEProdutos(final List<Alerta> listaAlertas) {
        referenciaPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listaAlertaComPadariaProduto.clear();
                    for (DataSnapshot snapPadaria : snapshot.getChildren()) {
                        Padaria padariaBanco = snapPadaria.getValue(Padaria.class);
                        padariaBanco.setIdentificador(snapPadaria.getKey());
                        for (Alerta alerta : listaAlertas) {
                            if (alerta.getIdPadaria().equalsIgnoreCase(padariaBanco.getIdentificador())) {
                                for (ProdutoVO produtoVO : padariaBanco.getListaProdutosVO()) {
                                    if (produtoVO.getIdProduto().equalsIgnoreCase(alerta.getIdProduto())) {
                                        alerta.setPadaria(padariaBanco);
                                        buscarProduto(produtoVO.getIdProduto(), alerta);
                                    }
                                }
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

    public void buscarProduto(String idProduto, final Alerta alerta){
        Query queryProduto = ConfiguracaoFirebase.getFirebase().child("produtos").orderByChild("id").equalTo(idProduto);
        queryProduto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot snapProduto : snapshot.getChildren()) {
                        Produto produto = snapProduto.getValue(Produto.class);
                        produtoRecuperado = new Produto();
                        produtoRecuperado = produto;
                        alerta.setProduto(produtoRecuperado);
                        listaAlertaComPadariaProduto.add(alerta);
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
