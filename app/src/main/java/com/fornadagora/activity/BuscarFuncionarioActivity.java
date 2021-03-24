package com.fornadagora.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.ValidaEmail;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BuscarFuncionarioActivity extends AppCompatActivity {

    private TextInputEditText editNomeFun;
    private TextInputEditText editEmailFun;
    private Button botaoSalvar;

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

    private Funcionario funcionario;

    private FirebaseUser user;

    private boolean padariaEncontrada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_funcionario);
        inicializarComponentes();
        buscarFuncionarios();
        buscarPadarias();
        escolherFuncionario();
        eventoClickBotaoSalvar();
    }

    public void inicializarComponentes(){
        editNomeFun = findViewById(R.id.edit_text_fun_nome_adm);
        editEmailFun = findViewById(R.id.edit_text_fun_email_adm);
        botaoSalvar = findViewById(R.id.btn_salvar_dados_fun_adm);
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
                    listaDeFuncionarios.clear();
                    listaNomesFuncionarios.clear();
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
                            funcionario = fun;
                            preencherCampos(funcionario);
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

    public void eventoClickBotaoSalvar(){
        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarDados();
            }
        });
    }

    public void salvarDados(){
        if(!autoCompleteFun.getText().toString().isEmpty()){
            if(!editNomeFun.getText().toString().isEmpty()){
                if(!editEmailFun.getText().toString().isEmpty()){
                    if(!autoCompletePadariaFun.getText().toString().isEmpty()){
                        String nome = editNomeFun.getText().toString();
                        String email = editEmailFun.getText().toString();
                        if(!email.isEmpty()){
                            if(validarEmail(email)){
                                user = FirebaseAuth.getInstance().getCurrentUser();
                                reautenticarFuncionario(funcionario);
                            }else{
                                Toast.makeText(this, "Informe um e-mail válido", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }else{
                        Toast.makeText(this, "Favor escolher uma padaria", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "Favor preencher o e-mail do funcionário", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Favor preencher o nome do funcionário", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Favor escolher um funcionário", Toast.LENGTH_SHORT).show();
        }
    }

    public void buscarPadariaFuncionario(Funcionario funcionario){
        if(!listaDePadaria.isEmpty()){
            for(Padaria padaria : listaDePadaria){
                if(padaria.getNome().equals(autoCompletePadariaFun.getText().toString())){
                    funcionario.setPadaria(padaria);
                    padariaEncontrada = true;
                }
            }
        }
    }

    public boolean validarEmail(String email){
        return ValidaEmail.validarEmail(email);
    }

    public void reautenticarFuncionario(final Funcionario funcionario){
        AuthCredential credential = EmailAuthProvider
                .getCredential(funcionario.getEmail(), funcionario.getSenha());
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                user.updateEmail(editEmailFun.getText().toString());
                atualizarDadosFuncionario(funcionario);
            }
        });
    }

    public void atualizarDadosFuncionario(Funcionario funcionario){
        funcionario.setNome(editNomeFun.getText().toString());
        funcionario.setEmail(editEmailFun.getText().toString());
        buscarPadariaFuncionario(funcionario);
        if(padariaEncontrada){
            funcionario.atualizarDadosPeloAdm();
            autoCompleteFun.setText(funcionario.getNome());
            buscarFuncionarios();
            Toast.makeText(this, "Dados atualizados com sucesso", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "A padaria informada não existe", Toast.LENGTH_SHORT).show();
        }
    }
}