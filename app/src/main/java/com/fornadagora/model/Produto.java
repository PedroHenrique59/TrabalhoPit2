package com.fornadagora.model;

public class Produto {

    private String nome;

    public Produto(){

    }

    public Produto(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
