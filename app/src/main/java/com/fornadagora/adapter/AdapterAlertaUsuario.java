package com.fornadagora.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.model.Alerta;

import java.util.List;

public class AdapterAlertaUsuario extends RecyclerView.Adapter<AdapterAlertaUsuario.MyViewHolder> {

    private List<Alerta> listaAlertas;

    public AdapterAlertaUsuario(List<Alerta> listaAlerta) {
        this.listaAlertas = listaAlerta;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_alerta_usuario, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Alerta alerta = listaAlertas.get(position);

        holder.nomeAlerta.setText(alerta.getNome());
        holder.nomePadaria.setText(alerta.getPadaria().getNome());
        holder.nomeProduto.setText(alerta.getProduto().getNome());
    }

    @Override
    public int getItemCount() {
        return listaAlertas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nomeAlerta;
        TextView nomePadaria;
        TextView nomeProduto;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nomeAlerta = itemView.findViewById(R.id.textViewNomeAlerta);
            nomePadaria = itemView.findViewById(R.id.textViewNomePadaria);
            nomeProduto = itemView.findViewById(R.id.textViewNomeProduto);
        }
    }
}
