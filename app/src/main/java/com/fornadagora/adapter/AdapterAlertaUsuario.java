package com.fornadagora.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fornadagora.R;
import com.fornadagora.helper.Base64Custom;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Alerta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterAlertaUsuario extends RecyclerView.Adapter<AdapterAlertaUsuario.MyViewHolder> {

    private List<Alerta> listaAlertas;

    private FirebaseAuth autenticacao;
    private DatabaseReference referenciaAlerta;

    private Context context;
    private AlertDialog alerta;

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
        ImageView imageViewExcluir;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);

            nomeAlerta = itemView.findViewById(R.id.textViewNomeAlerta);
            nomePadaria = itemView.findViewById(R.id.textViewNomePadaria);
            nomeProduto = itemView.findViewById(R.id.textViewNomeProduto);
            imageViewExcluir = itemView.findViewById(R.id.imageViewExcluir);

            imageViewExcluir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String nome = nomeAlerta.getText().toString();

                    autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
                    referenciaAlerta = ConfiguracaoFirebase.getFirebase();

                    String email = autenticacao.getCurrentUser().getEmail();
                    String id = Base64Custom.codificarBase64(email);

                    referenciaAlerta = referenciaAlerta.child("usuarios").child(id).child("alerta");
                    referenciaAlerta.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for(DataSnapshot snapAlerta : snapshot.getChildren()){
                                    Alerta alerta = snapAlerta.getValue(Alerta.class);
                                    if(alerta.getNome().equals(nome)){
                                        abrirDialog(snapAlerta);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        }
    }

    public void abrirDialog(final DataSnapshot dataSnap){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmar");
        builder.setMessage("Deseja realmente excluir esse alerta?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                dataSnap.getRef().removeValue();
                Toast.makeText(context, "Alerta excluido com sucesso", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });

        alerta = builder.create();
        alerta.show();
    }
}
