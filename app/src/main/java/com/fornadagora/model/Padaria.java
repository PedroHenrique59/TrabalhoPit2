package com.fornadagora.model;

public class Padaria {

    private String nome;
    private LocalMapa local;

    public Padaria(){

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
}
