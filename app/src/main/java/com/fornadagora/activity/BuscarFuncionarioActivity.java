package com.fornadagora.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.fornadagora.vo.PadariaVO;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BuscarFuncionarioActivity extends AppCompatActivity {

    private TextInputEditText editNomeFun;

    private Button botaoSalvar;

    private Toolbar toolbar;

    private AutoCompleteTextView autoCompleteFun;
    private AutoCompleteTextView autoCompletePadariaFun;

    private DatabaseReference referenciaFuncionarios;
    private DatabaseReference referenciaPadarias;

    private List<Funcionario> listaDeFuncionarios = new ArrayList<>();
    private List<Padaria> listaDePadaria = new ArrayList<>();

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<String> listaNomesFuncionarios = new ArrayList<>();

    private ArrayAdapter arrayAdapterFuncionario;
    private ArrayAdapter arrayAdapterPadaria;

    private Funcionario funcionario;

    private boolean padariaEncontrada = false;
    private boolean existeFun = false;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_funcionario);
        inicializarComponentes();
        configurarToolbar();
        buscarFuncionarios();
        buscarPadarias();
        escolherFuncionario();
        eventoClickBotaoSalvar();
    }

    public void inicializarComponentes() {
        editNomeFun = findViewById(R.id.edit_text_fun_nome_adm);
        botaoSalvar = findViewById(R.id.btn_salvar_dados_fun_adm);
        toolbar = findViewById(R.id.toolbarPrincipal);
        autoCompleteFun = findViewById(R.id.autoCompleteFunEditAdm);
        autoCompletePadariaFun = findViewById(R.id.autoComletePadariaFunEditAdm);
        referenciaFuncionarios = ConfiguracaoFirebase.getFirebase();
        referenciaFuncionarios = referenciaFuncionarios.child("funcionarios");
        referenciaPadarias = ConfiguracaoFirebase.getFirebase();
        referenciaPadarias = referenciaPadarias.child("padarias");
        arrayAdapterPadaria = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNomePadaria);
        context = this;
    }

    public void buscarFuncionarios() {
        referenciaFuncionarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listaDeFuncionarios.clear();
                    listaNomesFuncionarios.clear();
                    for (DataSnapshot snapFun : snapshot.getChildren()) {
                        Funcionario funcionario = snapFun.getValue(Funcionario.class);
                        if (funcionario != null) {
                            listaNomesFuncionarios.add(funcionario.getNome());
                            listaDeFuncionarios.add(funcionario);
                        }
                    }
                    arrayAdapterFuncionario = new ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listaNomesFuncionarios);
                    autoCompleteFun.setAdapter(arrayAdapterFuncionario);
                    autoCompleteFun.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buscarPadarias() {
        referenciaPadarias.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapPadaria : snapshot.getChildren()) {
                        Padaria padaria = snapPadaria.getValue(Padaria.class);
                        if (padaria != null) {
                            padaria.setIdentificador(snapPadaria.getKey());
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

    public void escolherFuncionario() {
        autoCompleteFun.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listaDeFuncionarios.isEmpty()) {
                    for (Funcionario fun : listaDeFuncionarios) {
                        String nome = (String) parent.getItemAtPosition(position);
                        if (fun.getNome().equals(nome)) {
                            funcionario = new Funcionario();
                            funcionario = fun;
                            buscarPadariaFuncionario(funcionario.getPadariaVO().getIdentificador());
                            preencherCampos(funcionario);
                        }
                    }
                }
            }
        });
    }

    public void preencherCampos(Funcionario funcionario) {
        editNomeFun.setText(funcionario.getNome());
        autoCompletePadariaFun.setAdapter(arrayAdapterPadaria);
        autoCompletePadariaFun.setText(funcionario.getPadaria().getNome());
    }

    public void eventoClickBotaoSalvar() {
        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Teclado.fecharTeclado(v);
                salvarDados();
            }
        });
    }

    public void salvarDados() {
        if (!autoCompleteFun.getText().toString().isEmpty()) {
            if (existeFuncionario()) {
                if (!editNomeFun.getText().toString().isEmpty()) {
                    if (!autoCompletePadariaFun.getText().toString().isEmpty()) {
                        atualizarDadosFuncionario(funcionario);
                    } else {
                        Toast.makeText(this, "Favor escolher uma padaria!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Favor preencher o nome do funcionário!", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(this, "Funcionário escolhido inexistente. Favor escolher um válido!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Favor escolher um funcionário!", Toast.LENGTH_LONG).show();
        }
    }

    public boolean existeFuncionario() {
        existeFun = false;
        if (!listaNomesFuncionarios.isEmpty()) {
            for (String nomeFun : listaNomesFuncionarios) {
                if (nomeFun.equalsIgnoreCase(autoCompleteFun.getText().toString())) {
                    existeFun = true;
                }
            }
        }
        return existeFun;
    }

    public void buscarPadariaFuncionario(String idPadaria) {
        if (!listaDePadaria.isEmpty()) {
            for (Padaria padaria : listaDePadaria) {
                if (padaria.getIdentificador().equalsIgnoreCase(idPadaria)) {
                    funcionario.setPadaria(padaria);
                    padariaEncontrada = true;
                }
            }
        }
    }

    public void atualizarDadosFuncionario(Funcionario funcionario) {
        funcionario.setNome(editNomeFun.getText().toString());
        buscarPadariaEscolhida(autoCompletePadariaFun.getText().toString());
        if (padariaEncontrada) {
            funcionario.atualizarDadosPeloAdm();
            autoCompleteFun.setText(funcionario.getNome());
            buscarFuncionarios();
            limparCampos();
            Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Padaria informada inválida. Favor escolher uma válida.", Toast.LENGTH_SHORT).show();
        }
    }

    public void limparCampos() {
        editNomeFun.setText("");
        autoCompleteFun.requestFocus();
        autoCompletePadariaFun.setText("");
    }

    public void configurarToolbar() {
        toolbar.setTitle("Alterar Dados Funcionário");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_voltar_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) context).finish();
            }
        });
    }

    public void buscarPadariaEscolhida(String nomePadaria) {
        padariaEncontrada = false;
        if (!listaDePadaria.isEmpty()) {
            for (Padaria padaria : listaDePadaria) {
                if (padaria.getNome().equalsIgnoreCase(nomePadaria)) {
                    PadariaVO padariaVO = new PadariaVO(padaria.getIdentificador());
                    funcionario.setPadariaVO(padariaVO);
                    padariaEncontrada = true;
                }
            }
        }
    }
}