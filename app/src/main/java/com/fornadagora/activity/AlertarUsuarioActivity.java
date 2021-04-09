package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
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
import com.fornadagora.vo.ProdutoVO;
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

    private Toolbar toolbar;

    private FirebaseAuth autenticacao;

    private DatabaseReference referenciaFuncionario;
    private DatabaseReference referenciaPadaria;
    private DatabaseReference referenciaUsuario;
    private DatabaseReference referenciaAlerta;
    private DatabaseReference referenciaProduto;

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
        configurarToolbar();
        validarFuncionario();
        carregarPadarias();
        carregarProdutosPadaria();
    }

    public void inicializarComponentes() {
        autoCompletePadaria = findViewById(R.id.autoComletePadaria);
        autoCompleteProduto = findViewById(R.id.autoComleteProduto);
        toolbar = findViewById(R.id.toolbarPrincipal);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios");
        referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos");
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

    public void carregarPadarias() {
        referenciaPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (funcionarioRecuperado != null) {
                        if (funcionarioRecuperado.getPadariaVO() != null) {
                            for (DataSnapshot snapPadaria : snapshot.getChildren()) {
                                Padaria padariaBanco = snapPadaria.getValue(Padaria.class);
                                padariaBanco.setIdentificador(snapPadaria.getKey());
                                if (padariaBanco.getIdentificador().equalsIgnoreCase(funcionarioRecuperado.getPadariaVO().getIdentificador())) {
                                    Padaria padariaFuncionario = padariaBanco;
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

    public void carregarProdutosPadaria() {
        autoCompletePadaria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listaPadarias.isEmpty()) {
                    listaNomeProduto.clear();
                    listaProdutos.clear();
                    for (Padaria padaria : listaPadarias) {
                        String nomePadaria = autoCompletePadaria.getText().toString();
                        if (padaria.getNome().equals(nomePadaria)) {
                            buscarProdutosPadaria(nomePadaria);
                        }
                    }
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

    public void configurarToolbar() {
        toolbar.setTitle("Enviar alerta");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_voltar_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirMenuLateral();
            }
        });
    }

    public void abrirMenuLateral() {
        Intent i = new Intent(AlertarUsuarioActivity.this, MenuLateralActivity.class);
        startActivity(i);
        finish();
    }

    public void buscarProdutosPadaria(final String nomePadaria) {
        referenciaPadaria.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snpaPadaria : snapshot.getChildren()) {
                        Padaria padariaBanco = snpaPadaria.getValue(Padaria.class);
                        if (padariaBanco.getNome().equalsIgnoreCase(nomePadaria)) {
                            if (!padariaBanco.getListaProdutosVO().isEmpty()) {
                                for (ProdutoVO produtoVO : padariaBanco.getListaProdutosVO()) {
                                    String idProduto = produtoVO.getIdProduto();
                                    buscarProduto(idProduto);
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

    public void buscarProduto(final String id){
        referenciaProduto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snapProduto : snapshot.getChildren()){
                        Produto produtoBanco = snapProduto.getValue(Produto.class);
                        produtoBanco.setId(snapProduto.getKey());
                        if(produtoBanco.getId().equalsIgnoreCase(id)){
                            listaProdutos.add(produtoBanco);
                            listaNomeProduto.add(produtoBanco.getNome());
                        }
                    }
                    autoCompleteProduto.setAdapter(arrayAdapterProduto);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
