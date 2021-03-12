package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.fornadagora.R;
import com.fornadagora.adapter.AdapterAlertaUsuario;
import com.fornadagora.adapter.AdapterDadosUsuario;
import com.fornadagora.helper.Base64Custom;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Alerta;
import com.fornadagora.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VerDadosUsuarioActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsuario;

    private List<Usuario> listaUsuario = new ArrayList<>();

    private DatabaseReference referenciaUsuario;
    private FirebaseAuth autenticacao;

    private AdapterDadosUsuario adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_dados_usuario);
        recyclerViewUsuario = findViewById(R.id.recyclerViewUsuario);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        listarDadosUsuario();
    }

    public void listarDadosUsuario() {
        if (autenticacao.getCurrentUser() != null) {
            String id = autenticacao.getCurrentUser().getUid();
            referenciaUsuario = ConfiguracaoFirebase.getFirebase();
            referenciaUsuario = referenciaUsuario.child("usuarios").child(id);
            referenciaUsuario.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        listaUsuario.clear();
                        Usuario usuario = snapshot.getValue(Usuario.class);
                        listaUsuario.add(usuario);
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
        adapter = new AdapterDadosUsuario(listaUsuario);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewUsuario.setLayoutManager(layoutManager);
        recyclerViewUsuario.setHasFixedSize(true);
        recyclerViewUsuario.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        recyclerViewUsuario.setAdapter(adapter);
    }
}