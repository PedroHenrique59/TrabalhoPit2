package com.fornadagora.model;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Usuario {

    private String idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String tipoPerfil;
    private String token;
    private Alerta alerta;

    public Usuario() {
    }

    public Usuario(String nome, String email, String senha, String tipoPerfil, String token) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipoPerfil = tipoPerfil;
        this.token = token;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipoPerfil() {
        return tipoPerfil;
    }

    public void setTipoPerfil(String tipoPerfil) {
        this.tipoPerfil = tipoPerfil;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Alerta getAlerta() {
        return alerta;
    }

    public void setAlerta(Alerta alerta) {
        this.alerta = alerta;
    }

    public void salvar(){
        DatabaseReference referencia = ConfiguracaoFirebase.getFirebase();
        referencia.child("usuarios").child(this.idUsuario).setValue(this);
    }

    public void atualizarDados(){
        DatabaseReference referencia = ConfiguracaoFirebase.getFirebase();
        referencia.child("usuarios").child(this.idUsuario).child("nome").setValue(this.nome);
        referencia.child("usuarios").child(this.idUsuario).child("email").setValue(this.email);
    }

    public void salvarAlerta(){
        DatabaseReference referencia = ConfiguracaoFirebase.getFirebase();
        referencia.child("usuarios").child(this.getIdUsuario()).child("alerta").push().setValue(alerta);
    }
}
