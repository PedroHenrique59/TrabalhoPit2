package com.fornadagora.model;

import androidx.annotation.NonNull;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class Categoria {

    private String identificador;
    private String nome;

    public Categoria(){

    }

    public Categoria(String nome) {
        this.nome = nome;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void salvar(){
        DatabaseReference referenciaCategoria = ConfiguracaoFirebase.getFirebase().child("categorias");
        referenciaCategoria.push().setValue(this);
    }
}
