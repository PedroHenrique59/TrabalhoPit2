package com.fornadagora.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.helper.Teclado;
import com.fornadagora.model.LocalMapa;
import com.fornadagora.model.Padaria;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuscarPadariaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseReference referenciaLocal;
    private DatabaseReference referenciaPadaria;

    private LocalMapa local;
    private Padaria padaria;

    private AlertDialog dialog;

    private LatLng latLng;

    private EditText editTextNomePadaria;
    private EditText campoPesquisa;

    private ImageView imageViewBuscar;

    private FusedLocationProviderClient client;

    private SupportMapFragment supportMapFragment;

    private Button botaoSalvar;
    private Button botaoCancelar;

    private Context context;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_padaria);

        campoPesquisa = findViewById(R.id.editTextBuscar2);
        imageViewBuscar = findViewById(R.id.imageViewBuscar2);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        supportMapFragment = mapFragment;

        client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(BuscarPadariaActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(BuscarPadariaActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        referenciaLocal = ConfiguracaoFirebase.getFirebase().child("padarias");
        referenciaLocal.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapPadaria : snapshot.getChildren()) {
                        Padaria padaria = snapPadaria.getValue(Padaria.class);
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

        MapsInitializer.initialize(getApplicationContext());

        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");
        context = this;
        inicializar();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions options = new MarkerOptions().position(latLng).title("Você está aqui");
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                            mMap.addMarker(options);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    private void inicializar() {
        imageViewBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Teclado.fecharTeclado(v);
                if (!campoPesquisa.getText().toString().isEmpty()) {
                    pesquisarEndereco();
                } else {
                    Toast.makeText(context, "Nenhum endereço informado!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void pesquisarEndereco() {
        String endereco = campoPesquisa.getText().toString();
        Geocoder geocoder = new Geocoder(BuscarPadariaActivity.this);
        List<Address> list = new ArrayList<>();
        MarkerOptions markerOptions = new MarkerOptions();
        try {
            list = geocoder.getFromLocationName(endereco, 1);
        } catch (IOException e) {

        }
        if (list.size() > 0) {
            Address address = list.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            MarkerOptions options = new MarkerOptions().position(latLng).title("Local encontrado");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            mMap.addMarker(options);
        } else {
            Toast.makeText(context, "Endereço não encontrado!", Toast.LENGTH_LONG).show();
        }
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
    }

    public void abrirDialog(final LocalMapa local, final Padaria padaria) {

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

    public void salvarPadariaMapa(LatLng latLng, EditText editNomePadaria) {
        if (editNomePadaria.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Favor preencher o nome da padaria", Toast.LENGTH_SHORT).show();
        } else {
            String nomePadaria = editNomePadaria.getText().toString();
            padaria.setNome(nomePadaria);
            padaria.salvar();

            mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(nomePadaria)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bakery_icone)));

            dialog.dismiss();
        }
    }
}
