package com.fornadagora.vo;

import com.fornadagora.model.Padaria;
import com.fornadagora.model.Produto;
import com.google.firebase.database.Exclude;

public class AlertaVO  {

    private String idAlerta;
    private String nome;
    private Padaria padaria;
    private Produto produto;
    private String idPadaria;
    private String idProduto;

    public AlertaVO(){

    }

    public AlertaVO(String idPadaria, String idProduto) {
        this.idPadaria = idPadaria;
        this.idProduto = idProduto;
    }

    public String getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(String idAlerta) {
        this.idAlerta = idAlerta;
    }

    @Exclude
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Exclude
    public Padaria getPadaria() {
        return padaria;
    }

    public void setPadaria(Padaria padaria) {
        this.padaria = padaria;
    }

    @Exclude
    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public String getIdPadaria() {
        return idPadaria;
    }

    public void setIdPadaria(String idPadaria) {
        this.idPadaria = idPadaria;
    }

    public String getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }
}
