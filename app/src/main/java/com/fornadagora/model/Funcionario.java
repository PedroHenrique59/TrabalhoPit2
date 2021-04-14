package com.fornadagora.model;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.vo.PadariaVO;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import kotlin.jvm.Transient;

public class Funcionario {

    private String idFuncionario;
    private String nome;
    private String email;
    private String senha;
    private String tipoPerfil;
    private Padaria padaria;
    private PadariaVO padariaVO;

    public Funcionario(){

    }

    public Funcionario(String nome, String email, String senha, String tipoPerfil) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipoPerfil = tipoPerfil;
    }

    public Funcionario(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

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

    @Exclude
    public Padaria getPadaria() {
        return padaria;
    }

    public void setPadaria(Padaria padaria) {
        this.padaria = padaria;
    }

    public PadariaVO getPadariaVO() {
        return padariaVO;
    }

    public void setPadariaVO(PadariaVO padariaVO) {
        this.padariaVO = padariaVO;
    }

    public void salvar(){
        DatabaseReference referencia = ConfiguracaoFirebase.getFirebase();
        referencia.child("funcionarios").child(this.idFuncionario).setValue(this);
    }

    public void atualizarDados(){
        DatabaseReference referencia = ConfiguracaoFirebase.getFirebase();
        referencia.child("funcionarios").child(this.idFuncionario).child("nome").setValue(this.nome);
        referencia.child("funcionarios").child(this.idFuncionario).child("email").setValue(this.email);
    }

    public void atualizarDadosPeloAdm(){
        DatabaseReference referencia = ConfiguracaoFirebase.getFirebase();
        referencia.child("funcionarios").child(this.idFuncionario).child("nome").setValue(this.nome);
        referencia.child("funcionarios").child(this.idFuncionario).child("padariaVO").setValue(this.padariaVO);
    }
}
