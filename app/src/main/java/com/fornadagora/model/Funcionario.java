package com.fornadagora.model;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Funcionario {

    private String idFuncionario;
    private String nome;
    private String email;
    private String senha;
    private String tipoPerfil;

    //Quais atributos a mais?

    public Funcionario(){

    }

    @Exclude
    public String getIdFuncionario() {
        return idFuncionario;
    }

    public void setIdFuncionario(String idFuncionario) {
        this.idFuncionario = idFuncionario;
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

    public void salvar(){
        DatabaseReference referencia = ConfiguracaoFirebase.getFirebase();
        referencia.child("funcionarios").child(this.idFuncionario).setValue(this);
    }
}
