package com.fornadagora.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.model.Produto;

import java.util.List;

public class AdapterProdutos extends RecyclerView.Adapter<AdapterProdutos.MyViewHolder>{

    private List<Produto> listaProdutos;

    private Context context;

    private TextView nomeProduto;

    public AdapterProdutos(List<Produto> listaProduto) {
        this.listaProdutos = listaProduto;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_produto, parent, false);
        context = parent.getContext();
        return new AdapterProdutos.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Produto produto = listaProdutos.get(position);
        nomeProduto.setText(produto.getNome());
    }

    @Override
    public int getItemCount() {
        return listaProdutos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            nomeProduto = itemView.findViewById(R.id.textViewNomeProdutoAdp);
        }
    }
}
