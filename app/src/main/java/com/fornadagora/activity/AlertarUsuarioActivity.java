package com.fornadagora.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.Teclado;
import com.fornadagora.model.Alerta;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.model.Usuario;
import com.fornadagora.notification.NotificacaoUsuario;
import com.fornadagora.vo.AlertaVO;
import com.fornadagora.vo.ProdutoVO;
import com.google.android.gms.common.internal.Objects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlertarUsuarioActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompletePadaria;
    private AutoCompleteTextView autoCompleteProduto;
    private AutoCompleteTextView autoCompleteCategoria;

    private Toolbar toolbar;

    private FirebaseAuth autenticacao;

    private DatabaseReference referenciaFuncionario;
    private DatabaseReference referenciaPadaria;
    private DatabaseReference referenciaUsuario;

    private DatabaseReference referenciaProduto;
    private DatabaseReference referenciaCategoria;

    private Funcionario funcionarioRecuperado;

    private Padaria padariaEscolhida;
    private Padaria padariaDoFun;
    private Categoria categoriaEscolhida;
    private Produto produtoEscolhido;

    private ArrayAdapter arrayAdapterPadaria;
    private ArrayAdapter arrayAdapterProduto;
    private ArrayAdapter arrayAdapterCategoria;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<String> listaNomeProduto = new ArrayList<>();
    private List<String> listaIdsCategoria = new ArrayList<>();
    private List<String> listaNomeCategoriasPadaria = new ArrayList<>();

    private List<Padaria> listaPadarias = new ArrayList<>();
    private List<Alerta> listaAlertasParaEnviar = new ArrayList<>();
    private List<ProdutoVO> listaProdutoVO;

    private boolean ehMesmoIdCategoria = false;

    private Context context;

    private int total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertar_usuario);

        inicializarComponentes();
        configurarToolbar();
        validarFuncionario();
        carregarPadarias();
        carregarProdutosCategoria();
    }

    public void inicializarComponentes() {
        autoCompletePadaria = findViewById(R.id.autoCompletePadariaNot);
        autoCompleteCategoria = findViewById(R.id.autoCompleteCategoriaNot);
        autoCompleteProduto = findViewById(R.id.autoCompleteProdutoNot);
        toolbar = findViewById(R.id.toolbarPrincipal);
        context = this;
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios");
        referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos");
        referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaNomePadaria);
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
                                    padariaDoFun = padariaFuncionario;
                                    listaNomePadaria.add(padariaFuncionario.getNome());
                                    listaPadarias.add(padariaFuncionario);
                                }
                            }
                            autoCompletePadaria.setAdapter(arrayAdapterPadaria);
                            autoCompletePadaria.setText(padariaDoFun.getNome());
                            carregarCategoriasPadaria();
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

    public void carregarCategoriasPadaria() {
        if (!listaPadarias.isEmpty()) {
            for (Padaria padaria : listaPadarias) {
                String nomePadaria = autoCompletePadaria.getText().toString();
                if (padaria.getNome().equals(nomePadaria)) {
                    buscarIdsCategoriasPadaria(nomePadaria);
                }
            }
        }
    }

    public void carregarProdutosCategoria() {
        autoCompleteCategoria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                buscarProdutoPorCategoria(autoCompleteCategoria.getText().toString());
            }
        });
    }

    public void enviarAlerta(View view) {
        Teclado.fecharTeclado(view);
        if (!autoCompletePadaria.getText().toString().isEmpty()) {
            if (!autoCompleteCategoria.getText().toString().isEmpty()) {
                if (!autoCompleteProduto.getText().toString().isEmpty()) {

                    String nomePadariaEscolhida = autoCompletePadaria.getText().toString();
                    String nomeCategoriaEscolhida = autoCompleteCategoria.getText().toString();
                    String nomeProdutoEscolhido = autoCompleteProduto.getText().toString();

                    Query queryPadaria = ConfiguracaoFirebase.getFirebase().child("padarias").orderByChild("nome").equalTo(nomePadariaEscolhida);
                    queryPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot snapPadaria : snapshot.getChildren()) {
                                    Padaria padaria = snapPadaria.getValue(Padaria.class);
                                    padaria.setIdentificador(snapPadaria.getKey());
                                    padariaEscolhida = new Padaria();
                                    padariaEscolhida = padaria;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Query queryCategoria = ConfiguracaoFirebase.getFirebase().child("categorias").orderByChild("nome").equalTo(nomeCategoriaEscolhida);
                    queryCategoria.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot snapCategoria : snapshot.getChildren()) {
                                    Categoria categoria = snapCategoria.getValue(Categoria.class);
                                    categoria.setIdentificador(snapCategoria.getKey());
                                    categoriaEscolhida = new Categoria();
                                    categoriaEscolhida = categoria;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Query queryProduto = ConfiguracaoFirebase.getFirebase().child("produtos").orderByChild("nome").equalTo(nomeProdutoEscolhido);
                    queryProduto.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot snapProduto : snapshot.getChildren()) {
                                    Produto produto = snapProduto.getValue(Produto.class);
                                    produto.setId(snapProduto.getKey());
                                    produtoEscolhido = new Produto();
                                    produtoEscolhido = produto;
                                    validarPadariaCategoriaProduto(padariaEscolhida, categoriaEscolhida, produtoEscolhido);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {
                    Toast.makeText(this, "Favor informar um produto", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Favor informar uma categoria", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Favor informar a padaria", Toast.LENGTH_SHORT).show();
        }
    }

    public void validarPadariaCategoriaProduto(Padaria padariaEscolhida, Categoria categoriaEscolhida, Produto produtoEscolhido) {
        if (padariaEscolhida != null) {
            if (categoriaEscolhida != null) {
                if (produtoEscolhido != null) {
                    buscarAlertasUsuario(padariaEscolhida, categoriaEscolhida, produtoEscolhido);
                } else {
                    Toast.makeText(this, "O produto informado não existe. Favor escolher um válido.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "A categoria informada não existe. Favor escolher uma válida.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "A padaria informada não existe. Favor escolher uma válida.", Toast.LENGTH_LONG).show();
        }
    }

    public void buscarAlertasUsuario(Padaria padariaEscolhida, Categoria categoriaEscolhida, final Produto produtoEscolhido) {

        Query queryAlerta = ConfiguracaoFirebase.getFirebase().child("alertas").orderByChild("idPadaria").equalTo(padariaEscolhida.getIdentificador());
        queryAlerta.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listaAlertasParaEnviar.clear();
                    for (DataSnapshot snapAlerta : snapshot.getChildren()) {
                        Alerta alerta = snapAlerta.getValue(Alerta.class);
                        if (alerta.getIdProduto().equalsIgnoreCase(produtoEscolhido.getId())) {
                            alerta.setIdAlerta(snapAlerta.getKey());
                            listaAlertasParaEnviar.add(alerta);
                        }
                    }
                    if(!listaAlertasParaEnviar.isEmpty()){
                        buscarAlertasUsuarioBanco(listaAlertasParaEnviar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buscarAlertasUsuarioBanco(final List<Alerta> listaAlerta) {
        total = 0;
        referenciaUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapUsuario : snapshot.getChildren()) {
                        if (snapUsuario.child("listaAlertasVO").exists()) {
                            Usuario usuario = snapUsuario.getValue(Usuario.class);
                            Map<String, AlertaVO> td = new HashMap<String, AlertaVO>();
                            for (DataSnapshot alertaSnapshot : snapUsuario.child("listaAlertasVO").getChildren()) {
                                AlertaVO alertaVO = alertaSnapshot.getValue(AlertaVO.class);
                                td.put(alertaSnapshot.getKey(), alertaVO);
                            }
                            ArrayList<AlertaVO> values = new ArrayList<>(td.values());
                            for (AlertaVO alertaVO : values) {
                                for (Alerta alerta : listaAlerta) {
                                    if (alerta.getIdAlerta().equalsIgnoreCase(alertaVO.getIdAlerta())) {
                                        alertarUsuario(padariaEscolhida.getNome(), "Acabou de sair do forno: " + produtoEscolhido.getNome(), usuario.getToken());
                                        total+=1;
                                        Toast.makeText(AlertarUsuarioActivity.this, "Alerta(s) enviado(s) com sucesso: " + total, Toast.LENGTH_LONG).show();
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

    public void buscarProduto(final String id) {
        listaNomeProduto.clear();
        referenciaProduto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapProduto : snapshot.getChildren()) {
                        Produto produtoBanco = snapProduto.getValue(Produto.class);
                        produtoBanco.setId(snapProduto.getKey());
                        if (produtoBanco.getId().equalsIgnoreCase(id)) {
                            listaNomeProduto.add(produtoBanco.getNome());
                        }
                    }
                    arrayAdapterProduto = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listaNomeProduto);
                    autoCompleteProduto.setAdapter(arrayAdapterProduto);
                    autoCompleteProduto.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buscarIdsCategoriasPadaria(final String nomePadaria) {
        referenciaPadaria.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapPadaria : snapshot.getChildren()) {
                        Padaria padariaBanco = snapPadaria.getValue(Padaria.class);
                        if (padariaBanco.getNome().equalsIgnoreCase(nomePadaria)) {
                            listaProdutoVO = new ArrayList<>();
                            listaProdutoVO.addAll(padariaBanco.getListaProdutosVO());
                            listaProdutoVO.removeAll(Collections.singleton(null));
                            listaIdsCategoria.clear();
                            for (ProdutoVO produtoVO : listaProdutoVO) {
                                ehMesmoIdCategoria = false;
                                if (listaIdsCategoria.isEmpty()) {
                                    listaIdsCategoria.add(produtoVO.getIdCategoria());
                                } else {
                                    for (String idCategoria : listaIdsCategoria) {
                                        if (produtoVO.getIdCategoria().equalsIgnoreCase(idCategoria)) {
                                            ehMesmoIdCategoria = true;
                                        }
                                    }
                                    if (!ehMesmoIdCategoria) {
                                        listaIdsCategoria.add(produtoVO.getIdCategoria());
                                    }
                                }
                            }
                        }
                    }
                    buscarCategoriasPadaria(listaIdsCategoria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buscarCategoriasPadaria(final List<String> listaIdsCategoria) {
        listaNomeCategoriasPadaria.clear();
        referenciaCategoria.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapCategoria : snapshot.getChildren()) {
                        Categoria categoriaBanco = snapCategoria.getValue(Categoria.class);
                        categoriaBanco.setIdentificador(snapCategoria.getKey());
                        for (String idCategoria : listaIdsCategoria) {
                            if (categoriaBanco.getIdentificador().equalsIgnoreCase(idCategoria)) {
                                listaNomeCategoriasPadaria.add(categoriaBanco.getNome());
                            }
                        }
                    }
                    arrayAdapterCategoria = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listaNomeCategoriasPadaria);
                    autoCompleteCategoria.setAdapter(arrayAdapterCategoria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buscarProdutoPorCategoria(final String nomeCategoria) {
        referenciaCategoria.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapCategoria : snapshot.getChildren()) {
                        Categoria categoriaBanco = snapCategoria.getValue(Categoria.class);
                        categoriaBanco.setIdentificador(snapCategoria.getKey());
                        if (categoriaBanco.getNome().equalsIgnoreCase(nomeCategoria)) {
                            for (ProdutoVO produtoVO : listaProdutoVO) {
                                if (produtoVO.getIdCategoria().equalsIgnoreCase(categoriaBanco.getIdentificador())) {
                                    buscarProduto(produtoVO.getIdProduto());
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
}
