package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.fornadagora.R;
import com.fornadagora.adapter.AdapterCategorias;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Categoria;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VerCategoriasActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCategoria;

    private DatabaseReference referenciaCategoria;

    private List<Categoria> listaCategorias = new ArrayList<>();

    private AdapterCategorias adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_categorias);
        inicializarComponentes();
        listarCategorias();
    }

    public void inicializarComponentes() {
        recyclerViewCategoria = findViewById(R.id.recyclerViewCategoria);
        referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
    }

    public void listarCategorias(){
        referenciaCategoria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    listaCategorias.clear();
                    for(DataSnapshot snapCategoria : snapshot.getChildren()){
                        Categoria categoria = snapCategoria.getValue(Categoria.class);
                        listaCategorias.add(categoria);
                    }
                    configuraRecyclerView();
                }else{
                    abrirTelaNaoExisteCategoria();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void configuraRecyclerView() {
        adapter = new AdapterCategorias(listaCategorias);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewCategoria.setLayoutManager(layoutManager);
        recyclerViewCategoria.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(this.getResources().getDrawable(R.drawable.linha_divisoria_recycler));
        recyclerViewCategoria.addItemDecoration(dividerItemDecoration);
        recyclerViewCategoria.setAdapter(adapter);
    }

    public void abrirTelaNaoExisteCategoria(){
        Intent i = new Intent(this, NaoExisteCategoriaActivity.class);
        startActivity(i);
        finish();
    }
}