package com.fornadagora.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class CategoriaVO implements Parcelable {

    private String identificador;
    private String nome;

    public CategoriaVO(){

    }

    public CategoriaVO(String identificador) {
        this.identificador = identificador;
    }

    public CategoriaVO(String identificador, String nome) {
        this.identificador = identificador;
        this.nome = nome;
    }

    protected CategoriaVO(Parcel in) {
        identificador = in.readString();
        nome = in.readString();
    }

    public static final Creator<CategoriaVO> CREATOR = new Creator<CategoriaVO>() {
        @Override
        public CategoriaVO createFromParcel(Parcel in) {
            return new CategoriaVO(in);
        }

        @Override
        public CategoriaVO[] newArray(int size) {
            return new CategoriaVO[size];
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

    public void salvar(){
        DatabaseReference referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
        referenciaCategoria.push().setValue(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identificador);
        dest.writeString(nome);
    }
}
