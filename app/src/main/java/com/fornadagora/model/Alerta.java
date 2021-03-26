package com.fornadagora.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Alerta implements Parcelable {

    private String nome;
    private Padaria padaria;
    private Produto produto;

    public Alerta(){

    }

    public Alerta(String nome, Padaria padaria, Produto produto) {
        this.nome = nome;
        this.padaria = padaria;
        this.produto = produto;
    }

    protected Alerta(Parcel in) {
        nome = in.readString();
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Padaria getPadaria() {
        return padaria;
    }

    public void setPadaria(Padaria padaria) {
        this.padaria = padaria;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nome);
        dest.writeParcelable(produto, flags);
        dest.writeParcelable(padaria, flags);
    }
}
