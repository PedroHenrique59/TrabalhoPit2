package com.fornadagora.vo;

import com.fornadagora.model.LocalMapa;
import com.fornadagora.model.Produto;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class PadariaVO {

    private String identificador;
    private String nome;
    private LocalMapa local;
    private List<Produto> listaProdutos = new ArrayList<>();

    public PadariaVO(){

    }

    public PadariaVO(String identificador) {
        this.identificador = identificador;
    }

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
}
