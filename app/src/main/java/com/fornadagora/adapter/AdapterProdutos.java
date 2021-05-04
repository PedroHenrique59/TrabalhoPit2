package com.fornadagora.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.activity.EditarCategoriaActivity;
import com.fornadagora.activity.EditarProdutoActivity;
import com.fornadagora.activity.NaoExisteAlertaActivity;
import com.fornadagora.activity.NaoExisteProdutoActivity;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Produto;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterProdutos extends RecyclerView.Adapter<AdapterProdutos.MyViewHolder>{

    private List<Produto> listaProdutos;

    private Context context;

    private TextView nomeProduto;

    private int posicao;

    private Produto produtoSelecionado;

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

        ImageView imageViewExcluir;
        ImageView imageViewEditar;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            nomeProduto = itemView.findViewById(R.id.textViewNomeProdutoAdp);
            imageViewExcluir = itemView.findViewById(R.id.imageViewExcluirProduto);
            imageViewEditar = itemView.findViewById(R.id.imageViewEditarProduto);

            imageViewExcluir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    posicao = getAdapterPosition();
                    if (posicao != RecyclerView.NO_POSITION) {
                        if (!listaProdutos.isEmpty()) {
                            produtoSelecionado = listaProdutos.get(posicao);
                        }
                    }
                    abrirDialogExcluir();
                }
            });

            imageViewEditar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posicao = getAdapterPosition();
                    if (posicao != RecyclerView.NO_POSITION) {
                        if (!listaProdutos.isEmpty()) {
                            produtoSelecionado = listaProdutos.get(posicao);
                        }
                    }
                    abrirDialogEditar();
                }
            });
        }
    }

    public void abrirDialogEditar(){
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.TemaDialog);
        materialAlertDialogBuilder.setTitle("Confirmar");
        materialAlertDialogBuilder.setMessage("Deseja realmente editar este produto?");

        materialAlertDialogBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                abrirEditarProduto();
            }
        });

        materialAlertDialogBuilder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        materialAlertDialogBuilder.create();
        materialAlertDialogBuilder.show();
    }

    public void abrirEditarProduto(){
        Intent intent = new Intent(context, EditarProdutoActivity.class);
        Bundle bd = new Bundle();
        bd.putParcelable("produtoObj", produtoSelecionado);
        intent.putExtras(bd);
        context.startActivity(intent);
    }

    public void abrirDialogExcluir() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.TemaDialog);
        materialAlertDialogBuilder.setTitle("Confirmar");
        materialAlertDialogBuilder.setMessage("Deseja realmente excluir este produto?");

        materialAlertDialogBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                excluirProduto();
            }
        });

        materialAlertDialogBuilder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        materialAlertDialogBuilder.create();
        materialAlertDialogBuilder.show();
    }

    public void excluirProduto(){
        final Query queryProduto = ConfiguracaoFirebase.getFirebase().child("produtos").child(produtoSelecionado.getId());
        queryProduto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    queryProduto.getRef().removeValue();
                    listaProdutos.remove(posicao);
                    notifyItemRemoved(posicao);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Produto excluído com sucesso", Toast.LENGTH_SHORT).show();
                    validarUltimoProduto(listaProdutos);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void validarUltimoProduto(List<Produto> listaProdutos){
        if(listaProdutos.isEmpty()){
            abrirTelaNaoExisteProduto();
        }
    }

    public void abrirTelaNaoExisteProduto(){
        Intent i = new Intent(context, NaoExisteProdutoActivity.class);
        context.startActivity(i);
        ((Activity)context).finish();
    }
}
