package com.fornadagora.model;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.vo.AlertaVO;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private String idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String tipoPerfil;
    private String token;
    private Alerta alerta;
    private List<AlertaVO> listaAlertaVO = new ArrayList<>();

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

    @Exclude
    public Alerta getAlerta() {
        return alerta;
    }

    public void setAlerta(Alerta alerta) {
        this.alerta = alerta;
    }

    public List<AlertaVO> getListaAlertaVO() {
        return listaAlertaVO;
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

    public void salvarAlertaVO(){
        DatabaseReference referencia = ConfiguracaoFirebase.getFirebase();
        referencia.child("usuarios").child(this.idUsuario).child("listaAlertasVO").setValue(listaAlertaVO);
    }
}
