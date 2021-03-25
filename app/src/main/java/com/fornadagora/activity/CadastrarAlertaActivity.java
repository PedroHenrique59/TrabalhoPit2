package com.fornadagora.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Alerta;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CadastrarAlertaActivity extends AppCompatActivity {

    private DatabaseReference referenciaPadaria;
    private DatabaseReference referenciaAlerta;

    private FirebaseAuth autenticacao;

    private AutoCompleteTextView autoCompletePadaria;
    private AutoCompleteTextView autoCompleteProduto;

    private TextInputEditText editTextNomeAlerta;

    private ArrayAdapter arrayAdapterPadaria;
    private ArrayAdapter arrayAdapterProduto;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<String> listaNomeProduto = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();
    private List<Produto> listaProdutos = new ArrayList<>();
    private List<Alerta> listaAlertas = new ArrayList<>();

    private Padaria padariaObj;
    private Produto produtoObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_alerta);
        inicializarComponentes();
        listarNomePadaria();
        listenerSpinnerNomePadaria();
        validarAlertaSalvo();
    }

    public void inicializarComponentes() {
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        editTextNomeAlerta = findViewById(R.id.editTextNomeAlerta);
        autoCompletePadaria = findViewById(R.id.autoComletePadariaAlert);
        autoCompleteProduto = findViewById(R.id.autoComleteProdutoAlert);
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomePadaria);
        arrayAdapterProduto = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomeProduto);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    }

    public void listarNomePadaria() {
        referenciaPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot padariaSnap : snapshot.getChildren()) {
                        Padaria padaria = padariaSnap.getValue(Padaria.class);
                        if (!padaria.getListaProdutos().isEmpty()) {
                            listaNomePadaria.add(padaria.getNome());
                            listaPadarias.add(padaria);
                        }
                    }
                    autoCompletePadaria.setAdapter(arrayAdapterPadaria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void listenerSpinnerNomePadaria() {
        autoCompletePadaria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listaPadarias.isEmpty()) {
                    listaNomeProduto.clear();
                    listaProdutos.clear();
                    for (Padaria padaria : listaPadarias) {
                        String nomePadaria = autoCompletePadaria.getText().toString();
                        if (padaria.getNome().equals(nomePadaria)) {
                            for (Produto produto : padaria.getListaProdutos()) {
                                listaNomeProduto.add(produto.getNome());
                                listaProdutos.add(produto);
                            }
                        }
                    }
                    autoCompleteProduto.setAdapter(arrayAdapterProduto);
                }
            }
        });
    }

    public void salvarAlerta(View view) {

        if (!editTextNomeAlerta.getText().toString().isEmpty()) {
            if (!autoCompletePadaria.getText().toString().isEmpty()) {
                if (!autoCompleteProduto.getText().toString().isEmpty()) {

                    String nomeAlerta = editTextNomeAlerta.getText().toString();
                    String nomePadaria = autoCompletePadaria.getText().toString();
                    String nomeProduto = autoCompleteProduto.getText().toString();

                    if (!listaPadarias.isEmpty()) {
                        for (Padaria padaria : listaPadarias) {
                            if (padaria.getNome().equals(nomePadaria)) {
                                padariaObj = new Padaria();
                                padariaObj = padaria;
                                for (Produto produto : listaProdutos) {
                                    if (produto.getNome().equals(nomeProduto)) {
                                        produtoObj = new Produto();
                                        produtoObj = produto;
                                    }
                                }
                            }
                        }

                        if (!listaAlertas.isEmpty()) {
                            for (Alerta alerta : listaAlertas) {
                                if (nomePadaria.equals(alerta.getPadaria().getNome())) {
                                    if (nomeProduto.equals(alerta.getProduto().getNome())) {
                                        Toast.makeText(this, "Você já salvou um alerta para esse produto nesta padaria", Toast.LENGTH_SHORT).show();
                                        produtoObj = null;
                                        break;
                                    }
                                }
                            }
                        }

                        if (padariaObj != null) {
                            if (produtoObj != null) {
                                if (autenticacao.getCurrentUser() != null) {
                                    montarAlerta(nomeAlerta);
                                    Toast.makeText(this, "Alerta salvo com sucesso", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Você já salvou um alerta para esse produto nesta padaria", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Favor informar a padaria", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Favor informar o produto", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Favor informar o nome do alerta", Toast.LENGTH_SHORT).show();
        }
    }

    public void validarAlertaSalvo() {
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
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void montarAlerta(String nomeAlerta) {
        Alerta alerta = new Alerta(nomeAlerta, padariaObj, produtoObj);
        Usuario usuario = new Usuario();

        String id = autenticacao.getCurrentUser().getUid();

        usuario.setIdUsuario(id);
        usuario.setAlerta(alerta);
        usuario.salvarAlerta();
    }
}

