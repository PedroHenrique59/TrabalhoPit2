package com.fornadagora.activity;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.adapter.AdapterDadosFuncionario;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Funcionario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VerDadosFuncionarioActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFuncionario;

    private List<Funcionario> listaFuncionario = new ArrayList<>();

    private DatabaseReference referenciaFuncionario;
    private FirebaseAuth autenticacao;

    private AdapterDadosFuncionario adapter;

    private String parametro = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_dados_funcionario);
        recyclerViewFuncionario = findViewById(R.id.recyclerViewFuncionario);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        recuperarParametro();
        listarDadosFuncionario();
    }

    public void listarDadosFuncionario() {
        if (autenticacao.getCurrentUser() != null) {
            String id = autenticacao.getCurrentUser().getUid();
            referenciaFuncionario = ConfiguracaoFirebase.getFirebase();
            referenciaFuncionario = referenciaFuncionario.child("funcionarios").child(id);
            referenciaFuncionario.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        listaFuncionario.clear();
                        Funcionario funcionario = snapshot.getValue(Funcionario.class);
                        funcionario.setSenha(parametro);
                        listaFuncionario.add(funcionario);
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
        adapter = new AdapterDadosFuncionario(listaFuncionario);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewFuncionario.setLayoutManager(layoutManager);
        recyclerViewFuncionario.setHasFixedSize(true);
        recyclerViewFuncionario.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        recyclerViewFuncionario.setAdapter(adapter);
    }

    public void recuperarParametro(){
        Bundle b = getIntent().getExtras();
        if(b != null){
            parametro = b.getString("parametro");
        }
    }
}