package com.fornadagora.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fornadagora.activity.CadastrarAlertaActivity;
import com.fornadagora.activity.CadastroUsuarioActivity;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Alerta implements Parcelable {

    private String idAlerta;
    private String nome;
    private Padaria padaria;
    private Produto produto;
    private String idPadaria;
    private String idProduto;

    public Alerta(){

    }

    public Alerta(String nome, Padaria padaria, Produto produto) {
        this.nome = nome;
        this.padaria = padaria;
        this.produto = produto;
    }

    public Alerta(String nome, String idPadaria, String idProduto) {
        this.nome = nome;
        this.idPadaria = idPadaria;
        this.idProduto = idProduto;
    }

    protected Alerta(Parcel in) {
        idAlerta = in.readString();
        nome = in.readString();
        idPadaria = in.readString();
        produto = in.readParcelable(Produto.class.getClassLoader());
        padaria = in.readParcelable(Padaria.class.getClassLoader());
    }

    public static final Creator<Alerta> CREATOR = new Creator<Alerta>() {
        @Override
        public Alerta createFromParcel(Parcel in) {
            return new Alerta(in);
        }

        @Override
        public Alerta[] newArray(int size) {
            return new Alerta[size];
        }
    };

    @Exclude
    public String getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(String idAlerta) {
        this.idAlerta = idAlerta;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Exclude
    public Padaria getPadaria() {
        return padaria;
    }

    public void setPadaria(Padaria padaria) {
        this.padaria = padaria;
    }

    @Exclude
    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public String getIdPadaria() {
        return idPadaria;
    }

    public void setIdPadaria(String idPadaria) {
        this.idPadaria = idPadaria;
    }

    public String getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idAlerta);
        dest.writeString(nome);
        dest.writeString(idPadaria);
        dest.writeParcelable(produto, flags);
        dest.writeParcelable(padaria, flags);
    }

    public void salvar(){
        DatabaseReference referenciaAlerta = ConfiguracaoFirebase.getFirebase().child("alertas");
        referenciaAlerta.push().setValue(this, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
               CadastrarAlertaActivity.teste(ref.getKey());
            }
        });
    }

    public void atualizarDados(Alerta alertaEditado, DatabaseReference referenciaAlertaExistente){
        referenciaAlertaExistente.child("nome").setValue(alertaEditado.getNome());
        referenciaAlertaExistente.child("padaria").setValue(alertaEditado.getPadaria());
        referenciaAlertaExistente.child("produto").setValue(alertaEditado.getProduto());
    }
}
