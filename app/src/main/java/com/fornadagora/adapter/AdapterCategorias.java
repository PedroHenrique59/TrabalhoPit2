package com.fornadagora.adapter;

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
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Produto;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterCategorias extends RecyclerView.Adapter<AdapterCategorias.MyViewHolder> {

    private List<Categoria> listaCategorias;

    private Context context;

    private TextView nomeCategoria;

    private Categoria categoriaSelecionada;

    private DatabaseReference referenciaCategoria;
    private DatabaseReference referenciaProduto;

    private boolean podeExcluir = true;

    private int posicao;

    public AdapterCategorias(List<Categoria> listaCategoria) {
        this.listaCategorias = listaCategoria;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_categoria, parent, false);
        context = parent.getContext();
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Categoria categoria = listaCategorias.get(position);
        nomeCategoria.setText(categoria.getNome());
    }

    @Override
    public int getItemCount() {
        return listaCategorias.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewExcluir;
        ImageView imageViewEditar;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            nomeCategoria = itemView.findViewById(R.id.textViewNomeCategoria);
            imageViewExcluir = itemView.findViewById(R.id.imageViewExcluirCategoria);
            imageViewEditar = itemView.findViewById(R.id.imageViewEditarCategoria);

            imageViewExcluir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    posicao = getAdapterPosition();
                    if (posicao != RecyclerView.NO_POSITION) {
                        if (!listaCategorias.isEmpty()) {
                            categoriaSelecionada = listaCategorias.get(posicao);
                        }
                    }
                    validarCategoriaVinculaProduto(categoriaSelecionada);
                }
            });

            imageViewEditar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posicao = getAdapterPosition();
                    if (posicao != RecyclerView.NO_POSITION) {
                        if (!listaCategorias.isEmpty()) {
                            categoriaSelecionada = listaCategorias.get(posicao);
                        }
                    }
                    editarCategoria(categoriaSelecionada);
                }
            });
        }
    }

    public void excluirCategoria(final Categoria categoria) {
        referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
        referenciaCategoria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapCategoria : snapshot.getChildren()) {
                        Categoria cat = snapCategoria.getValue(Categoria.class);
                        if (cat.getIdentificador().equalsIgnoreCase(categoria.getIdentificador())) {
                            abrirDialogExcluir(snapCategoria);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void abrirDialogExcluir(final DataSnapshot snapCategoria) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.TemaDialog);
        materialAlertDialogBuilder.setTitle("Confirmar");
        materialAlertDialogBuilder.setMessage("Deseja realmente excluir esta categoria?");

        materialAlertDialogBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                snapCategoria.getRef().removeValue();
                listaCategorias.remove(posicao);
                notifyItemRemoved(posicao);
                notifyDataSetChanged();
                Toast.makeText(context, "Categoria excluída com sucesso", Toast.LENGTH_SHORT).show();
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

    public void validarCategoriaVinculaProduto(final Categoria categoria) {
        referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos");
        referenciaProduto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapProduto : snapshot.getChildren()) {
                        Produto produto = snapProduto.getValue(Produto.class);
                        if (produto.getCategoriaVO().getIdentificador().equalsIgnoreCase(categoria.getIdentificador())) {
                            podeExcluir = false;
                            break;
                        }
                    }
                }
                if (podeExcluir) {
                    excluirCategoria(categoria);
                } else {
                    Toast.makeText(context, "Essa categoria está associada a algum produto. Não é possível exclui-la.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void editarCategoria(Categoria categoria){
        abrirDialogEditar(categoria);
    }

    public void abrirDialogEditar(final Categoria categoria){
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.TemaDialog);
        materialAlertDialogBuilder.setTitle("Confirmar");
        materialAlertDialogBuilder.setMessage("Deseja realmente editar esta categoria?");

        materialAlertDialogBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                abrirEditarCategoria(categoria);
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

    public void abrirEditarCategoria(Categoria categoria){
        Intent intent = new Intent(context, EditarCategoriaActivity.class);
        Bundle bd = new Bundle();
        bd.putParcelable("categoriaObj", categoria);
        intent.putExtras(bd);
        context.startActivity(intent);
    }
}
