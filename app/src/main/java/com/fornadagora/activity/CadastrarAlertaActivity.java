package com.fornadagora.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Alerta;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.model.Usuario;
import com.fornadagora.vo.AlertaVO;
import com.fornadagora.vo.ProdutoVO;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CadastrarAlertaActivity extends AppCompatActivity {

    private DatabaseReference referenciaPadaria;
    private DatabaseReference referenciaAlerta;
    private static DatabaseReference referenciaAlertaStatica;

    private DatabaseReference referenciaProduto;
    private DatabaseReference referenciaUsuario;
    private DatabaseReference referenciaCategoria;
    private static DatabaseReference referenciaUsuarioStatica;

    private FirebaseAuth autenticacao;
    private static FirebaseAuth autenticacaoStatica;

    private AutoCompleteTextView autoCompletePadaria;
    private AutoCompleteTextView autoCompleteProduto;
    private AutoCompleteTextView autoCompleteCategoria;

    private TextInputEditText editTextNomeAlerta;
    private Toolbar toolbar;

    private ArrayAdapter arrayAdapterPadaria;
    private ArrayAdapter arrayAdapterProduto;
    private ArrayAdapter arrayAdapterCategoria;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<String> listaNomeProduto = new ArrayList<>();
    private List<String> listaComIdsAlertaVO = new ArrayList<>();
    private List<String> listaIdsCategoria = new ArrayList<>();
    private List<String> listaNomeCategoriasPadaria = new ArrayList<>();

    private List<Padaria> listaPadarias = new ArrayList<>();
    private List<Produto> listaProdutos = new ArrayList<>();
    private List<AlertaVO> listaAlertaVO = new ArrayList<>();
    private List<ProdutoVO> listaProdutoVO;

    private Padaria padariaObj;
    private Produto produtoObj;

    private static Usuario usuarioRecuperadoStatic;

    private boolean produtoJaSalvo = false;
    private boolean ehMesmoAlerta = false;
    private boolean ehMesmoIdCategoria = false;

    private String nomeAlerta;

    private Context context;
    private static Context contextStatic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_alerta);

        inicializarComponentes();
        configurarToolbar();
        carregarPadarias();
        carregarCategoriasPadaria();
        carregarProdutosCategoria();
        carregarAlertasSalvos();
    }

    public void inicializarComponentes() {
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos");
        referenciaAlerta = ConfiguracaoFirebase.getFirebase().child("alertas");
        referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
        referenciaAlertaStatica = ConfiguracaoFirebase.getFirebase().child("alertas");
        editTextNomeAlerta = findViewById(R.id.editTextNomeAlerta);
        autoCompletePadaria = findViewById(R.id.autoComletePadariaAlert);
        autoCompleteProduto = findViewById(R.id.autoComleteProdutoAlert);
        autoCompleteCategoria = findViewById(R.id.autoCompleteCategoriaAlert);
        toolbar = findViewById(R.id.toolbarPrincipal);
        context = this;
        contextStatic = this;
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomePadaria);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacaoStatica = ConfiguracaoFirebase.getFirebaseAutenticacao();
    }

    public void carregarPadarias() {
        referenciaPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot padariaSnap : snapshot.getChildren()) {
                        Padaria padaria = padariaSnap.getValue(Padaria.class);
                        if (!padaria.getListaProdutosVO().isEmpty()) {
                            listaNomePadaria.add(padaria.getNome());
                            padaria.setIdentificador(padariaSnap.getKey());
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

    public void salvarAlerta(View view) {
        produtoJaSalvo = false;
        if (!editTextNomeAlerta.getText().toString().isEmpty()) {
            if (!autoCompletePadaria.getText().toString().isEmpty()) {
                if (!autoCompleteProduto.getText().toString().isEmpty()) {

                    nomeAlerta = editTextNomeAlerta.getText().toString();
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
                        if (!listaComIdsAlertaVO.isEmpty()) {
                            validarAlertaIgual(listaComIdsAlertaVO);
                        } else {
                            if (!produtoJaSalvo) {
                                montarAlertaESalvar(nomeAlerta);
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

    public void carregarAlertasSalvos() {
        referenciaUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios").child(autenticacao.getUid()).child("listaAlertasVO");
        referenciaUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<HashMap> lista;
                    lista = (List<HashMap>) snapshot.getValue();
                    listaComIdsAlertaVO.clear();
                    for (HashMap h : lista) {
                        String result = h.get("idAlerta").toString();
                        listaComIdsAlertaVO.add(result);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void montarAlertaESalvar(String nomeAlerta) {
        Alerta alerta = new Alerta(nomeAlerta, padariaObj.getIdentificador(), produtoObj.getId());
        alerta.salvar();
    }

    public void configurarToolbar() {
        toolbar.setTitle("Cadastrar Alerta");
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
        Intent i = new Intent(CadastrarAlertaActivity.this, MenuLateralActivity.class);
        startActivity(i);
        finish();
    }

    public void buscarProduto(final String id) {
        listaProdutos.clear();
        listaNomeProduto.clear();
        referenciaProduto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapProduto : snapshot.getChildren()) {
                        Produto produtoBanco = snapProduto.getValue(Produto.class);
                        produtoBanco.setId(snapProduto.getKey());
                        if (produtoBanco.getId().equalsIgnoreCase(id)) {
                            listaProdutos.add(produtoBanco);
                            listaNomeProduto.add(produtoBanco.getNome());
                        }
                    }
                    arrayAdapterProduto = new ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listaNomeProduto);
                    autoCompleteProduto.setAdapter(arrayAdapterProduto);
                    autoCompleteProduto.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void validarAlertaIgual(final List<String> listaDeIds) {
        listaAlertaVO.clear();
        ehMesmoAlerta = false;
        referenciaAlerta.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapAlerta : snapshot.getChildren()) {
                        Alerta alertaBanco = snapAlerta.getValue(Alerta.class);
                        alertaBanco.setIdAlerta(snapAlerta.getKey());
                        for (String id : listaDeIds) {
                            if (alertaBanco.getIdAlerta().equalsIgnoreCase(id)) {
                                AlertaVO alertaVO = new AlertaVO();
                                alertaVO.setIdAlerta(id);
                                alertaVO.setIdPadaria(alertaBanco.getIdPadaria());
                                alertaVO.setIdProduto(alertaBanco.getIdProduto());
                                listaAlertaVO.add(alertaVO);
                                validarMesmoAlerta(alertaBanco);
                            }
                        }
                    }
                    if (!ehMesmoAlerta) {
                        montarAlertaESalvar(nomeAlerta);
                    } else {
                        Toast.makeText(context, "Já existe um alerta para o produto escolhido nesta padaria", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void validarMesmoAlerta(Alerta alertaBanco) {
        if (padariaObj.getIdentificador().equalsIgnoreCase(alertaBanco.getIdPadaria())) {
            if (produtoObj.getId().equalsIgnoreCase(alertaBanco.getIdProduto())) {
                ehMesmoAlerta = true;
            }
        }
    }

    public static void gravarAlerta(final String idAlertaSalvo){
        referenciaAlertaStatica.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapAlerta : snapshot.getChildren()) {
                        Alerta alertaBanco = snapAlerta.getValue(Alerta.class);
                        alertaBanco.setIdAlerta(snapAlerta.getKey());
                        if (alertaBanco.getIdAlerta().equalsIgnoreCase(idAlertaSalvo)) {
                            salvarAlertaNoUsuario(alertaBanco);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void salvarAlertaNoUsuario(Alerta alerta) {
        AlertaVO alertaVO = new AlertaVO();
        alertaVO.setIdAlerta(alerta.getIdAlerta());
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(autenticacaoStatica.getCurrentUser().getUid());
        recuperarUsuarioESalvarAlerta(usuario, alertaVO);
    }

    public static Usuario recuperarUsuarioESalvarAlerta(final Usuario usuario, final AlertaVO alertaVO) {
        referenciaUsuarioStatica = ConfiguracaoFirebase.getFirebase().child("usuarios").child(usuario.getIdUsuario());
        referenciaUsuarioStatica.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Usuario usuarioBanco = snapshot.getValue(Usuario.class);
                    if (usuarioBanco.getIdUsuario().equalsIgnoreCase(usuario.getIdUsuario())) {
                        if (usuarioRecuperadoStatic == null) {
                            usuarioRecuperadoStatic = usuarioBanco;
                            usuarioRecuperadoStatic.getListaAlertaVO().add(alertaVO);
                            usuarioRecuperadoStatic.salvarAlertaVO();
                            Toast.makeText(contextStatic, "Alerta salvo com sucesso", Toast.LENGTH_SHORT).show();
                        } else {
                            usuarioRecuperadoStatic.getListaAlertaVO().add(alertaVO);
                            usuarioRecuperadoStatic.salvarAlertaVO();
                            Toast.makeText(contextStatic, "Alerta salvo com sucesso", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return usuarioRecuperadoStatic;
    }

    public void carregarCategoriasPadaria() {
        autoCompletePadaria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listaPadarias.isEmpty()) {
                    for (Padaria padaria : listaPadarias) {
                        String nomePadaria = autoCompletePadaria.getText().toString();
                        if (padaria.getNome().equals(nomePadaria)) {
                            buscarIdsCategoriasPadaria(nomePadaria);
                        }
                    }
                }
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
                            listaIdsCategoria.clear();
                            for (ProdutoVO produtoVO : listaProdutoVO) {
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

    public void carregarProdutosCategoria() {
        autoCompleteCategoria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                buscarProdutoPorCategoria(autoCompleteCategoria.getText().toString());
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

