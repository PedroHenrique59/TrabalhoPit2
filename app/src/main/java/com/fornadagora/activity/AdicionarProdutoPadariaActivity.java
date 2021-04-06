package com.fornadagora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
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

    private Button botaoSalvar;

    private Toolbar toolbar;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();
    private List<String> listaNomesProdutos = new ArrayList<>();
    private List<String> listaCarregaNomeProduto = new ArrayList<>();
    private List<Produto> listaProdutos = new ArrayList<>();

    private ArrayList<String> listaNomeCategoria = new ArrayList<>();
    private ArrayList<Categoria> listaCategoria = new ArrayList<>();

    private ArrayAdapter arrayAdapterPadaria;
    private ArrayAdapter arrayAdapterCategoria;
    private ArrayAdapter arrayAdapterProduto;

    private Funcionario funcionarioRecuperado;
    private Categoria categoria;

    private String nomePadaria;
    private String nomeCategoria;

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
        botaoSalvar = findViewById(R.id.btn_add_produto);
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaNomePadaria);
        arrayAdapterCategoria = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaNomeCategoria);
        arrayAdapterProduto = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaCarregaNomeProduto);
    }

    public void salvarProdutos(View view) {
        limparListas();
        if (!autoComletePadariaAdd.getText().toString().isEmpty()) {
            if (!autoComleteCategoriaAdd.getText().toString().isEmpty()) {
                if (!autoComleteProdutoAdd.getText().toString().isEmpty()) {
                    nomePadaria = autoComletePadariaAdd.getText().toString();
                    nomeCategoria = autoComleteCategoriaAdd.getText().toString();
                    String nomeProduto = autoComleteProdutoAdd.getText().toString();
                    if (!listaPadarias.isEmpty()) {
                        for (Padaria padaria : listaPadarias) {
                            if (padaria.getNome().equals(nomePadaria)) {
                                if (!padaria.getListaProdutos().isEmpty()) {
                                    for (Produto produto : padaria.getListaProdutos()) {
                                        if (produto.getNome().equals(nomeProduto)) {
                                            Toast.makeText(this, "Produto escolhido já adicionado a padaria", Toast.LENGTH_SHORT).show();
                                            listaNomesProdutos.clear();
                                            break;
                                        }else{
                                            if(listaNomesProdutos.isEmpty()){
                                                listaNomesProdutos.add(nomeProduto);
                                            }
                                        }
                                    }
                                }else{
                                    listaNomesProdutos.add(nomeProduto);
                                    buscarCategoria(nomeCategoria);
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Favor escolher um produto", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Favor escolher uma categoria", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Favor escolher a padaria", Toast.LENGTH_SHORT).show();
        }
        if (!listaNomesProdutos.isEmpty()) {
            for (String nomeProduto : listaNomesProdutos) {
                Produto produto = new Produto(nomeProduto, categoria);
                listaProdutos.add(produto);
            }
            for (Padaria padaria : listaPadarias) {
                if (padaria.getNome().equals(nomePadaria)) {
                    padaria.getListaProdutos().addAll(listaProdutos);
                    referenciaPadaria.child(padaria.getIdentificador()).setValue(padaria);
                    referenciaFuncionario.child("padaria").setValue(padaria);
                }
            }
            Toast.makeText(this, "Produto adicionado com sucesso", Toast.LENGTH_SHORT).show();
        }
    }

    public void carregarPadarias() {
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
                        if (produto.getCategoria().getNome().equalsIgnoreCase(nomeCategoria)) {
                            listaCarregaNomeProduto.add(produto.getNome());
                        }
                    }
                    autoComleteProdutoAdd.setAdapter(arrayAdapterProduto);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void limparListas(){
        listaNomesProdutos.clear();
        listaProdutos.clear();
    }

    public Categoria buscarCategoria(String nome){
        if(!listaCategoria.isEmpty()){
            for(Categoria cat : listaCategoria){
                if(cat.getNome().equalsIgnoreCase(nome)){
                    categoria = cat;
                }
            }
        }
        return categoria;
    }
}
