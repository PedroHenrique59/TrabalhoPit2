package com.fornadagora.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.activity.MenuLateralActivity;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.Teclado;
import com.fornadagora.helper.ValidaEmail;
import com.fornadagora.model.Funcionario;
import com.fornadagora.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AdapterDadosUsuario extends RecyclerView.Adapter<AdapterDadosUsuario.MyViewHolder> {

    private List<Usuario> listaUsuarios;

    private String nomeUser;
    private String emailUser;

    private Usuario usuario;

    private boolean emailAlterado = false;

    private FirebaseAuth autenticacao;
    private FirebaseUser user;

    private Context context;

    private TextInputEditText emailInformado;
    private TextInputEditText nomeInformado;

    private Toolbar toolbar;

    public AdapterDadosUsuario(List<Usuario> listaUsuario) {
        this.listaUsuarios = listaUsuario;
    }

    @NonNull
    @Override
    public AdapterDadosUsuario.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_dados_usuario, parent, false);
        context = parent.getContext();
        return new AdapterDadosUsuario.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterDadosUsuario.MyViewHolder holder, int position) {
        usuario = listaUsuarios.get(position);

        nomeInformado.setText(usuario.getNome());
        emailInformado.setText(usuario.getEmail());

        nomeUser = nomeInformado.getText().toString();
        emailUser = emailInformado.getText().toString();
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        Button botaoSalvar;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            nomeInformado = itemView.findViewById(R.id.EditTextNomeUsu);
            emailInformado = itemView.findViewById(R.id.EditTextEmailUsu);
            botaoSalvar = itemView.findViewById(R.id.btn_salvar);
            toolbar = itemView.findViewById(R.id.toolbarPrincipal);
            autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

            configurarToolbar();

            botaoSalvar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    validarCampos(v);
                }
            });
        }

        public void validarCampos(View view) {
            Teclado.fecharTeclado(view);
            if (!nomeInformado.getText().toString().isEmpty()) {
                if (!emailInformado.getText().toString().isEmpty()) {
                    if (nomeInformado.getText().toString().equals(nomeUser) && emailInformado.getText().toString().equals(emailUser)) {
                        emitirMensagem("Nome e email informados");
                    } else {
                        if (!usuario.getEmail().equals(emailInformado.getText().toString())) {
                            emailAlterado = true;
                        }
                        salvarDados(usuario);
                    }
                }
            }
        }

        public void emitirMensagem(String nome) {
            Toast.makeText(context, nome + " são os mesmos", Toast.LENGTH_SHORT).show();
        }

        public void salvarDados(Usuario usuario) {
            if (usuario != null) {
                if (autenticacao.getCurrentUser() != null) {
                    if (emailAlterado) {
                        if (validarEmail(emailInformado.getText().toString())) {
                            user = FirebaseAuth.getInstance().getCurrentUser();
                            reautenticarUsuario(usuario);
                        } else {
                            Toast.makeText(context, "Favor informar um e-mail válido!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        atualizarDados();
                    }
                }
            }
        }
    }

    public void reautenticarUsuario(final Usuario usuario) {
        AuthCredential credential = EmailAuthProvider.getCredential(usuario.getEmail(), usuario.getSenha());
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                user.updateEmail(emailInformado.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.sendEmailVerification();
                            atualizarDados();
                        } else {
                            String erroExcecao = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                erroExcecao = "Já existe uma conta cadastrada para esse endereço de e-mail!";
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(context, "Erro: " + erroExcecao, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    public void atualizarDados() {
        String id = autenticacao.getCurrentUser().getUid();
        usuario.setIdUsuario(id);
        usuario.setNome(nomeInformado.getText().toString());
        usuario.setEmail(emailInformado.getText().toString());
        usuario.atualizarDados();
        Toast.makeText(context, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
    }

    public boolean validarEmail(String email) {
        return ValidaEmail.validarEmail(email);
    }

    public void configurarToolbar() {
        toolbar.setTitle("Alterar dados");
        toolbar.setTitleTextColor(context.getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_voltar_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) context).finish();
            }
        });
    }
}


