package com.fornadagora.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BuscarFuncionarioActivity extends AppCompatActivity {

    private TextInputEditText editNomeFun;
    private TextInputEditText editEmailFun;

    private AutoCompleteTextView autoCompleteFun;
    private AutoCompleteTextView autoCompletePadariaFun;

    private DatabaseReference referenciaFuncionarios;
    private DatabaseReference referenciaPadarias;

    private List<Funcionario> listaDeFuncionarios = new ArrayList<>();
    private List<String> listaNomesFuncionarios = new ArrayList<>();
    private List<Padaria> listaDePadaria = new ArrayList<>();
    private List<String> listaNomePadaria = new ArrayList<>();

    private ArrayAdapter arrayAdapterFuncionario;
    private ArrayAdapter arrayAdapterPadaria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_funcionario);
        inicializarComponentes();
        buscarFuncionarios();
        buscarPadarias();
        escolherFuncionario();
    }

    public void inicializarComponentes(){
        editNomeFun = findViewById(R.id.edit_text_fun_nome_adm);
        editEmailFun = findViewById(R.id.edit_text_fun_email_adm);
        autoCompleteFun = findViewById(R.id.autoCompleteFunEditAdm);
        autoCompletePadariaFun = findViewById(R.id.autoComletePadariaFunEditAdm);
        referenciaFuncionarios = ConfiguracaoFirebase.getFirebase();
        referenciaFuncionarios = referenciaFuncionarios.child("funcionarios");
        referenciaPadarias = ConfiguracaoFirebase.getFirebase();
        referenciaPadarias = referenciaPadarias.child("padarias");
        arrayAdapterFuncionario = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomesFuncionarios);
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomePadaria);
    }

    public void buscarFuncionarios(){
        referenciaFuncionarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot snapFun : snapshot.getChildren()) {
                        Funcionario funcionario = snapFun.getValue(Funcionario.class);
                        if(funcionario != null){
                            listaNomesFuncionarios.add(funcionario.getNome());
                            listaDeFuncionarios.add(funcionario);
                        }
                    }
                    autoCompleteFun.setAdapter(arrayAdapterFuncionario);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buscarPadarias(){
        referenciaPadarias.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot snapPadaria : snapshot.getChildren()) {
                        Padaria padaria = snapPadaria.getValue(Padaria.class);
                        if(padaria != null){
                            listaDePadaria.add(padaria);
                            listaNomePadaria.add(padaria.getNome());
                        }
                    }
                    autoCompletePadariaFun.setAdapter(arrayAdapterPadaria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void escolherFuncionario(){
        autoCompleteFun.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!listaDeFuncionarios.isEmpty()){
                    for(Funcionario fun : listaDeFuncionarios){
                        String nome = (String) parent.getItemAtPosition(position);
                        if(fun.getNome().equals(nome)){
                            preencherCampos(fun);
                        }
                    }
                }
            }
        });
    }

    public void preencherCampos(Funcionario funcionario){
        editNomeFun.setText(funcionario.getNome());
        editEmailFun.setText(funcionario.getEmail());
        autoCompletePadariaFun.setAdapter(arrayAdapterPadaria);
        autoCompletePadariaFun.setText(funcionario.getPadaria().getNome());
    }
}