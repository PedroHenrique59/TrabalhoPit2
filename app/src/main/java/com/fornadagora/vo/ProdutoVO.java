package com.fornadagora.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Categoria;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class ProdutoVO {

    private String nome;
    private Categoria categoria;
    private String idProduto;
    private String idCategoria;

    public ProdutoVO(){

    }

    @Exclude
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

    public String getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }
}
