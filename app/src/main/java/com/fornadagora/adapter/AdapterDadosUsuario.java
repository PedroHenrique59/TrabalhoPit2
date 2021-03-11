package com.fornadagora.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.helper.Base64Custom;
import com.fornadagora.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class AdapterDadosUsuario extends RecyclerView.Adapter<AdapterDadosUsuario.MyViewHolder> {

    private List<Usuario> listaUsuarios;

    private String nomeUser;
    private String emailUser;
    private Usuario usuario;

    private FirebaseAuth autenticacao;
    private DatabaseReference referenciaUsuario;

    private Context context;

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

        holder.nomeUsuario.setText(usuario.getNome());
        holder.emailUsuario.setText(usuario.getEmail());

        nomeUser = holder.nomeUsuario.getText().toString();
        emailUser = holder.emailUsuario.getText().toString();
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        EditText nomeUsuario;
        EditText emailUsuario;
        Button botaoSalvar;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            nomeUsuario = itemView.findViewById(R.id.EditTextNomeUsu);
            emailUsuario = itemView.findViewById(R.id.EditTextEmailUsu);
            botaoSalvar = itemView.findViewById(R.id.btn_salvar);

            botaoSalvar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    atualizarDados(v);
                }
            });
        }
        public void atualizarDados(View view) {
            if (!nomeUsuario.getText().toString().isEmpty()) {
                if (!emailUsuario.getText().toString().isEmpty()) {
                    if (nomeUsuario.getText().toString().equals(nomeUser) && emailUsuario.getText().toString().equals(emailUser)) {
                        emitirMensagem("Nome e email");
                    } else {
                        usuario.setNome(nomeUsuario.getText().toString());
                        usuario.setEmail(emailUsuario.getText().toString());
                        salvarDados(usuario);
                    }
                }
            }
        }
        public void emitirMensagem(String nome) {
            Toast.makeText(context, nome + " são os mesmos", Toast.LENGTH_SHORT).show();
        }
        public void salvarDados(Usuario usuario){
            if(usuario != null){
                String email = usuario.getEmail();
                String id = Base64Custom.codificarBase64(email);
                usuario.setIdUsuario(id);
                usuario.salvar();

            }
        }
    }
}
