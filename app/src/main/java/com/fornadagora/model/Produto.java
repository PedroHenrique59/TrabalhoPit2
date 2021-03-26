package com.fornadagora.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Produto implements Parcelable {

    private String nome;

    public Produto(){

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nome);
    }
}
