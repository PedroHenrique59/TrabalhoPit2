package com.fornadagora.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class AdapterDadosUsuario extends RecyclerView.Adapter<AdapterDadosUsuario.MyViewHolder>{

    private List<Usuario> listaUsuarios;

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
        Usuario usuario = listaUsuarios.get(position);

        holder.nomeUsuario.setText(usuario.getNome());
        holder.emailUsuario.setText(usuario.getEmail());
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nomeUsuario;
        TextView emailUsuario;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            nomeUsuario = itemView.findViewById(R.id.textViewNomeUsu);
            emailUsuario = itemView.findViewById(R.id.textViewEmailUsu);
        }
    }
}
