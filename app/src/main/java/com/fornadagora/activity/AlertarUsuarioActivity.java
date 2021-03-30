package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.fornadagora.notification.NotificacaoUsuario;
import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Alerta;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AlertarUsuarioActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompletePadaria;
    private AutoCompleteTextView autoCompleteProduto;

    private FirebaseAuth autenticacao;

    private DatabaseReference referenciaFuncionario;
    private DatabaseReference referenciaPadaria;
    private DatabaseReference referenciaUsuario;
    private DatabaseReference referenciaAlerta;

    private Funcionario funcionarioRecuperado;

    private ArrayAdapter arrayAdapterPadaria;
    private ArrayAdapter arrayAdapterProduto;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<Produto> listaProdutos = new ArrayList<>();

    private List<Padaria> listaPadarias = new ArrayList<>();
    private List<String> listaNomeProduto = new ArrayList<>();

    private List<Alerta> listaAlertas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertar_usuario);

        inicializarComponentes();
        validarFuncionario();
        carregarSpinnerPadaria();
        listenerSpinnerNomePadaria();
    }

    public void inicializarComponentes() {
        autoCompletePadaria = findViewById(R.id.autoComletePadaria);
        autoCompleteProduto = findViewById(R.id.autoComleteProduto);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios");
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaNomePadaria);
        arrayAdapterProduto = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaNomeProduto);
    }

    public void validarFuncionario() {

        referenciaFuncionario = ConfiguracaoFirebase.getFirebase();

        if (autenticacao.getCurrentUser() != null) {

            String id = autenticacao.getCurrentUser().getUid();

            referenciaFuncionario = referenciaFuncionario.child("funcionarios").child(id);
            referenciaFuncionario.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    funcionarioRecuperado = snapshot.getValue(Funcionario.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void carregarSpinnerPadaria() {
        referenciaPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (funcionarioRecuperado != null) {
                        if (funcionarioRecuperado.getPadaria() != null) {
                            for (DataSnapshot snapPadaria : snapshot.getChildren()) {
                                Padaria padaria = snapPadaria.getValue(Padaria.class);
                                Padaria padariaFuncionario = funcionarioRecuperado.getPadaria();
                                if (padaria.getNome().equals(padariaFuncionario.getNome())) {
                                    padariaFuncionario.setIdentificador(snapPadaria.getKey());
                                    listaNomePadaria.add(padariaFuncionario.getNome());
                                    listaPadarias.add(padariaFuncionario);
                                }
                            }
                            autoCompletePadaria.setAdapter(arrayAdapterPadaria);
                        } else {
                            autoCompletePadaria.setAdapter(null);
                        }
                    }
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

    public void enviarAlerta(View view) {
        referenciaUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapUsu : snapshot.getChildren()) {
                        Usuario usuario = snapUsu.getValue(Usuario.class);
                        if (usuario.getToken() == null) {
                            usuario.setToken("");
                        } else {
                            buscarAlertasUsuario(usuario);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buscarAlertasUsuario(final Usuario usuario) {

        final String nomePadaria = autoCompletePadaria.getText().toString();
        final String nomeProduto = autoCompleteProduto.getText().toString();

        String id = usuario.getIdUsuario();

        referenciaAlerta = ConfiguracaoFirebase.getFirebase().child("usuarios").child(id).child("alerta");
        referenciaAlerta.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listaAlertas.clear();
                    for (DataSnapshot snapAlerta : snapshot.getChildren()) {
                        Alerta alerta = snapAlerta.getValue(Alerta.class);
                        listaAlertas.add(alerta);
                    }
                    if (!listaAlertas.isEmpty()) {
                        for (Alerta alerta : listaAlertas) {
                            if (alerta.getPadaria().getNome().equals(nomePadaria)) {
                                if (alerta.getProduto().getNome().equals(nomeProduto)) {
                                    alertarUsuario(alerta.getPadaria().getNome(), "Acabou de sair do forno " + alerta.getProduto().getNome(), usuario.getToken());
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

    public void alertarUsuario(String titulo, String mensagem, String tokenUsu) {
        NotificacaoUsuario notUsu = new NotificacaoUsuario();
        notUsu.chamarNotificacao(titulo, mensagem, tokenUsu);
    }
}