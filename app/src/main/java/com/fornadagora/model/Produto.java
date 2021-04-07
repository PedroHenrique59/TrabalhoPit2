package com.fornadagora.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

public class Produto implements Parcelable {

    private String id;
    private String nome;
    private Categoria categoria;

    public Produto(){

    }

    public Produto(String nome, Categoria categoria) {
        this.nome = nome;
        this.categoria = categoria;
    }

    public Produto(String nome) {
        this.nome = nome;
    }

    protected Produto(Parcel in) {
        nome = in.readString();
    }

    public static final Creator<Produto> CREATOR = new Creator<Produto>() {
        @Override
        public Produto createFromParcel(Parcel in) {
            return new Produto(in);
        }

        @Override
        public Produto[] newArray(int size) {
            return new Produto[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nome);
    }

    public void salvar(){
        DatabaseReference referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("produtos");
        referenciaCategoria.push().setValue(this);
    }
}
