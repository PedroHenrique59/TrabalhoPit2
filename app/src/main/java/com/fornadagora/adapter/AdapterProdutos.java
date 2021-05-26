package com.fornadagora.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.fornadagora.model.Alerta;
import com.fornadagora.model.Categoria;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.model.Usuario;
import com.fornadagora.vo.AlertaVO;
import com.fornadagora.vo.PadariaVO;
import com.fornadagora.vo.ProdutoVO;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterProdutos extends RecyclerView.Adapter<AdapterProdutos.MyViewHolder> {

    private List<Produto> listaProdutos;

    private Context context;

    private TextView nomeProduto;

    private int posicao;

    private Produto produtoSelecionado;

    private Padaria padariaFuncionario;

    private ArrayList<Alerta> listaAlertaProdutoSelecionado = new ArrayList<>();

    private ArrayList<AlertaVO> listaAlertaVOUsuarios = new ArrayList<>();

    private ArrayList<String> listaEmailsUsuarios = new ArrayList<>();

    public AdapterProdutos(List<Produto> listaProduto, Padaria padaria) {
        this.listaProdutos = listaProduto;
        this.padariaFuncionario = padaria;
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

        Button botaoExcluir;
        Button botaoEditar;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            nomeProduto = itemView.findViewById(R.id.textViewNomeProdutoAdp);
            botaoExcluir = itemView.findViewById(R.id.buttonExcluirProduto);
            botaoEditar = itemView.findViewById(R.id.buttonEditarProduto);

            botaoExcluir.setOnClickListener(new View.OnClickListener() {
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

            botaoEditar.setOnClickListener(new View.OnClickListener() {
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

    public void abrirDialogEditar() {
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

    public void abrirEditarProduto() {
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

    public void excluirProduto() {
        final Query queryProduto = ConfiguracaoFirebase.getFirebase().child("produtos").child(produtoSelecionado.getId());
        queryProduto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    queryProduto.getRef().removeValue();
                    listaProdutos.remove(posicao);
                    notifyItemRemoved(posicao);
                    Toast.makeText(context, "Produto excluído com sucesso", Toast.LENGTH_SHORT).show();
                    validarUltimoProduto(listaProdutos);
                    excluirProdutoPadaria();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void validarUltimoProduto(List<Produto> listaProdutos) {
        if (listaProdutos.isEmpty()) {
            abrirTelaNaoExisteProduto();
        }
    }

    public void abrirTelaNaoExisteProduto() {
        Intent i = new Intent(context, NaoExisteProdutoActivity.class);
        context.startActivity(i);
        ((Activity) context).finish();
    }

    public void excluirProdutoPadaria() {
        if (!padariaFuncionario.getListaProdutosVO().isEmpty()) {
            for (ProdutoVO produtoVO : padariaFuncionario.getListaProdutosVO()) {
                if (produtoVO.getIdProduto().equalsIgnoreCase(produtoSelecionado.getId())) {
                    removerProduto(produtoSelecionado);
                }
            }
        }
    }

    public void removerProduto(final Produto produto) {
        Query queryPadaria = ConfiguracaoFirebase.getFirebase().child("padarias").child(padariaFuncionario.getIdentificador());
        queryPadaria.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("listaProdutosVO").exists()) {
                        Map<String, ProdutoVO> td = new HashMap<String, ProdutoVO>();
                        for (DataSnapshot produtoSnapshot : snapshot.child("listaProdutosVO").getChildren()) {
                            ProdutoVO produtoVO = produtoSnapshot.getValue(ProdutoVO.class);
                            if (produtoVO.getIdProduto().equalsIgnoreCase(produtoSelecionado.getId())) {
                                produtoSnapshot.getRef().removeValue();
                                buscarAlertasBanco();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buscarAlertasBanco() {
        DatabaseReference referenciaAlerta = ConfiguracaoFirebase.getFirebase().child("alertas");
        referenciaAlerta.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listaAlertaProdutoSelecionado.clear();
                    for (DataSnapshot snapPadaria : snapshot.getChildren()) {
                        String idPadaria = snapPadaria.child("idPadaria").getValue(String.class);
                        String idProduto = snapPadaria.child("idProduto").getValue(String.class);
                        if (idPadaria.equalsIgnoreCase(produtoSelecionado.getPadariaVO().getIdentificador())) {
                            if (idProduto.equalsIgnoreCase(produtoSelecionado.getId())) {
                                Alerta alerta = snapPadaria.getValue(Alerta.class);
                                listaAlertaProdutoSelecionado.add(alerta);
                            }
                        }
                    }
                    buscarAlertasDosUsuarios();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buscarAlertasDosUsuarios() {
        DatabaseReference referenciaUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios");
        referenciaUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listaEmailsUsuarios.clear();
                    for (DataSnapshot snapId : snapshot.getChildren()) {
                        if (snapId.child("listaAlertasVO").exists()) {
                            listaAlertaVOUsuarios.clear();
                            Map<String, AlertaVO> td = new HashMap<String, AlertaVO>();
                            for (DataSnapshot alertaSnapshot : snapId.child("listaAlertasVO").getChildren()) {
                                AlertaVO alertaVO = alertaSnapshot.getValue(AlertaVO.class);
                                td.put(alertaSnapshot.getKey(), alertaVO);
                            }
                            listaAlertaVOUsuarios.addAll(td.values());
                            for (AlertaVO alertaVO : listaAlertaVOUsuarios) {
                                for (Alerta alerta : listaAlertaProdutoSelecionado) {
                                    if (alerta.getIdAlerta().equalsIgnoreCase(alertaVO.getIdAlerta())) {
                                        if (snapId.child("email").exists()) {
                                            String email = snapId.child("email").getValue(String.class);
                                            listaEmailsUsuarios.add(email);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    excluirAlertasUsuarios();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void excluirAlertasUsuarios() {
        DatabaseReference referenciaUsuario = ConfiguracaoFirebase.getFirebase().child("usuarios");
        referenciaUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapId : snapshot.getChildren()) {
                        if (snapId.child("listaAlertasVO").exists()) {
                            for (DataSnapshot alertaSnapshot : snapId.child("listaAlertasVO").getChildren()) {
                                AlertaVO alertaVO1 = alertaSnapshot.getValue(AlertaVO.class);
                                for (Alerta alerta1 : listaAlertaProdutoSelecionado) {
                                    if (alertaVO1.getIdAlerta().equalsIgnoreCase(alerta1.getIdAlerta())) {
                                        alertaSnapshot.getRef().removeValue();
                                    }
                                }
                            }
                        }
                    }
                    excluirAlertasBanco();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void excluirAlertasBanco(){
        DatabaseReference referenciaAlerta = ConfiguracaoFirebase.getFirebase().child("alertas");
        referenciaAlerta.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snapAlerta : snapshot.getChildren()){
                        for(Alerta alerta : listaAlertaProdutoSelecionado){
                            if(snapAlerta.getKey().equalsIgnoreCase(alerta.getIdAlerta())){
                                snapAlerta.getRef().removeValue();
                            }
                        }
                    }
                    abrirIntentEmail();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void abrirIntentEmail() {
        String[] arrayEmails = listaEmailsUsuarios.toArray(new String[0]);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, arrayEmails);
        i.putExtra(Intent.EXTRA_SUBJECT, "Fornadagora - Comunicado de produto");
        i.putExtra(Intent.EXTRA_TEXT, "Caro usuário(a), o produto: " + produtoSelecionado.getNome() + " não será mais comercializado na padaria: " + padariaFuncionario.getNome());
        try {
            context.startActivity(Intent.createChooser(i, "Enviar e-mail"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
