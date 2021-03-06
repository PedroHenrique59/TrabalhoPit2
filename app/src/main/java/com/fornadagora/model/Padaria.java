package com.fornadagora.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.vo.ProdutoVO;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Padaria implements Parcelable {

    private String identificador;
    private String nome;
    private LocalMapa local;
    private List<Produto> listaProdutos = new ArrayList<>();
    private List<ProdutoVO> listaProdutosVO = new ArrayList<>();

    public Padaria(){

    }

    protected Padaria(Parcel in) {
        nome = in.readString();
        listaProdutos = in.createTypedArrayList(Produto.CREATOR);
    }

    public static final Creator<Padaria> CREATOR = new Creator<Padaria>() {
        @Override
        public Padaria createFromParcel(Parcel in) {
            return new Padaria(in);
        }

        @Override
        public Padaria[] newArray(int size) {
            return new Padaria[size];
        }
    };

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
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

    public List<Produto> getListaProdutos() {
        return listaProdutos;
    }

    public List<ProdutoVO> getListaProdutosVO() {
        return listaProdutosVO;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nome);
        dest.writeList(listaProdutos);
    }

    public void salvar(){
        DatabaseReference referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaPadaria.push().setValue(this, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                ref.child("identificador").setValue(ref.getKey());
            }
        });
    }
}
