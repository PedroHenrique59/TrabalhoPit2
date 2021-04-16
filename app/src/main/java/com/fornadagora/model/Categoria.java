package com.fornadagora.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class Categoria implements Parcelable {

    private String identificador;
    private String nome;

    public Categoria(){

    }

    public Categoria(String nome) {
        this.nome = nome;
    }

    protected Categoria(Parcel in) {
        identificador = in.readString();
        nome = in.readString();
    }

    public static final Creator<Categoria> CREATOR = new Creator<Categoria>() {
        @Override
        public Categoria createFromParcel(Parcel in) {
            return new Categoria(in);
        }

        @Override
        public Categoria[] newArray(int size) {
            return new Categoria[size];
        }
    };

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void salvar(){
        DatabaseReference referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
        referenciaCategoria.push().setValue(this, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                ref.child("id").setValue(ref.getKey());
            }
        });
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
