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
import com.fornadagora.model.Alerta;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.vo.ProdutoVO;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EditarAlertaUsuarioActivity extends AppCompatActivity {

    private Alerta alerta;
    private Alerta alertaEditado;

    private TextInputEditText editTextNomeAlerta;

    private AutoCompleteTextView autoComletePadaria;
    private AutoCompleteTextView autoCompleteCategoria;
    private AutoCompleteTextView autoComleteProduto;

    private DatabaseReference referenciaPadaria;
    private DatabaseReference referenciaAlertaExistente;
    private DatabaseReference referenciaProduto;
    private DatabaseReference referenciaCategoria;

    private FirebaseAuth autenticacao;

    private ArrayAdapter arrayAdapterPadaria;
    private ArrayAdapter arrayAdapterProduto;
    private ArrayAdapter arrayAdapterCategoria;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();

    private List<String> listaNomeProduto = new ArrayList<>();
    private List<Produto> listaProdutos = new ArrayList<>();

    private List<String> listaNomeCategoria = new ArrayList<>();

    private List<ProdutoVO> listaProdutoVO;

    private List<String> listaIdsCategoria = new ArrayList<>();
    private List<String> listaNomeCategoriasPadaria = new ArrayList<>();
    private List<Categoria> listaCategoriasPadaria = new ArrayList<>();

    private boolean padariaDisponivel = false;
    private boolean produtoDisponivel = false;
    private boolean categoriaDisponivel = false;
    private boolean ehMesmoIdCategoria = false;
    private boolean ehMesmoAlerta = false;
    private boolean preencheuDadosDoAlerta = false;

    private Context context;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_alerta_usuario);
        alerta = getIntent().getExtras().getParcelable("alertaObj");
        context = this;
        inicializarComponentes();
        configurarToolbar();
        preencherCamposComDadosAlerta(alerta);
        carregarPadarias();
        carregarCategoriasPadaria();
        carregarProdutosCategoria();
    }

    public void inicializarComponentes() {
        editTextNomeAlerta = findViewById(R.id.editTextNomeAlertaEdit);
        autoComletePadaria = findViewById(R.id.autoComletePadariaAlertEdit);
        autoCompleteCategoria = findViewById(R.id.autoCompleteCategoriaEdit);
        autoComleteProduto = findViewById(R.id.autoComleteProdutoAlertEdit);
        toolbar = findViewById(R.id.toolbarPrincipal);
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaAlertaExistente = ConfiguracaoFirebase.getFirebase().child("alertas");
        referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos");
        referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomePadaria);
        arrayAdapterProduto = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomeProduto);
        arrayAdapterCategoria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomeCategoria);
    }

    public void configurarToolbar() {
        toolbar.setTitle("Editar Dados Alerta");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_voltar_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirMenuLateral();
            }
        });
    }

    public void preencherCamposComDadosAlerta(Alerta alerta) {
        if (alerta != null) {
            editTextNomeAlerta.setText(alerta.getNome());
            autoComletePadaria.setText(alerta.getPadaria().getNome());
            autoCompleteCategoria.setText(alerta.getProduto().getCategoria().getNome());
            autoComleteProduto.setText(alerta.getProduto().getNome());
            preencheuDadosDoAlerta = true;
        }
    }

    public void carregarPadarias() {
        referenciaPadaria.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listaNomePadaria.clear();
                    listaPadarias.clear();
                    for (DataSnapshot padariaSnap : snapshot.getChildren()) {
                        Padaria padaria = padariaSnap.getValue(Padaria.class);
                        if (!padaria.getListaProdutosVO().isEmpty()) {
                            listaNomePadaria.add(padaria.getNome());
                            listaPadarias.add(padaria);
                        }
                    }
                    autoComletePadaria.setAdapter(arrayAdapterPadaria);
                    preCarregarCategoria();
                    preCarregarProduto();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void preCarregarCategoria() {
        if (preencheuDadosDoAlerta) {
            if (!listaPadarias.isEmpty()) {
                for (Padaria padaria : listaPadarias) {
                    String nomePadaria = autoComletePadaria.getText().toString();
                    if (padaria.getNome().equals(nomePadaria)) {
                        buscarIdsCategoriasPadaria(nomePadaria);
                    }
                }
            }
        }
    }

    public void carregarCategoriasPadaria() {
        autoComletePadaria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listaPadarias.isEmpty()) {
                    for (Padaria padaria : listaPadarias) {
                        String nomePadaria = autoComletePadaria.getText().toString();
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
                                ehMesmoIdCategoria = false;
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
        listaCategoriasPadaria.clear();
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
                                listaCategoriasPadaria.add(categoriaBanco);
                            }
                        }
                    }
                    if (preencheuDadosDoAlerta) {
                        autoCompleteCategoria.setText(alerta.getProduto().getCategoria().getNome());
                        arrayAdapterCategoria = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listaNomeCategoriasPadaria);
                        autoCompleteCategoria.setAdapter(arrayAdapterCategoria);
                    } else {
                        autoCompleteCategoria.setText("");
                        arrayAdapterCategoria = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listaNomeCategoriasPadaria);
                        autoCompleteCategoria.setAdapter(arrayAdapterCategoria);
                    }
                    preencheuDadosDoAlerta = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void preCarregarProduto() {
        if (preencheuDadosDoAlerta) {
            buscarProdutoPorCategoria(autoCompleteCategoria.getText().toString());
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
                    if (preencheuDadosDoAlerta) {
                        autoComleteProduto.setText(alerta.getProduto().getNome());
                        arrayAdapterProduto = new ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listaNomeProduto);
                        autoComleteProduto.setAdapter(arrayAdapterProduto);
                    } else {
                        autoComleteProduto.setText("");
                        arrayAdapterProduto = new ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listaNomeProduto);
                        autoComleteProduto.setAdapter(arrayAdapterProduto);
                    }
                    preencheuDadosDoAlerta = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void validarAntesSalvar(View view) {
        if (!editTextNomeAlerta.getText().toString().isEmpty()) {
            String nomeEscolhido = editTextNomeAlerta.getText().toString();
            alertaEditado = new Alerta();
            alertaEditado.setNome(nomeEscolhido);
            if (!autoComletePadaria.getText().toString().isEmpty()) {
                String nomePadariaEscolhida = autoComletePadaria.getText().toString();
                if (!listaPadarias.isEmpty()) {
                    for (Padaria padaria : listaPadarias) {
                        if (padaria.getNome().equals(nomePadariaEscolhida)) {
                            padariaDisponivel = true;
                            alertaEditado.setPadaria(padaria);
                        }
                    }
                    if (!autoCompleteCategoria.getText().toString().isEmpty()) {
                        String nomeCategoriaEscolhida = autoCompleteCategoria.getText().toString();
                        if (!listaCategoriasPadaria.isEmpty()) {
                            for (Categoria categoria : listaCategoriasPadaria) {
                                if (categoria.getNome().equalsIgnoreCase(nomeCategoriaEscolhida)) {
                                    categoriaDisponivel = true;
                                }
                            }
                        }
                        if (padariaDisponivel) {
                            if (categoriaDisponivel) {
                                if (!autoComleteProduto.getText().toString().isEmpty()) {
                                    String nomeProdutoEscolhido = autoComleteProduto.getText().toString();
                                    if (!listaProdutos.isEmpty()) {
                                        for (Produto produto : listaProdutos) {
                                            if (produto.getNome().equals(nomeProdutoEscolhido)) {
                                                produtoDisponivel = true;
                                                alertaEditado.setProduto(produto);
                                            }
                                        }
                                        if (!produtoDisponivel) {
                                            Toast.makeText(this, "Este produto não está disponível para a padaria escolhida. Favor escolher um que esteja na lista acima.", Toast.LENGTH_SHORT).show();
                                        }
                                        if (produtoDisponivel && padariaDisponivel && categoriaDisponivel) {
                                            validarExisteAlerta(alertaEditado);
                                        }
                                    }
                                } else {
                                    Toast.makeText(this, "Favor escolher um produto!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Esta categoria não está disponível. Favor escolher uma na lista acima.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "Esta padaria não está disponível. Favor escolher uma na lista acima.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Favor escolher uma categoria!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Favor escolher uma padaria!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Favor informar um nome para o alerta!", Toast.LENGTH_SHORT).show();
        }
    }

    public void validarExisteAlerta(final Alerta alertaEditado) {
        referenciaAlertaExistente.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ehMesmoAlerta = false;
                    for (DataSnapshot snapAlerta : snapshot.getChildren()) {
                        Alerta alertaBanco = snapAlerta.getValue(Alerta.class);
                        if (!alertaBanco.getNome().equalsIgnoreCase(alertaEditado.getNome()) && alertaBanco.getIdPadaria().equalsIgnoreCase(alertaEditado.getPadaria().getIdentificador()) && alertaBanco.getIdProduto().equalsIgnoreCase(alertaEditado.getProduto().getId())) {
                            ehMesmoAlerta = false;
                        }else if(alertaBanco.getNome().equalsIgnoreCase(alertaEditado.getNome()) && alertaBanco.getIdPadaria().equalsIgnoreCase(alertaEditado.getPadaria().getIdentificador()) && alertaBanco.getIdProduto().equalsIgnoreCase(alertaEditado.getProduto().getId())){
                            ehMesmoAlerta = true;
                        }
                    }
                    if (ehMesmoAlerta) {
                        Toast.makeText(context, "Já existe um alerta salvo para esse produto nesta padaria. Favor escolher outra padaria ou outro produto!", Toast.LENGTH_LONG).show();
                    } else {
                        salvarEdicaoAlerta(alertaEditado);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void salvarEdicaoAlerta(final Alerta alertaEditado) {
        final DatabaseReference referencia = ConfiguracaoFirebase.getFirebase().child("alertas").child(alerta.getIdAlerta());
        referencia.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    alertaEditado.atualizarDados(alertaEditado, referencia);
                    Toast.makeText(context, "Alerta editado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                    abrirTelaListaAlertas();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void abrirMenuLateral() {
        Intent intent = new Intent(this, MenuLateralActivity.class);
        startActivity(intent);
        finish();
    }

    public void abrirTelaListaAlertas(){
        Intent i = new Intent(EditarAlertaUsuarioActivity.this, VerAlertaUsuarioActivity.class);
        startActivity(i);
        finish();
    }
}