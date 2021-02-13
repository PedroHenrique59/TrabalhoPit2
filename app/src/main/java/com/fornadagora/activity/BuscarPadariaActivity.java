package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.LocalMapa;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BuscarPadariaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference referencia;
    private LocalMapa local;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_padaria);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        referencia = ConfiguracaoFirebase.getFirebase().child("locais");

        referencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                   for(DataSnapshot teste : snapshot.getChildren()){
                       LocalMapa testeLocal = teste.getValue(LocalMapa.class);
                       double latitude = Double.parseDouble(testeLocal.getLatitude());
                       double longitude = Double.parseDouble(testeLocal.getLongitude());
                       mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                               .title("Local")
                               .icon(BitmapDescriptorFactory.fromResource(R.drawable.bakery_icone))
                               .snippet("Descrição"));
                   }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

        LatLng padaria = new LatLng(-19.893156213517234, -43.93205951184109);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng)
                        .title("Local")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bakery_icone))
                        .snippet("Descrição"));

                final double lat = latLng.latitude;
                final double longe = latLng.longitude;

                String latitude = Double.toString(lat);
                String longitude = Double.toString(longe);

                local = new LocalMapa(latitude, longitude);
                referencia.push().setValue(local);
            }
        });

        mMap.addMarker(new MarkerOptions().position(padaria)
                .title("Padaria FornoDouro")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bakery_icone)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(padaria, 15));
    }
}
