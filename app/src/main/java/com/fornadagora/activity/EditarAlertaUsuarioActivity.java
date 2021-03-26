package com.fornadagora.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.AutoCompleteTextView;

import com.fornadagora.R;
import com.fornadagora.model.Alerta;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;

public class EditarAlertaUsuarioActivity extends AppCompatActivity implements Serializable {

    private Alerta alerta;

    private TextInputEditText editTextNomeAlerta;
    private AutoCompleteTextView autoComletePadaria;
    private AutoCompleteTextView autoComleteProduto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_alerta_usuario);
        alerta = getIntent().getExtras().getParcelable("alertaObj");
        inicializarComponentes();
        preencherCamposComDadosAlerta(alerta);
    }

    public void inicializarComponentes(){
        editTextNomeAlerta = findViewById(R.id.editTextNomeAlertaEdit);
        autoComletePadaria = findViewById(R.id.autoComletePadariaAlertEdit);
        autoComleteProduto = findViewById(R.id.autoComleteProdutoAlert);
    }

    public void preencherCamposComDadosAlerta(Alerta alerta){
        if(alerta != null){
            editTextNomeAlerta.setText(alerta.getNome());
            autoComletePadaria.setText(alerta.getPadaria().getNome());
            autoComleteProduto.setText(alerta.getProduto().getNome());
        }
    }
}