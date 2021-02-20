package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CadastrarAlertaActivity extends AppCompatActivity {

    private DatabaseReference referenciaPadaria;

    private Spinner spinnerPadaria;
    private Spinner spinnerProduto;

    private ArrayAdapter arrayAdapterPadaria;
    private ArrayAdapter arrayAdapterProduto;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<String> listaNomeProduto = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_alerta);
        inicializarComponentes();
        listarNomePadaria();
        listenerSpinnerNomePadaria();
    }

    public void inicializarComponentes(){
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        spinnerPadaria = findViewById(R.id.spinnerNomePadaria);
        spinnerProduto = findViewById(R.id.spinnerProdutoPadaria);
        arrayAdapterPadaria = new ArrayAdapter (this, android.R.layout.simple_spinner_dropdown_item, listaNomePadaria);
        arrayAdapterProduto = new ArrayAdapter (this, android.R.layout.simple_spinner_dropdown_item, listaNomeProduto);
    }

    public void listarNomePadaria(){
        referenciaPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot padariaSnap : snapshot.getChildren()){
                        Padaria padaria = padariaSnap.getValue(Padaria.class);
                        if(!padaria.getListaProdutos().isEmpty()){
                            listaNomePadaria.add(padaria.getNome());
                            listaPadarias.add(padaria);
                        }
                    }
                    spinnerPadaria.setAdapter(arrayAdapterPadaria);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void listenerSpinnerNomePadaria(){
        spinnerPadaria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!listaPadarias.isEmpty()){
                    listaNomeProduto.clear();
                    for(Padaria padaria : listaPadarias){
                        String nomePadaria = spinnerPadaria.getSelectedItem().toString();
                        if(padaria.getNome().equals(nomePadaria)){
                            for(Produto produto : padaria.getListaProdutos()){
                                listaNomeProduto.add(produto.getNome());
                            }
                        }
                    }
                    spinnerProduto.setAdapter(arrayAdapterProduto);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
