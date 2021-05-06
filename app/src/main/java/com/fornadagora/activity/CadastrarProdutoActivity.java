package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.Teclado;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.vo.CategoriaVO;
import com.fornadagora.vo.PadariaVO;
import com.fornadagora.vo.ProdutoVO;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CadastrarProdutoActivity extends AppCompatActivity {

    private TextInputEditText editTextNome;

    private AutoCompleteTextView autoCompletePadaria;
    private AutoCompleteTextView autoCompleteCategoria;

    private Button botaoSalvar;

    private Toolbar toolbar;

    private DatabaseReference referenciaCategoria;
    private DatabaseReference referenciaProduto;
    private DatabaseReference referenciaFuncionario;
    private DatabaseReference referenciaPadaria;

    private static DatabaseReference referenciaPadariaStatica;
    private static DatabaseReference referenciaProdutoStatic;

    private FirebaseAuth autenticacao;

    private ArrayAdapter arrayAdapterPadaria;
    private ArrayAdapter arrayAdapterCategoria;

    private ArrayList<String> listaNomeCategoria = new ArrayList<>();
    private ArrayList<Categoria> listaCategoria = new ArrayList<>();
    private static ArrayList<Categoria> listaCategoriaStatica = new ArrayList<>();
    private static List<ProdutoVO> listaProdutosVO = new ArrayList<>();

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();
    private static List<Padaria> listaPadariasStatica = new ArrayList<>();

    private boolean padariaValida = false;
    private boolean categoriaExiste = false;
    private boolean produtoExiste = false;

    private static boolean produtoJaSalvo = false;
    private static boolean produtoValido = false;

    private Categoria categoria;
    private Funcionario funcionarioRecuperado;

    private static ProdutoVO produtoVO;
    private static Categoria categoriaProduto;
    private static Padaria padariaProduto;

    private static String nomeCategoria;

    private Context context;
    private static Context contextStatic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_produto);
        inicializarComponentes();
        validarFuncionario();
        carregarPadarias();
        carregarCategorias();
        configurarToolbar();

        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Teclado.fecharTeclado(v);
                salvar();
            }
        });
    }

    public void inicializarComponentes() {
        editTextNome = findViewById(R.id.editTextNomeProduto);
        autoCompletePadaria = findViewById(R.id.autoComletePadaria);
        autoCompleteCategoria = findViewById(R.id.autoComleteCategoria);
        toolbar = findViewById(R.id.toolbarPrincipal);
        botaoSalvar = findViewById(R.id.btn_cadastrar_produto);
        contextStatic = this;
        context = this;
        referenciaProdutoStatic = ConfiguracaoFirebase.getFirebase().child("produtos");
        referenciaPadariaStatica = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
        referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos");
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaNomePadaria);
        arrayAdapterCategoria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomeCategoria);
    }

    public void configurarToolbar() {
        toolbar.setTitle("Cadastrar Produto");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_voltar_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) context).finish();
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

    public void carregarPadarias() {
        limparListasStaticas();
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
                                    listaPadariasStatica.add(padaria);
                                }
                            }
                            autoCompletePadaria.setAdapter(arrayAdapterPadaria);
                            autoCompletePadaria.setText(listaNomePadaria.get(0));
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

    public void carregarCategorias() {
        limparListasStaticas();
        referenciaCategoria.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapCategoria : snapshot.getChildren()) {
                        Categoria cat = snapCategoria.getValue(Categoria.class);
                        cat.setIdentificador(snapCategoria.getKey());
                        listaNomeCategoria.add(cat.getNome());
                        listaCategoria.add(cat);
                        listaCategoriaStatica.add(cat);
                    }
                    autoCompleteCategoria.setAdapter(arrayAdapterCategoria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void salvar() {
        if (!autoCompletePadaria.getText().toString().isEmpty()) {
            if (!editTextNome.getText().toString().isEmpty()) {
                if (!autoCompleteCategoria.getText().toString().isEmpty()) {
                    String nomePadaria = autoCompletePadaria.getText().toString();
                    String nomeProduto = editTextNome.getText().toString();
                    nomeCategoria = autoCompleteCategoria.getText().toString();
                    if (validarPadaria(nomePadaria)) {
                        if (validarCategoria(nomeCategoria)) {
                            Categoria categ = buscarCategoria(nomeCategoria);
                            CategoriaVO categVO = new CategoriaVO(categ.getIdentificador());
                            Padaria padaria = buscarPadaria(nomePadaria);
                            PadariaVO padariaVO = new PadariaVO(padaria.getIdentificador());
                            Produto produto = new Produto(nomeProduto, categVO, padariaVO);
                            validarProduto(produto);
                        } else {
                            Toast.makeText(this, "A categoria informada é inválida. Favor informar uma válida", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "A padaria informada é inválida. Favor informar uma válida", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Favor escolher uma categoria para o produto", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Favor informar no nome do produto", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Favor informar a padaria", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validarPadaria(String nomePadaria) {
        if (!listaNomePadaria.isEmpty()) {
            for (String nome : listaNomePadaria) {
                if (nome.equalsIgnoreCase(nomePadaria)) {
                    padariaValida = true;
                }
            }
        }
        return padariaValida;
    }

    public boolean validarCategoria(String nome) {
        categoriaExiste = false;
        if (!listaCategoria.isEmpty()) {
            for (Categoria cat : listaCategoria) {
                if (cat.getNome().equalsIgnoreCase(nome)) {
                    categoriaExiste = true;
                }
            }
        }
        return categoriaExiste;
    }

    public static Categoria buscarCategoria(String nome) {
        if (!listaCategoriaStatica.isEmpty()) {
            for (Categoria cat : listaCategoriaStatica) {
                if (cat.getNome().equalsIgnoreCase(nome)) {
                    categoriaProduto = cat;
                }
            }
        }
        return categoriaProduto;
    }

    public static Padaria buscarPadaria(String nome) {
        if (!listaPadariasStatica.isEmpty()) {
            for (Padaria pad : listaPadariasStatica) {
                if (pad.getNome().equalsIgnoreCase(nome)) {
                    padariaProduto = pad;
                }
            }
        }
        return padariaProduto;
    }

    public void validarProduto(final Produto prod) {
        produtoExiste = false;
        referenciaProduto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapProduto : snapshot.getChildren()) {
                        Produto produto = snapProduto.getValue(Produto.class);
                        if (produto.getNome().equalsIgnoreCase(prod.getNome()) && produto.getPadariaVO().getIdentificador().equalsIgnoreCase(prod.getPadariaVO().getIdentificador())) {
                            produtoExiste = true;
                            Toast.makeText(context, "Esse produto já foi salvo", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (!produtoExiste) {
                        prod.salvar();
                        Toast.makeText(context, "Produto salvo com sucesso", Toast.LENGTH_SHORT).show();
                        limparCampos();
                    }
                } else {
                    prod.salvar();
                    Toast.makeText(context, "Produto salvo com sucesso", Toast.LENGTH_SHORT).show();
                    limparCampos();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void buscarProdutoESalvarNaPadaria(final String id) {
        produtoJaSalvo = false;
        produtoValido = false;
        limparListas();
        referenciaProdutoStatic.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapProduto : snapshot.getChildren()) {
                        Produto produto = dataSnapProduto.getValue(Produto.class);
                        if (produto.getId().equalsIgnoreCase(id)) {
                            produtoValido = true;
                            produtoVO = new ProdutoVO();
                            produtoVO.setIdProduto(dataSnapProduto.getKey());
                            buscarCategoriaProduto(nomeCategoria);
                            if (categoriaProduto != null) {
                                produtoVO.setIdCategoria(categoriaProduto.getIdentificador());
                                listaProdutosVO.add(produtoVO);
                                for (Padaria padaria : listaPadariasStatica) {
                                    if (padaria.getListaProdutosVO().isEmpty()) {
                                        padaria.getListaProdutosVO().addAll(listaProdutosVO);
                                        referenciaPadariaStatica.child(padaria.getIdentificador()).setValue(padaria);
                                        listaPadariasStatica.clear();
                                        listaPadariasStatica.add(padaria);
                                        Toast.makeText(contextStatic, "Produto salvo com sucesso!", Toast.LENGTH_LONG).show();
                                        //limparCampos();
                                    } else {
                                        for (ProdutoVO produtoListaVO : padaria.getListaProdutosVO()) {
                                            if (produtoListaVO.getIdProduto().equalsIgnoreCase(produtoVO.getIdProduto())) {
                                                Toast.makeText(contextStatic, "Esse produto já foi adicionado a essa padaria!", Toast.LENGTH_LONG).show();
                                                produtoJaSalvo = true;
                                                break;
                                            }
                                        }
                                        if (!produtoJaSalvo) {
                                            padaria.getListaProdutosVO().addAll(listaProdutosVO);
                                            referenciaPadariaStatica.child(padaria.getIdentificador()).setValue(padaria);
                                            listaPadariasStatica.clear();
                                            listaPadariasStatica.add(padaria);
                                            Toast.makeText(contextStatic, "Produto salvo com sucesso!", Toast.LENGTH_LONG).show();
                                            //limparCampos();
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(contextStatic, "Produto/Categoria escolhido(a) inválidos. Favor selecionar algum(a) válida.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
                if (!produtoValido) {
                    Toast.makeText(contextStatic, "Produto/Categoria escolhido(a) inválidos. Favor selecionar algum(a) válida.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static Categoria buscarCategoriaProduto(String nome) {
        categoriaProduto = null;
        if (!listaCategoriaStatica.isEmpty()) {
            for (Categoria cat : listaCategoriaStatica) {
                if (cat.getNome().equalsIgnoreCase(nome)) {
                    categoriaProduto = cat;
                }
            }
        }
        return categoriaProduto;
    }

    public void limparCampos() {
        editTextNome.setText("");
        autoCompleteCategoria.setText("");
        editTextNome.requestFocus();
    }

    public void limparListasStaticas() {
        listaPadariasStatica.clear();
        listaCategoriaStatica.clear();
    }

    public static void limparListas() {
        listaProdutosVO.clear();
    }
}