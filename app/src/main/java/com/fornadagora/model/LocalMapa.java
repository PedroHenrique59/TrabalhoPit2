package com.fornadagora.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

public class LocalMapa implements Parcelable {

    private String nome;
    private String latitude;
    private String longitude;

    public LocalMapa(){

    }

    public LocalMapa(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected LocalMapa(Parcel in) {
        nome = in.readString();
        latitude = in.readString();
        longitude = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nome);
        dest.writeString(latitude);
        dest.writeString(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocalMapa> CREATOR = new Creator<LocalMapa>() {
        @Override
        public LocalMapa createFromParcel(Parcel in) {
            return new LocalMapa(in);
        }

        @Override
        public LocalMapa[] newArray(int size) {
            return new LocalMapa[size];
        }
    };

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
