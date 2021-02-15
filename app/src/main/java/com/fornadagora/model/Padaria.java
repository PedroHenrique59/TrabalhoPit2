package com.fornadagora.model;

import android.widget.ProgressBar;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Padaria {

    private String identificador;
    private String nome;
    private LocalMapa local;
    private List<Produto> listaProdutos = new ArrayList<>();

    public Padaria(){

    }

    @Exclude
    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public Padaria(LocalMapa local) {
        this.local = local;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalMapa getLocal() {
        return local;
    }

    public void setLocal(LocalMapa local) {
        this.local = local;
    }

    public List<Produto> getListaProdutos() {
        return listaProdutos;
    }
}
