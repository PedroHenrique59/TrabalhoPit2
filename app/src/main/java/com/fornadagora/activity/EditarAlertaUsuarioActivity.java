package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Alerta;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EditarAlertaUsuarioActivity extends AppCompatActivity {

    private Alerta alerta;
    private Alerta alertaEditado;

    private TextInputEditText editTextNomeAlerta;
    private AutoCompleteTextView autoComletePadaria;
    private AutoCompleteTextView autoComleteProduto;

    private DatabaseReference referenciaPadaria;
    private DatabaseReference referenciaAlerta;

    private FirebaseAuth autenticacao;

    private ArrayAdapter arrayAdapterPadaria;
    private ArrayAdapter arrayAdapterProduto;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();

    private List<String> listaNomeProduto = new ArrayList<>();
    private List<Produto> listaProdutos = new ArrayList<>();

    private boolean padariaDisponivel = false;
    private boolean produtoDisponivel = false;

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
        carregarProdutos();
    }

    public void inicializarComponentes(){
        editTextNomeAlerta = findViewById(R.id.editTextNomeAlertaEdit);
        autoComletePadaria = findViewById(R.id.autoComletePadariaAlertEdit);
        autoComleteProduto = findViewById(R.id.autoComleteProdutoAlertEdit);
        toolbar = findViewById(R.id.toolbarPrincipal);
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaAlerta = ConfiguracaoFirebase.getFirebase();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomePadaria);
        arrayAdapterProduto = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomeProduto);
    }

    public void preencherCamposComDadosAlerta(Alerta alerta){
        if(alerta != null){
            editTextNomeAlerta.setText(alerta.getNome());
            recuperarPadariaAlerta(alerta.getIdPadaria());
            autoComletePadaria.setText(alerta.getPadaria().getNome());
            autoComleteProduto.setText(alerta.getProduto().getNome());
        }
    }

    public void carregarPadarias(){
        referenciaPadaria.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot padariaSnap : snapshot.getChildren()) {
                        Padaria padaria = padariaSnap.getValue(Padaria.class);
                        if (!padaria.getListaProdutos().isEmpty()) {
                            listaNomePadaria.add(padaria.getNome());
                            listaPadarias.add(padaria);
                        }
                    }
                    autoComletePadaria.setAdapter(arrayAdapterPadaria);
                    preCarregarProdutos();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void preCarregarProdutos(){
        if (!listaPadarias.isEmpty()) {
            listaNomeProduto.clear();
            listaProdutos.clear();
            for (Padaria padaria : listaPadarias) {
                String nomePadaria = autoComletePadaria.getText().toString();
                if (padaria.getNome().equals(nomePadaria)) {
                    for (Produto produto : padaria.getListaProdutos()) {
                        listaNomeProduto.add(produto.getNome());
                        listaProdutos.add(produto);
                    }
                }
            }
            autoComleteProduto.setAdapter(arrayAdapterProduto);
        }
    }

    public void carregarProdutos(){
       autoComletePadaria.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               if (!listaPadarias.isEmpty()) {
                   listaNomeProduto.clear();
                   listaProdutos.clear();
                   for (Padaria padaria : listaPadarias) {
                       String nomePadaria = autoComletePadaria.getText().toString();
                       if (padaria.getNome().equals(nomePadaria)) {
                           for (Produto produto : padaria.getListaProdutos()) {
                               listaNomeProduto.add(produto.getNome());
                               listaProdutos.add(produto);
                           }
                       }
                   }
                   autoComleteProduto.setAdapter(arrayAdapterProduto);
               }
           }
       });
    }
    public void validarAntesSalvar(View view){
        if(!editTextNomeAlerta.getText().toString().isEmpty()){
            String nomeEscolhido = editTextNomeAlerta.getText().toString();
            alertaEditado = new Alerta();
            alertaEditado.setNome(nomeEscolhido);
        }
        if(!autoComletePadaria.getText().toString().isEmpty()){
            String nomePadariaEscolhida = autoComletePadaria.getText().toString();
            if(!listaPadarias.isEmpty()){
                for(Padaria padaria : listaPadarias){
                    if(padaria.getNome().equals(nomePadariaEscolhida)){
                        padariaDisponivel = true;
                        alertaEditado.setPadaria(padaria);
                    }
                }
                if(padariaDisponivel){
                    if(!autoComleteProduto.getText().toString().isEmpty()){
                        String nomeProdutoEscolhido = autoComleteProduto.getText().toString();
                        if(!listaProdutos.isEmpty()){
                            for(Produto produto : listaProdutos){
                                if(produto.getNome().equals(nomeProdutoEscolhido)){
                                    produtoDisponivel = true;
                                    alertaEditado.setProduto(produto);
                                }
                            }
                            if(!produtoDisponivel){
                                Toast.makeText(this, "Este produto não está disponível para a padaria escolhida. Favor escolher um que esteja na listagem acima ", Toast.LENGTH_SHORT).show();
                            }
                            if(produtoDisponivel && padariaDisponivel){
                                salvarEdicaoAlerta(alertaEditado);
                            }
                        }
                    }else{
                        Toast.makeText(this, "Favor escolher um produto", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "Esta padaria não está disponível. Favor escolher uma que esteja na listagem acima", Toast.LENGTH_LONG).show();
                }
            }
        }else{
            Toast.makeText(this, "Favor escolher uma padaria", Toast.LENGTH_SHORT).show();
        }
    }

    public void salvarEdicaoAlerta(final Alerta alertaEditado){
        if(autenticacao.getCurrentUser() != null){
            String idUsuario = autenticacao.getCurrentUser().getUid();
            referenciaAlerta = referenciaAlerta.child("usuarios").child(idUsuario).child("alerta").child(alerta.getIdAlerta());
            referenciaAlerta.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        alertaEditado.atualizarDados(alertaEditado, referenciaAlerta);
                        Toast.makeText(context, "Alerta editado com sucesso", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void configurarToolbar(){
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

    public void abrirMenuLateral(){
        Intent intent = new Intent(this, MenuLateralActivity.class);
        startActivity(intent);
        finish();
    }

    public void recuperarPadariaAlerta(String idPadaria){
        referenciaPadaria.child(idPadaria).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Padaria teste = snapshot.getValue(Padaria.class);
                    alerta.setPadaria(teste);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}