package com.fornadagora.model;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

public class LocalMapa {

    private String nome;
    private String latitude;
    private String longitude;

    public LocalMapa(){

    }

    public LocalMapa(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitudde(String longitudde) {
        this.longitude = longitude;
    }
}
