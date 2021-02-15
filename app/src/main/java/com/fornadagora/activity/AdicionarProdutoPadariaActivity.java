package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdicionarProdutoPadariaActivity extends AppCompatActivity {

    private DatabaseReference referenciaPadaria;

    private Spinner spinner;

    private Button botaoSalvar;

    private CheckBox checkBoxPaoQueijo;
    private CheckBox checkBoxCoxinha;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();
    private List<String> nomesProdutos = new ArrayList<>();
    private List<Produto> listaProdutos = new ArrayList<>();

    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_produto_padaria);

        inicializarComponentes();

        referenciaPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot teste : snapshot.getChildren()){
                        Padaria padaria = teste.getValue(Padaria.class);
                        padaria.setIdentificador(teste.getKey());
                        listaNomePadaria.add(padaria.getNome());
                        listaPadarias.add(padaria);
                    }
                    spinner.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void inicializarComponentes(){
        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        spinner = findViewById(R.id.spinnerPadarias);

        checkBoxPaoQueijo = findViewById(R.id.checkBoxPaoQueijo);
        checkBoxCoxinha = findViewById(R.id.checkBoxCoxinha);

        botaoSalvar = findViewById(R.id.buttonSalvarProdutos);
        arrayAdapter = new ArrayAdapter (this, android.R.layout.simple_spinner_dropdown_item, listaNomePadaria);
    }

    public void salvarProdutos(View view){

        String nomePadaria = spinner.getSelectedItem().toString();

        if(!checkBoxPaoQueijo.isChecked() && !checkBoxCoxinha.isChecked()){
            Toast.makeText(this, "Nenhum produto selecionado", Toast.LENGTH_SHORT).show();
        }else{
            if(!nomePadaria.isEmpty()){
                if(checkBoxPaoQueijo.isChecked()){
                    if(!listaPadarias.isEmpty()){
                        for(Padaria padaria : listaPadarias){
                            if(padaria.getNome().equals(nomePadaria)){
                                if(!padaria.getListaProdutos().isEmpty()){
                                    for(Produto produto : padaria.getListaProdutos()){
                                        if(produto.getNome().equals(checkBoxPaoQueijo.getText().toString())){
                                            Toast.makeText(this, "Produto(s) escolhido(s) já adicionados a padaria", Toast.LENGTH_SHORT).show();
                                            nomesProdutos.clear();
                                            break;
                                        }else{
                                            nomesProdutos.add(checkBoxPaoQueijo.getText().toString());
                                        }
                                    }
                                }else{
                                    nomesProdutos.add(checkBoxPaoQueijo.getText().toString());
                                }
                            }
                        }
                    }
                }
                if(checkBoxCoxinha.isChecked()){
                    if(!listaPadarias.isEmpty()){
                        for(Padaria padaria : listaPadarias){
                            if(padaria.getNome().equals(nomePadaria)){
                                if(!padaria.getListaProdutos().isEmpty()){
                                    for(Produto produto : padaria.getListaProdutos()){
                                        if(produto.getNome().equals(checkBoxCoxinha.getText().toString())){
                                            Toast.makeText(this, "Produto(s) escolhido(s) já adicionados a padaria", Toast.LENGTH_SHORT).show();
                                            nomesProdutos.clear();
                                            break;
                                        }else{
                                            nomesProdutos.add(checkBoxCoxinha.getText().toString());
                                        }
                                    }
                                }else{
                                    nomesProdutos.add(checkBoxCoxinha.getText().toString());
                                }
                            }
                        }
                    }
                }
                if(!nomesProdutos.isEmpty()){
                    for(String nomeProduto : nomesProdutos){
                        Produto produto = new Produto(nomeProduto);
                        listaProdutos.add(produto);
                    }
                    for(Padaria padaria : listaPadarias){
                        if(padaria.getNome().equals(nomePadaria)){
                            padaria.getListaProdutos().addAll(listaProdutos);
                            referenciaPadaria.child(padaria.getIdentificador()).setValue(padaria);
                        }
                    }
                    Toast.makeText(this, "Produto(s) adicionados com sucesso", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Favor escolher uma padaria", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
