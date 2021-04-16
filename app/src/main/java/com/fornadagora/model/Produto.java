package com.fornadagora.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.vo.CategoriaVO;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Produto implements Parcelable {

    private String id;
    private String nome;
    private Categoria categoria;
    private CategoriaVO categoriaVO;

    public Produto(){

    }

    public Produto(String nome, CategoriaVO categoriaVO) {
        this.nome = nome;
        this.categoriaVO = categoriaVO;
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

    @Exclude
    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public CategoriaVO getCategoriaVO() {
        return categoriaVO;
    }

    public void setCategoriaVO(CategoriaVO categoriaVO) {
        this.categoriaVO = categoriaVO;
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
        DatabaseReference referenciaProduto = ConfiguracaoFirebase.getFirebase().child("produtos");
        referenciaProduto.push().setValue(this, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                ref.child("id").setValue(ref.getKey());
            }
        });
    }
}
