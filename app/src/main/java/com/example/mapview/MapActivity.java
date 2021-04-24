package com.example.mapview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private MapView mapView;
    private MapboxMap mapboxMap;
    MapData mapData;
    ArrayDeque<Layer> rooms = new ArrayDeque<>();
    TextView tv;
    ImageView icon;
    UserData data;
    BeaconData beaconData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //для использования библиотеки необходимо указывать токен доступа
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        mapView.getMapAsync(this);

        tv = findViewById(R.id.room);
        icon = findViewById(R.id.icon);

        int[] iconRes = {R.drawable.icon1, R.drawable.icon2, R.drawable.icon3};
        Bundle arguments = getIntent().getExtras();
        if (arguments!=null && !arguments.isEmpty()){
            data = arguments.getParcelable(UserData.class.getSimpleName());
            icon.setImageResource(iconRes[(data.getIcon()-1)]);
        }
        else {
            data = new UserData(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            data.setIcon(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), icon);
        }
    }

    public void onIconClick(View v){
        Dialog dialogMenu = new Dialog(MapActivity.this);
        dialogMenu.setTitle("Действия...");
        dialogMenu.setContentView(R.layout.menu);
        TextView settings = dialogMenu.findViewById(R.id.settings);
        TextView out = dialogMenu.findViewById(R.id.out);
        dialogMenu.show();
        dialogMenu.getWindow().getAttributes().dimAmount = 0f;
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapActivity.this, SettingsActivity.class);
                i.putExtra(UserData.class.getSimpleName(), data);
                Log.d("DATA", data.getName() + data.getId() + data.getIcon()+"");
                startActivity(i);
            }
        });
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(MapActivity.this, SignInActivity.class);
        startActivity(i);
    }

    public void onChat(View v){
        Intent i = new Intent(MapActivity.this, ChatsActivity.class);
        i.putExtra(UserData.class.getSimpleName(), data);
        startActivity(i);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MapActivity.this.mapboxMap = mapboxMap;
        //подгружаем созданный стиль из студии MapBox
        mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {

            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapData = new MapData(style, MapActivity.this);
                //назначаем карте слушатель
                mapboxMap.addOnMapClickListener(MapActivity.this);
               beaconData = new BeaconData(mapboxMap, MapActivity.this, mapData);
            }
        });
    }

    @Override
    public boolean onMapClick(@NonNull final LatLng point) {
        mapData.setLocation(beaconData.user_x, beaconData.user_y);
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                //после клика получаем позицию, переводим полученные долготу и широту в положение на экране
                final PointF finalPoint = mapboxMap.getProjection().toScreenLocation(point);
                //получаем данные из слоя, если точка принадлежит ему
                mapData.getFeatures(mapboxMap, finalPoint);
                int onRoomClick = -1;
                for (int i=0;i< mapData.features.size();i++){
                    if (mapData.features.get(i).size()>0){
                        onRoomClick = i;
                        break;
                    }
                }

                //если данные не пусты, т.е. если клик был совершен на слое
                if (onRoomClick>=0){
                    rooms.add(style.getLayer(mapData.roomsNames[onRoomClick]));
                    tv.setText(mapData.roomsNames[onRoomClick]);

                    //выделяем слой с кабинетом и делаем надпись
                    Layer room = rooms.getLast();
                    room.setProperties(PropertyFactory.fillOpacity(0.5f));
                    //заупскаем asuncTask на 3 секунды, после которых надпись и подсветка исчезнут
                    Task t = new Task();
                    t.execute();
                }
            }
        });
        return true;
    }

    //необходимые методы

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    public class Task extends AsyncTask<Void,Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Layer room = rooms.getFirst();
            room.setProperties(PropertyFactory.fillOpacity(0.0f));
            rooms.removeFirst();
            tv.setText("");
        }
    }
}
