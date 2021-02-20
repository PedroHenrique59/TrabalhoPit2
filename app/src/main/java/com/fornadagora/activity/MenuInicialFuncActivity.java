package com.fornadagora.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.fornadagora.R;
import com.fornadagora.helper.ConfiguracaoFirebase;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.auth.FirebaseAuth;

import java.nio.channels.Channel;

public class MenuInicialFuncActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_inicial_func);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Fornadagora");
        setSupportActionBar(toolbar);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_sair :
                deslogarUsuario();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deslogarUsuario(){
        try{
            autenticacao.signOut();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void abrirTelaAdicionarProdutoPadaria(View view){
        startActivity(new Intent(getApplicationContext(), AdicionarProdutoPadariaActivity.class));
    }

    public void enviarNotificacao(View view){

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent p = PendingIntent.getActivity(this, 0, new Intent(this, NotificacaoClickActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "idCanal");
        builder.setTicker("Ticker Texto");
        builder.setContentTitle("Titulo");
        builder.setContentText("Descrição");
        builder.setSmallIcon(R.drawable.ic_email_24dp);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.bakery_icone));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        Notification n = builder.build();
        n.vibrate = new long[]{150,300,150,600};
        nm.notify(R.drawable.ic_email_24dp, n);

        try{
            Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone toque = RingtoneManager.getRingtone(this, som);
            toque.play();

        }catch (Exception e){

        }
    }
}
