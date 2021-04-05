package com.fornadagora.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.activity.MenuLateralActivity;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Usuario;
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

public class AdapterDadosFuncionario extends RecyclerView.Adapter<AdapterDadosFuncionario.MyViewHolder> {

    private List<Funcionario> listaFuncionarios;

    private String nomeFun;
    private String emailFun;

    private ArrayAdapter arrayAdapterPadaria;

    private List<String> listaNomePadaria = new ArrayList<>();
    private List<Padaria> listaPadarias = new ArrayList<>();

    private AutoCompleteTextView autoComletePadariaFunEdit;
    private Toolbar toolbar;

    private Funcionario funcionario;

    private boolean emailAlterado = false;
    private boolean ehAdm = false;

    private FirebaseAuth autenticacao;
    private DatabaseReference referenciaPadarias;
    private DatabaseReference referenciaAdm;

    private FirebaseUser user;

    private Context context;

    private TextInputEditText nomeInformado;
    private TextInputEditText emailInformado;

    public AdapterDadosFuncionario(List<Funcionario> listaFuncionario) {
        this.listaFuncionarios = listaFuncionario;
    }

    @NonNull
    @Override
    public AdapterDadosFuncionario.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_dados_funcionario, parent, false);
        context = parent.getContext();
        inicializarComponentes();
        return new AdapterDadosFuncionario.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterDadosFuncionario.MyViewHolder holder, int position) {
        funcionario = listaFuncionarios.get(position);

        nomeInformado.setText(funcionario.getNome());
        emailInformado.setText(funcionario.getEmail());

        nomeFun = nomeInformado.getText().toString();
        emailFun = emailInformado.getText().toString();

        verificarPerfilLogado();
    }

    @Override
    public int getItemCount() {
        return listaFuncionarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        Button botaoSalvar;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            nomeInformado = itemView.findViewById(R.id.edit_text_fun_nome);
            emailInformado = itemView.findViewById(R.id.edit_text_fun_email);
            autoComletePadariaFunEdit = itemView.findViewById(R.id.autoComletePadariaFunEdit);
            toolbar = itemView.findViewById(R.id.toolbarPrincipal);

            botaoSalvar = itemView.findViewById(R.id.btn_salvar_dados_fun);
            autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

            carregarListaPadarias();
            configurarToolbar();

            botaoSalvar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    validarCamposAlterados(v);
                }
            });
        }
        public void validarCamposAlterados(View view) {
            if (!nomeInformado.getText().toString().isEmpty()) {
                if (!emailInformado.getText().toString().isEmpty()) {
                    if (nomeInformado.getText().toString().equals(nomeFun) && emailInformado.getText().toString().equals(emailFun)) {
                        emitirMensagem("Nome e email");
                    } else {
                        if(!funcionario.getEmail().equals(emailInformado.getText().toString())){
                            emailAlterado = true;
                        }
                        if(!funcionario.getNome().equals(nomeInformado.getText().toString())){
                            funcionario.setNome(nomeInformado.getText().toString());
                        }
                        salvarDados(funcionario);
                    }
                }
            }
        }

        public void emitirMensagem(String nome) {
            Toast.makeText(context, nome + " são os mesmos", Toast.LENGTH_SHORT).show();
        }

        public void salvarDados(final Funcionario funcionario){
            if(funcionario != null){
                if(autenticacao.getCurrentUser() != null){
                    if(emailAlterado){
                        user = FirebaseAuth.getInstance().getCurrentUser();
                        reautenticarFuncionario(funcionario);
                    }
                    atualizarDados();
                }
            }
        }
    }

    public void inicializarComponentes(){
        arrayAdapterPadaria = new ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, listaNomePadaria);
        referenciaAdm = ConfiguracaoFirebase.getFirebase();
    }

    public void carregarListaPadarias() {

        referenciaPadarias = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaPadarias.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapShotPadaria : snapshot.getChildren()) {
                        Padaria padaria = snapShotPadaria.getValue(Padaria.class);
                        listaPadarias.add(padaria);
                        listaNomePadaria.add(padaria.getNome());
                    }
                    if(ehAdm){
                        autoComletePadariaFunEdit.setAdapter(arrayAdapterPadaria);
                    }else{
                        autoComletePadariaFunEdit.setAdapter(null);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void reautenticarFuncionario(final Funcionario funcionario){
        AuthCredential credential = EmailAuthProvider
                .getCredential(funcionario.getEmail(), funcionario.getSenha());
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                user.updateEmail(emailInformado.getText().toString());
                atualizarDados();
            }
        });
    }

    public void atualizarDados(){
        String id = autenticacao.getCurrentUser().getUid();
        funcionario.setIdFuncionario(id);
        funcionario.setEmail(emailInformado.getText().toString());
        funcionario.atualizarDados();
        Toast.makeText(context, "Dados atualizados com sucesso", Toast.LENGTH_SHORT).show();
    }

    public void verificarPerfilLogado(){
        if(funcionario != null){
            desabilitarComponentes(false);
        }else{
            if(autenticacao.getCurrentUser() != null){

                String id = autenticacao.getCurrentUser().getUid();
                referenciaAdm = referenciaAdm.child("usuarios").child(id);

                referenciaAdm.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Usuario usuarioAdm = snapshot.getValue(Usuario.class);
                            if(usuarioAdm != null){
                                if(usuarioAdm.getTipoPerfil().equals("Administrador")){
                                    ehAdm = true;
                                    desabilitarComponentes(ehAdm);
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
    }

    public void desabilitarComponentes(boolean ehAdm){
        if(ehAdm){
            autoComletePadariaFunEdit.setEnabled(true);
        }else{
            autoComletePadariaFunEdit.setEnabled(false);
        }
    }

    public void configurarToolbar(){
        toolbar.setTitle("Alterar dados");
        toolbar.setTitleTextColor(context.getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_voltar_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirMenuLateral();
            }
        });
    }

    public void abrirMenuLateral(){
        Intent intent = new Intent(context, MenuLateralActivity.class);
        context.startActivity(intent);
    }
}
