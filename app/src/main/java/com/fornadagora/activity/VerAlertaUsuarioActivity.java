package com.fornadagora.activity;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.adapter.AdapterAlertaUsuario;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Alerta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VerAlertaUsuarioActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAlerta;

    private List<Alerta> listaAlertas = new ArrayList<>();

    private DatabaseReference referenciaAlerta;
    private FirebaseAuth autenticacao;

    private AdapterAlertaUsuario adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_alerta_usuario);
        recyclerViewAlerta = findViewById(R.id.recyclerViewAlerta);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        listarAlertas();
    }

    public void listarAlertas() {
        if (autenticacao.getCurrentUser() != null) {

            String id = autenticacao.getCurrentUser().getUid();

            referenciaAlerta = ConfiguracaoFirebase.getFirebase();

            referenciaAlerta = referenciaAlerta.child("usuarios").child(id).child("alerta");

            referenciaAlerta.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        listaAlertas.clear();
                        for (DataSnapshot snapAlerta : snapshot.getChildren()) {
                            Alerta alerta = snapAlerta.getValue(Alerta.class);
                            listaAlertas.add(alerta);
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
        adapter = new AdapterAlertaUsuario(listaAlertas);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewAlerta.setLayoutManager(layoutManager);
        recyclerViewAlerta.setHasFixedSize(true);
        recyclerViewAlerta.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        recyclerViewAlerta.setAdapter(adapter);
    }
}
