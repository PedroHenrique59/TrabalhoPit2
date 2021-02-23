package com.fornadagora.model;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

public class Alerta {

    private String nome;
    private Padaria padaria;
    private Produto produto;
    private Usuario usuario;

    public Alerta(){

    }

    public Alerta(String nome, Padaria padaria, Produto produto) {
        this.nome = nome;
        this.padaria = padaria;
        this.produto = produto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Padaria getPadaria() {
        return padaria;
    }

    public void setPadaria(Padaria padaria) {
        this.padaria = padaria;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void salvar(){
        DatabaseReference referencia = ConfiguracaoFirebase.getFirebase();
        referencia.child("usuarios").child(usuario.getIdUsuario()).child("alerta").push().setValue(this);
    }
}
