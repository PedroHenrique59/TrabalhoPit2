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
import com.fornadagora.activity.EditarAlertaUsuarioActivity;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Alerta;
import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.fornadagora.vo.AlertaVO;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterAlertaUsuario extends RecyclerView.Adapter<AdapterAlertaUsuario.MyViewHolder> {

    private List<Alerta> listaAlertas;

    private FirebaseAuth autenticacao;
    private DatabaseReference referenciaAlerta;
    private DatabaseReference referenciaAlertas;

    private DataSnapshot snapAlerta;

    private TextView nomeAlerta;
    private TextView nomePadaria;
    private TextView nomeProduto;

    private Context context;

    private Alerta alertaSelecionado;

    public AdapterAlertaUsuario(List<Alerta> listaAlerta) {
        this.listaAlertas = listaAlerta;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_alerta_usuario, parent, false);
        context = parent.getContext();
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Alerta alerta = listaAlertas.get(position);
        nomeAlerta.setText(alerta.getNome());
        nomePadaria.setText(alerta.getPadaria().getNome());
        nomeProduto.setText(alerta.getProduto().getNome());
    }

    @Override
    public int getItemCount() {
        return listaAlertas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewExcluir;
        ImageView imageViewEditar;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);

            nomeAlerta = itemView.findViewById(R.id.textViewNomeAlerta);
            nomePadaria = itemView.findViewById(R.id.textViewNomePadaria);
            nomeProduto = itemView.findViewById(R.id.textViewNomeProduto);
            imageViewExcluir = itemView.findViewById(R.id.imageViewExcluir);
            imageViewEditar = itemView.findViewById(R.id.imageViewEditar);

            imageViewExcluir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posicao = getAdapterPosition();
                    if (posicao != RecyclerView.NO_POSITION) {
                        if (!listaAlertas.isEmpty()) {
                            alertaSelecionado = listaAlertas.get(posicao);
                        }
                    }
                    excluirAlertaUsuario(alertaSelecionado);
                }
            });

            imageViewEditar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posicao = getAdapterPosition();
                    if (posicao != RecyclerView.NO_POSITION) {
                        if (!listaAlertas.isEmpty()) {
                            alertaSelecionado = listaAlertas.get(posicao);
                        }
                    }
                    editarAlerta(alertaSelecionado);
                }
            });
        }
    }

    public void excluirAlertaUsuario(final Alerta alerta) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        referenciaAlerta = ConfiguracaoFirebase.getFirebase().child("usuarios").child(autenticacao.getUid());
        referenciaAlerta.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("listaAlertasVO").exists()) {
                        Map<String, AlertaVO> td = new HashMap<String, AlertaVO>();
                        for (DataSnapshot snapAlertah : snapshot.child("listaAlertasVO").getChildren()) {
                            AlertaVO alertaVO = snapAlertah.getValue(AlertaVO.class);
                            td.put(snapAlertah.getKey(), alertaVO);
                            snapAlerta = snapAlertah;
                        }
                        ArrayList<AlertaVO> values = new ArrayList<>(td.values());
                        for(AlertaVO alertaVO : values){
                            if(alertaVO.getIdAlerta().equalsIgnoreCase(alerta.getIdAlerta())){
                                abrirDialogExcluir(snapAlerta);
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

    public void abrirDialogExcluir(final DataSnapshot dataSnap) {

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.TemaDialog);
        materialAlertDialogBuilder.setTitle("Confirmar");
        materialAlertDialogBuilder.setMessage("Deseja realmente excluir este alerta?");

        materialAlertDialogBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataSnap.getRef().removeValue();
                Toast.makeText(context, "Alerta excluído com sucesso", Toast.LENGTH_SHORT).show();
                excluirAlertaBanco(dataSnap);
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

    public void excluirAlertaBanco(final DataSnapshot dataSnap){
        final AlertaVO alertaVO = dataSnap.getValue(AlertaVO.class);
        referenciaAlertas = ConfiguracaoFirebase.getFirebase().child("alertas");
        referenciaAlertas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snapAlerta : snapshot.getChildren()){
                        Alerta alertaBanco = snapAlerta.getValue(Alerta.class);
                        alertaBanco.setIdAlerta(snapAlerta.getKey());
                        if(alertaBanco.getIdAlerta().equalsIgnoreCase(alertaVO.getIdAlerta())){
                            snapAlerta.getRef().removeValue();
                            Toast.makeText(context, "Alerta excluído com sucesso", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void editarAlerta(final Alerta alerta) {
        abrirDialogEditar(alerta);
    }

    public void abrirDialogEditar(final Alerta alerta) {

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.TemaDialog);
        materialAlertDialogBuilder.setTitle("Confirmar");
        materialAlertDialogBuilder.setMessage("Deseja realmente editar este alerta?");

        materialAlertDialogBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                abrirEditarAlerta(alerta);
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

    public void abrirEditarAlerta(Alerta alerta) {
        Intent intent = new Intent(context, EditarAlertaUsuarioActivity.class);
        Bundle bd = new Bundle();
        bd.putParcelable("alertaObj", alerta);
        intent.putExtras(bd);
        context.startActivity(intent);
    }
}
