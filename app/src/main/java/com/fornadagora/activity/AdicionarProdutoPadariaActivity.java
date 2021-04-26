package com.fornadagora.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.PointerIcon;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.Teclado;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.vo.PadariaVO;
import com.fornadagora.vo.ProdutoVO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdicionarProdutoPadariaActivity extends AppCompatActivity {

    private DatabaseReference referenciaPadaria;
    private DatabaseReference referenciaFuncionario;
    private DatabaseReference referenciaCategoria;
    private DatabaseReference referenciaProduto;

    private FirebaseAuth autenticacao;

    private AutoCompleteTextView autoComletePadariaAdd;
    private AutoCompleteTextView autoComleteCategoriaAdd;
    private AutoCompleteTextView autoComleteProdutoAdd;

    private Toolbar toolbar;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();
    private List<String> listaNomesProdutos = new ArrayList<>();
    private List<String> listaCarregaNomeProduto = new ArrayList<>();
    private List<Produto> listaProdutos = new ArrayList<>();
    private List<ProdutoVO> listaProdutosVO = new ArrayList<>();

    private ArrayList<String> listaNomeCategoria = new ArrayList<>();
    private ArrayList<Categoria> listaCategoria = new ArrayList<>();

    private ArrayAdapter arrayAdapterPadaria;
    private ArrayAdapter arrayAdapterCategoria;
    private ArrayAdapter arrayAdapterProduto;

    private Funcionario funcionarioRecuperado;
    private Categoria categoria;
    private ProdutoVO produtoVO;

    private String nomePadaria;
    private String nomeCategoria;

    private Context context;

    private boolean produtoJaSalvo = false;
    private boolean produtoValido = false;
    private boolean padariaValida = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_produto_padaria);
        inicializarComponentes();
        validarFuncionario();
        carregarPadarias();
        carregarCategorias();
        configurarToolbar();
    }

    public void inicializarComponentes() {
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
        referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos");
        autoComletePadariaAdd = findViewById(R.id.autoComletePadariaAdd);
        autoComleteCategoriaAdd = findViewById(R.id.autoComleteCategoriaAdd);
        autoComleteProdutoAdd = findViewById(R.id.autoComleteProdutoAdd);
        toolbar = findViewById(R.id.toolbarPrincipal);
        context = this;
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaNomePadaria);
        arrayAdapterCategoria = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaNomeCategoria);
    }

    public void salvarProdutos(View view) {
        Teclado.fecharTeclado(view);
        limparListas();
        padariaValida = false;
        if (!autoComletePadariaAdd.getText().toString().isEmpty()) {
            if (!autoComleteCategoriaAdd.getText().toString().isEmpty()) {
                if (!autoComleteProdutoAdd.getText().toString().isEmpty()) {
                    nomePadaria = autoComletePadariaAdd.getText().toString();
                    nomeCategoria = autoComleteCategoriaAdd.getText().toString();
                    String nomeProduto = autoComleteProdutoAdd.getText().toString();
                    validarPadaria();
                    if (padariaValida) {
                        buscarProdutoESalvarNaPadaria(nomeProduto);
                    } else {
                        Toast.makeText(this, "Padaria informada inválida. Favor escolher uma válida.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Favor escolher um produto!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Favor escolher uma categoria!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Favor escolher a padaria!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validarPadaria() {
        if (!listaNomePadaria.isEmpty()) {
            for (String nome : listaNomePadaria) {
                if (nome.equalsIgnoreCase(nomePadaria)) {
                    padariaValida = true;
                }
            }
        }
        return padariaValida;
    }

    public void carregarPadarias() {
        referenciaPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (funcionarioRecuperado != null) {
                        if (funcionarioRecuperado.getPadariaVO() != null) {
                            for (DataSnapshot snapPadaria : snapshot.getChildren()) {
                                Padaria padaria = snapPadaria.getValue(Padaria.class);
                                padaria.setIdentificador(snapPadaria.getKey());
                                PadariaVO padariaFuncionarioVO = funcionarioRecuperado.getPadariaVO();
                                if (padaria.getIdentificador().equals(padariaFuncionarioVO.getIdentificador())) {
                                    padaria.setIdentificador(snapPadaria.getKey());
                                    listaNomePadaria.add(padaria.getNome());
                                    listaPadarias.add(padaria);
                                }
                            }
                            autoComletePadariaAdd.setAdapter(arrayAdapterPadaria);
                        } else {
                            autoComletePadariaAdd.setAdapter(null);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void carregarCategorias() {
        referenciaCategoria.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapCategoria : snapshot.getChildren()) {
                        Categoria cat = snapCategoria.getValue(Categoria.class);
                        cat.setIdentificador(snapCategoria.getKey());
                        listaNomeCategoria.add(cat.getNome());
                        listaCategoria.add(cat);
                    }
                    autoComleteCategoriaAdd.setAdapter(arrayAdapterCategoria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        autoComleteCategoriaAdd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nomeCategoria = autoComleteCategoriaAdd.getText().toString();
                carregarProdutosDaCategoria(nomeCategoria);
            }
        });
    }

    public void validarFuncionario() {

        referenciaFuncionario = ConfiguracaoFirebase.getFirebase();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

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

    public void configurarToolbar() {
        toolbar.setTitle("Adicionar produto(s)");
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
        Intent intent = new Intent(this, MenuLateralActivity.class);
        startActivity(intent);
        finish();
    }

    public void carregarProdutosDaCategoria(final String nomeCategoria) {
        listaCarregaNomeProduto.clear();
        referenciaProduto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapProduto : snapshot.getChildren()) {
                        Produto produto = dataSnapProduto.getValue(Produto.class);
                        Categoria categoriaEscolhida = buscarCategoria(nomeCategoria);
                        if (produto.getCategoriaVO().getIdentificador().equalsIgnoreCase(categoriaEscolhida.getIdentificador())) {
                            listaCarregaNomeProduto.add(produto.getNome());
                        }
                    }
                    arrayAdapterProduto = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listaCarregaNomeProduto);
                    autoComleteProdutoAdd.setAdapter(arrayAdapterProduto);
                    autoComleteProdutoAdd.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void limparListas() {
        listaNomesProdutos.clear();
        listaProdutos.clear();
        listaProdutosVO.clear();
    }

    public Categoria buscarCategoria(String nome) {
        categoria = null;
        if (!listaCategoria.isEmpty()) {
            for (Categoria cat : listaCategoria) {
                if (cat.getNome().equalsIgnoreCase(nome)) {
                    categoria = cat;
                }
            }
        }
        return categoria;
    }

    public void buscarProdutoESalvarNaPadaria(final String nome) {
        produtoJaSalvo = false;
        produtoValido = false;
        referenciaProduto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapProduto : snapshot.getChildren()) {
                        Produto produto = dataSnapProduto.getValue(Produto.class);
                        if (produto.getNome().equalsIgnoreCase(nome)) {
                            produtoValido = true;
                            produtoVO = new ProdutoVO();
                            produtoVO.setIdProduto(dataSnapProduto.getKey());
                            buscarCategoria(nomeCategoria);
                            if (categoria != null) {
                                produtoVO.setIdCategoria(categoria.getIdentificador());
                                listaProdutosVO.add(produtoVO);
                                for (Padaria padaria : listaPadarias) {
                                    if (padaria.getListaProdutosVO().isEmpty()) {
                                        padaria.getListaProdutosVO().addAll(listaProdutosVO);
                                        referenciaPadaria.child(padaria.getIdentificador()).setValue(padaria);
                                        listaPadarias.clear();
                                        listaPadarias.add(padaria);
                                        Toast.makeText(context, "Produto salvo com sucesso!", Toast.LENGTH_LONG).show();
                                        limparCampos();
                                    } else {
                                        for (ProdutoVO produtoListaVO : padaria.getListaProdutosVO()) {
                                            if (produtoListaVO.getIdProduto().equalsIgnoreCase(produtoVO.getIdProduto())) {
                                                Toast.makeText(context, "Esse produto já foi adicionado a essa padaria!", Toast.LENGTH_LONG).show();
                                                produtoJaSalvo = true;
                                                break;
                                            }
                                        }
                                        if (!produtoJaSalvo) {
                                            padaria.getListaProdutosVO().addAll(listaProdutosVO);
                                            referenciaPadaria.child(padaria.getIdentificador()).setValue(padaria);
                                            listaPadarias.clear();
                                            listaPadarias.add(padaria);
                                            Toast.makeText(context, "Produto salvo com sucesso!", Toast.LENGTH_LONG).show();
                                            limparCampos();
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Produto/Categoria escolhido(a) inválidos. Favor selecionar algum(a) válida.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
                if (!produtoValido) {
                    Toast.makeText(context, "Produto/Categoria escolhido(a) inválidos. Favor selecionar algum(a) válida.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void limparCampos() {
        autoComleteCategoriaAdd.setText("");
        autoComleteProdutoAdd.setText("");
        autoComleteCategoriaAdd.requestFocus();
    }
}
