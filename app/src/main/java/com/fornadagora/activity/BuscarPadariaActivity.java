package com.fornadagora.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentActivity;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Alerta;
import com.fornadagora.model.LocalMapa;
import com.fornadagora.model.Padaria;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class BuscarPadariaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseReference referenciaLocal;
    private DatabaseReference referenciaPadaria;

    private LocalMapa local;
    private Padaria padaria;

    private AlertDialog dialog;

    private LatLng latLng;
    private EditText editTextNomePadaria;
    private Button botaoSalvar;
    private Button botaoCancelar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_padaria);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        referenciaLocal = ConfiguracaoFirebase.getFirebase().child("padarias");

        referenciaLocal.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot teste : snapshot.getChildren()) {
                        Padaria padaria = teste.getValue(Padaria.class);
                        double latitude = Double.parseDouble(padaria.getLocal().getLatitude());
                        double longitude = Double.parseDouble(padaria.getLocal().getLongitude());
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .title(padaria.getNome())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bakery_icone)));

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng latitudeLongitude = new LatLng(-19.893156213517234, -43.93205951184109);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {

                final double lat = latLng.latitude;
                final double longe = latLng.longitude;

                String latitude = Double.toString(lat);
                String longitude = Double.toString(longe);

                local = new LocalMapa(latitude, longitude);
                padaria = new Padaria(local);

                abrirDialog(local, padaria);
            }
        });

        mMap.addMarker(new MarkerOptions().position(latitudeLongitude)
                .title("Padaria FornoDouro")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bakery_icone)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudeLongitude, 15));

    }

    public void abrirDialog(final LocalMapa local, final Padaria padaria){

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this, R.style.TemaDialog);
        materialAlertDialogBuilder.setView(R.layout.dialogmapa);
        materialAlertDialogBuilder.create();
        dialog = materialAlertDialogBuilder.show();

        Double latitude = Double.parseDouble(local.getLatitude());
        Double longitude = Double.parseDouble(local.getLongitude());

        latLng = new LatLng(latitude, longitude);

        editTextNomePadaria = dialog.findViewById(R.id.editTextNomePadaria);
        botaoSalvar = dialog.findViewById(R.id.dialogBotaoSalvar);
        botaoCancelar = dialog.findViewById(R.id.dialogBotaoCancelar);

        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarPadariaMapa(latLng, editTextNomePadaria);
                dialog.dismiss();
            }
        });

        botaoCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void salvarPadariaMapa(LatLng latLng, EditText editNomePadaria){
        if (editNomePadaria.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Favor preencher o nome da padaria", Toast.LENGTH_SHORT).show();
        } else {
            String nomePadaria = editNomePadaria.getText().toString();
            padaria.setNome(nomePadaria);
            referenciaPadaria.push().setValue(padaria);

            mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(nomePadaria)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bakery_icone)));

            dialog.dismiss();
        }
    }
}
