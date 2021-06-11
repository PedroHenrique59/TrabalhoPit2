package com.fornadagora.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.fornadagora.model.LocalMapa;
import com.fornadagora.model.Produto;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class PadariaVO implements Parcelable {

    private String identificador;
    private String nome;
    private LocalMapa local;
    private List<Produto> listaProdutos = new ArrayList<>();

    public PadariaVO(){

    }

    public PadariaVO(String identificador) {
        this.identificador = identificador;
    }

    protected PadariaVO(Parcel in) {
        identificador = in.readString();
    }

    public static final Creator<PadariaVO> CREATOR = new Creator<PadariaVO>() {
        @Override
        public PadariaVO createFromParcel(Parcel in) {
            return new PadariaVO(in);
        }

        @Override
        public PadariaVO[] newArray(int size) {
            return new PadariaVO[size];
        }
    };

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    @Exclude
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Exclude
    public LocalMapa getLocal() {
        return local;
    }

    public void setLocal(LocalMapa local) {
        this.local = local;
    }

    @Exclude
    public List<Produto> getListaProdutos() {
        return listaProdutos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identificador);
    }
}
