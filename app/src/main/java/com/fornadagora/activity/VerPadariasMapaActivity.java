package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.fornadagora.model.Padaria;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class VerPadariasMapaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseReference referenciaPadaria;

    private Padaria padaria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_padarias_mapa);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        referenciaPadaria = ConfiguracaoFirebase.getFirebase().child("padarias");

        referenciaPadaria.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot teste : snapshot.getChildren()){
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

        mMap.addMarker(new MarkerOptions().position(latitudeLongitude)
                .title("Padaria FornoDouro")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bakery_icone)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudeLongitude, 15));

    }
}
